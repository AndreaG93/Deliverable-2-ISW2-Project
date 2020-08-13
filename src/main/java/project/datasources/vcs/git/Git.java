package project.datasources.vcs.git;

import project.datasources.vcs.VersionControlSystem;
import project.model.metadata.MetadataType;
import project.model.Commit;
import project.model.File;
import utilis.external.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class Git implements VersionControlSystem {

    private final ExternalApplication gitApplication;

    public Git(String rootDirectoryPath, String workingDirectoryPath, String repositoryURL) {

        java.io.File workingDirectory = new java.io.File(workingDirectoryPath);

        if (!workingDirectory.isDirectory()) {

            ExternalApplication git = new ExternalApplication("git", rootDirectoryPath);
            git.execute("clone", "--quiet", repositoryURL, workingDirectoryPath);
        }

        this.gitApplication = new ExternalApplication("git", workingDirectoryPath);
    }

    @Override
    public Commit getCommitByTag(String tag) {

        OneLineReader gitOutputReader = new OneLineReader();

        this.gitApplication.execute(gitOutputReader, "show-ref", "-s", tag);

        if (gitOutputReader.getOutput() != null)

            return getCommitByHash(gitOutputReader.getOutput());

        else {

            ExternalApplicationOutputListMaker reader = new ExternalApplicationOutputListMaker();

            this.gitApplication.execute(reader, "tag");

            for (String outputTag : reader.output)
                if (outputTag.contains(tag))
                    return getCommitByTag(outputTag);

        }

        return null;
    }

    @Override
    public Commit getCommitByDate(LocalDateTime date) {

        GitCommitGetter gitOutputReader = new GitCommitGetter();

        this.gitApplication.execute(gitOutputReader, "log", "--date=iso-strict", "--before=" + date.toString(), "--max-count=1", "--pretty=format:\"%H<->%cd\"");

        return gitOutputReader.output;
    }

    @Override
    public Commit getCommitByLogMessagePattern(String pattern) {

        GitCommitGetter gitOutputReader = new GitCommitGetter();

        this.gitApplication.execute(gitOutputReader, "log", "--date=iso-strict", "--grep=\"" + pattern + "\"", "--max-count=1", "--pretty=format:\"%H<->%cd\"");

        return gitOutputReader.output;
    }

    @Override
    public Map<String, File> getFiles(String commitHash) {

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
    public void computeFileMetrics(File file, Commit releaseCommit) {

        List<String> fileRevisionsHashList = this.getFileRevisionsHash((String) file.getMetadata(MetadataType.NAME), releaseCommit.hash);

        file.setMetadata(MetadataType.NUMBER_OF_REVISIONS, fileRevisionsHashList.size());

        computeFileLOCMetric(file);
        computeLOCMetrics(file, releaseCommit);
        computeFileAgeMetrics(file, releaseCommit);
        computeNumberOfAuthorsOfFile(file, releaseCommit);
        computeChangeSetSizeMetrics(file, releaseCommit, fileRevisionsHashList);
    }

    public void computeFileLOCMetric(File file) {

        ExternalApplicationOutputLinesCounter gitOutputReader = new ExternalApplicationOutputLinesCounter();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "cat-file", "-p", file.getHash());

        file.setMetadata(MetadataType.LOC, gitOutputReader.getOutput());
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

    private void computeFileAgeMetrics(File file, Commit releaseCommit) {

        GitFileWeekAgeGetter gitOutputReader = new GitFileWeekAgeGetter(releaseCommit.date);

        this.gitApplication.execute(gitOutputReader, "log", releaseCommit.hash, "--reverse", "--max-count=1", "--date=iso-strict", "--pretty=format:\"%cd\"", "--", file.getName());

        double ageInWeeks = gitOutputReader.output;
        long LOCTouched = (long) file.getMetadata(MetadataType.LOC_TOUCHED);

        file.setMetadata(MetadataType.AGE_IN_WEEKS, gitOutputReader.output);
        file.setMetadata(MetadataType.WEIGHTED_AGE_IN_WEEKS, ageInWeeks / LOCTouched);
    }

    private void computeNumberOfAuthorsOfFile(File file, Commit releaseCommit) {

        ExternalApplicationOutputLinesCounter gitOutputReader = new ExternalApplicationOutputLinesCounter();

        this.gitApplication.execute(gitOutputReader, "shortlog", releaseCommit.hash, "-s", "--", file.getName());

        file.setMetadata(MetadataType.NUMBER_OF_AUTHORS, gitOutputReader.getOutput());
    }


    private void computeChangeSetSizeMetrics(File file, Commit releaseCommit, List<String> fileRevisionsList) {

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

        file.setMetadata(MetadataType.CHANGE_SET_SIZE, changeSetSize);
        file.setMetadata(MetadataType.AVERAGE_CHANGE_SET_SIZE, averageChangeSetSize);
        file.setMetadata(MetadataType.MAX_CHANGE_SET_SIZE, maxChangeSetSize);
    }

    private void computeLOCMetrics(File file, Commit releaseCommit) {

        long addedCodeLines = 0;
        long removedCodeLines = 0;
        long modifiedCodeLines = 0;

        long maxChurn = 0;
        long maxLOCAdded = 0;

        ExternalApplicationOutputListMaker gitOutputReader = new ExternalApplicationOutputListMaker();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "log", "--follow", "--pretty=format:'%H<->%cd'", "--stat", releaseCommit.hash, "--", file.getName());

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

        int numberOfRevision = (int) file.getMetadata(MetadataType.NUMBER_OF_REVISIONS);

        file.setMetadata(MetadataType.LOC_TOUCHED, addedCodeLines + removedCodeLines + modifiedCodeLines);

        file.setMetadata(MetadataType.LOC_ADDED, addedCodeLines);
        file.setMetadata(MetadataType.MAX_LOC_ADDED, maxLOCAdded);
        file.setMetadata(MetadataType.AVERAGE_LOC_ADDED, addedCodeLines / numberOfRevision);

        file.setMetadata(MetadataType.CHURN, addedCodeLines - removedCodeLines);
        file.setMetadata(MetadataType.MAX_CHURN, maxChurn);
        file.setMetadata(MetadataType.AVERAGE_CHURN, (addedCodeLines - removedCodeLines) / numberOfRevision);
    }

    private long getChangeSetSize(String commitHash) {

        ExternalApplicationOutputLinesCounter gitOutputReader = new ExternalApplicationOutputLinesCounter();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "diff-tree", "--no-commit-id", "--name-only", "-r", commitHash);

        return gitOutputReader.getOutput() - 1;
    }
}