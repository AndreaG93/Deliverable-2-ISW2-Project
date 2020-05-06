package core.vcs;

import project.Commit;
import project.ProjectFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class Git extends VersionControlSystem {

    public Git(String repositoryURL, String repositoryLocalDirectory) {
        super(repositoryURL, repositoryLocalDirectory);
    }

    private BufferedReader executeExternalApplication(String... commands) {

        BufferedReader output = null;

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(this.repositoryLocalDirectory);

        try {

            Process process = processBuilder.start();
            output = new BufferedReader(new InputStreamReader(process.getInputStream()));

        } catch (Exception e) {

            e.printStackTrace();
            System.exit(e.hashCode());
        }

        return output;
    }

    private List<String> executeExternalApplication2(String... commands) {

        List<String> output = new ArrayList<>();

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(this.repositoryLocalDirectory);

        try {

            Process process = processBuilder.start();
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            for (String currentLine = bufferedReader.readLine(); currentLine != null; currentLine = bufferedReader.readLine())
                output.add(currentLine);

            process.getInputStream().close();
            inputStreamReader.close();
            bufferedReader.close();

        } catch (Exception e) {

            e.printStackTrace();
            System.exit(e.hashCode());
        }

        return output;
    }


    @Override
    public void cloneRepositoryLocally() {

        if (this.repositoryLocalDirectory.isDirectory())
            this.logger.info("Cloning repository: aborted. Local files already exist!");
        else {

            this.logger.info("Cloning repository: starting!");

            ProcessBuilder pb = new ProcessBuilder("git", "clone", "--quiet", this.repositoryURL, this.repositoryLocalDirectory.getName());
            try {
                Process p = pb.start();

                if (p.waitFor() != 0) {

                    this.logger.severe("'git clone' command failed!");
                    System.exit(p.exitValue());
                }

            } catch (Exception e) {

                this.logger.severe(e.getMessage());
                System.exit(e.hashCode());
            }

            this.logger.info("Cloning repository: complete!");
        }
    }

    public List<ProjectFile> getAllFilesFromCommit(String commitGUID) {

        List<ProjectFile> output = new ArrayList<>();

        BufferedReader gitOutputReader = executeExternalApplication("git", "ls-tree", "--name-only", "-r", commitGUID);

        try {

            for (String currentOutputLine = gitOutputReader.readLine(); currentOutputLine != null; currentOutputLine = gitOutputReader.readLine()) {
                ProjectFile projectFile = new ProjectFile();
                projectFile.name = currentOutputLine;

                output.add(projectFile);
            }

            gitOutputReader.close();

        } catch (IOException e) {

            this.logger.severe(e.getMessage());
            System.exit(e.hashCode());
        }

        return output;
    }

    @Override
    public AbstractMap<LocalDate, Commit> getAllCommits() {

        AbstractMap<LocalDate, Commit> output = new TreeMap<>();

        BufferedReader gitOutputReader = executeExternalApplication("git", "log", "--date=iso-strict", "--pretty=format:\"%H<->%cd\"");

        try {

            for (String currentOutputLine = gitOutputReader.readLine(); currentOutputLine != null; currentOutputLine = gitOutputReader.readLine()) {
                Commit commit = new Commit();

                String[] commitInfo = currentOutputLine.split("<->");

                commit.guid = commitInfo[0];
                commit.date = LocalDate.parse(commitInfo[1], DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                if (!output.containsKey(commit.date))
                    output.put(commit.date, commit);
            }

            gitOutputReader.close();

        } catch (Exception e) {

            this.logger.severe(e.getMessage());
            System.exit(e.hashCode());
        }

        return output;
    }

    public int getNumberOfAuthorsOfFile(String filename, LocalDate dateLowerBound, LocalDate dateUpperBound) {

        int output = 0;
        BufferedReader gitOutputReader;

        if (dateUpperBound != null)
            gitOutputReader = executeExternalApplication("git", "shortlog", "--summary", "--after=", dateLowerBound.toString(), "--before=", dateUpperBound.toString(), filename);
        else
            gitOutputReader = executeExternalApplication("git", "shortlog", "--summary", "--after=", dateLowerBound.toString(), filename);

        try {

            for (String currentOutputLine = gitOutputReader.readLine(); currentOutputLine != null; currentOutputLine = gitOutputReader.readLine())
                output++;

            gitOutputReader.close();

        } catch (Exception e) {

            this.logger.severe(e.getMessage());
            System.exit(e.hashCode());
        }

        return output;
    }


    // git diff --name-only
    // git log
    // git log --date=iso --pretty=format:"%H -> %cd"
    // git log --until 2013-05-21 --pretty="short" --name-only
    // git diff-tree --name-status -r @{3} master
}
