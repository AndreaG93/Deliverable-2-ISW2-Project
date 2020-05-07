package core.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import project.Project;
import project.ProjectFile;
import project.Release;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
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

            printer.printRecord("Index", "Version ID", "Version Name", "Release Date");


            for (int i = 1; i < this.project.releases.length; i++) {

                Release currentRelease = this.project.releases[i];

                printer.printRecord(i, currentRelease.id, currentRelease.name, currentRelease.releaseDate);
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

            Field[] projectFileFields = ProjectFile.class.getFields();

            printer.print("VersionID");
            for (Field field : projectFileFields)
                printer.print(field.getName());
            printer.println();

            int currentReleaseIndex = 1;
            for (Release currentRelease : this.project.releases) {

                for (ProjectFile currentProjectFile : currentRelease.files) {

                    printer.print(currentReleaseIndex);

                    for (Field field : projectFileFields)
                        printer.print(field.get(currentProjectFile));

                    printer.println();
                }

                break;
            }

            printer.close();
            fw.close();

        } catch (IOException | IllegalAccessException e) {

            this.logger.severe(e.getMessage());
            System.exit(e.hashCode());
        }
    }
}

