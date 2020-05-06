package core;

import core.utils.JSONManagement;
import org.json.JSONArray;
import org.json.JSONObject;
import project.Release;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.TreeMap;
import java.util.logging.Logger;

public class ProjectReleases {

    public static final Logger logger = Logger.getLogger(ProjectReleases.class.getName());
    private static final String[] releaseProperties = {"releaseDate", "name", "id"};

    public static Release[] downloadMetadata(String projectName) {

        AbstractMap<LocalDateTime, Release> output = new TreeMap<>();

        String url = "https://issues.apache.org/jira/rest/api/2/project/" + projectName;

        JSONObject json = JSONManagement.readJsonFromUrl(url);
        JSONArray projectVersions = json.getJSONArray("versions");

        for (int i = 0; i < projectVersions.length(); i++) {

            boolean isCurrentReleaseDiscarded = false;
            Release currentRelease = new Release();

            for (String releaseProperty : releaseProperties) {

                if (projectVersions.getJSONObject(i).has(releaseProperty)) {

                    String propertyValue = projectVersions.getJSONObject(i).get(releaseProperty).toString();

                    try {

                        Field field = Release.class.getField(releaseProperty);

                        if (field.getType().getName().equals("java.time.LocalDateTime"))
                            field.set(currentRelease, LocalDate.parse(propertyValue).atStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59));
                        else if (field.getType().getName().equals("int"))
                            field.set(currentRelease, Integer.parseInt(propertyValue));
                        else
                            field.set(currentRelease, propertyValue);

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
                output.put(currentRelease.releaseDate, currentRelease);
            }
        }

        return output.values().toArray(new Release[0]);
    }
}
