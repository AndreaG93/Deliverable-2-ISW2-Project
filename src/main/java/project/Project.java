package project;

import core.ApplicationEntryPoint;
import core.its.IssueTrackingSystem;
import core.its.jira.JIRA;
import core.vcs.VersionControlSystem;
import core.vcs.git.Git;
import project.entities.Commit;
import project.entities.ProjectFile;
import project.entities.ProjectRelease;
import project.exporter.ProjectDatasetExporter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;


public class Project {

    private static final String rootProjectDirectory = "C://";
    public final String name;

    private final VersionControlSystem git;
    private final IssueTrackingSystem jira;

    public ProjectRelease[] projectReleases;
    private final Logger logger;

    public Project(String name, String repositoryURL) {

        this.name = name;

        this.git = new Git(rootProjectDirectory, repositoryURL, rootProjectDirectory + name);
        this.jira = new JIRA();

        this.logger = Logger.getLogger(ApplicationEntryPoint.class.getName());
    }

    public void getDataFromIssueTrackingSystem() {

        this.projectReleases = this.jira.getProjectReleases(this.name);
    }

    public void getDataFromVersionControlSystem() {

        for (int i = 0; i < this.projectReleases.length / 2; i++) {

            ProjectRelease currentProjectRelease = this.projectReleases[i];

            Commit releaseCommit = this.git.getCommit(currentProjectRelease.releaseDate);

            currentProjectRelease.files = this.git.getFiles(releaseCommit.hash);

            ConcurrentLinkedQueue<ProjectFile> waitFreeQueue = new ConcurrentLinkedQueue<>(currentProjectRelease.files);

            List<Thread> threadList = new ArrayList<>();

            for (int threadID = 0; threadID < 4; threadID++) {

                Runnable runnable = new ProjectDatasetBuilderThread(waitFreeQueue, releaseCommit, rootProjectDirectory + name, threadID);
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
        }
    }

    public void exportCollectedDataset(String outputFileName) {

        ProjectDatasetExporter.exportReleaseInfo(this);
        ProjectDatasetExporter.exportTo(this, outputFileName);
    }
}