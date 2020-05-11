package project;

import project.datasources.ApplicationEntryPoint;
import project.datasources.its.IssueTrackingSystem;
import project.datasources.its.jira.JIRA;
import project.datasources.vcs.VersionControlSystem;
import project.datasources.vcs.git.Git;
import project.metadata.ReleaseFileMetadata;
import project.metadata.ReleaseMetadata;
import project.release.Exportable;
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
            currentRelease.metadata.put(ReleaseMetadata.releaseVersionOrderID, i);

            ReleaseCommit releaseCommit = this.git.getReleaseCommit((LocalDateTime) currentRelease.metadata.get(ReleaseMetadata.releaseDate));

            List<ReleaseFile> kk = this.git.getReleaseFiles(releaseCommit.hash);
            for (ReleaseFile releaseFile : kk)
                releaseFile.fileMetricsRegistry.put(ReleaseFileMetadata.releaseVersionOrderID, i);

            currentRelease.setFiles(kk);

            ConcurrentLinkedQueue<ReleaseFile> waitFreeQueue = new ConcurrentLinkedQueue<>(currentRelease.getFiles());

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

            } catch (Exception e) {

                logger.severe(e.getMessage());
                System.exit(e.hashCode());
            }

            break; // TODO
        }
    }

    public void exportCollectedDataset() {

        List<String> headerForReleaseDataset = Release.exportMetadataKey();
        List<String> headerForFileDataset = ReleaseFile.exportMetadataKey();

        boolean jj = true;

        ProjectDatasetExporter.exportToCSV("ReleaseInfo.csv", Arrays.asList(this.releases), headerForReleaseDataset, true);

        for (Release release : this.releases) {

            List<Exportable> GG = Arrays.asList(release.getFiles().toArray(new ReleaseFile[0]));

            ProjectDatasetExporter.exportToCSV("FileDataset.csv", GG, headerForFileDataset, jj);
            jj = false;
        }

    }
}