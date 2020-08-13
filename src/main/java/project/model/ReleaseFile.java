package project.model;

import project.model.metadata.MetadataType;
import project.model.metadata.MetadataProvider;

public class ReleaseFile extends MetadataProvider {

    public static final MetadataType[] METADATA_FOR_DATASET = {
            MetadataType.NAME,
            MetadataType.LOC,
            MetadataType.LOC_ADDED,
            MetadataType.LOCTouched,
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
            MetadataType.WEIGHTED_AGE_IN_WEEKS,};

    public ReleaseFile(String name, String hash) {

        super();

        this.setMetadataValue(MetadataType.NAME, name);
        this.setMetadataValue(MetadataType.HASH, hash);
        this.setMetadataValue(MetadataType.IS_BUGGY, false);
    }

    public String getName() {
        return (String) getMetadataValue(MetadataType.NAME);
    }

    public String getHash() {
        return (String) getMetadataValue(MetadataType.HASH);
    }
}