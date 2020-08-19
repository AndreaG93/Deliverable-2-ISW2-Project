package main;

import entities.project.Bookkeeper;
import entities.project.Project;
import predictor.Predictor;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

public class PredictionMaker {

    public static void main(String[] args) {

        Project project = new Bookkeeper();

        try {

            Classifier classifier = (Classifier) SerializationHelper.read(Predictor.predictorsDirectory + "RANDOM_FOREST-NO_SAMPLING-NO_SELECTION");

            Instances source = new ConverterUtils.DataSource(project.datasetForPredictionFilename).getDataSet();;
            source.setClassIndex(source.numAttributes() - 1);

            double label = classifier.classifyInstance(source.instance(0));
            source.instance(0).setClassValue(label);

            System.out.println(source.instance(0).stringValue(4));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
