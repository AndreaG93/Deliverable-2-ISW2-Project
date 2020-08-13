package project.datasources.its;

import project.model.Issue;

import java.util.List;

public class IssueRegistry {

    public final List<Issue> issues;
    public final List<Issue> issuesWithAffectedVersions;

    public IssueRegistry(List<Issue> issues, List<Issue> issuesWithAffectedVersions) {
        this.issues = issues;
        this.issuesWithAffectedVersions = issuesWithAffectedVersions;
    }
}