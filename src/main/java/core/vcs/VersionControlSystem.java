package core.vcs;

import project.Commit;
import project.ProjectFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

public abstract class VersionControlSystem {

    protected final Logger logger;
    protected final String repositoryURL;
    protected File repositoryLocalDirectory;


    public VersionControlSystem(String repositoryURL, String repositoryLocalDirectory) {

        this.logger = Logger.getLogger(VersionControlSystem.class.getName());

        this.repositoryURL = repositoryURL;
        this.repositoryLocalDirectory = new File(repositoryLocalDirectory);
    }

    public abstract void cloneRepositoryLocally();

    public abstract void changeLocalRepositoryStateToCommit(String commitHash);

    public abstract double getFileWeekAge(String filename, LocalDateTime releaseDate);

    public abstract int getNumberOfAuthorsOfFile(String filename);

    public abstract long getFileLOC(String filename);

    public abstract Commit getReleaseCommit(LocalDateTime releaseDate);

    public abstract List<ProjectFile> getFiles(String commitHash);
}