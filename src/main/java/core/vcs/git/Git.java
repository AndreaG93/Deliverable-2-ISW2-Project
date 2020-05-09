package core.vcs.git;

import core.vcs.FileChangeSetSizeMetric;
import core.vcs.FileMetric;
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

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "ls-tree", "-r", "--name-only", commitHash);

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

    // log --follow --format=%H 7210d7a4b11b97a6b0051c117d9290c2143e53e6 -- pom.xml
// diff-tree --no-commit-id --name-only -r d95ad49efb67b4455d6df9153ad46931a3ce3b0c
    @Override
    public FileChangeSetSizeMetric getChangeSetSizeMetric(List<String> fileRevisionsList) {

        FileChangeSetSizeMetric output = new FileChangeSetSizeMetric();

        for (String revisionHash : fileRevisionsList) {

            long currentChangeSetSize = getChangeSetSize(revisionHash);

            if (output.maxChangeSetSize < currentChangeSetSize)
                output.maxChangeSetSize = currentChangeSetSize;

            output.averageChangeSetSize += currentChangeSetSize;
        }

        output.averageChangeSetSize = output.averageChangeSetSize / fileRevisionsList.size();

        return output;
    }

    @Override
    public FileMetric getFileMetrics(String filename, List<String> fileRevisionsList) {

        long addedCodeLines = 0;
        long removedCodeLines = 0;
        long modifiedCodeLines = 0;

        FileMetric output = new FileMetric();
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

            // double actualFileAge = getFileAgeInWeeks(filename, null, revisionHash);
            // actualFileAge * (addedCodeLines + removedCodeLines + modifiedCodeLines);


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
    public long getFileLOC(String filename) {
        // TODO NOT WORK
        /*List<String> queryOutput = executeExternalApplication(this.repositoryLocalDirectory, "wsl", "cloc", "--quiet", filename);

        String[] output = queryOutput.get(3).split("\\s+");

        return Long.parseLong(output[4]);*/
        return 0;
    }
}