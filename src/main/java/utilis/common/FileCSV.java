package utilis.common;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import project.entities.MetadataExportable;

import java.io.FileWriter;
import java.util.List;
import java.util.logging.Logger;

public class FileCSV {

    private final String outputFilename;

    public FileCSV(String outputFilename, List<String> csvHeader) {

        this.outputFilename = outputFilename + ".csv";

        write(csvHeader, null);
    }

    public void append(List<MetadataExportable> dataset) {
        write(null, dataset);
    }

    private void write(List<String> csvHeader, List<MetadataExportable> dataset) {

        FileWriter fileWriter = null;
        CSVPrinter csvPrinter = null;

        try {

            fileWriter = new FileWriter(this.outputFilename, true);
            csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);

            if (csvHeader != null)
                csvPrinter.printRecord(csvHeader);

            if (dataset != null)
                for (MetadataExportable data : dataset)
                    csvPrinter.printRecord(data.exportMetadataValues());

        } catch (Exception e) {

            Logger.getLogger(FileCSV.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());

        } finally {

            ResourceManagement.close(csvPrinter);
            ResourceManagement.close(fileWriter);
        }
    }
}
