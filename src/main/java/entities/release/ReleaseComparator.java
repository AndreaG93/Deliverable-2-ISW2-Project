package entities.release;

import java.time.LocalDateTime;
import java.util.Comparator;

public class ReleaseComparator implements Comparator<Release> {

    @Override
    public int compare(Release release1, Release release2) {

        LocalDateTime releaseDate1 = release1.getReleaseDate();
        LocalDateTime releaseDate2 = release2.getReleaseDate();

        return releaseDate1.compareTo(releaseDate2);
    }
}
