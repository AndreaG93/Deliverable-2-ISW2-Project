package core.vcs;

import project.Commit;
import project.ProjectFile;

import java.io.BufferedReader;
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
        List<String> gitOutput = executeExternalApplication2("git", "ls-tree", "--name-only", "-r", commitGUID);

        for (String currentOutputString : gitOutput) {

            ProjectFile projectFile = new ProjectFile();
            projectFile.name = currentOutputString;

            output.add(projectFile);
        }

        return output;
    }

    public int getClocFile(String filename) {

        List<String> gitOutput = executeExternalApplication2("wsl", "cloc", "--quiet", filename);

        String[] output = gitOutput.get(3).split("\\s+");

        return Integer.parseInt(output[4]);
    }


    @Override
    public AbstractMap<LocalDateTime, Commit> getAllCommits() {

        AbstractMap<LocalDateTime, Commit> output = new TreeMap<>();
        List<String> gitOutput = executeExternalApplication2("git", "log", "--date=iso-strict", "--pretty=format:\"%H<->%cd\"");

        for (String currentOutputString : gitOutput) {

            Commit commit = new Commit();

            String[] commitInfo = currentOutputString.split("<->");

            commit.guid = commitInfo[0];
            commit.date = LocalDateTime.parse(commitInfo[1], DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            if (!output.containsKey(commit.date))
                output.put(commit.date, commit);
        }

        return output;
    }

    public int getNumberOfAuthorsOfFile(String filename, LocalDate dateLowerBound, LocalDate dateUpperBound) {

        List<String> gitOutput;

        if (dateUpperBound != null)
            gitOutput = executeExternalApplication2("git", "shortlog", "--summary", "--after=", dateLowerBound.toString(), "--before=", dateUpperBound.toString(), filename);
        else
            gitOutput = executeExternalApplication2("git", "shortlog", "--summary", "--after=", dateLowerBound.toString(), filename);

        return gitOutput.size();
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

    // git log --reverse --max-count=1 --date=iso-strict --pretty=format:"%cd" pom.xml


    // 5a381391031c2756bf0927f4246e20e220306f8d  TAG
    // bce3e8bd54cb7b7f27a2cfe11a4878d5bbcb473e
}
