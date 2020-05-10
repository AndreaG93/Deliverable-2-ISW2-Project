package project;

import core.vcs.ReleaseCommit;
import core.vcs.ReleaseFile;
import core.vcs.VersionControlSystem;

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

        int currentFile = 0;

        for (ReleaseFile currentReleaseFile = this.waitFreeQueue.poll(); currentReleaseFile != null; currentReleaseFile = this.waitFreeQueue.poll()) {

            this.versionControlSystem.computeFileMetrics(currentReleaseFile, this.releaseCommit);
            if (currentFile == 1)
                return;
            else
                currentFile++;
        }
    }
}