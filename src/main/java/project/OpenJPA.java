package project;

import project.model.Project;

public class OpenJPA extends Project {

    private static final String projectName = "openjpa";
    private static final String projectRepositoryURL = "https://github.com/apache/openjpa";

    public OpenJPA() {
        super(projectName, projectRepositoryURL);
    }
}
