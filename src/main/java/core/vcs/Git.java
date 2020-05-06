package core.vcs;

import project.Commit;
import project.ProjectFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Git extends VersionControlSystem {

    public Git(String repositoryURL, String repositoryLocalDirectory) {
        super(repositoryURL, repositoryLocalDirectory);
    }

    private BufferedReader executeGitApplication(List<String> commands) {

        BufferedReader output = null;

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(this.repositoryLocalDirectory);

        try {

            Process process = processBuilder.start();
            output = new BufferedReader(new InputStreamReader(process.getInputStream()));

        } catch (IOException e) {

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

        List<String> gitCommands = Arrays.asList("git", "ls-tree", "--name-only", "-r", commitGUID);
        BufferedReader gitOutputReader = executeGitApplication(gitCommands);

        try {

            for (String currentOutputLine = gitOutputReader.readLine(); currentOutputLine != null; currentOutputLine = gitOutputReader.readLine()) {
                ProjectFile projectFile = new ProjectFile();
                projectFile.name = currentOutputLine;

                output.add(projectFile);
            }

        } catch (IOException e) {

            this.logger.severe(e.getMessage());
            System.exit(e.hashCode());
        }

        return output;
    }

    @Override
    public AbstractMap<LocalDateTime, Commit> getAllCommits() {

        AbstractMap<LocalDateTime, Commit> output = new TreeMap<>();

        List<String> gitCommands = Arrays.asList("git", "log", "--date=iso-strict", "--pretty=format:\"%H<->%cd\"");
        BufferedReader gitOutputReader = executeGitApplication(gitCommands);

        try {

            for (String currentOutputLine = gitOutputReader.readLine(); currentOutputLine != null; currentOutputLine = gitOutputReader.readLine()) {
                Commit commit = new Commit();

                String[] commitInfo = currentOutputLine.split("<->");

                commit.guid = commitInfo[0];
                commit.date = LocalDate.parse(commitInfo[1], DateTimeFormatter.ISO_OFFSET_DATE_TIME).atStartOfDay();

                if (!output.containsKey(commit.date))
                    output.put(commit.date, commit);
            }

        } catch (Exception e) {

            this.logger.severe(e.getMessage());
            System.exit(e.hashCode());
        }

        return output;
    }

    public List<String> getAuthorsOfFile(String filename) {

        ArrayList<String> output = new ArrayList<>();

        List<String> gitCommands = Arrays.asList("git", "log", "--pretty=format:\"%an\"", filename);
        BufferedReader gitOutputReader = executeGitApplication(gitCommands);

        try {


            for (String currentOutputLine = gitOutputReader.readLine(); currentOutputLine != null; currentOutputLine = gitOutputReader.readLine()) {

                boolean authorAlreadyExist = false;
                String lowerCaseCurrentLine = currentOutputLine.toLowerCase();

                for (String author : output) {
                    if (author.contains(lowerCaseCurrentLine)) {
                        authorAlreadyExist = true;
                        break;
                    }
                }

                if (authorAlreadyExist == false) {
                    output.add(lowerCaseCurrentLine);
                }
            }


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
