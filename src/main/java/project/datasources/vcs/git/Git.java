package project.datasources.vcs.git;

import project.datasources.vcs.VersionControlSystem;
import project.entities.metadata.FileMetadata;
import project.entities.Commit;
import project.entities.File;
import utilis.external.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Git implements VersionControlSystem {

    private final ExternalApplication gitApplication;

    public Git(String workingDirectory, String repositoryURL, String repositoryLocalDirectory) {

        java.io.File repositoryLocalDirectoryAsFile = new java.io.File(repositoryLocalDirectory);
        ExternalApplication git = new ExternalApplication("git", workingDirectory);

        if (!repositoryLocalDirectoryAsFile.isDirectory())
            git.execute("clone", "--quiet", repositoryURL, repositoryLocalDirectory);

        this.gitApplication = new ExternalApplication("git", repositoryLocalDirectory);
    }

    public Git(String workingDirectory) {

        this.gitApplication = new ExternalApplication("git", workingDirectory);
    }

    @Override
    public Commit getCommitByTag(String tag) {

        Commit output = null;
        OneLineReader gitOutputReader = new OneLineReader();

        this.gitApplication.execute(gitOutputReader, "show-ref", "-s", tag);

        if (gitOutputReader.output != null)

            output = getCommitByHash(gitOutputReader.output);

        else {

            ExternalApplicationOutputListMaker reader = new ExternalApplicationOutputListMaker();

            this.gitApplication.execute(reader, "tag");

            for (String outputTag : reader.output)
                if (outputTag.contains(tag)) {

                    output = getCommitByTag(outputTag);
                    break;
                }
        }

        return output;
    }

    @Override
    public Commit getCommitByDate(LocalDateTime releaseDate) {

        GitCommitGetter gitOutputReader = new GitCommitGetter();

        this.gitApplication.execute(gitOutputReader, "log", "--date=iso-strict", "--before=" + releaseDate.toString(), "--max-count=1", "--pretty=format:\"%H<->%cd\"");

        return gitOutputReader.output;
    }

    @Override
    public Commit getCommitByLogMessagePattern(String pattern) {

        GitCommitGetter gitOutputReader = new GitCommitGetter();

        this.gitApplication.execute(gitOutputReader, "log", "--date=iso-strict", "--grep=\"" + pattern + "\"", "--max-count=1", "--pretty=format:\"%H<->%cd\"");

        return gitOutputReader.output;
    }

    @Override
    public List<File> getCommitFiles(String commitHash) {

        GitFilesGetter gitOutputReader = new GitFilesGetter();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "ls-tree", "-r", commitHash);

        return gitOutputReader.output;
    }

    @Override
    public List<String> getFilesChangedByCommit(String commitHash) {

        ExternalApplicationOutputListMaker reader = new ExternalApplicationOutputListMaker();

        this.gitApplication.executeWithOutputRedirection(reader, "diff-tree", "--no-commit-id", "--name-only", "-r", commitHash);

        return reader.output;
    }

    @Override
    public void computeFileMetrics(File releaseFile, Commit releaseCommit) {

        List<String> fileRevisionsHashList = this.getFileRevisionsHash(releaseFile.name, releaseCommit.hash);

        releaseFile.metadata.put(FileMetadata.NUMBER_OF_REVISIONS, fileRevisionsHashList.size());

        computeFileLOCMetric(releaseFile);
        computeLOCMetrics(releaseFile, releaseCommit);
        computeFileAgeMetrics(releaseFile, releaseCommit);
        computeNumberOfAuthorsOfFile(releaseFile, releaseCommit);
        computeChangeSetSizeMetrics(releaseFile, releaseCommit, fileRevisionsHashList);
    }

    private Commit getCommitByHash(String commitHash) {

        ExternalApplicationOutputListMaker gitOutputReader = new ExternalApplicationOutputListMaker();

        this.gitApplication.execute(gitOutputReader, "show", "--date=iso-strict", "--format=\"%cd\"", "-s", commitHash);

        List<String> gitOutput = gitOutputReader.output;
        LocalDateTime commitLocalDateTime = LocalDateTime.parse(gitOutput.get(gitOutput.size() - 1), DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return new Commit(commitHash, commitLocalDateTime);
    }

    private List<String> getFileRevisionsHash(String filename, String releaseCommitHash) {

        ExternalApplicationOutputListMaker gitOutputReader = new ExternalApplicationOutputListMaker();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "log", "--follow", "--format=%H", releaseCommitHash, "--", filename);

        return gitOutputReader.output;
    }

    private void computeFileAgeMetrics(File releaseFile, Commit releaseCommit) {

        GitFileWeekAgeGetter gitOutputReader = new GitFileWeekAgeGetter(releaseCommit.date);

        this.gitApplication.execute(gitOutputReader, "log", releaseCommit.hash, "--reverse", "--max-count=1", "--date=iso-strict", "--pretty=format:\"%cd\"", "--", releaseFile.name);

        double ageInWeeks = gitOutputReader.output;
        long LOCTouched = (long) releaseFile.metadata.get(FileMetadata.LOCTouched);

        releaseFile.metadata.put(FileMetadata.AGE_IN_WEEKS, gitOutputReader.output);
        releaseFile.metadata.put(FileMetadata.WEIGHTED_AGE_IN_WEEKS, ageInWeeks / LOCTouched);
    }

    private void computeNumberOfAuthorsOfFile(File releaseFile, Commit releaseCommit) {

        ExternalApplicationOutputLinesCounter gitOutputReader = new ExternalApplicationOutputLinesCounter();

        this.gitApplication.execute(gitOutputReader, "shortlog", releaseCommit.hash, "-s", "--", releaseFile.name);

        releaseFile.metadata.put(FileMetadata.NUMBER_OF_AUTHORS, gitOutputReader.output);
    }

    private void computeFileLOCMetric(File releaseFile) {

        ExternalApplicationOutputLinesCounter gitOutputReader = new ExternalApplicationOutputLinesCounter();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "cat-file", "-p", releaseFile.hash);

        releaseFile.metadata.put(FileMetadata.LOC, gitOutputReader.output);
    }

    private void computeChangeSetSizeMetrics(File releaseFile, Commit releaseCommit, List<String> fileRevisionsList) {

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

        releaseFile.metadata.put(FileMetadata.CHANGE_SET_SIZE, changeSetSize);
        releaseFile.metadata.put(FileMetadata.AVERAGE_CHANGE_SET_SIZE, averageChangeSetSize);
        releaseFile.metadata.put(FileMetadata.MAX_CHANGE_SET_SIZE, maxChangeSetSize);
    }

    private void computeLOCMetrics(File releaseFile, Commit releaseCommit) {

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

        int numberOfRevision = (int) releaseFile.metadata.get(FileMetadata.NUMBER_OF_REVISIONS);

        releaseFile.metadata.put(FileMetadata.LOCTouched, addedCodeLines + removedCodeLines + modifiedCodeLines);

        releaseFile.metadata.put(FileMetadata.LOC_ADDED, addedCodeLines);
        releaseFile.metadata.put(FileMetadata.MAX_LOC_ADDED, maxLOCAdded);
        releaseFile.metadata.put(FileMetadata.AVERAGE_LOC_ADDED, addedCodeLines / numberOfRevision);

        releaseFile.metadata.put(FileMetadata.CHURN, addedCodeLines - removedCodeLines);
        releaseFile.metadata.put(FileMetadata.MAX_CHURN, maxChurn);
        releaseFile.metadata.put(FileMetadata.AVERAGE_CHURN, (addedCodeLines - removedCodeLines) / numberOfRevision);
    }

    private long getChangeSetSize(String commitHash) {

        ExternalApplicationOutputLinesCounter gitOutputReader = new ExternalApplicationOutputLinesCounter();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "diff-tree", "--no-commit-id", "--name-only", "-r", commitHash);

        return gitOutputReader.output - 1;
    }
}