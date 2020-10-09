package launchers;

import entities.project.Bookkeeper;
import entities.project.OpenJPA;
import entities.project.Project;
import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.unsupervised.attribute.Remove;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class PredictionMaker {

    public static void main(String[] args) {

        Project project = new Bookkeeper();

        try {

            Instances labeled = new ConverterUtils.DataSource(project.datasetFilename).getDataSet();
            Instances unlabeled = new ConverterUtils.DataSource(project.datasetForPredictionFilename).getDataSet();

            Instances filteredLabeled = filterRemovingUselessAttribute(labeled);
            Instances filteredUnlabeled = filterRemovingUselessAttribute(unlabeled);

            labeled.setClassIndex(labeled.numAttributes() - 1);
            unlabeled.setClassIndex(unlabeled.numAttributes() - 1);

            filteredLabeled.setClassIndex(filteredLabeled.numAttributes() - 1);
            filteredUnlabeled.setClassIndex(filteredUnlabeled.numAttributes() - 1);

            Classifier classifier = new RandomForest();

            SMOTE smote = new SMOTE();
            FilteredClassifier fc = new FilteredClassifier();
            fc.setFilter(smote);
            fc.setClassifier(classifier);
            fc.buildClassifier(filteredLabeled);


            for (int index = 0; index < filteredUnlabeled.numInstances(); index++) {

                Instance instance = filteredUnlabeled.instance(index);

                double label = fc.classifyInstance(instance);
                unlabeled.instance(index).setClassValue(label);
            }


            BufferedWriter writer = new BufferedWriter(new FileWriter("./out.csv"));

            for (int index = 0; index < unlabeled.numInstances(); index++) {

                writer.write(unlabeled.get(index).toString());
                writer.newLine();
            }

            writer.flush();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Instances filterRemovingUselessAttribute(Instances instances) throws Exception {

        List<Integer> myList = new ArrayList<>();

        for (int n = 0; n < instances.numAttributes(); n++)
            if (instances.attribute(n).name().equalsIgnoreCase("NAME") && !myList.contains(n))
                myList.add(n);

        Remove remove = new Remove();
        remove.setAttributeIndicesArray(myList.stream().mapToInt(i -> i).toArray());
        remove.setInvertSelection(false);
        remove.setInputFormat(instances);

        return Filter.useFilter(instances, remove);
    }
}
