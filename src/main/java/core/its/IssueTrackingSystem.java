package core.its;

import project.entities.ProjectRelease;

public interface IssueTrackingSystem {

    ProjectRelease[] getProjectReleases(String projectName);
}
