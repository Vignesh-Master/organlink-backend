package com.organlink.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AI Model Training Service for Organ Donation Matching
 * Handles CSV dataset loading, preprocessing, and model training
 */
@Service
public class AIModelTrainingService {

    @Value("${ai.model.path}")
    private String modelPath;

    @Value("${ai.model.training-data-size}")
    private int trainingDataSize;

    private Classifier trainedModel;
    private Instances trainingDataset;

    /**
     * Load CSV dataset and convert to WEKA format
     */
    public Instances loadCSVDataset(String csvFilePath) throws Exception {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(csvFilePath));
        Instances dataset = loader.getDataSet();
        
        // Set the last attribute as class attribute (match success/failure)
        if (dataset.numAttributes() > 0) {
            dataset.setClassIndex(dataset.numAttributes() - 1);
        }
        
        return dataset;
    }

    /**
     * Create training dataset structure for organ matching
     */
    public Instances createOrganMatchingDataset() {
        // Define attributes for organ matching
        ArrayList<Attribute> attributes = new ArrayList<>();
        
        // Donor attributes
        attributes.add(new Attribute("donor_age"));
        attributes.add(new Attribute("donor_bmi"));
        attributes.add(new Attribute("donor_blood_type", Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")));
        attributes.add(new Attribute("donor_smoking", Arrays.asList("never", "former", "current")));
        attributes.add(new Attribute("donor_alcohol", Arrays.asList("none", "light", "moderate", "heavy")));
        
        // Patient attributes
        attributes.add(new Attribute("patient_age"));
        attributes.add(new Attribute("patient_bmi"));
        attributes.add(new Attribute("patient_blood_type", Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")));
        attributes.add(new Attribute("patient_urgency", Arrays.asList("LOW", "MEDIUM", "HIGH", "CRITICAL", "EMERGENCY")));
        attributes.add(new Attribute("patient_waiting_days"));
        
        // Compatibility attributes
        attributes.add(new Attribute("blood_type_compatible", Arrays.asList("yes", "no")));
        attributes.add(new Attribute("age_difference"));
        attributes.add(new Attribute("bmi_difference"));
        attributes.add(new Attribute("distance_km"));
        
        // Organ specific
        attributes.add(new Attribute("organ_type", Arrays.asList("heart", "liver", "kidney", "lung", "pancreas")));
        
        // Medical compatibility
        attributes.add(new Attribute("hla_match_score")); // 0-6 scale
        attributes.add(new Attribute("crossmatch_compatible", Arrays.asList("yes", "no")));
        
        // Outcome (class attribute)
        attributes.add(new Attribute("match_success", Arrays.asList("success", "failure")));
        
        // Create dataset
        Instances dataset = new Instances("OrganMatching", attributes, trainingDataSize);
        dataset.setClassIndex(dataset.numAttributes() - 1);
        
        return dataset;
    }

    /**
     * Generate synthetic training data (if no real data available)
     */
    public Instances generateSyntheticTrainingData() {
        Instances dataset = createOrganMatchingDataset();
        
        // Generate synthetic data points
        for (int i = 0; i < trainingDataSize; i++) {
            Instance instance = new DenseInstance(dataset.numAttributes());
            instance.setDataset(dataset);
            
            // Generate realistic synthetic data
            double donorAge = 20 + Math.random() * 50; // 20-70 years
            double patientAge = 18 + Math.random() * 60; // 18-78 years
            double donorBMI = 18 + Math.random() * 15; // 18-33 BMI
            double patientBMI = 18 + Math.random() * 15;
            
            instance.setValue(0, donorAge);
            instance.setValue(1, donorBMI);
            instance.setValue(2, getRandomBloodType());
            instance.setValue(3, getRandomSmokingStatus());
            instance.setValue(4, getRandomAlcoholStatus());
            
            instance.setValue(5, patientAge);
            instance.setValue(6, patientBMI);
            instance.setValue(7, getRandomBloodType());
            instance.setValue(8, getRandomUrgencyLevel());
            instance.setValue(9, Math.random() * 1000); // Waiting days
            
            // Calculate compatibility
            boolean bloodCompatible = isBloodTypeCompatible(
                dataset.attribute(2).value((int)instance.value(2)),
                dataset.attribute(7).value((int)instance.value(7))
            );
            instance.setValue(10, bloodCompatible ? "yes" : "no");
            instance.setValue(11, Math.abs(donorAge - patientAge));
            instance.setValue(12, Math.abs(donorBMI - patientBMI));
            instance.setValue(13, Math.random() * 1000); // Distance
            
            instance.setValue(14, getRandomOrganType());
            instance.setValue(15, Math.random() * 6); // HLA match score
            instance.setValue(16, Math.random() > 0.3 ? "yes" : "no"); // Crossmatch
            
            // Determine success based on compatibility factors
            double successProbability = calculateSuccessProbability(instance, bloodCompatible);
            instance.setValue(17, Math.random() < successProbability ? "success" : "failure");
            
            dataset.add(instance);
        }
        
        return dataset;
    }

    /**
     * Train Random Forest model
     */
    public void trainModel(Instances trainingData) throws Exception {
        // Configure Random Forest
        RandomForest rf = new RandomForest();
        rf.setNumIterations(100); // Number of trees
        rf.setMaxDepth(10); // Maximum depth
        rf.setNumFeatures(5); // Features per tree
        
        // Train the model
        rf.buildClassifier(trainingData);
        this.trainedModel = rf;
        this.trainingDataset = trainingData;
        
        // Save model to file
        saveModel();
        
        System.out.println("Model trained successfully with " + trainingData.numInstances() + " instances");
    }

    /**
     * Predict match success probability
     */
    public double predictMatchSuccess(double[] features) throws Exception {
        if (trainedModel == null) {
            loadModel();
        }
        
        Instance instance = new DenseInstance(trainingDataset.numAttributes());
        instance.setDataset(trainingDataset);
        
        // Set feature values
        for (int i = 0; i < features.length && i < trainingDataset.numAttributes() - 1; i++) {
            instance.setValue(i, features[i]);
        }
        
        // Get probability distribution
        double[] probabilities = trainedModel.distributionForInstance(instance);
        
        // Return probability of success (assuming "success" is index 1)
        return probabilities.length > 1 ? probabilities[1] : probabilities[0];
    }

    /**
     * Save trained model to file
     */
    private void saveModel() throws Exception {
        File modelDir = new File(modelPath);
        if (!modelDir.exists()) {
            modelDir.mkdirs();
        }
        
        weka.core.SerializationHelper.writeAll(
            modelPath + "/organ_matching_model.model", 
            new Object[]{trainedModel, trainingDataset}
        );
    }

    /**
     * Load trained model from file
     */
    private void loadModel() throws Exception {
        Object[] objects = weka.core.SerializationHelper.readAll(
            modelPath + "/organ_matching_model.model"
        );
        trainedModel = (Classifier) objects[0];
        trainingDataset = (Instances) objects[1];
    }

    /**
     * Process your Kaggle CSV dataset
     */
    public void processKaggleDataset(String csvFilePath) throws Exception {
        // Load CSV data
        Instances rawData = loadCSVDataset(csvFilePath);
        
        // Preprocess data (handle missing values, normalize, etc.)
        Instances processedData = preprocessData(rawData);
        
        // Train model
        trainModel(processedData);
        
        // Evaluate model
        evaluateModel(processedData);
    }

    /**
     * Preprocess CSV data
     */
    private Instances preprocessData(Instances rawData) {
        // Add preprocessing logic here:
        // - Handle missing values
        // - Normalize numerical features
        // - Encode categorical variables
        // - Feature selection
        
        return rawData; // Simplified for now
    }

    /**
     * Evaluate model performance
     */
    private void evaluateModel(Instances data) throws Exception {
        weka.classifiers.Evaluation eval = new weka.classifiers.Evaluation(data);
        eval.crossValidateModel(trainedModel, data, 10, new java.util.Random(1));
        
        System.out.println("=== Model Evaluation ===");
        System.out.println("Accuracy: " + (eval.pctCorrect()) + "%");
        System.out.println("Precision: " + eval.weightedPrecision());
        System.out.println("Recall: " + eval.weightedRecall());
        System.out.println("F-Measure: " + eval.weightedFMeasure());
    }

    // Helper methods
    private String getRandomBloodType() {
        String[] bloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        return bloodTypes[(int)(Math.random() * bloodTypes.length)];
    }

    private String getRandomSmokingStatus() {
        String[] statuses = {"never", "former", "current"};
        return statuses[(int)(Math.random() * statuses.length)];
    }

    private String getRandomAlcoholStatus() {
        String[] statuses = {"none", "light", "moderate", "heavy"};
        return statuses[(int)(Math.random() * statuses.length)];
    }

    private String getRandomUrgencyLevel() {
        String[] levels = {"LOW", "MEDIUM", "HIGH", "CRITICAL", "EMERGENCY"};
        return levels[(int)(Math.random() * levels.length)];
    }

    private String getRandomOrganType() {
        String[] organs = {"heart", "liver", "kidney", "lung", "pancreas"};
        return organs[(int)(Math.random() * organs.length)];
    }

    private boolean isBloodTypeCompatible(String donorType, String patientType) {
        // Simplified blood type compatibility
        if ("O-".equals(donorType)) return true;
        if ("AB+".equals(patientType)) return true;
        return donorType.equals(patientType);
    }

    private double calculateSuccessProbability(Instance instance, boolean bloodCompatible) {
        double probability = 0.5; // Base probability
        
        if (bloodCompatible) probability += 0.3;
        if (instance.value(11) < 10) probability += 0.1; // Age difference < 10
        if (instance.value(13) < 100) probability += 0.1; // Distance < 100km
        if (instance.value(15) > 4) probability += 0.1; // Good HLA match
        
        return Math.min(1.0, probability);
    }
}
