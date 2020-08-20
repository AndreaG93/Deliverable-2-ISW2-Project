package predictor.validation;

import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class WalkForward {

    private final Instances[] parts;
    private final int numOfAllInstance;

    public WalkForward(Instances allInstancesOfDataset) {

        this.numOfAllInstance = allInstancesOfDataset.numInstances();

        int datasetSubSetsAmount = allInstancesOfDataset.numDistinctValues(0);

        this.parts = new Instances[datasetSubSetsAmount];

        int[] datasetSubSetsCardinality = new int[datasetSubSetsAmount];

        for (int index = 0; index < allInstancesOfDataset.numInstances(); index++) {

            Instance instance = allInstancesOfDataset.instance(index);
            int releaseIndex = Integer.parseInt(instance.toString(0));

            datasetSubSetsCardinality[releaseIndex]++;
        }

        int firstIndex = 0;
        for (int index = 0; index < datasetSubSetsAmount; index++) {

            this.parts[index] = new Instances(allInstancesOfDataset, firstIndex, datasetSubSetsCardinality[index]);

            firstIndex += datasetSubSetsCardinality[index];
        }
    }

    private Instances getDataSetSubsetForTraining(int runIndex) {

        Instances output = new Instances(parts[0]);

        for (int index = 1; index <= runIndex; index++) {

            Enumeration<Instance> enumeration = parts[index].enumerateInstances();
            while (enumeration.hasMoreElements())
                output.add(enumeration.nextElement());

        }

        return output;
    }

    private Instances getDataSetSubsetForTesting(int runIndex) {
        return new Instances(parts[runIndex + 1]);
    }


    public List<WalkForwardRunInput> getWalkForwardRunInputs() {

        List<WalkForwardRunInput> output = new ArrayList<>();

        for (int runIndex = 0; runIndex < (parts.length - 1); runIndex++) {

            Instances currentTrainingSet = getDataSetSubsetForTraining(runIndex);
            Instances currentTestingSet = getDataSetSubsetForTesting(runIndex);

            output.add(new WalkForwardRunInput(currentTrainingSet, currentTestingSet, runIndex + 1, this.numOfAllInstance));
        }

        return output;
    }
}