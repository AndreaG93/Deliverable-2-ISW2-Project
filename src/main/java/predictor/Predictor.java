package predictor;

import entities.MetadataProvider;
import predictor.options.WekaAttributeSelection;
import predictor.options.WekaClassifier;
import predictor.options.WekaFilter;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

public abstract class Predictor<T> {

    protected final WekaClassifier wekaClassifier;
    protected final WekaFilter wekaFilter;
    protected final WekaAttributeSelection wekaAttributeSelection;

    protected Classifier classifier;
    protected Filter filter;
    protected AttributeSelection attributeSelection;


    public Predictor(WekaClassifier wekaClassifier, WekaFilter wekaFilter, WekaAttributeSelection wekaAttributeSelection) {

        this.wekaClassifier = wekaClassifier;
        this.wekaFilter = wekaFilter;
        this.wekaAttributeSelection = wekaAttributeSelection;

        try {

            setClassifier();
            setAttributeSelection();

        } catch (Exception e) {

            Logger.getLogger(Predictor.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());
        }
    }

    private void setClassifier() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        this.classifier = (Classifier) Class.forName(this.wekaClassifier.getWekaClassName()).getDeclaredConstructor().newInstance();

        if (this.wekaFilter != WekaFilter.NO_SAMPLING)
            this.filter = (Filter) Class.forName(this.wekaFilter.className).getDeclaredConstructor().newInstance();
        else
            this.filter = null;
    }

    private void setAttributeSelection() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        if (this.wekaAttributeSelection.searchTechniqueClassName != null && this.wekaAttributeSelection.evaluationTechniqueClassName != null) {

            this.attributeSelection = new AttributeSelection();
            ASSearch asSearch = (ASSearch) Class.forName(this.wekaAttributeSelection.searchTechniqueClassName).getDeclaredConstructor().newInstance();
            ASEvaluation asEvaluation = (ASEvaluation) Class.forName(this.wekaAttributeSelection.evaluationTechniqueClassName).getDeclaredConstructor().newInstance();

            this.attributeSelection.setSearch(asSearch);
            this.attributeSelection.setEvaluator(asEvaluation);

        } else {
            this.attributeSelection = null;
        }
    }

    protected Evaluation evaluate(Instances trainingSet, Instances testingSet) {

        Evaluation output = null;

        try {

            trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
            testingSet.setClassIndex(testingSet.numAttributes() - 1);

            this.classifier.buildClassifier(trainingSet);

            output = new Evaluation(testingSet);
            output.evaluateModel(this.classifier, testingSet);

        } catch (Exception e) {

            Logger.getLogger(Predictor.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());
        }

        return output;
    }

    public abstract MetadataProvider<T> getEvaluationResults(PredictorInput input);
}
