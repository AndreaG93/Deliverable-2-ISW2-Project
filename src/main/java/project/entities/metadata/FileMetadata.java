package project.entities.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum FileMetadata {

    RELEASE_VERSION_ORDER_ID,

    NAME,

    LOC,
    LOCTouched,

    NUMBER_OF_REVISIONS,
    NUMBER_OF_AUTHORS,

    LOC_ADDED,
    MAX_LOC_ADDED,
    AVERAGE_LOC_ADDED,

    CHURN,
    MAX_CHURN,
    AVERAGE_CHURN,

    CHANGE_SET_SIZE,
    MAX_CHANGE_SET_SIZE,
    AVERAGE_CHANGE_SET_SIZE,

    AGE_IN_WEEKS,
    WEIGHTED_AGE_IN_WEEKS,

    IS_BUGGY;

    public static List<String> exportAsStringList() {

        List<String> output = new ArrayList<>();

        for (FileMetadata fileMetric : FileMetadata.values())
            output.add(fileMetric.toString());

        return output;
    }
}