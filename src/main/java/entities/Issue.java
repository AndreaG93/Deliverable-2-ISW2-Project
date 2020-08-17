package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Issue {

    public final String key;

    public final int[] affectedVersionsIDs;
    public final int[] fixedVersionsIDs;

    public final LocalDateTime creationDate;

    public Issue(String key, int[] affectedVersionsIDs, int[] fixedVersionsIDs, LocalDateTime creationDate) {

        this.key = key;

        this.affectedVersionsIDs = affectedVersionsIDs;
        this.fixedVersionsIDs = fixedVersionsIDs;

        this.creationDate = creationDate;
    }

    public List<Integer> getUtilizableFixedVersionsIDs() {

        List<Integer> output = new ArrayList<>();

        for (int av : this.affectedVersionsIDs) {

            boolean bugNotFixed = true;

            for (int fv : this.fixedVersionsIDs)
                if (av == fv) {
                    bugNotFixed = false;
                    break;
                }

            if (bugNotFixed)
                output.add(av);
        }

        return output;
    }
}
