package project.model.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class MetadataProvider {

    private final Map<MetadataType, Object> metadataRegistry;

    protected MetadataProvider() {
        this.metadataRegistry = new TreeMap<>();
    }

    public void setMetadataValue(MetadataType type, Object value) {
        this.metadataRegistry.put(type, value);
    }

    public Object getMetadataValue(MetadataType type) {
        return this.metadataRegistry.get(type);
    }

    public List<String> getMetadataValuesList(MetadataType[] types) {

        List<String> output = new ArrayList<>();

        for (MetadataType metadataType : types) {

            Object value = this.metadataRegistry.get(metadataType);
            if (value != null)
                output.add(value.toString());
            else
                output.add("");
        }

        return output;
    }
}