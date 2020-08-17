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

        this.datasetFilename = this.name + "-Dataset";
        this.releasesFilename = this.name + "-Releases";
        this.predictorsEvaluationFilename = this.name + "-PredictorsEvaluation";
    }
}
