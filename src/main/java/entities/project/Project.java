package entities.project;

public class Project {

    public final String name;
    public final String gitRepositoryURL;
    public final String datasetFilename;
    public final String releasesFilename;
    public final String predictorsEvaluationFilename;
    public final String datasetForPredictionFilename;
    public final String lastVersion;

    public Project(String name, String gitRepositoryURL, String lastVersion) {

        this.name = name;
        this.gitRepositoryURL = gitRepositoryURL;
        this.lastVersion = lastVersion;

        this.datasetFilename = this.name.toUpperCase() + "-Dataset.csv";
        this.releasesFilename = this.name.toUpperCase() + "-Releases.csv";
        this.predictorsEvaluationFilename = this.name.toUpperCase() + "-PredictorsEvaluation.csv";
        this.datasetForPredictionFilename = this.name.toUpperCase() + "-ForPrediction.csv";
    }
}
