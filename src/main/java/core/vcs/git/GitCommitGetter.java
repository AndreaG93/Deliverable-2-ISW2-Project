package core.vcs.git;

import core.vcs.ReleaseCommit;
import utilis.external.ExternalApplicationOutputReader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GitCommitGetter implements ExternalApplicationOutputReader {

    ReleaseCommit output;
    boolean isOutputObtained;

    public GitCommitGetter() {

        this.isOutputObtained = false;
    }

    @Override
    public void readOutputLine(String input) {

        String[] commitInfo = input.split("<->");

        String commitHash = commitInfo[0];
        LocalDateTime commitLocalDateTime = LocalDateTime.parse(commitInfo[1], DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        this.isOutputObtained = true;
        this.output = new ReleaseCommit(commitHash, commitLocalDateTime);
    }

    @Override
    public boolean isOutputReadingTerminated() {
        return this.isOutputObtained;
    }
}
