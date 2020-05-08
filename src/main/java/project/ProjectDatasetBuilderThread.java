package project;

import core.vcs.FileChangeSetSizeMetric;
import core.vcs.VersionControlSystem;
import core.vcs.git.Git;
import project.entities.Commit;
import project.entities.ProjectFile;

import java.util.List;

public class ProjectDatasetBuilderThread implements Runnable {

    private final List<ProjectFile> projectFileList;
    private final VersionControlSystem versionControlSystem;
    private final Commit releaseCommit;

    public ProjectDatasetBuilderThread(List<ProjectFile> projectFileList, Commit releaseCommit, String repositoryLocalDirectory) {

        this.projectFileList = projectFileList;
        this.versionControlSystem = new Git(repositoryLocalDirectory);
        this.releaseCommit = releaseCommit;
    }

    @Override
    public void run() {

        for (ProjectFile projectFile : this.projectFileList) {

            FileChangeSetSizeMetric metricSet1 = this.versionControlSystem.getChangeSetSizeMetric(projectFile.name, releaseCommit.hash);

            //FileMetric fileMetrics = versionControlSystem.getFileMetrics(projectFile.name, releaseCommit.hash);

            projectFile.LOC = 0;
            /*
            projectFile.LOCTouched = fileMetrics.LOCTouched;
            projectFile.numberOfRevisions = fileMetrics.numberOfRevisions;
            projectFile.churn = fileMetrics.churn;
            projectFile.numberOfAuthors = versionControlSystem.getNumberOfAuthorsOfFile(projectFile.name, releaseCommit.hash);
            projectFile.ageInWeeks = versionControlSystem.getFileAgeInWeeks(projectFile.name, releaseCommit.date, releaseCommit.hash);
             */

            projectFile.changeSetSize = metricSet1.changeSetSize;
            projectFile.maxChangeSetSize = metricSet1.maxChangeSetSize;
            projectFile.averageChangeSetSize = metricSet1.averageChangeSetSize;
        }
    }
}
