package predictor.options;

public enum WekaFilter {

    NO_SAMPLING(null, null),
    OVER_SAMPLING("weka.filters.supervised.instance.Resample", null),
    UNDER_SAMPLING("weka.filters.supervised.instance.SpreadSubsample", new String[]{"-M", "1.0"}),
    SMOTE("weka.filters.supervised.instance.SMOTE", null);

    public final String className;
    public final String[] options;

    WekaFilter(String className, String[] options) {
        this.className = className;
        this.options = options;
    }
}
