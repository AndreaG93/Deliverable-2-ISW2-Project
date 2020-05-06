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
        String line = "";
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

            File tempFile = File.createTempFile("hello", ".tmp");
            File errorTempFile = File.createTempFile("error", ".tmp");

            processBuilder.redirectError(errorTempFile);
            processBuilder.redirectOutput(tempFile);

            Process process = processBuilder.start();

            if (process.waitFor() != 0)
                this.logger.severe(readErrors(process));

            FileReader fr = new FileReader(tempFile);   //reads the file
            BufferedReader br = new BufferedReader(fr);


            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                output.add(currentLine);
            }

            br.close();
            fr.close();


            process.destroy();

            /*

            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            for (String currentLine = bufferedReader.readLine(); currentLine != null; currentLine = bufferedReader.readLine())
                output.add(currentLine);

            process.getInputStream().close();
            inputStreamReader.close();
            bufferedReader.close();

*/


            if (!tempFile.delete())
                this.logger.severe(readErrors(process));

            if (!errorTempFile.delete())
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
    public List<ProjectFile> getFiles(String commitHash) {

        List<ProjectFile> output = new ArrayList<>();
        List<String> gitOutput = executeExternalApplication(this.repositoryLocalDirectory, "git", "ls-files");

        for (String currentOutputString : gitOutput) {

            ProjectFile projectFile = new ProjectFile();
            projectFile.name = currentOutputString;

            output.add(projectFile);
        }

        return output;
    }

    @Override
    public double getFileWeekAge(String filename, LocalDateTime releaseDate) {

        List<String> gitOutput = executeExternalApplication(this.repositoryLocalDirectory, "git", "log", "--reverse", "--max-count=1", "--date=iso-strict", "--pretty=format:\"%cd\"", filename);

        LocalDateTime creationDate = LocalDateTime.parse(gitOutput.get(0), DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return Duration.between(creationDate, releaseDate).toDays() / 7.0;
    }

    @Override
    public int getNumberOfAuthorsOfFile(String filename) {

        List<String> gitOutput = executeExternalApplication(this.repositoryLocalDirectory, "git", "shortlog", "-s", filename);

        return gitOutput.size();
    }

    @Override
    public long getFileLOC(String filename) {

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


    // 5a381391031c2756bf0927f4246e20e220306f8d  TAG
    // bce3e8bd54cb7b7f27a2cfe11a4878d5bbcb473e
}
