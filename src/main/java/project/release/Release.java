package project.release;

import project.metadata.ReleaseMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Release implements Exportable {

    public final Map<ReleaseMetadata, Object> metadata;
    private List<ReleaseFile> files;

    public Release(Map<ReleaseMetadata, Object> metadata) {
        this.metadata = metadata;
    }

    public static List<String> exportMetadataKey() {

        List<String> output = new ArrayList<>();

        for (ReleaseMetadata metadata : ReleaseMetadata.values())
            output.add(metadata.toString());

        return output;
    }

    public List<ReleaseFile> getFiles() {
        return files;
    }

    public void setFiles(List<ReleaseFile> files) {
        this.files = files;
    }

    @Override
    public List<String> exportMetadataValues() {

        List<String> output = new ArrayList<>();

        for (ReleaseMetadata metadata : ReleaseMetadata.values())
            output.add(metadata.toString());

        return output;
    }
}