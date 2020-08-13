package project.model.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MetadataProvider {

    private final Map<MetadataType, Object> registry;

    protected MetadataProvider() {
        this.registry = new TreeMap<>();
    }

    public void setMetadata(MetadataType type, Object value) {
        this.registry.put(type, value);
    }

    public Object getMetadata(MetadataType type) {
        return this.registry.get(type);
    }

    public List<String> getMetadataStringValues(MetadataType[] types) {

        List<String> output = new ArrayList<>();

        for (MetadataType type : types) {

            Object value = this.registry.get(type);
            if (value != null)
                output.add(value.toString());
            else
                output.add("");
        }

        return output;
    }
}