package entities;

import java.time.LocalDateTime;

public final class Commit {

    public final String hash;
    public final LocalDateTime date;

    public Commit(String hash, LocalDateTime date) {
        this.hash = hash;
        this.date = date;
    }
}
