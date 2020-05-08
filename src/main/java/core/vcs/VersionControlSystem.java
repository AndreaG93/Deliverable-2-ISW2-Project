package core.vcs;

import project.entities.Commit;
import project.entities.ProjectFile;

import java.time.LocalDateTime;
import java.util.List;

public interface VersionControlSystem {

    List<ProjectFile> getFiles(String commitHash, String revisionHash);

    double getFileAgeInWeeks(String filename, LocalDateTime releaseDate, String revisionHash);

    int getNumberOfAuthorsOfFile(String filename, String revisionHash);

    FileMetric getFileMetrics(String filename, String revisionHash);

    long getFileLOC(String filename);

    Commit getCommit(LocalDateTime releaseDate);

    FileChangeSetSizeMetric getChangeSetSizeMetric(String filename, String commitHash);
}