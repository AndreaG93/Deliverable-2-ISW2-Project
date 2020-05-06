package project;

import java.time.LocalDate;
import java.util.List;

public class Release {

    public int id;
    public String name;

    public LocalDate releaseDate;
    public LocalDate endOfLifeDate;

    public List<ProjectFile> files;

    @Override
    public String toString() {
        return String.format("--> Release-Info: ID %d,\tRelease Name %s,\t Date %s", this.id, this.name, this.releaseDate.toString());
    }
}