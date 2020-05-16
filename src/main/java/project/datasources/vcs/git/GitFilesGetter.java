package project.datasources.vcs.git;

import project.entities.File;
import utilis.common.Utils;
import utilis.external.ExternalApplicationOutputReader;

import java.util.ArrayList;
import java.util.List;

public class GitFilesGetter implements ExternalApplicationOutputReader {

    public final List<File> output;

    public GitFilesGetter() {
        this.output = new ArrayList<>();
    }

    @Override
    public void readOutputLine(String input) {

        String[] fileData = input.split("\\s+");

        String fileName = fileData[3];
        String fileHash = fileData[2];

        if (Utils.isJavaFile(fileName))
            this.output.add(new File(fileName, fileHash));
    }

    @Override
    public boolean isOutputReadingTerminated() {
        return false;
    }
}
