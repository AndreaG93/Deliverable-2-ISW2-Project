package entities.enums;

import java.util.ArrayList;
import java.util.List;

public enum DatasetOutputField {

    VERSION_INDEX,
    NAME,

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

    public static List<String> convertToStringList() {

        List<String> output = new ArrayList<>();

        for (DatasetOutputField x : DatasetOutputField.values())
            output.add(x.name());

        return output;
    }
}
