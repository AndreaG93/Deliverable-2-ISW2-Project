package project;

import project.model.Project;

public class Bookkeeper extends Project {

    private static final String projectName = "bookkeeper";
    private static final String projectRepositoryURL = "https://github.com/apache/bookkeeper";

    public Bookkeeper() {
        super(projectName, projectRepositoryURL);
    }
}

