package utilis.external;

import java.util.ArrayList;
import java.util.List;

public class ExternalApplicationOutputListMaker implements ExternalApplicationOutputReader {

    public final List<String> output;

    public ExternalApplicationOutputListMaker() {
        this.output = new ArrayList<>();
    }

    @Override
    public void readOutputLine(String input) {
        this.output.add(input);
    }

    @Override
    public boolean isOutputReadingTerminated() {
        return false;
    }
}