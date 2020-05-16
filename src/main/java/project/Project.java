package project;

import entrypoint.ApplicationEntryPoint;
import project.datasources.its.IssueTrackingSystem;
import project.datasources.its.jira.jira;
import project.datasources.vcs.VersionControlSystem;
import project.datasources.vcs.git.Git;

import project.entities.*;
import project.entities.metadata.FileMetadata;
import project.entities.metadata.ReleaseMetadata;
import project.utils.ProjectDatasetBuilderThread;
import utilis.common.FileCSV;
import utilis.common.Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;


public class Project {

    private static final String rootProjectDirectory = "C://";

    public final String name;

    private Map<Integer, Release> releases;
    private Map<LocalDateTime, Release> releasesIndexedByDate;
    private List<Issue> issues;

    private final Logger logger;
    private final VersionControlSystem versionControlSystem;
    private final IssueTrackingSystem issueTrackingSystem;

    public Project(String name, String repositoryURL) {

        this.name = name;

        this.versionControlSystem = new Git(rootProjectDirectory, repositoryURL, rootProjectDirectory + name);
        this.issueTrackingSystem = new jira();

        this.logger = Logger.getLogger(ApplicationEntryPoint.class.getName());
    }

    public void collectAllReleasesAndIssues() {

        this.issues = this.issueTrackingSystem.getIssues(this.name);
        this.releases = this.issueTrackingSystem.getReleases(this.name);
    }

    public void collectCommitAssociatedToEachRelease() {

        for (Map.Entry<Integer, Release> releaseEntry : releases.entrySet()) {

            Release release = releaseEntry.getValue();

            String releaseTag = (String) release.metadata.get(ReleaseMetadata.NAME);
            LocalDateTime releaseDate = (LocalDateTime) release.metadata.get(ReleaseMetadata.RELEASE_DATE);

            Commit releaseCommit = getReleaseCommit(releaseTag, releaseDate);

            release.setReleaseCommit(releaseCommit);
            release.metadata.put(ReleaseMetadata.RELEASE_DATE, releaseCommit.date);
        }

        orderReleasesAccordingToReleaseDate();
    }

    private Commit getReleaseCommit(String releaseTag, LocalDateTime releaseDate) {

        Commit output;

        output = this.versionControlSystem.getCommitByTag(releaseTag);
        if (output == null)
            output = this.versionControlSystem.getCommitByDate(releaseDate);

        return output;
    }

    private void orderReleasesAccordingToReleaseDate() {

        this.releasesIndexedByDate = new TreeMap<>();

        for (Map.Entry<Integer, Release> releaseEntry : this.releases.entrySet()) {

            Release release = releaseEntry.getValue();

            LocalDateTime releaseDate = (LocalDateTime) release.metadata.get(ReleaseMetadata.RELEASE_DATE);

            this.releasesIndexedByDate.put(releaseDate, release);
        }

        int orderID = 0;
        for (Map.Entry<LocalDateTime, Release> releaseEntry : this.releasesIndexedByDate.entrySet()) {

            Release release = releaseEntry.getValue();

            release.metadata.put(ReleaseMetadata.RELEASE_VERSION_ORDER_ID, orderID);

            orderID++;
        }
    }

    public void collectFilesAssociatedToEachRelease() {

        for (Map.Entry<Integer, Release> releaseEntry : releases.entrySet()) {

            Release release = releaseEntry.getValue();
            Commit releaseCommit = release.getReleaseCommit();

            List<File> releaseFiles = this.versionControlSystem.getCommitFiles(releaseCommit.hash);

            for (File file : releaseFiles)
                file.metadata.put(FileMetadata.RELEASE_VERSION_ORDER_ID, release.metadata.get(ReleaseMetadata.RELEASE_VERSION_ORDER_ID));

            release.setReleaseFiles(releaseFiles);
        }
    }

    public void collectBuggyReleaseFiles() {

        for (Issue issue : this.issues) {

            Commit fixCommit = this.versionControlSystem.getCommitByLogMessagePattern(issue.key);

            if (fixCommit != null) {

                List<String> buggyFilesNames = this.versionControlSystem.getFilesChangedByCommit(fixCommit.hash);

                for (String affectedVersionsID : issue.affectedVersionsIDs) {

                    Release release = this.releases.get(Integer.valueOf(affectedVersionsID));

                    for (String buggyFileName : buggyFilesNames)
                        if (Utils.isJavaFile(buggyFileName))
                            markReleaseFileAsBuggy(release, buggyFileName);
                }
            }
        }
    }

    private void markReleaseFileAsBuggy(Release release, String filename) {

        Map<String, File> map = release.getReleasesFileIndexedByFilename();
        File buggyFile = map.get(filename);

        if (buggyFile != null)
            buggyFile.metadata.put(FileMetadata.IS_BUGGY, true);
    }


    public void collectReleasesFileMetadata() {

        int numberOfReleaseAnalyzed = 0;

        for (Map.Entry<Integer, Release> releaseEntry : releases.entrySet()) {

            Release currentRelease = releaseEntry.getValue();

            ConcurrentLinkedQueue<File> waitFreeQueue = new ConcurrentLinkedQueue<>(currentRelease.getReleaseFiles());

            List<Thread> threadList = new ArrayList<>();

            for (int threadID = 0; threadID < 4; threadID++) {

                Runnable runnable = new ProjectDatasetBuilderThread(waitFreeQueue, new Git(rootProjectDirectory, null, rootProjectDirectory + name), currentRelease.getReleaseCommit());
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

            if (numberOfReleaseAnalyzed == 1)
                break;
            else
                numberOfReleaseAnalyzed++;
        }
    }


    public void exportCollectedDataset() {

        FileCSV datasetFile = new FileCSV(this.name + "-Dataset", FileMetadata.exportAsStringList());
        FileCSV releaseFile = new FileCSV(this.name + "-Release", ReleaseMetadata.exportAsStringList());

        for (Map.Entry<LocalDateTime, Release> releaseEntry : this.releasesIndexedByDate.entrySet()) {

            List<File> releaseFiles = releaseEntry.getValue().getReleaseFiles();
            List<MetadataExportable> exportableFileData = new ArrayList<>(releaseFiles);

            datasetFile.append(exportableFileData);
        }

        List<MetadataExportable> exportableReleaseData = new ArrayList<>();

        for (Map.Entry<LocalDateTime, Release> releaseEntry : this.releasesIndexedByDate.entrySet())
            exportableReleaseData.add(releaseEntry.getValue());

        releaseFile.append(exportableReleaseData);
    }
}