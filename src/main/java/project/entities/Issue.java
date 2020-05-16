package project.entities;

import java.time.LocalDateTime;

public class Issue {

    public final int id;
    public final String key;

    public final String[] affectedVersionsIDs;
    public final String[] fixedVersionsIDs;

    public final LocalDateTime creationDate;
    public final LocalDateTime resolutionDate;

    public Issue(int id, String key, String[] affectedVersionsIDs, String[] fixedVersionsIDs, LocalDateTime creationDate, LocalDateTime resolutionDate) {
        this.id = id;
        this.key = key;
        this.affectedVersionsIDs = affectedVersionsIDs;
        this.fixedVersionsIDs = fixedVersionsIDs;
        this.creationDate = creationDate;
        this.resolutionDate = resolutionDate;
    }
}
