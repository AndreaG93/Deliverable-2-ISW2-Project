package core.vcs;

public class FileChangeSetSizeMetric {

    public long changeSetSize;
    public long maxChangeSetSize;
    public double averageChangeSetSize;

    public FileChangeSetSizeMetric() {

        this.changeSetSize = 0;
        this.maxChangeSetSize = 0;
        this.averageChangeSetSize = 0;
    }
}
