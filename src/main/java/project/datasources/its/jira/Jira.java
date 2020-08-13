package project.datasources.its.jira;

import org.json.JSONArray;
import org.json.JSONObject;
import project.datasources.its.IssueTrackingSystem;
import project.model.Issue;
import project.model.Release;
import project.model.metadata.MetadataType;
import utilis.common.Utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static utilis.common.JSONManagement.extractFieldFromJsonArray;
import static utilis.common.JSONManagement.readJsonFromUrl;

public class Jira implements IssueTrackingSystem {

    private static final String JIRA_URL = "https://issues.apache.org/jira/rest/api/2/project/";

    private List<Issue> issue;
    private List<Issue> issueWithAffectedVersions;
    private List<Release> releases;

    public Jira(String projectName) {

        collectReleases(projectName);
        collectIssues(projectName);
    }

    @Override
    public List<Issue> getIssues() {
        return this.issue;
    }

    @Override
    public List<Issue> getIssuesWithAffectedVersions() {
        return this.issueWithAffectedVersions;
    }

    @Override
    public List<Release> getReleases() {
        return this.releases;
    }

    private void collectReleases(String projectName) {

        this.releases = new ArrayList<>();

        String url = JIRA_URL + projectName.toUpperCase();

        JSONArray releasesAsJsonArray = readJsonFromUrl(url).getJSONArray("versions");

        for (int index = 0; index < releasesAsJsonArray.length(); index++) {

            JSONObject releaseAsJsonObject = releasesAsJsonArray.getJSONObject(index);
            Release release = createReleaseFromJSON(releaseAsJsonObject);

            if (release != null)
                this.releases.add(release);
        }
    }

    private void collectIssues(String projectName) {

        this.issue = new ArrayList<>();
        this.issueWithAffectedVersions = new ArrayList<>();

        int j;
        int i = 0;
        int total;

        do {

            j = i + 1000;
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + projectName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                    + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,fixVersions,versions,created&startAt="
                    + i + "&maxResults=" + j;


            JSONObject issuesAsJsonObject = readJsonFromUrl(url);
            JSONArray issuesAsJsonArray = issuesAsJsonObject.getJSONArray("issues");

            total = issuesAsJsonObject.getInt("total");

            for (; i < total && i < j; i++) {

                JSONObject issueAsJsonObject = issuesAsJsonArray.getJSONObject(i % 1000);

                Issue issue = createIssueFromJSON(issueAsJsonObject);

                if (issue != null) {

                    if (issue.affectedVersionsIDs.length > 0)
                        this.issueWithAffectedVersions.add(issue);

                    this.issue.add(issue);
                }
            }

        } while (i < total);
    }

    private Release createReleaseFromJSON(JSONObject input) {

        Release output = null;

        if (input.has("id") && input.has("name") && input.has("releaseDate")) {

            output = new Release();

            output.setMetadata(MetadataType.VERSION_ID, input.getInt("id"));
            output.setMetadata(MetadataType.NAME, input.getString("name"));
            output.setMetadata(MetadataType.RELEASE_DATE, LocalDate.parse(input.getString("releaseDate")).atStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59));
        }

        return output;
    }

    private Issue createIssueFromJSON(JSONObject input) {

        Issue output = null;

        JSONObject jiraFields = input.getJSONObject("fields");
        JSONArray jiraFixedVersions = jiraFields.getJSONArray("fixVersions");
        JSONArray jiraAffectedVersions = jiraFields.getJSONArray("versions");

        String key = input.getString("key");
        String[] fixedVersionsIDs = extractFieldFromJsonArray(jiraFixedVersions, "id");
        String[] affectedVersionsIDs = extractFieldFromJsonArray(jiraAffectedVersions, "id");
        LocalDateTime creationDate = LocalDateTime.parse(jiraFields.getString("created").substring(0, 19));

        if (fixedVersionsIDs.length != 0)
            output = new Issue(key, Utils.stringArrayToIntArray(affectedVersionsIDs), Utils.stringArrayToIntArray(fixedVersionsIDs), creationDate);

        return output;
    }
}