package project.model;

import project.model.metadata.MetadataProvider;
import project.model.metadata.MetadataType;

public class File extends MetadataProvider {

    public static final MetadataType[] METADATA_FOR_DATASET = {
            MetadataType.VERSION_INDEX,
            MetadataType.NAME,
            MetadataType.LOC,
            MetadataType.LOC_ADDED,
            MetadataType.LOC_TOUCHED,
            MetadataType.NUMBER_OF_REVISIONS,
            MetadataType.NUMBER_OF_AUTHORS,
            MetadataType.NUMBER_OF_FIX,
            MetadataType.MAX_LOC_ADDED,
            MetadataType.AVERAGE_LOC_ADDED,
            MetadataType.CHURN,
            MetadataType.MAX_CHURN,
            MetadataType.AVERAGE_CHURN,
            MetadataType.CHANGE_SET_SIZE,
            MetadataType.MAX_CHANGE_SET_SIZE,
            MetadataType.AVERAGE_CHANGE_SET_SIZE,
            MetadataType.AGE_IN_WEEKS,
            MetadataType.WEIGHTED_AGE_IN_WEEKS,
            MetadataType.IS_BUGGY};

    public File(String name, String hash) {

        this.setMetadata(MetadataType.NAME, name);
        this.setMetadata(MetadataType.HASH, hash);
        this.setMetadata(MetadataType.IS_BUGGY, false);
    }

    public String getName() {
        return (String) this.getMetadata(MetadataType.NAME);
    }
}