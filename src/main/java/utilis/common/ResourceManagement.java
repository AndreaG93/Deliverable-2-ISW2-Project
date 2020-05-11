package utilis.common;

import project.utils.ProjectDatasetExporter;

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

            Logger.getLogger(ProjectDatasetExporter.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());
        }
    }
}
