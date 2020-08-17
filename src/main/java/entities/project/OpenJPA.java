package entities.project;

import entities.project.Project;

public class OpenJPA extends Project {

    private static final String PROJECT_NAME = "openjpa";
    private static final String PROJECT_REPOSITORY_URL = "https://github.com/apache/openjpa";

    public OpenJPA() {
        super(PROJECT_NAME, PROJECT_REPOSITORY_URL);
    }
}
