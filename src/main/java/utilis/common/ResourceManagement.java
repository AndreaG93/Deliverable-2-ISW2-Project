package utilis.common;

import entities.FileCSV;

import java.io.Closeable;
import java.util.logging.Logger;

public class ResourceManagement {

    private ResourceManagement() {
    }

    public static void close(Closeable closeableResource) {

        try {

            if (closeableResource != null)
                closeableResource.close();

        } catch (Exception e) {

            Logger.getLogger(FileCSV.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());
        }
    }
}
