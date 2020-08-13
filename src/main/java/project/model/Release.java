package project.model;

import project.model.metadata.MetadataProvider;
import project.model.metadata.MetadataType;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

public class Release extends MetadataProvider {

    public static final MetadataType[] METADATA_FOR_DATASET = {
            MetadataType.VERSION_INDEX,
            MetadataType.VERSION_ID,
            MetadataType.NAME,
            MetadataType.RELEASE_DATE};

    private Map<String, File> fileRegistry;
    private Commit commit;

    public Release() {
        super();
    }

    public void setFileAsDefective(String filename) {

        File file = this.fileRegistry.get(filename);
        if (file != null)
            file.setMetadata(MetadataType.IS_BUGGY, true);
    }

    public Commit getCommit() {
        return commit;
    }

    public LocalDateTime getReleaseDate() {
        return (LocalDateTime) this.getMetadata(MetadataType.RELEASE_DATE);
    }

    public int getReleaseVersionID() {
        return (int) this.getMetadata(MetadataType.VERSION_ID);
    }

    public void setCommit(Commit commit) {
        this.commit = commit;
        this.setMetadata(MetadataType.RELEASE_DATE, this.commit.date);
    }

    public void setFileRegistry(Map<String, File> fileRegistry) {
        this.fileRegistry = fileRegistry;

        for (File file : this.fileRegistry.values())
            file.setMetadata(MetadataType.VERSION_INDEX, this.getMetadata(MetadataType.VERSION_INDEX));
    }

    public Collection<File> getFiles() {
        return this.fileRegistry.values();
    }


    public void setVersionIndex(int index) {
        this.setMetadata(MetadataType.VERSION_INDEX, index);
    }

    public int getVersionIndex() {
        return (int) this.getMetadata(MetadataType.VERSION_INDEX);
    }
}