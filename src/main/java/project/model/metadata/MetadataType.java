package project.model.metadata;

import java.util.ArrayList;
import java.util.List;

public enum MetadataType {

    VERSION_ID,
    VERSION_INDEX,
    RELEASE_DATE,
    NAME,
    HASH,

    LOC,
    LOC_TOUCHED,
    LOC_ADDED,
    MAX_LOC_ADDED,
    AVERAGE_LOC_ADDED,


    NUMBER_OF_REVISIONS,
    NUMBER_OF_FIX,
    NUMBER_OF_AUTHORS,

    CHURN,
    MAX_CHURN,
    AVERAGE_CHURN,

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
