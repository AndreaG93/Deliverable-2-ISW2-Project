import datasetbuilder.ProjectDatasetBuilder;
import entities.project.Bookkeeper;
import entities.project.OpenJPA;
import entities.project.Project;

import java.util.ArrayList;
import java.util.List;

public class DatasetBuilder {

    public static void main(String[] args) {

        List<Project> projectList = new ArrayList<>();

        projectList.add(new Bookkeeper());
        projectList.add(new OpenJPA());

        for (Project project : projectList) {

            ProjectDatasetBuilder projectDatasetBuilder = new ProjectDatasetBuilder(project);
            projectDatasetBuilder.buildProjectDataset();
            projectDatasetBuilder.exportProjectDatasetAsCSV();

        }
    }
}