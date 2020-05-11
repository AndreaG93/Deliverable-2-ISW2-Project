package project.datasources.vcs.git;

import project.release.ReleaseFile;
import utilis.external.ExternalApplicationOutputReader;

import java.util.ArrayList;
import java.util.List;

public class GitFilesGetter implements ExternalApplicationOutputReader {

    public final List<ReleaseFile> output;

    public GitFilesGetter() {
        this.output = new ArrayList<>();
    }

    private boolean isJavaFile(String filename) {

        int lastIndexOf = filename.lastIndexOf('.');
        if (lastIndexOf == -1)
            return false;

        String extension = filename.substring(lastIndexOf).toLowerCase();

        return extension.equals(".java");
    }

    @Override
    public void readOutputLine(String input) {

        String[] fileData = input.split("\\s+");

        String fileName = fileData[3];
        String fileHash = fileData[2];

        if (isJavaFile(fileName))
            this.output.add(new ReleaseFile(fileName, fileHash));
    }

    @Override
    public boolean isOutputReadingTerminated() {
        return false;
    }
}
