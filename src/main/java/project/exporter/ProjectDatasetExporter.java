package project.exporter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import project.Project;
import project.entities.ProjectFile;
import project.entities.ProjectRelease;
import utilis.common.ResourceManagement;

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

        FileWriter fileWriter = null;
        CSVPrinter csvPrinter = null;

        try {

            fileWriter = new FileWriter(outputFileDirectory);
            csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);

            printProjectFilesDatasetHeader(csvPrinter);

            for (int i = 0; i < project.projectReleases.length / 2; i++) {

                ProjectRelease currentProjectRelease = project.projectReleases[i];

                for (ProjectFile currentProjectFile : currentProjectRelease.files)
                    printFileDatasetAndVersionReleaseID(csvPrinter, currentProjectFile, i);
            }

        } catch (Exception e) {

            Logger.getLogger(ProjectDatasetExporter.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());

        } finally {

            ResourceManagement.close(csvPrinter);
            ResourceManagement.close(fileWriter);
        }
    }

    private static void printProjectFilesDatasetHeader(CSVPrinter csvPrinter) throws Exception {

        Field[] projectFileFields = ProjectFile.class.getFields();

        csvPrinter.print("VersionID");

        for (Field field : projectFileFields)
            csvPrinter.print(field.getName());

        csvPrinter.println();
    }

    private static void printFileDatasetAndVersionReleaseID(CSVPrinter csvPrinter, ProjectFile projectFile, int releaseID) throws Exception {

        Field[] projectFileFields = ProjectFile.class.getFields();

        csvPrinter.print(releaseID);

        for (Field field : projectFileFields)
            csvPrinter.print(field.get(projectFile));

        csvPrinter.println();
    }
}
