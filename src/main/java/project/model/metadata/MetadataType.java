package project.model.metadata;

import java.util.ArrayList;
import java.util.List;

public enum MetadataType {

    VERSION_ID,
    NAME,
    HASH,

    VERSION_INDEX,
    DATE,

    LOC,
    LOCTouched,

    NUMBER_OF_REVISIONS,
    NUMBER_OF_AUTHORS,
    NUMBER_OF_FIX,

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

    public static List<String> convertToStringList(MetadataType[] types) {

        List<String> output = new ArrayList<>();

        for (MetadataType x : types)
            output.add(x.name());

        return output;
    }
}
