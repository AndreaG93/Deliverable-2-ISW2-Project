package entities.predictor;

import entities.MetadataProvider;
import entities.enums.PredictorEvaluationOutputField;
import predictor.Predictor;
import predictor.PredictorInput;
import predictor.options.WekaAttributeSelection;
import predictor.options.WekaClassifier;
import predictor.options.WekaFilter;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.filters.Filter;

import java.util.logging.Logger;

public class DefectiveClassesPredictor extends Predictor<PredictorEvaluationOutputField> {

    private final MetadataProvider<PredictorEvaluationOutputField> evaluation;

    public DefectiveClassesPredictor(WekaClassifier wekaClassifier, WekaFilter wekaFilter, WekaAttributeSelection wekaAttributeSelection) {
        super(wekaClassifier, wekaFilter, wekaAttributeSelection);

        this.evaluation = new MetadataProvider<>();

        this.evaluation.setMetadata(PredictorEvaluationOutputField.CLASSIFIER, this.wekaClassifier);
        this.evaluation.setMetadata(PredictorEvaluationOutputField.BALANCING, this.wekaFilter);
        this.evaluation.setMetadata(PredictorEvaluationOutputField.FEATURE_SELECTION, this.wekaAttributeSelection);
    }

    private Instances setupTrainingSet(PredictorInput input) {

        Instances output = null;

        try {

            if (this.attributeSelection != null) {

                this.attributeSelection.setInputFormat(input.trainingSet);
                output = Filter.useFilter(input.trainingSet, this.attributeSelection);

            } else
                output = input.trainingSet;

        } catch (Exception e) {

            Logger.getLogger(Predictor.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());
        }

        return output;
    }

    private void setupFilterOptions() {

        try {

            if (this.wekaFilter == WekaFilter.OVER_SAMPLING) {

                double percentageDefectiveClassTraining = (double) this.evaluation.getMetadata(PredictorEvaluationOutputField.PERCENTAGE_DEFECTIVE_TRAINING);
                double percentageOfMajorityClass = Math.max(percentageDefectiveClassTraining, 100 - percentageDefectiveClassTraining);

                FilteredClassifier filteredClassifier = (FilteredClassifier) this.classifier;

                String[] options = new String[]{"-B", "1.0", "-Z", Double.toString(2 * percentageOfMajorityClass)};
                filteredClassifier.setOptions(options);
            }

        } catch (Exception e) {

            Logger.getLogger(Predictor.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());
        }
    }


    @Override
    public MetadataProvider<PredictorEvaluationOutputField> getEvaluationResults(PredictorInput input) {

        int evaluationClassIndex = input.testingSet.numAttributes() - 1;

        this.evaluation.setMetadata(PredictorEvaluationOutputField.PERCENTAGE_TRAINING, input.percentageTrainingInstances);
        this.evaluation.setMetadata(PredictorEvaluationOutputField.PERCENTAGE_DEFECTIVE_TRAINING, input.getPercentageOfTrainingSetInstancesWith(evaluationClassIndex, "true"));
        this.evaluation.setMetadata(PredictorEvaluationOutputField.PERCENTAGE_DEFECTIVE_TESTING, input.getPercentageOfTestingSetInstancesWith(evaluationClassIndex, "true"));

        Instances trainingSet = setupTrainingSet(input);
        Instances testingSet = input.testingSet;

        setupFilterOptions();

        Evaluation evaluation = evaluate(trainingSet, testingSet, evaluationClassIndex);

        this.evaluation.setMetadata(PredictorEvaluationOutputField.TRUE_POSITIVE, evaluation.numTruePositives(evaluationClassIndex));
        this.evaluation.setMetadata(PredictorEvaluationOutputField.TRUE_NEGATIVE, evaluation.numTruePositives(evaluationClassIndex));
        this.evaluation.setMetadata(PredictorEvaluationOutputField.FALSE_POSITIVE, evaluation.falsePositiveRate(evaluationClassIndex));
        this.evaluation.setMetadata(PredictorEvaluationOutputField.FALSE_NEGATIVE, evaluation.falseNegativeRate(evaluationClassIndex));

        this.evaluation.setMetadata(PredictorEvaluationOutputField.PRECISION, evaluation.precision(evaluationClassIndex));
        this.evaluation.setMetadata(PredictorEvaluationOutputField.RECALL, evaluation.recall(evaluationClassIndex));
        this.evaluation.setMetadata(PredictorEvaluationOutputField.ROC_AREA, evaluation.areaUnderROC(evaluationClassIndex));
        this.evaluation.setMetadata(PredictorEvaluationOutputField.KAPPA, evaluation.kappa());

        return this.evaluation;
    }
}
