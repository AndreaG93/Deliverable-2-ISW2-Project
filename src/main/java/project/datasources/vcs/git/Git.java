package project.datasources.vcs.git;

import project.datasources.vcs.VersionControlSystem;
import project.metadata.ReleaseFileMetadata;
import project.release.ReleaseCommit;
import project.release.ReleaseFile;
import utilis.external.ExternalApplication;
import utilis.external.ExternalApplicationOutputLinesCounter;
import utilis.external.ExternalApplicationOutputListMaker;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class Git implements VersionControlSystem {

    private final ExternalApplication gitApplication;

    public Git(String workingDirectory, String repositoryURL, String repositoryLocalDirectory) {

        File repositoryLocalDirectoryAsFile = new File(repositoryLocalDirectory);
        ExternalApplication git = new ExternalApplication("git", workingDirectory);

        if (!repositoryLocalDirectoryAsFile.isDirectory())
            git.execute("clone", "--quiet", repositoryURL, repositoryLocalDirectory);

        this.gitApplication = new ExternalApplication("git", repositoryLocalDirectory);
    }

    public Git(String workingDirectory) {

        this.gitApplication = new ExternalApplication("git", workingDirectory);
    }

    @Override
    public List<ReleaseFile> getReleaseFiles(String releaseCommitHash) {

        GitFilesGetter gitOutputReader = new GitFilesGetter();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "ls-tree", "-r", releaseCommitHash);

        return gitOutputReader.output;
    }

    @Override
    public ReleaseCommit getReleaseCommit(LocalDateTime releaseDate) {

        GitCommitGetter gitOutputReader = new GitCommitGetter();

        this.gitApplication.execute(gitOutputReader, "log", "--date=iso-strict", "--before=" + releaseDate.toString(), "--max-count=1", "--pretty=format:\"%H<->%cd\"");

        return gitOutputReader.output;
    }

    @Override
    public void computeFileMetrics(ReleaseFile releaseFile, ReleaseCommit releaseCommit) {

        List<String> fileRevisionsHashList = this.getFileRevisionsHash(releaseFile.name, releaseCommit.hash);

        releaseFile.fileMetricsRegistry.put(ReleaseFileMetadata.numberOfRevisions, fileRevisionsHashList.size());

        computeFileLOCMetric(releaseFile);
        computeLOCMetrics(releaseFile, releaseCommit);
        computeFileAgeMetrics(releaseFile, releaseCommit);
        computeNumberOfAuthorsOfFile(releaseFile, releaseCommit);
        computeChangeSetSizeMetrics(releaseFile, releaseCommit, fileRevisionsHashList);
    }

    private List<String> getFileRevisionsHash(String filename, String releaseCommitHash) {

        ExternalApplicationOutputListMaker gitOutputReader = new ExternalApplicationOutputListMaker();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "log", "--follow", "--format=%H", releaseCommitHash, "--", filename);

        return gitOutputReader.output;
    }

    private void computeFileAgeMetrics(ReleaseFile releaseFile, ReleaseCommit releaseCommit) {

        GitFileWeekAgeGetter gitOutputReader = new GitFileWeekAgeGetter(releaseCommit.date);

        this.gitApplication.execute(gitOutputReader, "log", releaseCommit.hash, "--reverse", "--max-count=1", "--date=iso-strict", "--pretty=format:\"%cd\"", "--", releaseFile.name);

        double ageInWeeks = gitOutputReader.output;
        long LOCTouched = (long) releaseFile.fileMetricsRegistry.get(ReleaseFileMetadata.LOCTouched);

        releaseFile.fileMetricsRegistry.put(ReleaseFileMetadata.ageInWeeks, gitOutputReader.output);
        releaseFile.fileMetricsRegistry.put(ReleaseFileMetadata.weightedAgeInWeeks, ageInWeeks / LOCTouched);
    }

    private void computeNumberOfAuthorsOfFile(ReleaseFile releaseFile, ReleaseCommit releaseCommit) {

        ExternalApplicationOutputLinesCounter gitOutputReader = new ExternalApplicationOutputLinesCounter();

        this.gitApplication.execute(gitOutputReader, "shortlog", releaseCommit.hash, "-s", "--", releaseFile.name);

        releaseFile.fileMetricsRegistry.put(ReleaseFileMetadata.numberOfAuthors, gitOutputReader.output);
    }

    private void computeFileLOCMetric(ReleaseFile releaseFile) {

        ExternalApplicationOutputLinesCounter gitOutputReader = new ExternalApplicationOutputLinesCounter();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "cat-file", "-p", releaseFile.hash);

        releaseFile.fileMetricsRegistry.put(ReleaseFileMetadata.LOC, gitOutputReader.output);
    }

    private void computeChangeSetSizeMetrics(ReleaseFile releaseFile, ReleaseCommit releaseCommit, List<String> fileRevisionsList) {

        long changeSetSize = 0;
        long maxChangeSetSize = 0;
        long averageChangeSetSize = 0;

        for (String revisionHash : fileRevisionsList) {

            long currentChangeSetSize = getChangeSetSize(revisionHash);

            if (maxChangeSetSize < currentChangeSetSize)
                maxChangeSetSize = currentChangeSetSize;

            averageChangeSetSize += currentChangeSetSize;
        }

        if (fileRevisionsList.contains(releaseCommit.hash))
            changeSetSize = getChangeSetSize(releaseCommit.hash);

        averageChangeSetSize /= fileRevisionsList.size();

        releaseFile.fileMetricsRegistry.put(ReleaseFileMetadata.changeSetSize, changeSetSize);
        releaseFile.fileMetricsRegistry.put(ReleaseFileMetadata.averageChangeSetSize, averageChangeSetSize);
        releaseFile.fileMetricsRegistry.put(ReleaseFileMetadata.maxChangeSetSize, maxChangeSetSize);
    }

    private void computeLOCMetrics(ReleaseFile releaseFile, ReleaseCommit releaseCommit) {

        long addedCodeLines = 0;
        long removedCodeLines = 0;
        long modifiedCodeLines = 0;

        long maxChurn = 0;
        long maxLOCAdded = 0;

        ExternalApplicationOutputListMaker gitOutputReader = new ExternalApplicationOutputListMaker();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "log", "--follow", "--pretty=format:'%H<->%cd'", "--stat", releaseCommit.hash, "--", releaseFile.name);

        List<String> gitOutput = gitOutputReader.output;

        for (int index = gitOutput.size() - 1; index >= 0; index -= 4) {

            long insertions = 0;
            long deletions = 0;

            String[] outputContainingInsertionAndDeletion = gitOutput.get(index).split("\\s+");
            String[] outputContainingModifications = gitOutput.get(index - 1).split("\\s+");

            String modificationFlag = outputContainingModifications[outputContainingModifications.length - 1];

            if (modificationFlag.contains("-") && modificationFlag.contains("+")) {

                insertions = Long.parseLong(outputContainingInsertionAndDeletion[4]);
                deletions = Long.parseLong(outputContainingInsertionAndDeletion[6]);

            } else if (modificationFlag.contains("-"))

                deletions = Long.parseLong(outputContainingInsertionAndDeletion[4]);

            else if (modificationFlag.contains("+"))

                insertions = Long.parseLong(outputContainingInsertionAndDeletion[4]);

            modifiedCodeLines += (insertions + deletions);
            addedCodeLines += insertions;
            removedCodeLines += deletions;

            if (maxChurn < (insertions - deletions))
                maxChurn = (insertions - deletions);

            if (maxLOCAdded < insertions)
                maxLOCAdded = insertions;
        }

        int numberOfRevision = (int) releaseFile.fileMetricsRegistry.get(ReleaseFileMetadata.numberOfRevisions);

        releaseFile.fileMetricsRegistry.put(ReleaseFileMetadata.LOCTouched, addedCodeLines + removedCodeLines + modifiedCodeLines);

        releaseFile.fileMetricsRegistry.put(ReleaseFileMetadata.LOCAdded, addedCodeLines);
        releaseFile.fileMetricsRegistry.put(ReleaseFileMetadata.maxLOCAdded, maxLOCAdded);
        releaseFile.fileMetricsRegistry.put(ReleaseFileMetadata.averageLOCAdded, addedCodeLines / numberOfRevision);

        releaseFile.fileMetricsRegistry.put(ReleaseFileMetadata.churn, addedCodeLines - removedCodeLines);
        releaseFile.fileMetricsRegistry.put(ReleaseFileMetadata.maxChurn, maxChurn);
        releaseFile.fileMetricsRegistry.put(ReleaseFileMetadata.averageChurn, (addedCodeLines - removedCodeLines) / numberOfRevision);
    }

    private long getChangeSetSize(String commitHash) {

        ExternalApplicationOutputLinesCounter gitOutputReader = new ExternalApplicationOutputLinesCounter();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "diff-tree", "--no-commit-id", "--name-only", "-r", commitHash);

        return gitOutputReader.output - 1;
    }
}