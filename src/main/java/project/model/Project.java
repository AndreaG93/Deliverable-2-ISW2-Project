package project.model;

import project.datasources.its.IssueTrackingSystem;
import project.datasources.its.jira.jira;
import project.datasources.vcs.VersionControlSystem;
import project.datasources.vcs.git.Git;

import project.model.metadata.MetadataType;
import project.utils.ProjectDatasetBuilderThread;
import utilis.common.ArithmeticMean;
import utilis.common.FileCSV;
import utilis.common.Utils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;


public class Project {

    public final String name;
    private final String rootDirectory;
    private final String workingDirectory;

    private List<Release> releases;
    private Map<Integer, Release> releasesByVersionID;


    private final VersionControlSystem versionControlSystem;
    private final IssueTrackingSystem issueTrackingSystem;

    private double proportion;

    public Project(String projectName, String repositoryURL) {

        this.name = projectName;

        this.rootDirectory = "C://";
        this.workingDirectory = this.rootDirectory + this.name;

        this.versionControlSystem = new Git(this.rootDirectory, this.workingDirectory, repositoryURL);
        this.issueTrackingSystem = new jira(projectName);
    }

    public void buildProjectDataset() {

        collectCommitOfEachRelease();
        orderReleasesByReleaseDate();
        collectFilesOfEachRelease();
        searchForDefectiveFile();
        //collectReleasesFilesMetadata();
    }

    public void exportProjectDatasetAsCSV() {

        FileCSV datasetCSV = new FileCSV("Dataset-" + this.name, MetadataType.convertToStringList(ReleaseFile.METADATA_FOR_DATASET));
        FileCSV releasesCSV = new FileCSV("Releases-" + this.name, MetadataType.convertToStringList(Release.METADATA_FOR_DATASET));

        for (Release release : this.releases) {

            releasesCSV.write(release.getMetadataValuesList(Release.METADATA_FOR_DATASET));

            for (Map.Entry<String, ReleaseFile> releaseFileEntry : release.getReleaseFiles().entrySet()) {

                ReleaseFile currentReleaseFile = releaseFileEntry.getValue();
                datasetCSV.write(currentReleaseFile.getMetadataValuesList(ReleaseFile.METADATA_FOR_DATASET));
            }
        }

        datasetCSV.close();
        releasesCSV.close();
    }

    private void orderReleasesByReleaseDate() {

        this.releases.sort(new Comparator<Release>() {
            @Override
            public int compare(Release o1, Release o2) {
                return o1.getReleaseCommit().date.compareTo(o2.getReleaseCommit().date);
            }
        });
    }

    private void collectCommitOfEachRelease() {

        this.releasesByVersionID = new TreeMap<>();
        this.releases = this.issueTrackingSystem.getReleases();

        for (Release release : this.releases) {

            String releaseTag = (String) release.getMetadataValue(MetadataType.NAME);
            LocalDateTime releaseDate = (LocalDateTime) release.getMetadataValue(MetadataType.DATE);

            Commit releaseCommit = this.versionControlSystem.getCommitByTag(releaseTag);
            if (releaseCommit == null)
                releaseCommit = this.versionControlSystem.getCommitByDate(releaseDate);

            release.setReleaseCommit(releaseCommit);

            this.releasesByVersionID.put((int) release.getMetadataValue(MetadataType.VERSION_ID), release);
        }
    }

    private void collectFilesOfEachRelease() {

        for (Release release : this.releases) {

            Commit releaseCommit = release.getReleaseCommit();

            Map<String, ReleaseFile> releaseFiles = this.versionControlSystem.getFiles(releaseCommit.hash);

            release.setReleaseFiles(releaseFiles);
        }
    }

    private void collectFilesMetadataOfEachRelease() {

        int numberOfReleaseAnalyzed = 0;

        for (Release release : this.releases) {

            ConcurrentLinkedQueue<ReleaseFile> waitFreeQueue = new ConcurrentLinkedQueue<>(release.getReleaseFiles().values());

            List<Thread> threadList = new ArrayList<>();

            for (int threadID = 0; threadID < 4; threadID++) {

                Runnable runnable = new ProjectDatasetBuilderThread(waitFreeQueue, new Git(rootDirectory, workingDirectory, null), release.getReleaseCommit());
                Thread thread = new Thread(runnable);

                thread.start();
                threadList.add(thread);
            }

            try {

                for (Thread currentThread : threadList)
                    currentThread.join();

            } catch (Exception e) {

                Logger.getLogger(Project.class.getName()).severe(e.getMessage());
                System.exit(e.hashCode());
            }

            if (numberOfReleaseAnalyzed == 1)
                break;
            else
                numberOfReleaseAnalyzed++;
        }
    }

    public void searchForDefectiveFile() {

        searchForDefectiveFileThroughAffectedVersions();
        searchForDefectiveFileThroughProportion();
    }


    private void searchForDefectiveFileThroughAffectedVersions() {

        List<Issue> issues = this.issueTrackingSystem.getIssuesWithAffectedVersions();
        ArithmeticMean arithmeticMean = new ArithmeticMean();

        for (Issue issue : issues) {

            SortedSet<Release> affectedVersions = new TreeSet<>(new Comparator<Release>() {
                @Override
                public int compare(Release o1, Release o2) {
                    return o1.getReleaseCommit().date.compareTo(o2.getReleaseCommit().date);
                }
            });

            for (int affectedVersionsID : issue.affectedVersionsIDs)
                affectedVersions.add(this.releasesByVersionID.get(affectedVersionsID));


            int fixedVersionIndex = getFixedVersionIndex(issue);
            int openingVersionIndex = getOpeningVersionIndex(issue);
            int introductionVersionIndex = this.releases.indexOf(affectedVersions.first());

            setDefectiveFiles(affectedVersions, issue);

            double proportion = (double) (fixedVersionIndex - introductionVersionIndex) / (fixedVersionIndex - openingVersionIndex);
            arithmeticMean.addValue(proportion);
        }

        this.proportion = arithmeticMean.getMean();
    }

    private void searchForDefectiveFileThroughProportion() {

        List<Issue> issues = this.issueTrackingSystem.getIssuesWithoutAffectedVersions();

        for (Issue issue : issues) {

            int fixedVersionIndex = getFixedVersionIndex(issue);
            int openingVersionIndex = getOpeningVersionIndex(issue);
            int introductionVersionIndex = (int) Math.round((fixedVersionIndex -  ((fixedVersionIndex - openingVersionIndex) * this.proportion)));

            List<Release> affectedVersions = this.releases.subList(introductionVersionIndex, fixedVersionIndex);
            setDefectiveFiles(affectedVersions, issue);
        }
    }

    private int getFixedVersionIndex(Issue issue) {

        SortedSet<Release> fixedReleases = new TreeSet<>(new Comparator<Release>() {
            @Override
            public int compare(Release o1, Release o2) {
                return o1.getReleaseCommit().date.compareTo(o2.getReleaseCommit().date);
            }
        });

        for (int versionID : issue.fixedVersionsIDs)
            fixedReleases.add(this.releasesByVersionID.get(versionID));

        return this.releases.indexOf(fixedReleases.first());
    }

    private int getOpeningVersionIndex(Issue issue) {

        LocalDateTime issueReleaseDate = issue.creationDate;

        for (int index = 0; index < this.releases.size(); index++) {

            LocalDateTime releaseDateOfCurrentRelease = this.releases.get(index).getReleaseCommit().date;
            if (releaseDateOfCurrentRelease.compareTo(issueReleaseDate) >= 0)
                return index;
        }

        return 0;
    }

    private void setDefectiveFiles(Iterable<Release> affectedVersion, Issue issue) {

        Commit fixCommit = this.versionControlSystem.getCommitByLogMessagePattern(issue.key);

        if (fixCommit != null) {

            List<String> defectiveFilenameList = this.versionControlSystem.getFilesChangedByCommit(fixCommit.hash);

            for (Release release : affectedVersion)
                for (String defectiveFilename : defectiveFilenameList)
                    if (Utils.isJavaFile(defectiveFilename)) {

                        ReleaseFile defectiveFile = release.getReleaseFiles().get(defectiveFilename);
                        if (defectiveFile != null)
                            defectiveFile.setMetadataValue(MetadataType.IS_BUGGY, true);
                    }
        }
    }

    private double calculateProportion(Release introductionVersion, Release openingVersion, Release fixedVersion) {

        double iv = this.releases.indexOf(introductionVersion);
        double ov = this.releases.indexOf(openingVersion);
        double fv = this.releases.indexOf(fixedVersion);

        return (fv - iv) / (fv - ov);
    }
}