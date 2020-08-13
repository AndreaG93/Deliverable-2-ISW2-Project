package project.model;

import project.model.metadata.MetadataProvider;
import project.model.metadata.MetadataType;

import java.util.Map;

public class Release extends MetadataProvider {

    public static MetadataType[] METADATA_FOR_DATASET = {MetadataType.VERSION_INDEX, MetadataType.VERSION_ID, MetadataType.NAME, MetadataType.DATE};

    private Map<String, ReleaseFile> releaseFiles;
    private Commit releaseCommit;

    public Release() {
        super();
    }

    public Map<String, ReleaseFile> getReleaseFiles() {
        return releaseFiles;
    }

    public void setReleaseFiles(Map<String, ReleaseFile> releaseFiles) {
        this.releaseFiles = releaseFiles;
    }

    public Commit getReleaseCommit() {
        return releaseCommit;
    }

    public void setReleaseCommit(Commit releaseCommit) {

        this.setMetadataValue(MetadataType.DATE, releaseCommit.date);
        this.releaseCommit = releaseCommit;
    }


}