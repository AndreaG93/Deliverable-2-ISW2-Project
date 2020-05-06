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
        //this.commits = versionControlSystem.getAllCommits();

        for (int i = 0; i < releases.length / 2; i++) {

            Release currentRelease = this.releases[i];

            Commit lastCommit = this.commits.get(currentRelease.releaseDate);

            currentRelease.files = this.versionControlSystem.getAllFilesFromCommit(lastCommit.guid);

            for (ProjectFile projectFile : currentRelease.files) {

                projectFile.numberOfAuthors = this.versionControlSystem.getNumberOfAuthorsOfFile(projectFile.name, currentRelease.releaseDate, currentRelease.endOfLifeDate);
            }
            break;
        }
    }
}