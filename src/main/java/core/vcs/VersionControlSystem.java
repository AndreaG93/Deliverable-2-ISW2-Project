package core.vcs;

import project.entities.Commit;
import project.entities.ProjectFile;

import java.time.LocalDateTime;
import java.util.List;

public interface VersionControlSystem {

    List<ProjectFile> getFiles(String commitHash);

    double getFileAgeInWeeks(String filename, LocalDateTime releaseDate, String upperBoundCommitHash);

    int getNumberOfAuthorsOfFile(String filename, String upperBoundCommitHash);

    Commit getCommit(LocalDateTime releaseDate);

    List<String> getFileRevisions(String filename, String upperBoundCommitHash);

    long getChangeSetSize(String commitHash);

    FileChangeSetSizeMetric getChangeSetSizeMetric(List<String> fileRevisionsList);

    FileMetric getFileMetrics(String filename, List<String> fileRevisionsList);

    long getFileLOC(String filename);
}