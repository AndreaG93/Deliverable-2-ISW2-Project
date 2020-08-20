package datasetbuilder.datasources.vcs.git;

import entities.File;
import utilis.Utils;
import utilis.external.ExternalApplicationOutputReader;

import java.util.Map;
import java.util.TreeMap;

public class GitFilesGetter implements ExternalApplicationOutputReader {

    public final Map<String, File> output;

    public GitFilesGetter() {
        this.output = new TreeMap<>();
    }

    @Override
    public void readOutputLine(String input) {

        String[] fileData = input.split("\\s+");

        String fileName = fileData[3];
        String fileHash = fileData[2];

        if (Utils.isJavaFile(fileName))
            this.output.put(fileName, new File(fileName, fileHash));
    }

    @Override
    public boolean isOutputReadingTerminated() {
        return false;
    }
}
