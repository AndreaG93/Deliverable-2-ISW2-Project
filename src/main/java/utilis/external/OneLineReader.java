package utilis.external;

public class OneLineReader implements ExternalApplicationOutputReader {

    public String output;
    boolean stopReading = false;

    @Override
    public void readOutputLine(String input) {

        this.output = input;
        this.stopReading = true;
    }

    @Override
    public boolean isOutputReadingTerminated() {
        return this.stopReading;
    }
}
