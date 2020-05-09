package project.entities;

public class ProjectFile {

    public String name;
    public String hash;

    public long LOC;
    public long LOCTouched;

    public long numberOfRevisions;
    public long numberOfAuthors;

    public long LOCAdded;
    public long maxLOCAdded;
    public double averageLOCAdded;

    public long churn;
    public long maxChurn;
    public double averageChurn;

    public long changeSetSize;
    public long maxChangeSetSize;
    public double averageChangeSetSize;

    public double ageInWeeks;
    public double weightedAgeInWeeks;

    public boolean isBuggy;
}