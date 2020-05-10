package core.vcs;

import java.time.LocalDateTime;
import java.util.List;

public class Release {

    public int id;
    public String name;
    public LocalDateTime releaseDate;

    public List<ReleaseFile> files;
}