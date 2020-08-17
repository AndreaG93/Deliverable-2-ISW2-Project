package entities.predictor;

import entities.MetadataProvider;
import entities.enums.PredictorEvaluationOutputField;
import predictor.Predictor;
import predictor.PredictorInput;
import predictor.options.WekaAttributeSelection;
import predictor.options.WekaClassifier;
import predictor.options.WekaFilter;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;

import java.util.logging.Logger;

public class DefectiveClassesPredictor extends Predictor<PredictorEvaluationOutputField> {

    public DefectiveClassesPredictor(WekaClassifier wekaClassifier, WekaFilter wekaFilter, WekaAttributeSelection wekaAttributeSelection) {
        super(wekaClassifier, wekaFilter, wekaAttributeSelection);
    }

    @Override
    public MetadataProvider<PredictorEvaluationOutputField> getEvaluationResults(PredictorInput input) {

        MetadataProvider<PredictorEvaluationOutputField> output = new MetadataProvider<>();

        Instances testingSet;
        Instances trainingSet;

        try {

            testingSet = input.testingSet;

            if (this.attributeSelection != null) {

                this.attributeSelection.setInputFormat(input.trainingSet);
                trainingSet = Filter.useFilter(input.trainingSet, this.attributeSelection);

            } else
                trainingSet = input.trainingSet;

            int evaluationClassIndex = trainingSet.numAttributes() - 1;

            trainingSet.setClassIndex(evaluationClassIndex);
            testingSet.setClassIndex(evaluationClassIndex);

            this.classifier.buildClassifier(trainingSet);

            Evaluation evaluation = new Evaluation(testingSet);

            output.setMetadata(PredictorEvaluationOutputField.CLASSIFIER, this.wekaClassifier);
            output.setMetadata(PredictorEvaluationOutputField.BALANCING, this.wekaFilter);
            output.setMetadata(PredictorEvaluationOutputField.FEATURE_SELECTION, this.wekaAttributeSelection);

            output.setMetadata(PredictorEvaluationOutputField.PERCENTAGE_TRAINING, input.percentageTrainingInstances);
            output.setMetadata(PredictorEvaluationOutputField.PERCENTAGE_DEFECTIVE_TRAINING, input.getPercentageOfTrainingSetInstancesWith(evaluationClassIndex, "true"));
            output.setMetadata(PredictorEvaluationOutputField.PERCENTAGE_DEFECTIVE_TESTING, input.getPercentageOfTestingSetInstancesWith(evaluationClassIndex, "true"));

            output.setMetadata(PredictorEvaluationOutputField.TRUE_POSITIVE, evaluation.numTruePositives(evaluationClassIndex));
            output.setMetadata(PredictorEvaluationOutputField.TRUE_NEGATIVE, evaluation.numTruePositives(evaluationClassIndex));
            output.setMetadata(PredictorEvaluationOutputField.FALSE_POSITIVE, evaluation.falsePositiveRate(evaluationClassIndex));
            output.setMetadata(PredictorEvaluationOutputField.FALSE_NEGATIVE, evaluation.falseNegativeRate(evaluationClassIndex));

            output.setMetadata(PredictorEvaluationOutputField.PRECISION, evaluation.precision(evaluationClassIndex));
            output.setMetadata(PredictorEvaluationOutputField.RECALL, evaluation.recall(evaluationClassIndex));
            output.setMetadata(PredictorEvaluationOutputField.ROC_AREA, evaluation.areaUnderROC(evaluationClassIndex));
            output.setMetadata(PredictorEvaluationOutputField.KAPPA, evaluation.kappa());

        } catch (Exception e) {

            Logger.getLogger(Predictor.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());
        }

        return output;
    }
}
