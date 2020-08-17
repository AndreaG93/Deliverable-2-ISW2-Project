package predictor.options;

public enum WekaAttributeSelection {

    NO_SELECTION(null, null),
    BEST_FIRST("weka.attributeSelection.BestFirst", "weka.attributeSelection.CfsSubsetEval");

    public final String searchTechniqueClassName;
    public final String evaluationTechniqueClassName;

    WekaAttributeSelection(String searchTechniqueClassName, String evaluationTechniqueClassName) {
        this.searchTechniqueClassName = searchTechniqueClassName;
        this.evaluationTechniqueClassName = evaluationTechniqueClassName;
    }
}
