package utilis.common;

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

    public static int[] stringArrayToIntArray(String[] stringArray) {
        return Stream.of(stringArray).mapToInt(Integer::parseInt).toArray();
    }
}
