package predictor.options;

public enum WekaClassifier {

    IBK("weka.classifiers.lazy.IBk"),
    NAIVE_BAYES("weka.classifiers.bayes.NaiveBayes"),
    RANDOM_FOREST("weka.classifiers.trees.RandomForest");

    private final String wekaClassName;

    WekaClassifier(String name) {
        this.wekaClassName = name;
    }

    public String getWekaClassName() {
        return this.wekaClassName;
    }
}
