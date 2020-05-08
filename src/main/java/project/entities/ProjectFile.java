package project.entities;

public class ProjectFile {

    public String name;

    public long LOC;
    public long LOCTouched;
    public long numberOfAuthors;
    public long numberOfRevisions;

    public long changeSetSize;
    public long maxChangeSetSize;
    public double averageChangeSetSize;

    public long churn;
    public double ageInWeeks;

    boolean isBuggy;
}