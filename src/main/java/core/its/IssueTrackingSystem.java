package core.its;

import core.vcs.Release;

public interface IssueTrackingSystem {

    Release[] getProjectReleases(String projectName);
}
