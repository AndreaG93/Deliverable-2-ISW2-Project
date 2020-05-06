package core.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import project.Project;
import project.ProjectFile;
import project.Release;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.logging.Logger;

public class ProjectDatasetExporter {

    private final Logger logger;
    private final Project project;


    public ProjectDatasetExporter(Project project) {
        this.project = project;
        this.logger = Logger.getLogger(ProjectDatasetExporter.class.getName());
    }


    public void exportReleaseInfo() {
        try {

            FileWriter fw = new FileWriter("./ReleaseInfo");
            CSVPrinter printer = new CSVPrinter(fw, CSVFormat.DEFAULT);

            printer.printRecord("Index", "Version ID", "Version Name", "Date");

            int currentReleaseIndex = 1;
            for (Map.Entry<LocalDateTime, Release> releaseEntry : this.project.releases.entrySet()) {

                Release currentRelease = releaseEntry.getValue();

                printer.printRecord(currentReleaseIndex, currentRelease.id, currentRelease.name, currentRelease.releaseDate);

                currentReleaseIndex++;
            }

            printer.close();
            fw.close();

        } catch (IOException e) {

            this.logger.severe(e.getMessage());
            System.exit(e.hashCode());
        }
    }

    public void exportTo(String outputFileDirectory) {
        try {

            FileWriter fw = new FileWriter(outputFileDirectory);
            CSVPrinter printer = new CSVPrinter(fw, CSVFormat.DEFAULT);

            printer.printRecord("Version ID", "File Name", "Number Of Authors");

            int currentReleaseIndex = 1;
            for (Map.Entry<LocalDateTime, Release> releaseEntry : this.project.releases.entrySet()) {

                Release currentRelease = releaseEntry.getValue();

                for (ProjectFile currentProjectFile : currentRelease.files)
                    printer.printRecord(currentReleaseIndex, currentProjectFile.name, currentProjectFile.numberOfAuthors);

                currentReleaseIndex++;
            }

            printer.close();
            fw.close();

        } catch (IOException e) {

            this.logger.severe(e.getMessage());
            System.exit(e.hashCode());
        }
    }
}

