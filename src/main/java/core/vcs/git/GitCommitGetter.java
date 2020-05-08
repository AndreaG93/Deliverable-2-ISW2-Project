package core.vcs.git;

import project.entities.Commit;
import utilis.external.ExternalApplicationOutputReader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GitCommitGetter implements ExternalApplicationOutputReader {

    Commit output;
    boolean isOutputObtained;

    public GitCommitGetter() {

        this.isOutputObtained = false;
        this.output = new Commit();
    }

    @Override
    public void readOutputLine(String input) {

        String[] commitInfo = input.split("<->");

        this.output.hash = commitInfo[0];
        this.output.date = LocalDateTime.parse(commitInfo[1], DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        this.isOutputObtained = true;
    }

    @Override
    public boolean isOutputReadingTerminated() {
        return this.isOutputObtained;
    }
}
