package main;

import datasetbuilder.ProjectDatasetBuilderForPredictionPurposes;
import entities.project.Bookkeeper;
import entities.project.Project;
import java.util.ArrayList;
import java.util.List;

public class DatasetBuilderForPrediction {

    public static void main(String[] args) {

        List<Project> projectList = new ArrayList<>();

        projectList.add(new Bookkeeper());

        for (Project project : projectList) {

            ProjectDatasetBuilderForPredictionPurposes object = new ProjectDatasetBuilderForPredictionPurposes(project);

            object.buildProjectDataset();
            object.exportProjectDatasetAsCSV();
        }
    }
}
