package project.datasources.its;

import project.model.Release;
import java.util.List;
import java.util.Map;

public interface IssueTrackingSystem {

    List<Release> getReleases(String projectName);

    IssueRegistry getIssuesRegistry(String projectName, Map<Integer, Release> releasesByVersionID);
}