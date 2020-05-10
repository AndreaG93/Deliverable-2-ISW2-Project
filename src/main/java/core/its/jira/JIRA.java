package core.its.jira;

import core.its.IssueTrackingSystem;
import core.vcs.Release;
import org.json.JSONArray;
import org.json.JSONObject;
import utilis.common.JSONManagement;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.TreeMap;
import java.util.logging.Logger;

public class JIRA implements IssueTrackingSystem {

    private static final String[] releaseProperties = {"releaseDate", "name", "id"};
    private static final String jiraURL = "https://issues.apache.org/jira/rest/api/2/project/";

    private final Logger logger;

    public JIRA() {

        this.logger = Logger.getLogger(JIRA.class.getName());
    }

    @Override
    public Release[] getProjectReleases(String projectName) {

        AbstractMap<LocalDateTime, Release> output = new TreeMap<>();

        String url = jiraURL + projectName;

        JSONObject json = JSONManagement.readJsonFromUrl(url);
        JSONArray projectVersions = json.getJSONArray("versions");

        for (int i = 0; i < projectVersions.length(); i++) {

            boolean isCurrentReleaseDiscarded = false;
            Release currentProjectRelease = new Release();

            for (String releaseProperty : releaseProperties) {

                if (projectVersions.getJSONObject(i).has(releaseProperty)) {

                    String propertyValue = projectVersions.getJSONObject(i).get(releaseProperty).toString();

                    try {

                        Field field = Release.class.getField(releaseProperty);

                        if (field.getType().getName().equals("java.time.LocalDateTime"))
                            field.set(currentProjectRelease, LocalDate.parse(propertyValue).atStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59));
                        else if (field.getType().getName().equals("int"))
                            field.set(currentProjectRelease, Integer.parseInt(propertyValue));
                        else
                            field.set(currentProjectRelease, propertyValue);

                    } catch (Exception e) {

                        logger.severe(e.getMessage());
                        System.exit(e.hashCode());
                    }

                } else {

                    isCurrentReleaseDiscarded = true;
                    break;
                }
            }

            if (!isCurrentReleaseDiscarded) {
                output.put(currentProjectRelease.releaseDate, currentProjectRelease);
            }
        }

        return output.values().toArray(new Release[0]);
    }
}
