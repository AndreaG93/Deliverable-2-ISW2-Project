package core.vcs.git;

import utilis.external.ExternalApplicationOutputReader;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GitFileWeekAgeGetter implements ExternalApplicationOutputReader {

    final LocalDateTime releaseDate;
    double output;
    boolean isOutputObtained;

    public GitFileWeekAgeGetter(LocalDateTime releaseDate) {

        this.releaseDate = releaseDate;
        this.isOutputObtained = false;
    }

    @Override
    public void readOutputLine(String input) {

        LocalDateTime creationDate = LocalDateTime.parse(input, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        this.output = Duration.between(creationDate, releaseDate).toDays() / 7.0;
        this.isOutputObtained = true;
    }

    @Override
    public boolean isOutputReadingTerminated() {
        return this.isOutputObtained;
    }
}
