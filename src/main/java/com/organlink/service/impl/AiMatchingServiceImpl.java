package com.organlink.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.organlink.entity.*;
import com.organlink.repository.*;
import com.organlink.service.AIMatchingService;
import com.organlink.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.*;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Matching Service Implementation using Weka
 */
@Service
public class AiMatchingServiceImpl implements AIMatchingService {

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

        // --- Create Cross-Hospital Notifications ---
        for (Match match : savedMatches) {
            // Notify the requesting hospital (where patient is)
            User patientHospitalUser = userRepository.findByTenantId(match.getPatient().getHospital().getHospitalId())
                .orElse(null);
            if (patientHospitalUser != null) {
                String patientHospitalMessage = String.format("🎯 Match found for patient %s! Donor %s from %s hospital (Score: %.2f).",
                    match.getPatient().getFullName(), 
                    match.getDonor().getFullName(), 
                    match.getDonor().getHospital().getHospitalName(),
                    match.getMatchScore());
                notificationService.createNotification(patientHospitalUser, patientHospitalMessage, "/hospital/ai-matching");
            }
            
            // Notify the donor hospital (cross-hospital notification)
            if (!match.getPatient().getHospital().getId().equals(match.getDonor().getHospital().getId())) {
                User donorHospitalUser = userRepository.findByTenantId(match.getDonor().getHospital().getHospitalId())
                    .orElse(null);
                if (donorHospitalUser != null) {
                    String donorHospitalMessage = String.format("🏥 Your donor %s has a potential match! Patient %s from %s needs %s (Score: %.2f).",
                        match.getDonor().getFullName(),
                        match.getPatient().getFullName(),
                        match.getPatient().getHospital().getHospitalName(),
                        match.getPatient().getOrganNeeded(),
                        match.getMatchScore());
                    notificationService.createNotification(donorHospitalUser, donorHospitalMessage, "/hospital/ai-matching");
                }
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

    @Override
    public List<Match> findMatchesForPatient(Long patientId) throws Exception {
        // This can be an alias for findBestMatchesForPatient
        return findBestMatchesForPatient(patientId);
    }

    @Override
    public Optional<Match> getMatchById(String matchId) throws Exception {
        try {
            Long id = Long.parseLong(matchId);
            return matchRepository.findById(id);
        } catch (NumberFormatException e) {
            throw new Exception("Invalid match ID format: " + matchId);
        }
    }

    @Override
    public Match acceptMatch(String matchId) throws Exception {
        Optional<Match> matchOpt = getMatchById(matchId);
        if (matchOpt.isEmpty()) {
            throw new Exception("Match not found: " + matchId);
        }
        
        Match match = matchOpt.get();
        match.setStatus(MatchStatus.ACCEPTED);
        
        // Update donor availability
        Donor donor = match.getDonor();
        donor.setAvailabilityStatus(AvailabilityStatus.MATCHED);
        donorRepository.save(donor);
        
        // Update patient status
        Patient patient = match.getPatient();
        patient.setStatus(PatientStatus.MATCHED);
        patientRepository.save(patient);
        
        return matchRepository.save(match);
    }

    @Override
    public Match rejectMatch(String matchId, String reason) throws Exception {
        Optional<Match> matchOpt = getMatchById(matchId);
        if (matchOpt.isEmpty()) {
            throw new Exception("Match not found: " + matchId);
        }
        
        Match match = matchOpt.get();
        match.setStatus(MatchStatus.REJECTED);
        // Note: Rejection reason would need to be stored in a separate entity or field
        
        return matchRepository.save(match);
    }
    
    @Override
    public List<Match> getMatchesForHospital(String hospitalId) throws Exception {
        // Get matches where either patient or donor belongs to this hospital
        return matchRepository.findMatchesForHospital(hospitalId);
    }
    
    @Override
    public int triggerMatchingForHospital(String hospitalId) throws Exception {
        // Get all waiting patients for this hospital
        List<Patient> waitingPatients = patientRepository.findByHospitalHospitalIdAndStatus(hospitalId, PatientStatus.WAITING);
        
        int totalMatches = 0;
        
        for (Patient patient : waitingPatients) {
            try {
                List<Match> matches = findBestMatchesForPatient(patient.getId());
                totalMatches += matches.size();
            } catch (Exception e) {
                System.err.println("Failed to find matches for patient " + patient.getId() + ": " + e.getMessage());
            }
        }
        
        return totalMatches;
    }
}
