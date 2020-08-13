package project;

import project.model.Project;

public class Bookkeeper extends Project {

    private static final String PROJECT_NAME = "bookkeeper";
    private static final String PROJECT_REPOSITORY_URL = "https://github.com/apache/bookkeeper";

    public Bookkeeper() {
        super(PROJECT_NAME, PROJECT_REPOSITORY_URL);
    }
}

