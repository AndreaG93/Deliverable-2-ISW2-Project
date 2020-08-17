package datasetbuilder;

import datasetbuilder.datasources.its.IssueRegistry;
import datasetbuilder.datasources.its.IssueTrackingSystem;
import datasetbuilder.datasources.its.jira.Jira;
import datasetbuilder.datasources.vcs.VersionControlSystem;
import datasetbuilder.datasources.vcs.git.Git;
import entities.Commit;
import entities.File;
import entities.FileCSV;
import entities.Issue;
import entities.enums.DatasetOutputField;
import entities.enums.ReleaseOutputField;
import entities.project.Project;
import entities.release.Release;
import entities.release.ReleaseComparator;
import entities.ArithmeticMean;
import utilis.Utils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;


public class ProjectDatasetBuilder {

    public final Project project;

    private final String rootDirectory;
    private final String workingDirectory;
    private final VersionControlSystem versionControlSystem;
    private final IssueTrackingSystem issueTrackingSystem;

    private Map<LocalDateTime, Release> releasesByReleaseDate;
    private Map<Integer, Release> releasesByVersionID;
    private Map<Integer, Release> releasesByIndex;

    private double defectiveFileProportion;

    public ProjectDatasetBuilder(Project project) {

        this.project = project;

        this.rootDirectory = "C://";
        this.workingDirectory = this.rootDirectory + this.project.name;

        this.versionControlSystem = new Git(this.rootDirectory, this.workingDirectory, this.project.gitRepositoryURL);
        this.issueTrackingSystem = new Jira();
    }

    public void buildProjectDataset() {

        collectReleases();
        collectFilesBelongingToEachRelease();

        IssueRegistry issueRegistry = this.issueTrackingSystem.getIssuesRegistry(this.project.name, this.releasesByVersionID);

        calculateDefectiveFileProportion(issueRegistry.issuesWithAffectedVersions);
        searchForDefectiveFile(issueRegistry.issues);

        collectFileMetadataOfEachRelease();
    }

    public void exportProjectDatasetAsCSV() {

        FileCSV datasetCSV = new FileCSV(this.project.datasetFilename,  DatasetOutputField.convertToStringList());
        FileCSV releasesCSV = new FileCSV(this.project.releasesFilename, ReleaseOutputField.convertToStringList());

        for (Release release : this.releasesByReleaseDate.values()) {

            releasesCSV.write(release.getMetadataAsString(ReleaseOutputField.values()));

            for (File file : release.getFiles())
                datasetCSV.write(file.getMetadataAsString(DatasetOutputField.values()));
        }

        datasetCSV.close();
        releasesCSV.close();
    }

    private List<Release> retrieveReleasesDiscardingHalfOfThem() {

        List<Release> output = this.issueTrackingSystem.getReleases(this.project.name);

        int numberOfReleaseToAnalyze = (int) Math.round(output.size() * 0.5);

        while (output.size() > numberOfReleaseToAnalyze)
            output.remove(output.size() - 1);

        return output;
    }

    private void collectReleases() {

        this.releasesByVersionID = new TreeMap<>();
        this.releasesByReleaseDate = new TreeMap<>();
        this.releasesByIndex = new TreeMap<>();

        for (Release release : retrieveReleasesDiscardingHalfOfThem()) {

            String releaseTag = (String) release.getMetadata(ReleaseOutputField.NAME);
            LocalDateTime releaseDate = (LocalDateTime) release.getMetadata(ReleaseOutputField.RELEASE_DATE);

            Commit releaseCommit = this.versionControlSystem.getCommitByTag(releaseTag);
            if (releaseCommit == null)
                releaseCommit = this.versionControlSystem.getCommitByDate(releaseDate);

            release.setCommit(releaseCommit);

            this.releasesByVersionID.put(release.getReleaseVersionID(), release);
            this.releasesByReleaseDate.put(release.getReleaseDate(), release);
        }

        int index = 0;
        for (Release release : this.releasesByReleaseDate.values()) {

            this.releasesByIndex.put(index, release);
            release.setVersionIndex(index);

            index++;
        }
    }

    private void collectFilesBelongingToEachRelease() {

        for (Release release : this.releasesByReleaseDate.values()) {

            Commit releaseCommit = release.getCommit();

            Map<String, File> files = this.versionControlSystem.getFiles(releaseCommit.hash);

            release.setFileRegistry(files);
        }
    }

    private void calculateDefectiveFileProportion(List<Issue> issues) {

        ArithmeticMean proportion = new ArithmeticMean();

        for (Issue issue : issues) {

            SortedSet<Release> affectedVersions = new TreeSet<>(new ReleaseComparator());

            for (int affectedVersionsID : issue.affectedVersionsIDs)
                affectedVersions.add(this.releasesByVersionID.get(affectedVersionsID));

            int fixedVersionIndex = getFixedVersionIndex(issue);
            int openingVersionIndex = getOpeningVersionIndex(issue);
            int introductionVersionIndex = affectedVersions.first().getVersionIndex();

            if (fixedVersionIndex > openingVersionIndex) {

                double proportionPartialValue = (double) (fixedVersionIndex - introductionVersionIndex) / (fixedVersionIndex - openingVersionIndex);
                proportion.addValue(proportionPartialValue);
            }
        }

        this.defectiveFileProportion = proportion.getMean();
    }

    private void searchForDefectiveFile(List<Issue> issues) {

        for (Issue issue : issues) {

            List<Release> affectedVersions = new ArrayList<>();

            if (issue.affectedVersionsIDs.length > 0) {

                for (int av : issue.getUtilizableFixedVersionsIDs())
                    affectedVersions.add(this.releasesByVersionID.get(av));

            } else {

                int fixedVersionIndex = getFixedVersionIndex(issue);
                int openingVersionIndex = getOpeningVersionIndex(issue);
                int introductionVersionIndex = (int) Math.round((fixedVersionIndex - ((fixedVersionIndex - openingVersionIndex) * this.defectiveFileProportion)));

                for (int x = introductionVersionIndex; x < fixedVersionIndex; x++)
                    affectedVersions.add(this.releasesByIndex.get(x));

            }

            setDefectiveFiles(affectedVersions, issue);
        }
    }

    private int getFixedVersionIndex(Issue issue) {

        SortedSet<Release> fixedReleases = new TreeSet<>(new ReleaseComparator());

        for (int versionID : issue.fixedVersionsIDs)
            fixedReleases.add(this.releasesByVersionID.get(versionID));

        return fixedReleases.first().getVersionIndex();
    }

    private int getOpeningVersionIndex(Issue issue) {

        int output = 0;
        LocalDateTime issueReleaseDate = issue.creationDate;

        for (Release release : this.releasesByReleaseDate.values()) {

            LocalDateTime releaseDate = release.getReleaseDate();

            if (releaseDate.compareTo(issueReleaseDate) < 0)
                output++;
            else
                break;
        }

        return output;
    }

    private void setDefectiveFiles(Iterable<Release> affectedVersion, Issue issue) {

        Commit fixCommit = this.versionControlSystem.getCommitByLogMessagePattern(issue.key);

        if (fixCommit != null) {

            List<String> defectiveFilenameList = this.versionControlSystem.getFilesChangedByCommit(fixCommit.hash);

            for (Release release : affectedVersion)
                for (String defectiveFilename : defectiveFilenameList)
                    if (Utils.isJavaFile(defectiveFilename))
                        release.setFileAsDefectiveIncrementingNumberOfFix(defectiveFilename);
        }
    }

    private void collectFileMetadataOfEachRelease() {

        for (Release release : this.releasesByReleaseDate.values()) {

            ConcurrentLinkedQueue<File> waitFreeQueue = new ConcurrentLinkedQueue<>(release.getFiles());

            List<Thread> threadList = new ArrayList<>();

            for (int threadID = 0; threadID < 4; threadID++) {

                Runnable runnable = new ProjectDatasetBuilderThread(waitFreeQueue, new Git(rootDirectory, workingDirectory, null), release.getCommit());
                Thread thread = new Thread(runnable);

                thread.start();
                threadList.add(thread);
            }

            try {

                for (Thread currentThread : threadList)
                    currentThread.join();

            } catch (Exception e) {

                Logger.getLogger(ProjectDatasetBuilder.class.getName()).severe(e.getMessage());
                System.exit(e.hashCode());
            }
        }
    }
}