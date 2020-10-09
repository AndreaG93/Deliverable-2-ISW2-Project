package launchers;

import entities.project.Bookkeeper;
import entities.project.OpenJPA;
import entities.project.Project;
import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.unsupervised.attribute.Remove;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PredictionMaker {

    private PredictionMaker() {
    }

    public static void main(String[] args) {

        Project project = new OpenJPA();

        try {

            Instances labeled = new ConverterUtils.DataSource(project.datasetFilename).getDataSet();
            Instances unlabeled = new ConverterUtils.DataSource(project.datasetForPredictionFilename).getDataSet();

            Instances filteredLabeled = filterRemovingUselessAttribute(labeled);
            Instances filteredUnlabeled = filterRemovingUselessAttribute(unlabeled);

            labeled.setClassIndex(labeled.numAttributes() - 1);
            unlabeled.setClassIndex(unlabeled.numAttributes() - 1);

            filteredLabeled.setClassIndex(filteredLabeled.numAttributes() - 1);
            filteredUnlabeled.setClassIndex(filteredUnlabeled.numAttributes() - 1);

            makePredictions(filteredLabeled, filteredUnlabeled, unlabeled);
            exportAsCSV(unlabeled, project.name);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void makePredictions(Instances filteredLabeledSet, Instances filteredUnlabeledSet, Instances unlabeledSet) throws Exception {

        Classifier classifier = new RandomForest();

        SMOTE smote = new SMOTE();

        FilteredClassifier filteredClassifier = new FilteredClassifier();
        filteredClassifier.setFilter(smote);
        filteredClassifier.setClassifier(classifier);
        filteredClassifier.buildClassifier(filteredLabeledSet);

        for (int index = 0; index < filteredUnlabeledSet.numInstances(); index++) {

            Instance instance = filteredUnlabeledSet.instance(index);

            double label = classifier.classifyInstance(instance);
            unlabeledSet.instance(index).setClassValue(label);
        }
    }

    private static void exportAsCSV(Instances instances, String filename) throws Exception {

        BufferedWriter writer = new BufferedWriter(new FileWriter("./" + filename + ".csv"));

        Enumeration<Attribute> enumeration = instances.enumerateAttributes();
        while (enumeration.hasMoreElements())
        {
            String nameAttribute = enumeration.nextElement().name();
            writer.write(nameAttribute + ",");
        }

        writer.newLine();

        for (int index = 0; index < instances.numInstances(); index++) {

            writer.write(instances.get(index).toString());
            writer.newLine();
        }

        writer.close();
    }

    private static Instances filterRemovingUselessAttribute(Instances instances) throws Exception {

        List<Integer> myList = new ArrayList<>();

        for (int n = 0; n < instances.numAttributes(); n++) {

            if (instances.attribute(n).name().equalsIgnoreCase("NAME") && !myList.contains(n))
                myList.add(n);

            if (instances.attribute(n).name().equalsIgnoreCase("VERSION_INDEX") && !myList.contains(n))
                myList.add(n);
        }

        Remove remove = new Remove();
        remove.setAttributeIndicesArray(myList.stream().mapToInt(i -> i).toArray());
        remove.setInvertSelection(false);
        remove.setInputFormat(instances);

        return Filter.useFilter(instances, remove);
    }
}
