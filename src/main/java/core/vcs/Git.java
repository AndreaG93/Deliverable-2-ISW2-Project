package core.vcs;

import project.Commit;
import project.ProjectFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

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

        ProcessBuilder processBuilder = new ProcessBuilder("git", "ls-tree", "--name-only", "-r", commitGUID);
        processBuilder.directory(this.repositoryLocalDirectory);

        try {

            Process process = processBuilder.start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String currentLine;

            while ((currentLine = stdInput.readLine()) != null) {

                ProjectFile projectFile = new ProjectFile();
                projectFile.name = currentLine;

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

        ProcessBuilder pb = new ProcessBuilder("git", "log", "--date=iso-strict", "--pretty=format:\"%H<->%cd\"");
        pb.directory(new File("C:\\test"));

        try {

            Process p = pb.start();

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;

            while ((line = stdInput.readLine()) != null) {

                Commit commit = new Commit();

                String[] commitInfo = line.split("<->");

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

    // git diff --name-only
    // git log
    // git log --date=iso --pretty=format:"%H -> %cd"
    // git log --until 2013-05-21 --pretty="short" --name-only
    // git diff-tree --name-status -r @{3} master
}
