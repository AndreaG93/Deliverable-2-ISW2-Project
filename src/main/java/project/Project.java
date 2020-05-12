package project;

import entrypoint.ApplicationEntryPoint;
import project.datasources.its.IssueTrackingSystem;
import project.datasources.its.jira.JIRA;
import project.datasources.vcs.VersionControlSystem;
import project.datasources.vcs.git.Git;
import project.metadata.ReleaseFileMetadata;
import project.metadata.ReleaseMetadata;
import project.release.Release;
import project.release.ReleaseCommit;
import project.release.ReleaseFile;
import project.utils.ProjectDatasetBuilderThread;
import project.utils.ProjectDatasetExporter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;


public class Project {

    private static final String rootProjectDirectory = "C://";
    private int numberOfReleaseToAnalyze = 0;

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
        this.numberOfReleaseToAnalyze = 1; //this.releases.length / 2;
    }

    public void getDataFromVersionControlSystem() {

        for (int i = 0; i < this.numberOfReleaseToAnalyze; i++) {

            Release currentRelease = this.releases[i];
            ReleaseCommit currentReleaseCommit = this.git.getReleaseCommit((LocalDateTime) currentRelease.metadata.get(ReleaseMetadata.releaseDate));
            List<ReleaseFile> currentReleaseFileList = this.git.getReleaseFiles(currentReleaseCommit.hash);

            for (ReleaseFile releaseFile : currentReleaseFileList)
                releaseFile.fileMetricsRegistry.put(ReleaseFileMetadata.releaseVersionOrderID, i);
            currentRelease.setFiles(currentReleaseFileList);

            ConcurrentLinkedQueue<ReleaseFile> waitFreeQueue = new ConcurrentLinkedQueue<>(currentRelease.getFiles());

            List<Thread> threadList = new ArrayList<>();

            for (int threadID = 0; threadID < 4; threadID++) {

                Runnable runnable = new ProjectDatasetBuilderThread(waitFreeQueue, new Git(rootProjectDirectory, null, rootProjectDirectory + name), currentReleaseCommit);
                Thread thread = new Thread(runnable);

                thread.start();
                threadList.add(thread);
            }

            try {

                for (Thread currentThread : threadList)
                    currentThread.join();

            } catch (Exception e) {

                logger.severe(e.getMessage());
                System.exit(e.hashCode());
            }
        }
    }

    public void exportCollectedDataset() {

        List<String> headerForReleaseDataset = Release.exportMetadataKey();
        List<String> headerForFileDataset = ReleaseFile.exportMetadataKey();

        ProjectDatasetExporter.exportHeaderAndDataset("ReleaseInfo.csv", headerForReleaseDataset, Arrays.asList(this.releases));

        ProjectDatasetExporter.exportHeader("FileDataset.csv", headerForFileDataset);

        for (int i = 0; i < this.numberOfReleaseToAnalyze; i++)
            ProjectDatasetExporter.exportDataset("FileDataset.csv", this.releases[i].getFilesAsExportable());
    }
}