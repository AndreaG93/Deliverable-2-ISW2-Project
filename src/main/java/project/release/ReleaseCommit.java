package project.release;

import java.time.LocalDateTime;

public final class ReleaseCommit {

    public final String hash;
    public final LocalDateTime date;

    public ReleaseCommit(String hash, LocalDateTime date) {
        this.hash = hash;
        this.date = date;
    }
}
