package entrypoint;

import project.Bookkeeper;
import project.OpenJPA;
import project.model.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ApplicationEntryPoint {

    public static void main(String[] args) {

        Logger logger = Logger.getLogger(ApplicationEntryPoint.class.getName());

        List<Project> projects = new ArrayList<>();
        projects.add(new Bookkeeper());
        //projects.add(new OpenJPA());

        for (Project project : projects) {

            logger.info("Analyzing project " + project.name);

            project.buildProjectDataset();
            project.exportProjectDatasetAsCSV();

            logger.info(project.name + "'s analysis complete!");
        }
    }
}