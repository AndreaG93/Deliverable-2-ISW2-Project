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

    private Instances trainingSet;
    private Instances testingSet;

    private final MetadataProvider<PredictorEvaluationOutputField> evaluation;

    public DefectiveClassesPredictor(WekaClassifier wekaClassifier, WekaFilter wekaFilter, WekaAttributeSelection wekaAttributeSelection) {
        super(wekaClassifier, wekaFilter, wekaAttributeSelection);

        this.evaluation = new MetadataProvider<>();

        this.evaluation.setMetadata(PredictorEvaluationOutputField.CLASSIFIER, this.wekaClassifier);
        this.evaluation.setMetadata(PredictorEvaluationOutputField.BALANCING, this.wekaFilter);
        this.evaluation.setMetadata(PredictorEvaluationOutputField.FEATURE_SELECTION, this.wekaAttributeSelection);
    }

    private void setupSets(PredictorInput input) {

        try {

            if (this.attributeSelection != null) {

                this.attributeSelection.setInputFormat(input.trainingSet);

                this.trainingSet = Filter.useFilter(input.trainingSet, this.attributeSelection);
                this.testingSet = Filter.useFilter(input.trainingSet, this.attributeSelection);

            } else {

                this.trainingSet = input.trainingSet;
                this.testingSet = input.testingSet;
            }

        } catch (Exception e) {

            Logger.getLogger(Predictor.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());
        }
    }

    private void setupFilterOptions() {

        try {

            String[] options;

            switch (this.wekaFilter) {

                case OVER_SAMPLING:

                    double percentageDefectiveClassTraining = (double) this.evaluation.getMetadata(PredictorEvaluationOutputField.PERCENTAGE_DEFECTIVE_TRAINING);
                    double percentageOfMajorityClass = Math.max(percentageDefectiveClassTraining, 100 - percentageDefectiveClassTraining);

                    options = new String[]{"-B", "1.0", "-Z", Double.toString(2 * percentageOfMajorityClass)};
                    break;

                case UNDER_SAMPLING:

                    options = new String[]{"-M", "1.0"};
                    break;

                default:
                    options = null;
            }

            if (options != null) {

                this.filter.setOptions(options);

                FilteredClassifier filteredClassifier = new FilteredClassifier();
                filteredClassifier.setFilter(this.filter);
                filteredClassifier.setClassifier(this.classifier);

                this.classifier = filteredClassifier;
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

        setupSets(input);

        if (this.filter != null)
            setupFilterOptions();

        Evaluation wekaEval = evaluate(this.trainingSet, this.testingSet);

        this.evaluation.setMetadata(PredictorEvaluationOutputField.TRUE_POSITIVE, wekaEval.numTruePositives(1));
        this.evaluation.setMetadata(PredictorEvaluationOutputField.TRUE_NEGATIVE, wekaEval.numTrueNegatives(1));
        this.evaluation.setMetadata(PredictorEvaluationOutputField.FALSE_POSITIVE, wekaEval.numFalsePositives(1));
        this.evaluation.setMetadata(PredictorEvaluationOutputField.FALSE_NEGATIVE, wekaEval.numFalseNegatives(1));

        this.evaluation.setMetadata(PredictorEvaluationOutputField.PRECISION, wekaEval.precision(1));
        this.evaluation.setMetadata(PredictorEvaluationOutputField.RECALL, wekaEval.recall(1));
        this.evaluation.setMetadata(PredictorEvaluationOutputField.ROC_AREA, wekaEval.areaUnderROC(1));
        this.evaluation.setMetadata(PredictorEvaluationOutputField.KAPPA, wekaEval.kappa());

        return this.evaluation;
    }
}
