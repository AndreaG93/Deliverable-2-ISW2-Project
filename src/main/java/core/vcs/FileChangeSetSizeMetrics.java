package core.vcs;

public final class FileChangeSetSizeMetrics {

    public final long maxChangeSetSize;
    public final double averageChangeSetSize;

    public FileChangeSetSizeMetrics(long maxChangeSetSize, double averageChangeSetSize) {

        this.maxChangeSetSize = maxChangeSetSize;
        this.averageChangeSetSize = averageChangeSetSize;
    }
}