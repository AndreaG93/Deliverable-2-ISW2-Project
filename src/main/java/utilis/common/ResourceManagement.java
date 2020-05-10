package utilis.common;

import project.exporter.ProjectDatasetExporter;

import java.io.Closeable;
import java.util.logging.Logger;

public class ResourceManagement {

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
