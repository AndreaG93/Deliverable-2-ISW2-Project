package project.release;

import project.metadata.ReleaseFileMetadata;
import project.utils.Exportable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReleaseFile implements Exportable {

    public final String name;
    public final String hash;
    public final Map<ReleaseFileMetadata, Object> fileMetricsRegistry;

    public ReleaseFile(String name, String hash) {

        this.name = name;
        this.hash = hash;

        this.fileMetricsRegistry = new HashMap<>();

        this.fileMetricsRegistry.put(ReleaseFileMetadata.name, name);
    }

    public static List<String> exportMetadataKey() {

        List<String> output = new ArrayList<>();

        for (ReleaseFileMetadata fileMetric : ReleaseFileMetadata.values())
            output.add(fileMetric.toString());

        return output;
    }

    @Override
    public List<String> exportMetadataValues() {

        List<String> output = new ArrayList<>();

        for (ReleaseFileMetadata fileMetric : ReleaseFileMetadata.values())
            output.add(this.fileMetricsRegistry.get(fileMetric).toString());

        return output;
    }
}