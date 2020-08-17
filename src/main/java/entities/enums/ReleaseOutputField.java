package entities.enums;

import java.util.ArrayList;
import java.util.List;

public enum ReleaseOutputField {

    VERSION_ID,
    VERSION_INDEX,
    RELEASE_DATE,
    NAME;

    public static List<String> convertToStringList() {

        List<String> output = new ArrayList<>();

        for (ReleaseOutputField x : ReleaseOutputField.values())
            output.add(x.name());

        return output;
    }
}
