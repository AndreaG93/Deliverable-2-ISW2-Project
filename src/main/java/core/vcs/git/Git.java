package core.vcs.git;

import core.vcs.FileChangeSetSizeMetrics;
import core.vcs.FileLOCMetrics;
import core.vcs.VersionControlSystem;
import project.entities.Commit;
import project.entities.ProjectFile;
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
            if (repositoryLocalDirectoryAsFile.mkdir())
                git.execute("clone", "--quiet", repositoryURL, repositoryLocalDirectory);

        this.gitApplication = new ExternalApplication("git", repositoryLocalDirectory);
    }

    public Git(String workingDirectory) {

        this.gitApplication = new ExternalApplication("git", workingDirectory);
    }

    @Override
    public List<ProjectFile> getFiles(String commitHash) {

        GitFilesGetter gitOutputReader = new GitFilesGetter();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "ls-tree", "-r", commitHash);

        return gitOutputReader.output;
    }

    @Override
    public double getFileAgeInWeeks(String filename, LocalDateTime releaseDate, String upperBoundCommitHash) {

        GitFileWeekAgeGetter gitOutputReader = new GitFileWeekAgeGetter(releaseDate);

        this.gitApplication.execute(gitOutputReader, "log", upperBoundCommitHash, "--reverse", "--max-count=1", "--date=iso-strict", "--pretty=format:\"%cd\"", "--", filename);

        return gitOutputReader.output;
    }

    @Override
    public int getNumberOfAuthorsOfFile(String filename, String upperBoundCommitHash) {

        ExternalApplicationOutputLinesCounter gitOutputReader = new ExternalApplicationOutputLinesCounter();

        this.gitApplication.execute(gitOutputReader, "shortlog", upperBoundCommitHash, "-s", "--", filename);

        return gitOutputReader.output;
    }

    @Override
    public Commit getCommit(LocalDateTime releaseDate) {

        GitCommitGetter gitOutputReader = new GitCommitGetter();

        this.gitApplication.execute(gitOutputReader, "log", "--date=iso-strict", "--before=" + releaseDate.toString(), "--max-count=1", "--pretty=format:\"%H<->%cd\"");

        return gitOutputReader.output;
    }

    @Override
    public List<String> getFileRevisions(String filename, String upperBoundCommitHash) {

        ExternalApplicationOutputListMaker gitOutputReader = new ExternalApplicationOutputListMaker();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "log", "--follow", "--format=%H", upperBoundCommitHash, "--", filename);

        return gitOutputReader.output;
    }

    @Override
    public long getChangeSetSize(String commitHash) {

        ExternalApplicationOutputLinesCounter gitOutputReader = new ExternalApplicationOutputLinesCounter();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "diff-tree", "--no-commit-id", "--name-only", "-r", commitHash);

        return gitOutputReader.output - 1;
    }

    @Override
    public FileChangeSetSizeMetrics getChangeSetSizeMetric(List<String> fileRevisionsList) {

        long maxChangeSetSize = 0;
        long averageChangeSetSize = 0;

        for (String revisionHash : fileRevisionsList) {

            long currentChangeSetSize = getChangeSetSize(revisionHash);

            if (maxChangeSetSize < currentChangeSetSize)
                maxChangeSetSize = currentChangeSetSize;

            averageChangeSetSize += currentChangeSetSize;
        }

        averageChangeSetSize /= fileRevisionsList.size();

        return new FileChangeSetSizeMetrics(maxChangeSetSize, averageChangeSetSize);
    }

    @Override
    public FileLOCMetrics getFileMetrics(String filename, List<String> fileRevisionsList) {

        long addedCodeLines = 0;
        long removedCodeLines = 0;
        long modifiedCodeLines = 0;

        FileLOCMetrics output = new FileLOCMetrics();
        GitFileCodeChangesGetter gitOutputReader = new GitFileCodeChangesGetter();

        for (String revisionHash : fileRevisionsList) {

            this.gitApplication.executeWithOutputRedirection(gitOutputReader, "show", "--stat", "--oneline", "--no-commit-id", revisionHash, "--", filename);

            modifiedCodeLines += (gitOutputReader.insertions + gitOutputReader.deletions);
            addedCodeLines += gitOutputReader.insertions;
            removedCodeLines += gitOutputReader.deletions;

            if (output.maxChurn < (gitOutputReader.insertions - gitOutputReader.deletions))
                output.maxChurn = (gitOutputReader.insertions - gitOutputReader.deletions);

            if (output.maxLOCAdded < gitOutputReader.insertions)
                output.maxLOCAdded = gitOutputReader.insertions;

            gitOutputReader.clearStatistics();
        }

        output.LOCAdded = addedCodeLines;
        output.averageLOCAdded = (double) output.LOCAdded / fileRevisionsList.size();

        output.churn = addedCodeLines - removedCodeLines;
        output.averageChurn = (double) output.churn / fileRevisionsList.size();

        output.LOCTouched = addedCodeLines + removedCodeLines + modifiedCodeLines;

        return output;
    }

    @Override
    public long getFileLOC(String fileHash) {

        ExternalApplicationOutputLinesCounter gitOutputReader = new ExternalApplicationOutputLinesCounter();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "cat-file", "-p", fileHash);

        return gitOutputReader.output;
    }
}