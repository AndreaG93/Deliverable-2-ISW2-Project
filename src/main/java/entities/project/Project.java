package entities.project;

public class Project {

    public final String name;
    public final String gitRepositoryURL;
    public final String datasetFilename;
    public final String releasesFilename;
    public final String predictorsEvaluationFilename;

    public Project(String name, String gitRepositoryURL) {

        this.name = name;
        this.gitRepositoryURL = gitRepositoryURL;

        this.datasetFilename = this.name.toUpperCase() + "-Dataset.csv";
        this.releasesFilename = this.name.toUpperCase() + "-Releases.csv";
        this.predictorsEvaluationFilename = this.name.toUpperCase() + "-PredictorsEvaluation.csv";
    }
}
