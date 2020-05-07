package core.vcs;

import project.Commit;
import project.ProjectFile;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Git extends VersionControlSystem {

    public Git(String repositoryURL, String repositoryLocalDirectory) {
        super(repositoryURL, repositoryLocalDirectory);
    }

    private static String readErrors(Process p) throws IOException {
        StringBuilder bld = new StringBuilder();
        String line;
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        while ((line = stdError.readLine()) != null)
            bld.append(line);
        return bld.toString();
    }

    private List<String> executeExternalApplication(File rootDirectory, String... commands) {

        List<String> output = new ArrayList<>();

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(rootDirectory);

        try {

            Process process = processBuilder.start();

            if (process.waitFor() != 0)
                this.logger.severe(readErrors(process));

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null)
                output.add(currentLine);

            bufferedReader.close();
            process.destroy();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output;
    }

    private List<String> executeExternalApplicationRedirectingOutput(File rootDirectory, String... commands) {

        List<String> output = new ArrayList<>();

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(rootDirectory);

        try {

            File tempFile = File.createTempFile("hello", ".tmp");

            processBuilder.redirectOutput(tempFile);
            Process process = processBuilder.start();

            if (process.waitFor() != 0)
                this.logger.severe(readErrors(process));

            BufferedReader bufferedReader = new BufferedReader(new FileReader(tempFile));

            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null)
                output.add(currentLine);

            bufferedReader.close();
            process.destroy();

            if (!tempFile.delete())
                this.logger.severe(readErrors(process));

        } catch (Exception e) {

            e.printStackTrace();
            System.exit(e.hashCode());
        }

        return output;
    }

    @Override
    public void cloneRepositoryLocally() {

        if (!this.repositoryLocalDirectory.isDirectory())
            if (this.repositoryLocalDirectory.mkdir())
                executeExternalApplication(new File("C://"), "git", "clone", "--quiet", this.repositoryURL, this.repositoryLocalDirectory.getName());

    }

    @Override
    public void changeLocalRepositoryStateToCommit(String commitHash) {

        this.logger.info("Excuting '" + Thread.currentThread().getStackTrace()[1].getMethodName() + "'...");

        ProcessBuilder processBuilder = new ProcessBuilder("git", "checkout", commitHash);
        processBuilder.directory(this.repositoryLocalDirectory);

        try {

            processBuilder.start();

        } catch (IOException e) {

            this.logger.severe(e.getMessage());
            System.exit(e.hashCode());
        }

        this.logger.info("Execution of '" + Thread.currentThread().getStackTrace()[1].getMethodName() + "' is now COMPLETE!");
    }

    @Override
    public List<ProjectFile> getFiles(String commitHash, String revisionHash) {

        List<ProjectFile> output = new ArrayList<>();
        List<String> gitOutput = executeExternalApplicationRedirectingOutput(this.repositoryLocalDirectory, "git", "ls-tree", "-r", "--name-only", revisionHash);

        for (String currentOutputString : gitOutput) {

            ProjectFile projectFile = new ProjectFile();
            projectFile.name = currentOutputString;

            output.add(projectFile);
        }

        return output;
    }

    @Override
    public double getFileWeekAge(String filename, LocalDateTime releaseDate, String revisionHash) {

        List<String> gitOutput = executeExternalApplication(this.repositoryLocalDirectory, "git", "log", revisionHash, "--reverse", "--max-count=1", "--date=iso-strict", "--pretty=format:\"%cd\"", "--", filename);

        LocalDateTime creationDate = LocalDateTime.parse(gitOutput.get(0), DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return Duration.between(creationDate, releaseDate).toDays() / 7.0;
    }

    @Override
    public int getNumberOfAuthorsOfFile(String filename, String revisionHash) {

        List<String> gitOutput = executeExternalApplication(this.repositoryLocalDirectory, "git", "shortlog", revisionHash, "-s", "--", filename);

        return gitOutput.size();
    }

    public FileMetric getFileLOCTouched(String filename, String revisionHash) {

        FileMetric output = new FileMetric();
        List<String> gitOutput = executeExternalApplicationRedirectingOutput(this.repositoryLocalDirectory, "git", "log", "--follow", "--pretty=format:'%H<->%cd'", "--stat", revisionHash, "--", filename);

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


    // git log --follow --pretty=format:'%H<->%cd' --stat f83cfe160abf1ac66312312cb3a7065b49adce47 -- pom.xml   GIUSTO GIUSTO GIUSTO

    @Override
    public long getFileLOC(String filename) {
        // TODO NOT WORK
        List<String> queryOutput = executeExternalApplication(this.repositoryLocalDirectory, "wsl", "cloc", "--quiet", filename);

        String[] output = queryOutput.get(3).split("\\s+");

        return Long.parseLong(output[4]);
    }

    @Override
    public Commit getReleaseCommit(LocalDateTime releaseDate) {

        Commit output = new Commit();
        List<String> gitOutput = executeExternalApplication(this.repositoryLocalDirectory, "git", "log", "--date=iso-strict", "--before=" + releaseDate.toString(), "--max-count=1", "--pretty=format:\"%H<->%cd\"");

        String[] commitInfo = gitOutput.get(0).split("<->");

        output.hash = commitInfo[0];
        output.date = LocalDateTime.parse(commitInfo[1], DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return output;
    }


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