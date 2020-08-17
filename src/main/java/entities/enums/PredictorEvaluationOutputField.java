package entities.enums;

import java.util.ArrayList;
import java.util.List;

public enum PredictorEvaluationOutputField {

    DATASET_NAME,
    NUMBER_OF_TRAINING_RELEASES,

    PERCENTAGE_TRAINING,
    PERCENTAGE_DEFECTIVE_TRAINING,
    PERCENTAGE_DEFECTIVE_TESTING,

    CLASSIFIER,
    BALANCING,
    FEATURE_SELECTION,

    TRUE_POSITIVE,
    FALSE_POSITIVE,
    TRUE_NEGATIVE,
    FALSE_NEGATIVE,

    PRECISION,
    RECALL,
    ROC_AREA,
    KAPPA;

    public static List<String> convertToStringList() {

        List<String> output = new ArrayList<>();

        for (PredictorEvaluationOutputField x : PredictorEvaluationOutputField.values())
            output.add(x.name());

        return output;
    }
}
