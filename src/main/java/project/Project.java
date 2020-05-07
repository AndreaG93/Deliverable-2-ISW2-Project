package project;

import core.ProjectReleases;
import core.vcs.FileMetric;
import core.vcs.Git;
import core.vcs.VersionControlSystem;

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;


public class Project {

    public final String name;
    private final VersionControlSystem versionControlSystem;

    public Release[] releases;

    public AbstractMap<LocalDate, Commit> commits;

    public Project(String name, String repositoryURL) {
        this.name = name;
        this.versionControlSystem = new Git(repositoryURL, "C://" + name);
    }

    public void buildDataset() {

        versionControlSystem.cloneRepositoryLocally();

        this.releases = ProjectReleases.downloadMetadata(this.name);

        for (Release currentRelease : this.releases) {

            Commit releaseCommit = this.versionControlSystem.getReleaseCommit(currentRelease.releaseDate);

            currentRelease.files = this.versionControlSystem.getFiles(releaseCommit.hash, releaseCommit.hash);

            List<Thread> threadList = new ArrayList<>();

            //this.versionControlSystem.changeLocalRepositoryStateToCommit(releaseCommit.hash);

            threadList.add(new Thread(() -> {
                for (ProjectFile projectFile : currentRelease.files)
                    projectFile.numberOfAuthors = versionControlSystem.getNumberOfAuthorsOfFile(projectFile.name, releaseCommit.hash);
            }));

            threadList.add(new Thread(() -> {
                for (ProjectFile projectFile : currentRelease.files)
                    projectFile.ageInWeeks = versionControlSystem.getFileWeekAge(projectFile.name, releaseCommit.date, releaseCommit.hash);
            }));

            threadList.add(new Thread(() -> {

                for (ProjectFile projectFile : currentRelease.files) {

                    FileMetric fileMetrics = versionControlSystem.getFileLOCTouched(projectFile.name, releaseCommit.hash);

                    projectFile.LOCTouched = fileMetrics.LOCTouched;
                    projectFile.churn = fileMetrics.churn;
                    projectFile.numberOfRevisions = fileMetrics.numberOfRevisions;
                }
            }));

            threadList.add(new Thread(() -> {
                for (ProjectFile projectFile : currentRelease.files)
                    projectFile.LOC = 0;
            }));

            for (Thread currentThread : threadList)
                currentThread.start();

            try {

                for (Thread currentThread : threadList)
                    currentThread.join();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            break;
        }
    }
}