package core.vcs;

public class FileMetric {

    public long LOCTouched;

    public long LOCAdded;
    public long maxLOCAdded;
    public double averageLOCAdded;

    public long churn;
    public long maxChurn;
    public double averageChurn;

    public FileMetric() {

        this.averageChurn = 0;
        this.maxChurn = 0;
    }
}
