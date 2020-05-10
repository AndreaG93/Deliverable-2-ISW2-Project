package core.its;

public class Issue {

    public final int id;
    public final String[] affectedVersions;
    public final String[] fixedVersions;

    public Issue(int id, String[] affectedVersions, String[] fixedVersions) {
        this.id = id;
        this.affectedVersions = affectedVersions;
        this.fixedVersions = fixedVersions;
    }
}
