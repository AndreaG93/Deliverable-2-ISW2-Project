package launchers;

import datasetbuilder.ProjectDatasetBuilder;
import entities.project.Bookkeeper;
import entities.project.OpenJPA;
import entities.project.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DatasetBuilder {

    public static void main(String[] args) {

        List<Project> projectList = new ArrayList<>();

        projectList.add(new Bookkeeper());
        projectList.add(new OpenJPA());

        for (Project project : projectList) {

            Logger.getLogger(DatasetBuilder.class.getName()).info("Creating dataset for project: " + project.name);

            ProjectDatasetBuilder projectDatasetBuilder = new ProjectDatasetBuilder(project);
            projectDatasetBuilder.buildProjectDataset();
            projectDatasetBuilder.exportProjectDatasetAsCSV();

            Logger.getLogger(DatasetBuilder.class.getName()).info("Dataset creation for project " + project.name + " is now complete!");
        }
    }
}