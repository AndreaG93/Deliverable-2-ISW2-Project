package project.datasources.vcs;

import project.release.ReleaseCommit;
import project.release.ReleaseFile;

import java.time.LocalDateTime;
import java.util.List;

public interface VersionControlSystem {

    List<ReleaseFile> getReleaseFiles(String releaseCommitHash);

    ReleaseCommit getReleaseCommit(LocalDateTime releaseDate);

    void computeFileMetrics(ReleaseFile releaseFile, ReleaseCommit releaseCommit);
}