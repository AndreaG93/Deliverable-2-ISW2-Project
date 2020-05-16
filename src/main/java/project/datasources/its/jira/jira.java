package project.datasources.its.jira;

import org.json.JSONArray;
import org.json.JSONObject;
import project.datasources.its.IssueTrackingSystem;
import project.entities.Issue;
import project.entities.metadata.ReleaseMetadata;
import project.entities.Release;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static utilis.common.JSONManagement.extractFieldFromJsonArray;
import static utilis.common.JSONManagement.readJsonFromUrl;

public class jira implements IssueTrackingSystem {

    private static final String jiraURL = "https://issues.apache.org/jira/rest/api/2/project/";

    public Map<Integer, Release> getReleases(String projectName) {

        Map<Integer, Release> output = new TreeMap<>();

        String url = jiraURL + projectName.toUpperCase();

        JSONArray releasesAsJsonArray = readJsonFromUrl(url).getJSONArray("versions");

        for (int index = 0; index < releasesAsJsonArray.length(); index++) {

            JSONObject releaseAsJsonObject = releasesAsJsonArray.getJSONObject(index);

            Map<ReleaseMetadata, Object> releaseMetadata = getReleaseMetadataFrom(releaseAsJsonObject);

            if (releaseMetadata != null) {

                Release release = new Release(releaseMetadata);
                output.put((Integer) release.metadata.get(ReleaseMetadata.ID), release);
            }
        }

        return output;
    }

    public List<Issue> getIssues(String projectName) {

        List<Issue> output = new ArrayList<>();

        int j, i = 0, total;

        do {

            j = i + 1000;
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + projectName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                    + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,fixVersions,versions,created&startAt="
                    + i + "&maxResults=" + j;


            JSONObject issuesAsJsonObject = readJsonFromUrl(url);
            JSONArray issuesAsJsonArray = issuesAsJsonObject.getJSONArray("issues");

            total = issuesAsJsonObject.getInt("total");

            for (; i < total && i < j; i++) {

                JSONObject issueAsJsonObject = issuesAsJsonArray.getJSONObject(i % 1000);

                Issue issue = getIssueFrom(issueAsJsonObject);
                output.add(issue);
            }

        } while (i < total);

        return output;
    }

    private Map<ReleaseMetadata, Object> getReleaseMetadataFrom(JSONObject input) {

        Map<ReleaseMetadata, Object> output = new HashMap<>();

        if (input.has("id") && input.has("name") & input.has("releaseDate")) {

            output.put(ReleaseMetadata.ID, input.getInt("id"));
            output.put(ReleaseMetadata.NAME, input.getString("name"));
            output.put(ReleaseMetadata.RELEASE_DATE, LocalDate.parse(input.getString("releaseDate")).atStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59));

        } else
            return null;

        return output;
    }

    private Issue getIssueFrom(JSONObject input) {

        JSONObject jiraFields = input.getJSONObject("fields");
        JSONArray jiraFixedVersions = jiraFields.getJSONArray("fixVersions");
        JSONArray jiraAffectedVersions = jiraFields.getJSONArray("versions");

        int id = input.getInt("id");
        String key = input.getString("key");
        String[] fixedVersionsIDs = extractFieldFromJsonArray(jiraFixedVersions, "id");
        String[] affectedVersionsIDs = extractFieldFromJsonArray(jiraAffectedVersions, "id");

        LocalDateTime creationDate = LocalDateTime.parse(jiraFields.getString("created").substring(0, 19));
        LocalDateTime resolutionDate = LocalDateTime.parse(jiraFields.getString("resolutiondate").substring(0, 19));

        return new Issue(id, key, affectedVersionsIDs, fixedVersionsIDs, creationDate, resolutionDate);
    }
}