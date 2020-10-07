package utilis;

public final class OsUtils {

    private static String operativeSystem = null;

    private OsUtils() {
    }

    private static String getOsName() {

        if (operativeSystem == null)
            operativeSystem = System.getProperty("os.name");

        return operativeSystem;
    }

    public static boolean isWindows() {
        return getOsName().startsWith("Windows");
    }
}
