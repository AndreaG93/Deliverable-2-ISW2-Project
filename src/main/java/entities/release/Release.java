package entities.release;

import entities.Commit;
import entities.File;
import entities.MetadataProvider;
import entities.enums.DatasetOutputField;
import entities.enums.ReleaseOutputField;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

public class Release extends MetadataProvider<ReleaseOutputField> {

    private Map<String, File> fileRegistry;
    private Commit commit;

    public Release() {
        super();
    }

    public void setFileAsDefectiveIncrementingNumberOfFix(String filename) {

        File file = this.fileRegistry.get(filename);
        if (file != null) {

            int numberOfFix = (int) file.getMetadata(DatasetOutputField.NUMBER_OF_FIX);
            numberOfFix++;

            file.setMetadata(DatasetOutputField.IS_BUGGY, true);
            file.setMetadata(DatasetOutputField.NUMBER_OF_FIX, numberOfFix);
        }
    }


    public Commit getCommit() {
        return commit;
    }

    public void setCommit(Commit commit) {
        this.commit = commit;
        this.setMetadata(ReleaseOutputField.RELEASE_DATE, this.commit.date);
    }

    public LocalDateTime getReleaseDate() {
        return (LocalDateTime) this.getMetadata(ReleaseOutputField.RELEASE_DATE);
    }

    public int getReleaseVersionID() {
        return (int) this.getMetadata(ReleaseOutputField.VERSION_ID);
    }

    public void setFileRegistry(Map<String, File> fileRegistry) {
        this.fileRegistry = fileRegistry;

        for (File file : this.fileRegistry.values())
            file.setMetadata(DatasetOutputField.VERSION_INDEX, this.getMetadata(ReleaseOutputField.VERSION_INDEX));
    }

    public Collection<File> getFiles() {
        return this.fileRegistry.values();
    }

    public int getVersionIndex() {
        return (int) this.getMetadata(ReleaseOutputField.VERSION_INDEX);
    }

    public void setVersionIndex(int index) {
        this.setMetadata(ReleaseOutputField.VERSION_INDEX, index);
    }
}