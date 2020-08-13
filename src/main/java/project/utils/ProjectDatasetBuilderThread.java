package project.utils;

import project.datasources.vcs.VersionControlSystem;
import project.model.Commit;
import project.model.ReleaseFile;

import java.util.Queue;


public class ProjectDatasetBuilderThread implements Runnable {

    private final Queue<ReleaseFile> waitFreeQueue;
    private final VersionControlSystem versionControlSystem;
    private final Commit releaseCommit;

    public ProjectDatasetBuilderThread(Queue<ReleaseFile> waitFreeQueue, VersionControlSystem versionControlSystem, Commit releaseCommit) {
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