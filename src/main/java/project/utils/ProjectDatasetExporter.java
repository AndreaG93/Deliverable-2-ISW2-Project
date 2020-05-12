package project.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import utilis.common.ResourceManagement;

import java.io.FileWriter;
import java.util.List;
import java.util.logging.Logger;

public class ProjectDatasetExporter {

    private ProjectDatasetExporter() {
    }


    public static void exportHeader(String outputFile, List<String> header) {
        exportHeaderAndDataset(outputFile, header, null);
    }

    public static void exportDataset(String outputFile, List<Exportable> dataset) {
        exportHeaderAndDataset(outputFile, null, dataset);
    }

    public static void exportHeaderAndDataset(String outputFile, List<String> header, List<Exportable> dataset) {

        FileWriter fileWriter = null;
        CSVPrinter csvPrinter = null;

        try {

            fileWriter = new FileWriter(outputFile, true);
            csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);

            if (header != null)
                csvPrinter.printRecord(header);

            if (dataset != null)
                for (Exportable data : dataset)
                    csvPrinter.printRecord(data.exportMetadataValues());

        } catch (Exception e) {

            Logger.getLogger(ProjectDatasetExporter.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());

        } finally {

            ResourceManagement.close(csvPrinter);
            ResourceManagement.close(fileWriter);
        }
    }
}
