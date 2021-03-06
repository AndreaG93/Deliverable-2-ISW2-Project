package utilis.external;

public class ExternalApplicationOutputLinesCounter implements ExternalApplicationOutputReader {

    private int output;

    public ExternalApplicationOutputLinesCounter() {
        this.output = 0;
    }

    @Override
    public void readOutputLine(String input) {
        this.output++;
    }

    @Override
    public boolean isOutputReadingTerminated() {
        return false;
    }

    public int getOutput() {
        return output;
    }
}
