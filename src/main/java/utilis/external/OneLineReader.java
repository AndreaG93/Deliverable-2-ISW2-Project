package utilis.external;

public class OneLineReader implements ExternalApplicationOutputReader {

    private String output;
    private boolean stopReading = false;

    @Override
    public void readOutputLine(String input) {

        this.output = input;
        this.stopReading = true;
    }

    @Override
    public boolean isOutputReadingTerminated() {
        return this.stopReading;
    }

    public String getOutput() {
        return output;
    }
}
