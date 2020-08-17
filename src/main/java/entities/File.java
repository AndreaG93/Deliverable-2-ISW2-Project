package entities;

import entities.MetadataProvider;
import entities.enums.DatasetOutputField;

public class File extends MetadataProvider<DatasetOutputField> {

    public final String hash;

    public File(String name, String hash) {

        this.hash = hash;

        this.setMetadata(DatasetOutputField.NAME, name);
        this.setMetadata(DatasetOutputField.NUMBER_OF_FIX, 0);
        this.setMetadata(DatasetOutputField.IS_BUGGY, false);
    }

    public String getName() {
        return (String) this.getMetadata(DatasetOutputField.NAME);
    }
}