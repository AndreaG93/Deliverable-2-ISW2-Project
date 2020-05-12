package project.utils;

import project.datasources.vcs.VersionControlSystem;
import project.release.ReleaseCommit;
import project.release.ReleaseFile;

import java.util.Queue;


public class ProjectDatasetBuilderThread implements Runnable {

    private final Queue<ReleaseFile> waitFreeQueue;
    private final VersionControlSystem versionControlSystem;
    private final ReleaseCommit releaseCommit;

    public ProjectDatasetBuilderThread(Queue<ReleaseFile> waitFreeQueue, VersionControlSystem versionControlSystem, ReleaseCommit releaseCommit) {
        this.waitFreeQueue = waitFreeQueue;
        this.versionControlSystem = versionControlSystem;
        this.releaseCommit = releaseCommit;
    }

    @Override
    public void run() {

        for (ReleaseFile currentReleaseFile = this.waitFreeQueue.poll(); currentReleaseFile != null; currentReleaseFile = this.waitFreeQueue.poll())
            this.versionControlSystem.computeFileMetrics(currentReleaseFile, this.releaseCommit);
    }
}