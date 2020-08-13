package project.datasources.its;

import project.model.Issue;
import project.model.Release;
import java.util.List;

public interface IssueTrackingSystem {

    List<Release> getReleases();

    List<Issue> getIssues();

    List<Issue> getIssuesWithAffectedVersions();
}