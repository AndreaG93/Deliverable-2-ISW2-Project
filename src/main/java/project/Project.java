package project;

import core.ProjectReleases;
import core.vcs.Git;
import core.vcs.VersionControlSystem;

import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.Map;


public class Project {

    public final String name;
    private final VersionControlSystem versionControlSystem;

    public AbstractMap<LocalDateTime, Release> releases;
    public AbstractMap<LocalDateTime, Commit> commits;

    public Project(String name, String repositoryURL) {
        this.name = name;
        this.versionControlSystem = new Git(repositoryURL, "C://" + name);
    }

    public void buildDataset() {

        versionControlSystem.cloneRepositoryLocally();

        this.releases = ProjectReleases.downloadMetadata(this.name);
        this.commits = versionControlSystem.getAllCommits();

        for (Map.Entry<LocalDateTime, Release> releaseEntry : this.releases.entrySet()) {

            Release currentRelease = releaseEntry.getValue();
            Commit lastCommit = this.commits.get(currentRelease.releaseDate);

            currentRelease.files = this.versionControlSystem.getAllFilesFromCommit(lastCommit.guid);
        }
    }
}