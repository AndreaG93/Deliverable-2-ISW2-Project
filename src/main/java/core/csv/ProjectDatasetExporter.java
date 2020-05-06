package core.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import project.Project;
import project.ProjectFile;
import project.Release;

import java.io.FileWriter;
import java.io.IOException;
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

            printer.printRecord("Index", "Version ID", "Version Name", "Release Date", "EndOfLife Date");


            for (int i = 1; i < this.project.releases.length; i++) {

                Release currentRelease = this.project.releases[i];

                printer.printRecord(i, currentRelease.id, currentRelease.name, currentRelease.releaseDate, currentRelease.endOfLifeDate);
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


            for (int i = 0; i < this.project.releases.length / 2; i++) {

                Release currentRelease = this.project.releases[i];

                for (ProjectFile currentProjectFile : currentRelease.files)
                    printer.printRecord(i, currentProjectFile.name, currentProjectFile.numberOfAuthors);

                break;
            }

            printer.close();
            fw.close();

        } catch (IOException e) {

            this.logger.severe(e.getMessage());
            System.exit(e.hashCode());
        }
    }
}

