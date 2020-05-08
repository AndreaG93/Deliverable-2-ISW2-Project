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

    @Override
    public void readOutputLine(String input) {

        ProjectFile projectFile = new ProjectFile();
        projectFile.name = input;

        this.output.add(projectFile);
    }

    @Override
    public boolean isOutputReadingTerminated() {
        return false;
    }
}
