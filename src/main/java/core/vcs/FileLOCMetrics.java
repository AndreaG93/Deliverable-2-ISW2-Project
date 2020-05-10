package core.vcs;

final public class FileLOCMetrics {

    public long LOCTouched;

    public long LOCAdded;
    public long maxLOCAdded;
    public double averageLOCAdded;

    public long churn;
    public long maxChurn;
    public double averageChurn;

    public FileLOCMetrics() {

        this.averageChurn = 0;
        this.maxChurn = 0;
    }
}
