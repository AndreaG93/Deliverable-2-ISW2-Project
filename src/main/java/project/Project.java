package project;

import core.ApplicationEntryPoint;
import core.its.IssueTrackingSystem;
import core.its.jira.JIRA;
import core.vcs.Release;
import core.vcs.ReleaseCommit;
import core.vcs.ReleaseFile;
import core.vcs.VersionControlSystem;
import core.vcs.git.Git;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;


public class Project {

    private static final String rootProjectDirectory = "C://";

    private final VersionControlSystem git;
    private final IssueTrackingSystem jira;

    public final String name;
    private final Logger logger;

    private Release[] releases;

    public Project(String name, String repositoryURL) {

        this.name = name;

        this.git = new Git(rootProjectDirectory, repositoryURL, rootProjectDirectory + name);
        this.jira = new JIRA();

        this.logger = Logger.getLogger(ApplicationEntryPoint.class.getName());
    }

    public void getDataFromIssueTrackingSystem() {

        this.releases = this.jira.getProjectReleases(this.name);
    }

    public void getDataFromVersionControlSystem() {

        for (int i = 0; i < this.releases.length / 2; i++) {

            Release currentRelease = this.releases[i];

            ReleaseCommit releaseCommit = this.git.getReleaseCommit(currentRelease.releaseDate);

            currentRelease.files = this.git.getReleaseFiles(releaseCommit.hash);

            ConcurrentLinkedQueue<ReleaseFile> waitFreeQueue = new ConcurrentLinkedQueue<>(currentRelease.files);

            List<Thread> threadList = new ArrayList<>();

            for (int threadID = 0; threadID < 4; threadID++) {

                Runnable runnable = new ProjectDatasetBuilderThread(waitFreeQueue, new Git(rootProjectDirectory, null, rootProjectDirectory + name), releaseCommit);
                Thread thread = new Thread(runnable);

                thread.start();
                threadList.add(thread);
            }

            try {

                for (Thread currentThread : threadList)
                    currentThread.join();

            } catch (InterruptedException e) {

                logger.severe(e.getMessage());
                System.exit(e.hashCode());
            }

            break;
        }
    }

    public void exportCollectedDataset() {

        ProjectDatasetExporter.exportProjectReleasesInfo(this.name, this.releases);
        ProjectDatasetExporter.exportProjectReleasesFileDataset(this.name, this.releases);
    }
}