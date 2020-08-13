package project.model;

import java.time.LocalDateTime;

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
}
