package project;

import core.vcs.FileChangeSetSizeMetrics;
import core.vcs.FileLOCMetrics;
import core.vcs.VersionControlSystem;
import core.vcs.git.Git;
import project.entities.Commit;
import project.entities.ProjectFile;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class ProjectDatasetBuilderThread implements Runnable {

    private final ConcurrentLinkedQueue<ProjectFile> projectFileWaitFreeList;
    private final VersionControlSystem versionControlSystem;
    private final Commit releaseCommit;
    private final int id;

    public ProjectDatasetBuilderThread(ConcurrentLinkedQueue<ProjectFile> projectFileWaitFreeList, Commit releaseCommit, String repositoryLocalDirectory, int id) {

        this.projectFileWaitFreeList = projectFileWaitFreeList;
        this.versionControlSystem = new Git(repositoryLocalDirectory);
        this.releaseCommit = releaseCommit;
        this.id = id;
    }

    @Override
    public void run() {

        int currentFileIndex = 0;

        int percentage25 = (int) (this.projectFileWaitFreeList.size() * 0.25);
        int percentage50 = (int) (this.projectFileWaitFreeList.size() * 0.50);
        int percentage75 = (int) (this.projectFileWaitFreeList.size() * 0.75);
        int percentage100 = this.projectFileWaitFreeList.size();

        Logger logger = Logger.getLogger(ProjectDatasetBuilderThread.class.getName());

        for (ProjectFile currentProjectFile = this.projectFileWaitFreeList.poll(); currentProjectFile != null; currentProjectFile = this.projectFileWaitFreeList.poll()) {

            List<String> fileRevisionsList = this.versionControlSystem.getFileRevisions(currentProjectFile.name, this.releaseCommit.hash);

            FileLOCMetrics fileMetricSet1 = this.versionControlSystem.getFileMetrics(currentProjectFile.name, fileRevisionsList);
            FileChangeSetSizeMetrics fileMetricSet2 = this.versionControlSystem.getChangeSetSizeMetric(fileRevisionsList);

            currentProjectFile.numberOfRevisions = fileRevisionsList.size();

            currentProjectFile.LOC = 0;
            currentProjectFile.LOCTouched = fileMetricSet1.LOCTouched;

            currentProjectFile.LOCAdded = fileMetricSet1.LOCAdded;
            currentProjectFile.maxLOCAdded = fileMetricSet1.maxLOCAdded;
            currentProjectFile.averageLOCAdded = fileMetricSet1.averageLOCAdded;

            currentProjectFile.churn = fileMetricSet1.churn;
            currentProjectFile.maxChurn = fileMetricSet1.maxChurn;
            currentProjectFile.averageChurn = fileMetricSet1.averageChurn;

            if (fileRevisionsList.contains(this.releaseCommit.hash))
                currentProjectFile.changeSetSize = this.versionControlSystem.getChangeSetSize(this.releaseCommit.hash);
            else
                currentProjectFile.changeSetSize = 0;

            currentProjectFile.maxChangeSetSize = fileMetricSet2.maxChangeSetSize;
            currentProjectFile.averageChangeSetSize = fileMetricSet2.averageChangeSetSize;

            currentProjectFile.numberOfAuthors = versionControlSystem.getNumberOfAuthorsOfFile(currentProjectFile.name, releaseCommit.hash);

            currentProjectFile.ageInWeeks = versionControlSystem.getFileAgeInWeeks(currentProjectFile.name, releaseCommit.date, releaseCommit.hash);
            currentProjectFile.weightedAgeInWeeks = currentProjectFile.ageInWeeks / currentProjectFile.LOCTouched;

            currentProjectFile.LOC = this.versionControlSystem.getFileLOC(currentProjectFile.hash);

            currentFileIndex++;

            if (currentFileIndex == percentage25)
                logger.info("Thread n: " + id + "--> 25% complete...");
            else if (currentFileIndex == percentage50)
                logger.info("Thread n: " + id + "--> 50% complete...");
            else if (currentFileIndex == percentage75)
                logger.info("Thread n: " + id + "--> 75% complete...");
            else if (currentFileIndex == percentage100)
                logger.info("Thread n: " + id + "--> 100% complete...");
        }
    }
}
