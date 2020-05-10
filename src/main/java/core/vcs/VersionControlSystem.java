package core.vcs;

import java.time.LocalDateTime;
import java.util.List;

public interface VersionControlSystem {

    List<ReleaseFile> getReleaseFiles(String releaseCommitHash);

    ReleaseCommit getReleaseCommit(LocalDateTime releaseDate);

    void computeFileMetrics(ReleaseFile releaseFile, ReleaseCommit releaseCommit);
}