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
    public List<ProjectFile> getFiles(String commitHash, String revisionHash) {

        GitFilesGetter gitOutputReader = new GitFilesGetter();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "ls-tree", "-r", "--name-only", revisionHash);

        return gitOutputReader.output;
    }

    @Override
    public double getFileAgeInWeeks(String filename, LocalDateTime releaseDate, String revisionHash) {

        GitFileWeekAgeGetter gitOutputReader = new GitFileWeekAgeGetter(releaseDate);

        this.gitApplication.execute(gitOutputReader, "log", revisionHash, "--reverse", "--max-count=1", "--date=iso-strict", "--pretty=format:\"%cd\"", "--", filename);

        return gitOutputReader.output;
    }

    @Override
    public int getNumberOfAuthorsOfFile(String filename, String revisionHash) {

        ExternalApplicationOutputLinesCounter gitOutputReader = new ExternalApplicationOutputLinesCounter();

        this.gitApplication.execute(gitOutputReader, "shortlog", revisionHash, "-s", "--", filename);

        return gitOutputReader.output;
    }

    @Override
    public Commit getCommit(LocalDateTime releaseDate) {

        GitCommitGetter gitOutputReader = new GitCommitGetter();

        this.gitApplication.execute(gitOutputReader, "log", "--date=iso-strict", "--before=" + releaseDate.toString(), "--max-count=1", "--pretty=format:\"%H<->%cd\"");

        return gitOutputReader.output;
    }

    private long getChangeSetSize(String commitHash) {

        ExternalApplicationOutputLinesCounter gitOutputReader = new ExternalApplicationOutputLinesCounter();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "diff-tree", "--no-commit-id", "--name-only", "-r", commitHash);

        return gitOutputReader.output - 1;
    }
    
    @Override
    public FileChangeSetSizeMetric getChangeSetSizeMetric(String filename, String commitHash) {

        FileChangeSetSizeMetric output = new FileChangeSetSizeMetric();

        ExternalApplicationOutputListMaker gitOutputReader = new ExternalApplicationOutputListMaker();
        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "log", "--follow", "--format=%H", commitHash, "--", filename);

        for (String outputCommitHash : gitOutputReader.output) {

            long currentChangeSetSize = getChangeSetSize(outputCommitHash);

            if (outputCommitHash.equals(commitHash))
                output.changeSetSize = currentChangeSetSize;

            if (output.maxChangeSetSize < currentChangeSetSize)
                output.maxChangeSetSize = currentChangeSetSize;

            output.averageChangeSetSize += currentChangeSetSize;
        }

        output.averageChangeSetSize = output.averageChangeSetSize / gitOutputReader.output.size();

        return output;
    }

    @Override
    public FileMetric getFileMetrics(String filename, String revisionHash) {

        FileMetric output = new FileMetric();
        ExternalApplicationOutputListMaker gitOutputReader = new ExternalApplicationOutputListMaker();

        this.gitApplication.executeWithOutputRedirection(gitOutputReader, "log", "--follow", "--pretty=format:'%H<->%cd'", "--stat", revisionHash, "--", filename);

        List<String> gitOutput = gitOutputReader.output;

        long addedCodeLines = 0;
        long removedCodeLines = 0;
        long modifiedCodeLines = 0;

        for (int index = gitOutput.size() - 1; index >= 0; ) {

            long insertions = 0;
            long deletions = 0;

            String[] outputContainingInsertionAndDeletion = gitOutput.get(index).split("\\s+");
            String[] outputContainingModifications = gitOutput.get(index - 1).split("\\s+");

            int modificationFlagsIndex = outputContainingModifications.length - 1;

            if (outputContainingModifications[modificationFlagsIndex].contains("-") && outputContainingModifications[modificationFlagsIndex].contains("+")) {

                insertions = Long.parseLong(outputContainingInsertionAndDeletion[4]);
                deletions = Long.parseLong(outputContainingInsertionAndDeletion[6]);

            } else if (outputContainingModifications[modificationFlagsIndex].contains("-"))
                deletions = Long.parseLong(outputContainingInsertionAndDeletion[4]);

            else if (outputContainingModifications[modificationFlagsIndex].contains("+"))
                insertions = Long.parseLong(outputContainingInsertionAndDeletion[4]);

            modifiedCodeLines += (insertions + deletions);
            addedCodeLines += insertions;
            removedCodeLines += deletions;

            index -= 4;
            output.numberOfRevisions++;
        }

        output.churn = addedCodeLines - removedCodeLines;
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


    // "git", "checkout", commitHash
    // git diff --name-only
    // git log
    // git log --date=iso --pretty=format:"%H -> %cd"
    // git log --until 2013-05-21 --pretty="short" --name-only
    // git diff-tree --name-status -r @{3} master
    // git ls-files
    //  git log --before=2015-10-19
    //  git log --before=2015-10-20

    // git checkout hash
// git checkout master

    //  git log --date=iso-strict --before=2015-07-16T23:59:59 --max-count=1 --pretty=format:"%H<->%cd"


    // git log --reverse --max-count=1 --date=iso-strict --pretty=format:"%cd" pom.xml

// 2015-07-14T08:21:14-04:00
//
//  git log -p --pretty=format:"%H<->%cd" -- pom.xml


    //  git log --follow --format='%H' --stat -- pom.xml
}

/*
 pom.xml | 193 ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
         1 file changed, 193 insertions(+)
         671109ba2bbb3bf81f0d75d9243dc2f9ed4fa375
         50b9f3b22ed598fadd1e5b43590f3985507083b1

git diff --stat 671109ba2bbb3bf81f0d75d9243dc2f9ed4fa375 50b9f3b22ed598fadd1e5b43590f3985507083b1 -- pom.xml
*/






/*
         pom.xml | 11 ++++++-----
         1 file changed, 6 insertions(+), 5 deletions(-)
         ce5c4e8094a620c999ffe7f26f00cc6bece4cf37

         pom.xml | 37 ++++++++++++++++++++++++++++++++-----
         1 file changed, 32 insertions(+), 5 deletions(-)
         4cd53677c05d0b7d02b027bf445eb302843d3327

         git diff --stat 722df7ee79166fd99131b178d063be64825b30a8 -- pom.xml

         pom.xml | 2 +-
         1 file changed, 1 insertion(+), 1 deletion(-)
         722df7ee79166fd99131b178d063be64825b30a8



            git ls-tree pom.xml
         git cat-file -p 0670cdeb266c4d23f2549d416161689ff7a6e223

*/


/*

git cat-file -p 0670cdeb266c4d23f2549d416161689ff7a6e223 | wc
   1607    2152   84485


 */

// FOR LOC git cat-file -p <hashfileofrevision> | wc

// git log --follow --format='%H' --stat -- pom.xml

// git log --reverse --follow --stat -p pom.xml


// git checkout 6554ac251fad41c0981976a7b942ad48c592741e
// git log --follow --format='%H<->%cd' --stat -- pom.xml
//  git log --follow --format='%H<->%cd' --stat e15287f110a32909c2f5e32d9c69dea39bb0f562...f83cfe160abf1ac66312312cb3a7065b49adce47 -- pom.xml


// git log --follow --format='%H<->%cd' --stat f83cfe160abf1ac66312312cb3a7065b49adce47 -- pom.xml   GIUSTO GIUSTO GIUSTO


//  git log --follow --format='%H<->%cd' f83cfe160abf1ac66312312cb3a7065b49adce47 -- pom.xml
// git diff-tree --no-commit-id --name-only -r f83cfe160abf1ac66312312cb3a7065b49adce47