package project;

import core.vcs.FileChangeSetSizeMetric;
import core.vcs.FileMetric;
import core.vcs.VersionControlSystem;
import core.vcs.git.Git;
import project.entities.Commit;
import project.entities.ProjectFile;

import java.util.List;
import java.util.logging.Logger;

public class ProjectDatasetBuilderThread implements Runnable {

    private final List<ProjectFile> projectFileList;
    private final VersionControlSystem versionControlSystem;
    private final Commit releaseCommit;
    private final int id;

    public ProjectDatasetBuilderThread(List<ProjectFile> projectFileList, Commit releaseCommit, String repositoryLocalDirectory, int id) {

        this.projectFileList = projectFileList;
        this.versionControlSystem = new Git(repositoryLocalDirectory);
        this.releaseCommit = releaseCommit;
        this.id = id;
    }

    @Override
    public void run() {

        int currentFileIndex = 0;

        int percentage25 = (int) (this.projectFileList.size() * 0.25);
        int percentage50 = (int) (this.projectFileList.size() * 0.50);
        int percentage75 = (int) (this.projectFileList.size() * 0.75);
        int percentage100 = this.projectFileList.size();

        Logger logger = Logger.getLogger(ProjectDatasetBuilderThread.class.getName());

        for (ProjectFile projectFile : this.projectFileList) {

            List<String> fileRevisionsList = this.versionControlSystem.getFileRevisions(projectFile.name, this.releaseCommit.hash);

            FileMetric fileMetricSet1 = this.versionControlSystem.getFileMetrics(projectFile.name, fileRevisionsList);
            FileChangeSetSizeMetric fileMetricSet2 = this.versionControlSystem.getChangeSetSizeMetric(fileRevisionsList);

            projectFile.numberOfRevisions = fileRevisionsList.size();

            projectFile.LOC = 0;
            projectFile.LOCTouched = fileMetricSet1.LOCTouched;

            projectFile.LOCAdded = fileMetricSet1.LOCAdded;
            projectFile.maxLOCAdded = fileMetricSet1.maxLOCAdded;
            projectFile.averageLOCAdded = fileMetricSet1.averageLOCAdded;

            projectFile.churn = fileMetricSet1.churn;
            projectFile.maxChurn = fileMetricSet1.maxChurn;
            projectFile.averageChurn = fileMetricSet1.averageChurn;

            if (fileRevisionsList.contains(this.releaseCommit.hash))
                projectFile.changeSetSize = this.versionControlSystem.getChangeSetSize(this.releaseCommit.hash);
            else
                projectFile.changeSetSize = 0;

            projectFile.maxChangeSetSize = fileMetricSet2.maxChangeSetSize;
            projectFile.averageChangeSetSize = fileMetricSet2.averageChangeSetSize;

            projectFile.numberOfAuthors = versionControlSystem.getNumberOfAuthorsOfFile(projectFile.name, releaseCommit.hash);
            projectFile.ageInWeeks = versionControlSystem.getFileAgeInWeeks(projectFile.name, releaseCommit.date, releaseCommit.hash);

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
