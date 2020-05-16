package project.entities.metadata;

import java.util.ArrayList;
import java.util.List;

public enum ReleaseMetadata {

    RELEASE_VERSION_ORDER_ID,
    ID,
    NAME,
    RELEASE_DATE;

    public static List<String> exportAsStringList() {

        List<String> output = new ArrayList<>();

        for (ReleaseMetadata metadata : ReleaseMetadata.values())
            output.add(metadata.toString());

        return output;
    }
}