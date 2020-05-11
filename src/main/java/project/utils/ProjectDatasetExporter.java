package project.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import project.release.Exportable;
import utilis.common.ResourceManagement;

import java.io.FileWriter;
import java.util.List;
import java.util.logging.Logger;

public class ProjectDatasetExporter {

    private ProjectDatasetExporter() {
    }

    public static void exportToCSV(String outputFile, List<Exportable> exportableList, List<String> header, boolean addHeader) {

        FileWriter fileWriter = null;
        CSVPrinter csvPrinter = null;

        try {

            fileWriter = new FileWriter(outputFile, true);
            csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);

            if (header != null)
                csvPrinter.printRecord(header);

            for (Exportable exportable : exportableList) {

                List<String> values = exportable.exportMetadataValues();
                if (values.size() == header.size())
                    csvPrinter.printRecord(exportable.exportMetadataValues());
            }


        } catch (Exception e) {

            Logger.getLogger(ProjectDatasetExporter.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());

        } finally {

            ResourceManagement.close(csvPrinter);
            ResourceManagement.close(fileWriter);
        }
    }
}
