package predictor.options;

public enum WekaFilter {

    NO_SAMPLING(null),
    OVER_SAMPLING("weka.filters.supervised.instance.Resample"),
    UNDER_SAMPLING("weka.filters.supervised.instance.SpreadSubsample"),
    SMOTE("weka.filters.supervised.instance.SMOTE");

    public final String className;

    WekaFilter(String className) {
        this.className = className;
    }
}
