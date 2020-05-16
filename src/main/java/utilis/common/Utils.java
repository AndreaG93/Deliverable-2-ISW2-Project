package utilis.common;

public class Utils {

    public static boolean isJavaFile(String filename) {

        int lastIndexOf = filename.lastIndexOf('.');
        if (lastIndexOf == -1)
            return false;

        String extension = filename.substring(lastIndexOf).toLowerCase();

        return extension.equals(".java");
    }
}
