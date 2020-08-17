package predictor.options;

public enum WekaFilter {

    NO_SAMPLING(null, null),
    OVER_SAMPLING("weka.filters.supervised.instance.Resample", new String[]{"-M", "1.0"}),
    UNDER_SAMPLING("weka.filters.supervised.instance.SpreadSubsample", new String[]{"-M", "1.0"}),
    SMOTE("weka.filters.supervised.instance.SMOTE", null);

    public final String className;
    public final String[] wekaFilterOptions;

    WekaFilter(String className, String[] wekaFilterOptions) {
        this.className = className;
        this.wekaFilterOptions = wekaFilterOptions;
    }
}
