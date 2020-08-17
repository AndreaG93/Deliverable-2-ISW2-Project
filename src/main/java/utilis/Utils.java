package utilis;

import entities.FileCSV;

import java.io.Closeable;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Utils {

    private Utils() {
    }

    public static boolean isJavaFile(String filename) {

        int lastIndexOf = filename.lastIndexOf('.');
        if (lastIndexOf == -1)
            return false;

        String extension = filename.substring(lastIndexOf).toLowerCase();

        return extension.equals(".java");
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

    public static int[] stringArrayToIntArray(String[] stringArray) {
        return Stream.of(stringArray).mapToInt(Integer::parseInt).toArray();
    }
}
