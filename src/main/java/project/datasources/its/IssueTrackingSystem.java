package project.datasources.its;

import project.entities.Issue;
import project.entities.Release;

import java.util.List;
import java.util.Map;

public interface IssueTrackingSystem {

    Map<Integer, Release> getReleases(String projectName);

    List<Issue> getIssues(String projectName);
}
