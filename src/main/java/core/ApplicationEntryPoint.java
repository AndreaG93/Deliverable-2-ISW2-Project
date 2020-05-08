package core;

import project.Project;

import java.util.logging.Logger;

public class ApplicationEntryPoint {

    private static final String projectName = "TINKERPOP";
    private static final String projectRepositoryURL = "https://github.com/apache/tinkerpop";

    public static void main(String[] args) {

        Logger logger = Logger.getLogger(ApplicationEntryPoint.class.getName());

        Project project = new Project(projectName, projectRepositoryURL);

        logger.info("Getting data from 'Issue Tracking System'...");
        project.getDataFromIssueTrackingSystem();

        logger.info("Getting data from 'Version Control System'...");
        project.getDataFromVersionControlSystem();

        logger.info("Exporting collected dataset...");
        project.exportCollectedDataset("./OUTPUT.csv");
    }
}