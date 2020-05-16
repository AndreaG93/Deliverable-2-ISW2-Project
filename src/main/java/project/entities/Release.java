package project.entities;

import project.entities.metadata.ReleaseMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Release implements MetadataExportable {

    public final Map<ReleaseMetadata, Object> metadata;

    private Commit releaseCommit;
    private List<File> releaseFiles;
    private Map<String, File> releasesFileIndexedByFilename;

    public Release(Map<ReleaseMetadata, Object> metadata) {

        this.metadata = metadata;
        this.releasesFileIndexedByFilename = new TreeMap<>();
    }

    public Commit getReleaseCommit() {
        return releaseCommit;
    }

    public void setReleaseCommit(Commit releaseCommit) {
        this.releaseCommit = releaseCommit;
    }

    public List<File> getReleaseFiles() {
        return releaseFiles;
    }

    public void setReleaseFiles(List<File> releaseFiles) {

        this.releaseFiles = releaseFiles;

        for (File file : this.releaseFiles)
            this.releasesFileIndexedByFilename.put(file.name, file);
    }

    public Map<String, File> getReleasesFileIndexedByFilename() {
        return releasesFileIndexedByFilename;
    }

    @Override
    public List<String> exportMetadataValues() {

        List<String> output = new ArrayList<>();

        for (ReleaseMetadata releaseMetadata : ReleaseMetadata.values())
            output.add(this.metadata.get(releaseMetadata).toString());

        return output;
    }
}