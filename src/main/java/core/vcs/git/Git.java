package core.vcs.git;

import core.FileMetrics;
import core.vcs.ReleaseCommit;
import core.vcs.ReleaseFile;
import core.vcs.VersionControlSystem;
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

        releaseFile.fileMetricsRegistry.put(FileMetrics.numberOfRevisions, fileRevisionsHashList.size());

        computeFileLOCMetric(releaseFile);
        computeLOCMetrics(releaseFile, fileRevisionsHashList);
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
        long LOCTouched = (long) releaseFile.fileMetricsRegistry.get(FileMetrics.LOCTouched);

        releaseFile.fileMetricsRegistry.put(FileMetrics.ageInWeeks, gitOutputReader.output);
        releaseFile.fileMetricsRegistry.put(FileMetrics.weightedAgeInWeeks, ageInWeeks / LOCTouched);
    }

    private void computeNumberOfAuthorsOfFile(ReleaseFile releaseFile, ReleaseCommit releaseCommit) {

        ExternalApplicationOutputLinesCounter gitOutputReader = new ExternalApplicationOutputLinesCounter();

        this.gitApplication.execute(gitOutputReader, "shortlog", releaseCommit.hash, "-s", "--", releaseFile.name);

        releaseFile.fileMetricsRegistry.put(FileMetrics.numberOfAuthors, gitOutputReader.output);
    }

    private void computeFileLOCMetric(ReleaseFile releaseFile) {

        ExternalApplicationOutputLinesCounter gitOutputReader = new ExternalApplicationOutputLinesCounter();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "cat-file", "-p", releaseFile.hash);

        releaseFile.fileMetricsRegistry.put(FileMetrics.LOC, gitOutputReader.output);
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

        releaseFile.fileMetricsRegistry.put(FileMetrics.changeSetSize, changeSetSize);
        releaseFile.fileMetricsRegistry.put(FileMetrics.averageChangeSetSize, averageChangeSetSize);
        releaseFile.fileMetricsRegistry.put(FileMetrics.maxChangeSetSize, maxChangeSetSize);
    }

    private void computeLOCMetrics(ReleaseFile releaseFile, List<String> fileRevisionsList) {

        long addedCodeLines = 0;
        long removedCodeLines = 0;
        long modifiedCodeLines = 0;

        long maxChurn = 0;
        long maxLOCAdded = 0;

        GitFileCodeChangesGetter gitOutputReader = new GitFileCodeChangesGetter();

        for (String revisionHash : fileRevisionsList) {

            this.gitApplication.executeWithOutputRedirection(gitOutputReader, "show", "--stat", "--oneline", "--no-commit-id", revisionHash, "--", releaseFile.name);

            modifiedCodeLines += (gitOutputReader.insertions + gitOutputReader.deletions);
            addedCodeLines += gitOutputReader.insertions;
            removedCodeLines += gitOutputReader.deletions;

            if (maxChurn < (gitOutputReader.insertions - gitOutputReader.deletions))
                maxChurn = (gitOutputReader.insertions - gitOutputReader.deletions);

            if (maxLOCAdded < gitOutputReader.insertions)
                maxLOCAdded = gitOutputReader.insertions;

            gitOutputReader.clearStatistics();
        }

        releaseFile.fileMetricsRegistry.put(FileMetrics.LOCTouched, addedCodeLines + removedCodeLines + modifiedCodeLines);

        releaseFile.fileMetricsRegistry.put(FileMetrics.LOCAdded, addedCodeLines);
        releaseFile.fileMetricsRegistry.put(FileMetrics.maxLOCAdded, maxLOCAdded);
        releaseFile.fileMetricsRegistry.put(FileMetrics.averageLOCAdded, addedCodeLines / fileRevisionsList.size());

        releaseFile.fileMetricsRegistry.put(FileMetrics.churn, addedCodeLines - removedCodeLines);
        releaseFile.fileMetricsRegistry.put(FileMetrics.maxChurn, maxChurn);
        releaseFile.fileMetricsRegistry.put(FileMetrics.averageChurn, (addedCodeLines - removedCodeLines) / fileRevisionsList.size());
    }

    private long getChangeSetSize(String commitHash) {

        ExternalApplicationOutputLinesCounter gitOutputReader = new ExternalApplicationOutputLinesCounter();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "diff-tree", "--no-commit-id", "--name-only", "-r", commitHash);

        return gitOutputReader.output - 1;
    }
}