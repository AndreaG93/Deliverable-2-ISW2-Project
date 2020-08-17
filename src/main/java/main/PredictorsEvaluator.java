package main;

import entities.FileCSV;
import entities.MetadataProvider;
import entities.project.Project;
import entities.project.Bookkeeper;
import entities.project.OpenJPA;
import entities.predictor.DefectiveClassesPredictor;
import entities.enums.PredictorEvaluationOutputField;
import predictor.options.WekaAttributeSelection;
import predictor.options.WekaClassifier;
import predictor.options.WekaFilter;
import predictor.validation.WalkForward;
import predictor.validation.WalkForwardRunInput;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PredictorsEvaluator {

    public static void main(String[] args) {

        List<Project> projectList = new ArrayList<>();

        projectList.add(new Bookkeeper());
        projectList.add(new OpenJPA());

        for (Project project : projectList) {

            List<DefectiveClassesPredictor> predictors = generatePredictors();
            List<List<MetadataProvider<PredictorEvaluationOutputField>>> evaluations = evaluatePredictors(predictors, project);

            exportEvaluationsAsCSV(evaluations, project);
        }
    }

    private static List<DefectiveClassesPredictor> generatePredictors() {

        List<DefectiveClassesPredictor> output = new ArrayList<>();

        for (WekaAttributeSelection wekaAttributeSelection : WekaAttributeSelection.values())
            for (WekaClassifier wekaClassifier : WekaClassifier.values())
                for (WekaFilter wekaFilter : WekaFilter.values())
                    output.add(new DefectiveClassesPredictor(wekaClassifier, wekaFilter, wekaAttributeSelection));

        return output;
    }

    private static List<List<MetadataProvider<PredictorEvaluationOutputField>>> evaluatePredictors(List<DefectiveClassesPredictor> predictors, Project project) {

        List<List<MetadataProvider<PredictorEvaluationOutputField>>> output = new ArrayList<>();

        for (DefectiveClassesPredictor predictor : predictors)
            output.add(getPredictorEvaluation(predictor, project));

        return output;
    }


    private static List<MetadataProvider<PredictorEvaluationOutputField>> getPredictorEvaluation(DefectiveClassesPredictor predictor, Project project) {

        List<MetadataProvider<PredictorEvaluationOutputField>> output = new ArrayList<>();
        Instances instances = getDatasetInstances(project.datasetFilename);

        WalkForward validationTechnique = new WalkForward(instances);
        List<WalkForwardRunInput> runInputs = validationTechnique.getWalkForwardRunInputs();

        for (WalkForwardRunInput input : runInputs) {

            MetadataProvider<PredictorEvaluationOutputField> evaluation = predictor.getEvaluationResults(input);

            evaluation.setMetadata(PredictorEvaluationOutputField.DATASET_NAME, project.name);
            evaluation.setMetadata(PredictorEvaluationOutputField.NUMBER_OF_TRAINING_RELEASES, input.numberOfDatasetPartUsedAsTraining);

            output.add(evaluation);
        }

        return output;
    }

    private static void exportEvaluationsAsCSV(List<List<MetadataProvider<PredictorEvaluationOutputField>>> evaluations, Project project) {

        FileCSV fileCSV = new FileCSV(project.predictorsEvaluationFilename, PredictorEvaluationOutputField.convertToStringList());

        for (List<MetadataProvider<PredictorEvaluationOutputField>> evaluation : evaluations)
            for (MetadataProvider<PredictorEvaluationOutputField> record : evaluation)
                fileCSV.write(record.getMetadataAsString(PredictorEvaluationOutputField.values()));

        fileCSV.close();
    }

    private static Instances getDatasetInstances(String path) {

        Instances output = null;

        try {

            ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(path);
            output = dataSource.getDataSet();

        } catch (Exception e) {

            Logger.getLogger(PredictorsEvaluator.class.getName()).severe(e.getMessage());
            System.exit(e.hashCode());
        }

        return output;
    }
}
