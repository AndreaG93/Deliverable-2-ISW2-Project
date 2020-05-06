package core.vcs;

import project.Commit;
import project.ProjectFile;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.AbstractMap;
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

    public abstract AbstractMap<LocalDateTime, Commit> getAllCommits();

    public abstract List<ProjectFile> getAllFilesFromCommit(String commitGUID);

    public abstract int getNumberOfAuthorsOfFile(String filename, LocalDate dateLowerBound, LocalDate dateUpperBound);
}

