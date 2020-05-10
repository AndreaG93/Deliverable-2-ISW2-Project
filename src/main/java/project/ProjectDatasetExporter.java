package project;

import core.FileMetrics;
import core.vcs.Release;
import core.vcs.ReleaseFile;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import utilis.common.ResourceManagement;

import java.io.FileWriter;
import java.util.logging.Logger;

public class ProjectDatasetExporter {

    public static void exportProjectReleasesInfo(String projectName, Release[] releases) {

        FileWriter fileWriter = null;
        CSVPrinter csvPrinter = null;

        try {

            fileWriter = new FileWriter(projectName + "ReleaseInfo.csv");
            csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);

            csvPrinter.printRecord("Index", "Version ID", "Version Name", "Release Date");

            for (int i = 1; i < releases.length; i++) {

                Release currentRelease = releases[i];
                csvPrinter.printRecord(i, currentRelease.id, currentRelease.name, currentRelease.releaseDate);
            }

        } catch (Exception e) {

            Logger.getLogger(ProjectDatasetExporter.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());

        } finally {

            ResourceManagement.close(csvPrinter);
            ResourceManagement.close(fileWriter);
        }
    }

    public static void exportProjectReleasesFileDataset(String projectName, Release[] releases) {

        FileWriter fileWriter = null;
        CSVPrinter csvPrinter = null;

        try {

            fileWriter = new FileWriter(projectName + "FileDataset.csv");
            csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);

            printReleaseFilesDatasetHeader(csvPrinter);

            for (int i = 0; i < releases.length; i++) {

                Release currentRelease = releases[i];

                if (currentRelease.files != null)
                    for (ReleaseFile currentReleaseFile : currentRelease.files)
                        printReleaseFileDatasetAndVersionReleaseID(csvPrinter, currentReleaseFile, i);
            }

        } catch (Exception e) {

            Logger.getLogger(ProjectDatasetExporter.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());

        } finally {

            ResourceManagement.close(csvPrinter);
            ResourceManagement.close(fileWriter);
        }
    }

    private static void printReleaseFilesDatasetHeader(CSVPrinter csvPrinter) throws Exception {

        csvPrinter.print("ReleaseID");
        csvPrinter.print("Name");

        for (FileMetrics fileMetric : FileMetrics.values())
            csvPrinter.print(fileMetric.toString());

        csvPrinter.println();
    }

    private static void printReleaseFileDatasetAndVersionReleaseID(CSVPrinter csvPrinter, ReleaseFile releaseFile, int releaseID) throws Exception {

        if (releaseFile.fileMetricsRegistry.isEmpty())
            return;

        csvPrinter.print(releaseID);
        csvPrinter.print(releaseFile.name);

        for (FileMetrics fileMetric : FileMetrics.values())
            csvPrinter.print(releaseFile.fileMetricsRegistry.get(fileMetric).toString());

        csvPrinter.println();
    }
}
