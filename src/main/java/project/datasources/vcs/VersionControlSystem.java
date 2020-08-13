package project.datasources.vcs;

import project.model.Commit;
import project.model.File;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface VersionControlSystem {

    Commit getCommitByTag(String tag);

    Commit getCommitByDate(LocalDateTime date);

    Commit getCommitByLogMessagePattern(String pattern);

    Map<String, File> getFiles(String commitHash);

    List<String> getFilesChangedByCommit(String commitHash);

    void computeFileMetrics(File file, Commit releaseCommit);
}