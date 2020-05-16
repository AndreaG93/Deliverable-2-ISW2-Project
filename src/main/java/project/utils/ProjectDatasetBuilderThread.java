package project.utils;

import project.datasources.vcs.VersionControlSystem;
import project.entities.Commit;
import project.entities.File;

import java.util.Queue;


public class ProjectDatasetBuilderThread implements Runnable {

    private final Queue<File> waitFreeQueue;
    private final VersionControlSystem versionControlSystem;
    private final Commit releaseCommit;

    public ProjectDatasetBuilderThread(Queue<File> waitFreeQueue, VersionControlSystem versionControlSystem, Commit releaseCommit) {
        this.waitFreeQueue = waitFreeQueue;
        this.versionControlSystem = versionControlSystem;
        this.releaseCommit = releaseCommit;
    }

    @Override
    public void run() {

        for (File currentReleaseFile = this.waitFreeQueue.poll(); currentReleaseFile != null; currentReleaseFile = this.waitFreeQueue.poll())
            this.versionControlSystem.computeFileMetrics(currentReleaseFile, this.releaseCommit);
    }
}