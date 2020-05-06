package project;

import core.ProjectReleases;
import core.vcs.Git;
import core.vcs.VersionControlSystem;

import java.time.LocalDate;
import java.util.AbstractMap;


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

            this.versionControlSystem.changeLocalRepositoryStateToCommit(releaseCommit.hash);

            currentRelease.files = this.versionControlSystem.getFiles(releaseCommit.hash);

            for (ProjectFile projectFile : currentRelease.files) {

                projectFile.numberOfAuthors = this.versionControlSystem.getNumberOfAuthorsOfFile(projectFile.name);
                projectFile.weekAge = this.versionControlSystem.getFileWeekAge(projectFile.name, releaseCommit.date);
                projectFile.LOC = this.versionControlSystem.getFileLOC(projectFile.name);
            }

            break;
        }
    }
}