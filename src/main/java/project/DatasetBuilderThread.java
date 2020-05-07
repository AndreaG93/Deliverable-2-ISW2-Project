package project;

import core.vcs.FileMetric;
import core.vcs.VersionControlSystem;

import java.util.List;

public class DatasetBuilderThread implements Runnable {

    private final List<ProjectFile> projectFileList;
    private final VersionControlSystem versionControlSystem;
    private final Commit releaseCommit;

    public DatasetBuilderThread(List<ProjectFile> projectFileList, VersionControlSystem versionControlSystem, Commit releaseCommit) {

        this.projectFileList = projectFileList;
        this.versionControlSystem = versionControlSystem;
        this.releaseCommit = releaseCommit;
    }

    @Override
    public void run() {

        for (ProjectFile projectFile : this.projectFileList) {

            FileMetric fileMetrics = versionControlSystem.getFileLOCTouched(projectFile.name, releaseCommit.hash);

            projectFile.LOC = 0;
            projectFile.LOCTouched = fileMetrics.LOCTouched;
            projectFile.numberOfRevisions = fileMetrics.numberOfRevisions;
            projectFile.churn = fileMetrics.churn;
            projectFile.numberOfAuthors = versionControlSystem.getNumberOfAuthorsOfFile(projectFile.name, releaseCommit.hash);
            projectFile.ageInWeeks = versionControlSystem.getFileWeekAge(projectFile.name, releaseCommit.date, releaseCommit.hash);
        }
    }
}
