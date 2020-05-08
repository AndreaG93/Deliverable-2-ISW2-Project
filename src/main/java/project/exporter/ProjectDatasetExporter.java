package project.exporter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import project.Project;
import project.entities.ProjectFile;
import project.entities.ProjectRelease;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Logger;

public class ProjectDatasetExporter {

    public static void exportReleaseInfo(Project project) {
        try {

            FileWriter fw = new FileWriter("./ReleaseInfo");
            CSVPrinter printer = new CSVPrinter(fw, CSVFormat.DEFAULT);

            printer.printRecord("Index", "Version ID", "Version Name", "Release Date");


            for (int i = 1; i < project.projectReleases.length; i++) {

                ProjectRelease currentProjectRelease = project.projectReleases[i];

                printer.printRecord(i, currentProjectRelease.id, currentProjectRelease.name, currentProjectRelease.releaseDate);
            }


            printer.close();
            fw.close();

        } catch (IOException e) {

            Logger.getLogger(ProjectDatasetExporter.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());
        }
    }

    public static void exportTo(Project project, String outputFileDirectory) {

        try {

            FileWriter fw = new FileWriter(outputFileDirectory);
            CSVPrinter printer = new CSVPrinter(fw, CSVFormat.DEFAULT);

            Field[] projectFileFields = ProjectFile.class.getFields();

            printer.print("VersionID");
            for (Field field : projectFileFields)
                printer.print(field.getName());
            printer.println();

            int currentReleaseIndex = 1;
            for (ProjectRelease currentProjectRelease : project.projectReleases) {

                for (ProjectFile currentProjectFile : currentProjectRelease.files) {

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

            Logger.getLogger(ProjectDatasetExporter.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());
        }
    }
}

