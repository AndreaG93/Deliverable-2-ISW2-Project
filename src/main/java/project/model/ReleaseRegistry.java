package project.model;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.TreeSet;

public class ReleaseRegistry extends TreeSet<Release> {




    public ReleaseRegistry() {
        super(new Comparator<Release>() {
            @Override
            public int compare(Release release1, Release release2) {

                LocalDateTime releaseDate1 = release1.getReleaseCommit().date;
                LocalDateTime releaseDate2 = release2.getReleaseCommit().date;

                return releaseDate1.compareTo(releaseDate2);
            }
        });
    }
}
