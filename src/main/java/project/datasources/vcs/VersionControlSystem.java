package project.datasources.vcs;

import project.entities.Commit;
import project.entities.File;

import java.time.LocalDateTime;
import java.util.List;

public interface VersionControlSystem {

    Commit getCommitByTag(String tag);

    Commit getCommitByDate(LocalDateTime releaseDate);

    Commit getCommitByLogMessagePattern(String pattern);

    List<File> getCommitFiles(String commitHash);

    List<String> getFilesChangedByCommit(String commitHash);

    void computeFileMetrics(File releaseFile, Commit releaseCommit);
}