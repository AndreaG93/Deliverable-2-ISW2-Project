package core;

import core.csv.ProjectDatasetExporter;
import project.Project;

import java.util.logging.Logger;

public class ApplicationEntryPoint {

    private static final String projectName = "TINKERPOP";
    private static final String projectRepositoryURL = "https://github.com/apache/tinkerpop";

    public static void main(String[] args) {

        Logger logger = Logger.getLogger(ApplicationEntryPoint.class.getName());
        Project project = new Project(projectName, projectRepositoryURL);

        logger.info("Start dataset building...");
        project.buildDataset();
        logger.info("Dataset building: COMPLETE!");

        logger.info("Start dataset exportation...");

        ProjectDatasetExporter projectDatasetExporter = new ProjectDatasetExporter(project);

        projectDatasetExporter.exportTo("./OUTPUT.csv");
        projectDatasetExporter.exportReleaseInfo();

        logger.info("Dataset exportation: COMPLETE!");
    }
}