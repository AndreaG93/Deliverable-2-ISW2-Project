package entities.project;

public class Bookkeeper extends Project {

    private static final String PROJECT_NAME = "bookkeeper";
    private static final String PROJECT_REPOSITORY_URL = "https://github.com/apache/bookkeeper";
    private static final String LAST_VERSION = "4.11.0";

    public Bookkeeper() {
        super(PROJECT_NAME, PROJECT_REPOSITORY_URL, LAST_VERSION);
    }
}

