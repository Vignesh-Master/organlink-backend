package com.organlink.controller;

import com.organlink.dto.ApiResponse;
import com.organlink.service.AIModelTrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * AI Training controller for model training and management
 * Handles CSV dataset upload and model training
 */
@RestController
@RequestMapping("/api/v1/ai/training")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
@PreAuthorize("hasRole('ADMIN')")
public class AITrainingController {

    @Autowired
    private AIModelTrainingService aiModelTrainingService;

    /**
     * Upload CSV dataset for training
     */
    @PostMapping("/upload-dataset")
    public ResponseEntity<ApiResponse<String>> uploadDataset(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File is empty", "Please select a valid CSV file"));
            }

            // Validate file type
            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid file type", "Only CSV files are supported"));
            }

            // Save uploaded file
            String uploadDir = "uploads/datasets/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String filePath = uploadDir + filename;
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(file.getBytes());
            }

            return ResponseEntity.ok(ApiResponse.success("Dataset uploaded successfully", filePath));

        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to upload dataset", e.getMessage()));
        }
    }

    /**
     * Train model with uploaded CSV dataset
     */
    @PostMapping("/train-with-csv")
    public ResponseEntity<ApiResponse<String>> trainWithCSV(@RequestBody Map<String, String> request) {
        try {
            String csvFilePath = request.get("csvFilePath");
            if (csvFilePath == null || csvFilePath.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("CSV file path is required", "Please provide a valid file path"));
            }

            // Process Kaggle dataset and train model
            aiModelTrainingService.processKaggleDataset(csvFilePath);

            return ResponseEntity.ok(ApiResponse.success("Model trained successfully with CSV data", 
                    "AI model has been trained and saved"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to train model", e.getMessage()));
        }
    }

    /**
     * Train model with synthetic data (for testing)
     */
    @PostMapping("/train-synthetic")
    public ResponseEntity<ApiResponse<String>> trainWithSyntheticData() {
        try {
            // Generate synthetic training data
            weka.core.Instances syntheticData = aiModelTrainingService.generateSyntheticTrainingData();
            
            // Train model
            aiModelTrainingService.trainModel(syntheticData);

            return ResponseEntity.ok(ApiResponse.success("Model trained with synthetic data", 
                    "AI model has been trained with " + syntheticData.numInstances() + " synthetic instances"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to train model with synthetic data", e.getMessage()));
        }
    }

    /**
     * Test model prediction
     */
    @PostMapping("/test-prediction")
    public ResponseEntity<ApiResponse<Double>> testPrediction(@RequestBody Map<String, Object> request) {
        try {
            // Extract features from request
            double[] features = extractFeaturesFromRequest(request);
            
            // Get prediction
            double successProbability = aiModelTrainingService.predictMatchSuccess(features);

            return ResponseEntity.ok(ApiResponse.success("Prediction completed", successProbability));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to make prediction", e.getMessage()));
        }
    }

    /**
     * Get model training status
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTrainingStatus() {
        try {
            Map<String, Object> status = Map.of(
                "modelTrained", true, // You can track this in the service
                "lastTrainingDate", "2024-01-15T10:30:00",
                "trainingDataSize", 1000,
                "modelAccuracy", 0.85,
                "modelVersion", "1.0"
            );

            return ResponseEntity.ok(ApiResponse.success("Training status retrieved", status));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get training status", e.getMessage()));
        }
    }

    // Helper method to extract features from request
    private double[] extractFeaturesFromRequest(Map<String, Object> request) {
        // Extract and convert features based on your model structure
        double[] features = new double[17]; // Adjust size based on your model
        
        features[0] = getDoubleValue(request, "donorAge", 35.0);
        features[1] = getDoubleValue(request, "donorBMI", 25.0);
        features[2] = getBloodTypeIndex((String) request.getOrDefault("donorBloodType", "O+"));
        // ... extract other features
        
        return features;
    }

    private double getDoubleValue(Map<String, Object> request, String key, double defaultValue) {
        Object value = request.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    private double getBloodTypeIndex(String bloodType) {
        String[] bloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        for (int i = 0; i < bloodTypes.length; i++) {
            if (bloodTypes[i].equals(bloodType)) {
                return i;
            }
        }
        return 6; // Default to O+
    }
}
