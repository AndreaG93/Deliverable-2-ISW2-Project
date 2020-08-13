package utilis.external;

public interface ExternalApplicationOutputReader {

    void readOutputLine(String input);

    boolean isOutputReadingTerminated();
}
