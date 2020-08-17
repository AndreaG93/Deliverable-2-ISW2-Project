package datasetbuilder.datasources.its.jira;

import datasetbuilder.datasources.its.IssueRegistry;
import datasetbuilder.datasources.its.IssueTrackingSystem;
import entities.Issue;
import entities.enums.ReleaseOutputField;
import entities.release.Release;
import org.json.JSONArray;
import org.json.JSONObject;
import utilis.common.Utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static utilis.common.JSONManagement.extractFieldFromJsonArray;
import static utilis.common.JSONManagement.readJsonFromUrl;

public class Jira implements IssueTrackingSystem {

    private static final String JIRA_URL = "https://issues.apache.org/jira/rest/api/2/project/";

    public static boolean isIssueCorrectAccordingToGivenReleases(Map<Integer, Release> releasesByVersionID, Issue issue) {

        for (int x : issue.fixedVersionsIDs)
            if (releasesByVersionID.get(x) == null)
                return false;

        for (int x : issue.affectedVersionsIDs)
            if (releasesByVersionID.get(x) == null)
                return false;

        return true;
    }

    @Override
    public List<Release> getReleases(String projectName) {

        List<Release> output = new ArrayList<>();

        String url = JIRA_URL + projectName.toUpperCase();

        JSONArray releasesAsJsonArray = readJsonFromUrl(url).getJSONArray("versions");

        for (int index = 0; index < releasesAsJsonArray.length(); index++) {

            JSONObject releaseAsJsonObject = releasesAsJsonArray.getJSONObject(index);
            Release release = createReleaseFromJSON(releaseAsJsonObject);

            if (release != null)
                output.add(release);
        }

        return output;
    }

    @Override
    public IssueRegistry getIssuesRegistry(String projectName, Map<Integer, Release> releasesByVersionID) {

        List<Issue> issues = new ArrayList<>();
        List<Issue> issueWithAffectedVersions = new ArrayList<>();

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

                Issue newIssue = createIssueFromJSON(issueAsJsonObject);

                if (newIssue != null && isIssueCorrectAccordingToGivenReleases(releasesByVersionID, newIssue)) {

                    if (newIssue.affectedVersionsIDs.length > 0)
                        issueWithAffectedVersions.add(newIssue);

                    issues.add(newIssue);
                }
            }

        } while (i < total);

        return new IssueRegistry(issues, issueWithAffectedVersions);
    }

    private Release createReleaseFromJSON(JSONObject input) {

        Release output = null;

        if (input.has("id") && input.has("name") && input.has("releaseDate")) {

            output = new Release();

            output.setMetadata(ReleaseOutputField.VERSION_ID, input.getInt("id"));
            output.setMetadata(ReleaseOutputField.NAME, input.getString("name"));
            output.setMetadata(ReleaseOutputField.RELEASE_DATE, LocalDate.parse(input.getString("releaseDate")).atStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59));
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