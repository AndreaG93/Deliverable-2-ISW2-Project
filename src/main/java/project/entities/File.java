package project.entities;

import project.entities.metadata.FileMetadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class File implements MetadataExportable {

    public final Map<FileMetadata, Object> metadata;

    public final String name;
    public final String hash;

    public File(String name, String hash) {

        this.name = name;
        this.hash = hash;
        this.metadata = new HashMap<>();

        for (FileMetadata fileMetadata : FileMetadata.values())
            this.metadata.put(fileMetadata, "");

        this.metadata.put(FileMetadata.NAME, name);
        this.metadata.put(FileMetadata.IS_BUGGY, false);
    }

    @Override
    public List<String> exportMetadataValues() {

        List<String> output = new ArrayList<>();

        for (FileMetadata fileMetadata : FileMetadata.values())
            output.add(this.metadata.get(fileMetadata).toString());

        return output;
    }
}