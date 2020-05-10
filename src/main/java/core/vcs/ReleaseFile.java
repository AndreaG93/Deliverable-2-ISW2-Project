package core.vcs;

import core.FileMetrics;

import java.util.AbstractMap;
import java.util.HashMap;

public class ReleaseFile {

    public final String name;
    public final String hash;
    public final AbstractMap<FileMetrics, Object> fileMetricsRegistry;

    public ReleaseFile(String name, String hash) {

        this.name = name;
        this.hash = hash;

        this.fileMetricsRegistry = new HashMap<>();
    }
}