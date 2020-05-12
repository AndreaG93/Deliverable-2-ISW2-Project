package project.datasources.its.jira;

import org.json.JSONArray;
import org.json.JSONObject;
import project.datasources.its.IssueTrackingSystem;
import project.metadata.ReleaseMetadata;
import project.release.Release;
import utilis.common.JSONManagement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class JIRA implements IssueTrackingSystem {

    private static final ReleaseMetadata[] jiraProperties = {ReleaseMetadata.id, ReleaseMetadata.releaseDate, ReleaseMetadata.name};
    private static final String jiraURL = "https://issues.apache.org/jira/rest/api/2/project/";

    @Override
    public Release[] getProjectReleases(String projectName) {

        Release[] output;

        AbstractMap<LocalDateTime, Release> treeMap = new TreeMap<>();

        String url = jiraURL + projectName;

        JSONObject json = JSONManagement.readJsonFromUrl(url);
        JSONArray projectVersions = json.getJSONArray("versions");

        for (int i = 0; i < projectVersions.length(); i++) {

            Map<ReleaseMetadata, Object> registry = getReleaseMetadata(projectVersions.getJSONObject(i));

            if (registry != null) {

                Release release = new Release(registry);
                treeMap.put((LocalDateTime) release.metadata.get(ReleaseMetadata.releaseDate), release);
            }
        }

        output = treeMap.values().toArray(new Release[0]);
        for (int i = 0; i < output.length; i++)
            output[i].metadata.put(ReleaseMetadata.releaseVersionOrderID, i);

        return output;
    }


    private Map<ReleaseMetadata, Object> getReleaseMetadata(JSONObject input) {

        Map<ReleaseMetadata, Object> output = new HashMap<>();

        for (ReleaseMetadata releaseProperty : jiraProperties) {

            if (input.has(releaseProperty.toString())) {

                Object data = input.get(releaseProperty.toString()).toString();

                switch (releaseProperty) {

                    case id:
                        data = Integer.parseInt((String) data);
                        break;

                    case releaseDate:
                        data = LocalDate.parse((String) data).atStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59);
                        break;
                }

                output.put(releaseProperty, data);

            } else
                return null;
        }

        return output;
    }
}