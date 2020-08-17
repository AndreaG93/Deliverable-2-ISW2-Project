package predictor.validation;

import predictor.PredictorInput;
import weka.core.Instances;

public class WalkForwardRunInput extends PredictorInput {

    public final int numberOfDatasetPartUsedAsTraining;

    public WalkForwardRunInput(Instances trainingSet, Instances testingSet, int numberOfDatasetPartUsedAsTraining, int numOfAllInstance) {
        super(trainingSet, testingSet, numOfAllInstance);
        this.numberOfDatasetPartUsedAsTraining = numberOfDatasetPartUsedAsTraining;
    }
}
