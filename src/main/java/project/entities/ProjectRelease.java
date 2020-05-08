package project.entities;

import java.time.LocalDateTime;
import java.util.List;

public class ProjectRelease {

    public int id;
    public String name;
    public LocalDateTime releaseDate;

    public List<ProjectFile> files;

    /*
    public LocalDate endOfLifeDate;

    public List<ProjectFile> files;

    public Release() {
        this.endOfLifeDate = null;
    }

    @Override
    public String toString() {
        return String.format("--> Release-Info: ID %d,\tRelease Name %s,\t Date %s", this.id, this.name, this.releaseDate.toString());
    }
    */

}