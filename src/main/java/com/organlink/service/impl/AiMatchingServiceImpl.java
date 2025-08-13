package com.organlink.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.organlink.entity.*;
import com.organlink.repository.*;
import com.organlink.service.AiMatchingService;
import com.organlink.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiMatchingServiceImpl implements AiMatchingService {

    @Value("${ai.datasets.path}")
    private String datasetsPath;

    @Value("${ai.model.path}")
    private String modelPath;

    @Value("${ai.matching.threshold}")
    private double matchingThreshold;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    private Classifier classifier;

    @Override
    public void trainModels() throws Exception {
        System.out.println("🤖 Starting AI model training...");
        File datasetFile = new File(datasetsPath + "Organ_Transplant.csv");
        if (!datasetFile.exists()) {
            System.err.println("Training dataset not found at: " + datasetFile.getAbsolutePath());
            return;
        }
        CSVLoader loader = new CSVLoader();
        loader.setSource(datasetFile);
        Instances data = loader.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);

        RandomForest rf = new RandomForest();
        rf.setNumIterations(100);
        rf.buildClassifier(data);
        this.classifier = rf;

        File modelDir = new File(modelPath);
        if (!modelDir.exists()) modelDir.mkdirs();
        SerializationHelper.write(modelPath + "organlink_matching.model", this.classifier);
        System.out.println("✅ AI model trained and saved successfully.");
    }

    @Override
    public List<Match> findBestMatchesForPatient(Long patientId) throws Exception {
        if (this.classifier == null) {
            try {
                this.classifier = (Classifier) SerializationHelper.read(modelPath + "organlink_matching.model");
            } catch (Exception e) {
                System.out.println("Could not load pre-trained model. Training a new one...");
                trainModels();
            }
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new Exception("Patient not found with ID: " + patientId));
        List<Donor> potentialDonors = donorRepository.findAllByOrganTypesContainingAndAvailabilityStatus(
                patient.getOrganNeeded(), AvailabilityStatus.AVAILABLE);

        List<Policy> activePolicies = policyRepository.findActivePoliciesForOrgan(patient.getOrganNeeded(), PolicyStatus.IMPLEMENTED);

        ArrayList<Attribute> attributes = createWekaAttributes();
        Instances dataUnlabeled = new Instances("TestInstances", attributes, 0);
        dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);

        List<Match> potentialMatches = new ArrayList<>();

        for (Donor donor : potentialDonors) {
            DenseInstance instance = createInstanceForMatch(patient, donor, activePolicies, dataUnlabeled);
            double matchProbability = this.classifier.distributionForInstance(instance)[1];

            if (matchProbability >= matchingThreshold) {
                Match match = new Match();
                match.setPatient(patient);
                match.setDonor(donor);
                match.setMatchScore(matchProbability);
                match.setStatus(MatchStatus.PENDING);
                match.setHospital(patient.getHospital());
                potentialMatches.add(match);
            }
        }

        potentialMatches.sort(Comparator.comparingDouble(Match::getMatchScore).reversed());
        List<Match> bestMatches = potentialMatches.stream().limit(10).collect(Collectors.toList());

        List<Match> savedMatches = matchRepository.saveAll(bestMatches);

        // --- Create Notifications ---
        for (Match match : savedMatches) {
            User hospitalUser = userRepository.findByTenantId(match.getHospital().getHospitalId())
                .orElse(null);
            if (hospitalUser != null) {
                String message = String.format("New potential match found for patient %s! Donor %s from %s.",
                    match.getPatient().getFullName(), match.getDonor().getFullName(), match.getDonor().getHospital().getHospitalName());
                notificationService.createNotification(hospitalUser, message, "/hospital/matching");
            }
        }

        return savedMatches;
    }

    private ArrayList<Attribute> createWekaAttributes() {
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("PatientAge"));
        attributes.add(new Attribute("DonorAge"));
        attributes.add(new Attribute("BloodTypeMatch"));
        attributes.add(new Attribute("UrgencyLevel"));
        attributes.add(new Attribute("WaitingTime"));
        attributes.add(new Attribute("PolicyAdjustment"));
        ArrayList<String> classValues = new ArrayList<>();
        classValues.add("0");
        classValues.add("1");
        attributes.add(new Attribute("class", classValues));
        return attributes;
    }

    private DenseInstance createInstanceForMatch(Patient patient, Donor donor, List<Policy> policies, Instances dataset) {
        DenseInstance instance = new DenseInstance(dataset.numAttributes());
        instance.setDataset(dataset);

        instance.setValue(0, patient.getAge());
        instance.setValue(1, donor.getAge());
        instance.setValue(2, patient.getBloodType().equals(donor.getBloodType()) ? 1 : 0);
        instance.setValue(3, patient.getUrgencyLevel().ordinal());
        instance.setValue(4, patient.getWaitingTime());

        double policyAdjustment = 0.0;
        for (Policy policy : policies) {
            try {
                Map<String, Object> rules = new ObjectMapper().readValue(policy.getPolicyData(), new TypeReference<>() {});
                if (rules.containsKey("age_priority") && patient.getAge() < (Integer)rules.get("age_priority")) {
                    policyAdjustment += 10;
                }
                if (rules.containsKey("location_bonus") && patient.getCity().equalsIgnoreCase((String)rules.get("location_bonus"))) {
                    policyAdjustment += 5;
                }
            } catch (Exception e) {
                System.err.println("Failed to parse policy data: " + e.getMessage());
            }
        }
        instance.setValue(5, policyAdjustment);

        return instance;
    }
}
