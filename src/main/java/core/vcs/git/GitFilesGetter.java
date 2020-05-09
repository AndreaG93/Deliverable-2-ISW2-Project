package core.vcs.git;

import project.entities.ProjectFile;
import utilis.external.ExternalApplicationOutputReader;

import java.util.ArrayList;
import java.util.List;

public class GitFilesGetter implements ExternalApplicationOutputReader {

    public final List<ProjectFile> output;

    public GitFilesGetter() {
        this.output = new ArrayList<>();
    }

    private boolean isJavaFile(String filename) {

        int lastIndexOf = filename.lastIndexOf(".");
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

        if (isJavaFile(fileName)) {

            ProjectFile projectFile = new ProjectFile();

            projectFile.name = fileName;
            projectFile.hash = fileHash;

            this.output.add(projectFile);
        }
    }

    @Override
    public boolean isOutputReadingTerminated() {
        return false;
    }
}
