package datasetbuilder;

import entities.File;
import entities.FileCSV;
import entities.enums.DatasetOutputField;
import entities.project.Project;
import entities.release.Release;

import java.util.HashMap;
import java.util.Map;

public class ProjectDatasetBuilderForPredictionPurposes extends ProjectDatasetBuilder {

    private Release release;

    public ProjectDatasetBuilderForPredictionPurposes(Project project) {
        super(project);
    }

    @Override
    public void buildProjectDataset() {

        collectRelease();
        collectFilesOfRelease(this.release);
        discardJavaTestFile();
        collectReleaseFileMetadata(this.release);

        for (File file : this.release.getFiles()) {
            file.setMetadata(DatasetOutputField.IS_BUGGY, true);
            break;
        }
    }

    @Override
    public void exportProjectDatasetAsCSV() {

        FileCSV datasetCSV = new FileCSV(this.project.datasetForPredictionFilename, DatasetOutputField.convertToStringList());

        for (File file : release.getFiles())
            datasetCSV.write(file.getMetadataAsString(DatasetOutputField.values()));

        datasetCSV.close();
    }

    private void collectRelease() {

        collectReleases();

        this.release = this.releasesByReleaseDate.lastEntry().getValue();
    }

    private void discardJavaTestFile() {

        Map<String, File> obj = new HashMap<>();

        for (File file : this.release.getFiles()) {
            if (!file.getName().contains("/src/test/"))
                obj.put(file.getName(), file);
        }

        this.release.setFileRegistry(obj);
    }
}
