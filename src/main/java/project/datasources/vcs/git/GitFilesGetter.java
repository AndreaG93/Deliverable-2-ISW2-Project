package project.datasources.vcs.git;

import project.model.ReleaseFile;
import utilis.common.Utils;
import utilis.external.ExternalApplicationOutputReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GitFilesGetter implements ExternalApplicationOutputReader {

    public final Map<String, ReleaseFile> output;

    public GitFilesGetter() {
        this.output = new TreeMap<>();
    }

    @Override
    public void readOutputLine(String input) {

        String[] fileData = input.split("\\s+");

        String fileName = fileData[3];
        String fileHash = fileData[2];

        if (Utils.isJavaFile(fileName))
            this.output.put(fileName, new ReleaseFile(fileName, fileHash));
    }

    @Override
    public boolean isOutputReadingTerminated() {
        return false;
    }
}
