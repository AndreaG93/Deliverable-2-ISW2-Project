package entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetadataProvider<T> {

    private final Map<T, Object> registry = new HashMap<>();

    public void setMetadata(T type, Object value) {
        this.registry.put(type, value);
    }

    public Object getMetadata(T type) {
        return this.registry.get(type);
    }

    public String getMetadataAsString(T type) {

        Object object = this.registry.get(type);

        if (object != null)
            return object.toString();
        else
            return "";
    }

    public List<String> getMetadataAsString(T[] types) {

        List<String> output = new ArrayList<>();

        for (T type : types)
            output.add(getMetadataAsString(type));

        return output;
    }
}