package predictor;

import weka.core.Instance;
import weka.core.Instances;

import java.util.Enumeration;

public class PredictorInput {

    public final Instances trainingSet;
    public final Instances testingSet;
    public final double percentageTrainingInstances;

    private final double trainingSetNumberOfInstances;
    private final double testingSetNumberOfInstances;

    public PredictorInput(Instances trainingSet, Instances testingSet) {
        this.trainingSet = trainingSet;
        this.testingSet = testingSet;

        this.trainingSetNumberOfInstances = this.trainingSet.numInstances();
        this.testingSetNumberOfInstances = this.testingSet.numInstances();

        this.percentageTrainingInstances = (this.trainingSetNumberOfInstances * 100) / (this.trainingSetNumberOfInstances + this.testingSetNumberOfInstances);
    }

    public double getPercentageOfTrainingSetInstancesWith(int attributeIndex, String expectedAttributeValue) {

        int count = this.count(this.trainingSet, attributeIndex, expectedAttributeValue);

        return (count * 100) / this.trainingSetNumberOfInstances;
    }

    public double getPercentageOfTestingSetInstancesWith(int attributeIndex, String expectedAttributeValue) {

        int count = this.count(this.testingSet, attributeIndex, expectedAttributeValue);

        return (count * 100) / this.testingSetNumberOfInstances;
    }

    private int count(Instances instances, int attributeIndex, String expectedAttributeValue) {

        int output = 0;

        Enumeration<Instance> enumeration = instances.enumerateInstances();

        while (enumeration.hasMoreElements()) {
            Instance currentInstance = enumeration.nextElement();
            String currentAttributeValue = currentInstance.stringValue(attributeIndex);

            if (currentAttributeValue.equals(expectedAttributeValue))
                output++;
        }

        return output;
    }
}
