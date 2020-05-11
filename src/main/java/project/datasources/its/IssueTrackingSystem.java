package project.datasources.its;

import project.release.Release;

public interface IssueTrackingSystem {

    Release[] getProjectReleases(String projectName);
}
