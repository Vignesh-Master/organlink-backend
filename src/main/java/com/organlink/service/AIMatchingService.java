package com.organlink.service;

import com.organlink.entity.*;
import com.organlink.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI-powered matching service for donor-patient matching
 * Uses multiple criteria and policy-weighted scoring
 */
@Service
@Transactional
public class AIMatchingService {

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PolicyRepository policyRepository;

    /**
     * Find matches for a patient using AI algorithms
     */
    public List<Match> findMatchesForPatient(Long patientId) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (patientOpt.isEmpty()) {
            throw new RuntimeException("Patient not found: " + patientId);
        }

        Patient patient = patientOpt.get();
        
        // Get available donors for the required organ type
        List<Donor> availableDonors = donorRepository.findAvailableDonorsByOrganTypeAndBloodType(
            patient.getOrganNeeded(), patient.getBloodType());

        // Get active policies for scoring weights
        List<Policy> activePolicies = policyRepository.findImplementedPoliciesByOrganType(patient.getOrganNeeded());

        List<Match> matches = new ArrayList<>();

        for (Donor donor : availableDonors) {
            // Skip if donor is from same hospital (conflict of interest)
            if (donor.getHospital().getId().equals(patient.getHospital().getId())) {
                continue;
            }

            Match match = createMatch(donor, patient, activePolicies);
            if (match.getFinalScore() >= 0.75) { // Minimum threshold
                matches.add(match);
            }
        }

        // Sort by final score (highest first)
        matches.sort((m1, m2) -> Double.compare(m2.getFinalScore(), m1.getFinalScore()));

        // Limit to top 10 matches
        return matches.stream().limit(10).collect(Collectors.toList());
    }

    /**
     * Create a match with comprehensive scoring
     */
    private Match createMatch(Donor donor, Patient patient, List<Policy> activePolicies) {
        Match match = new Match();
        match.setMatchId(generateMatchId());
        match.setDonor(donor);
        match.setPatient(patient);
        match.setStatus(MatchStatus.PENDING);
        match.setExpiryDate(LocalDateTime.now().plusHours(24)); // 24-hour expiry
        match.setAiModelVersion("1.0");
        match.setMatchingAlgorithm("Multi-Criteria-Policy-Weighted");

        // Calculate compatibility score
        double compatibilityScore = calculateCompatibilityScore(donor, patient);
        match.setCompatibilityScore(compatibilityScore);

        // Calculate distance factor
        double distanceKm = calculateDistance(donor, patient);
        match.setDistanceKm(distanceKm);

        // Calculate urgency factor
        double urgencyFactor = calculateUrgencyFactor(patient);
        match.setUrgencyFactor(urgencyFactor);

        // Apply policy weights
        double policyWeightedScore = applyPolicyWeights(donor, patient, activePolicies);
        match.setPolicyWeightedScore(policyWeightedScore);

        // Calculate final score
        double finalScore = calculateFinalScore(compatibilityScore, distanceKm, urgencyFactor, policyWeightedScore);
        match.setFinalScore(finalScore);

        // Create detailed match information
        Map<String, Object> matchDetails = createMatchDetails(donor, patient, compatibilityScore, distanceKm, urgencyFactor, policyWeightedScore);
        match.setMatchDetails(matchDetails.toString());

        return matchRepository.save(match);
    }

    /**
     * Calculate biological compatibility score
     */
    private double calculateCompatibilityScore(Donor donor, Patient patient) {
        double score = 0.0;

        // Blood type compatibility (40% weight)
        if (isBloodTypeCompatible(donor.getBloodType(), patient.getBloodType())) {
            score += 0.4;
        }

        // Age compatibility (20% weight)
        int donorAge = donor.getAge();
        int patientAge = patient.getAge();
        double ageDiff = Math.abs(donorAge - patientAge);
        double ageScore = Math.max(0, 1 - (ageDiff / 50.0)); // Penalty for large age differences
        score += ageScore * 0.2;

        // Size compatibility (BMI) (15% weight)
        if (donor.getBmi() != null && patient.getBmi() != null) {
            double bmiDiff = Math.abs(donor.getBmi() - patient.getBmi());
            double bmiScore = Math.max(0, 1 - (bmiDiff / 10.0));
            score += bmiScore * 0.15;
        } else {
            score += 0.075; // Half points if BMI not available
        }

        // Medical history compatibility (15% weight)
        double medicalScore = calculateMedicalCompatibility(donor, patient);
        score += medicalScore * 0.15;

        // Lifestyle factors (10% weight)
        double lifestyleScore = calculateLifestyleCompatibility(donor, patient);
        score += lifestyleScore * 0.1;

        return Math.min(1.0, score);
    }

    /**
     * Check blood type compatibility
     */
    private boolean isBloodTypeCompatible(String donorBloodType, String patientBloodType) {
        // Universal donor/recipient rules
        if ("O-".equals(donorBloodType)) return true; // Universal donor
        if ("AB+".equals(patientBloodType)) return true; // Universal recipient

        // Exact match
        if (donorBloodType.equals(patientBloodType)) return true;

        // ABO compatibility
        String donorABO = donorBloodType.substring(0, donorBloodType.length() - 1);
        String patientABO = patientBloodType.substring(0, patientBloodType.length() - 1);
        String donorRh = donorBloodType.substring(donorBloodType.length() - 1);
        String patientRh = patientBloodType.substring(patientBloodType.length() - 1);

        // ABO compatibility rules
        boolean aboCompatible = switch (patientABO) {
            case "A" -> "A".equals(donorABO) || "O".equals(donorABO);
            case "B" -> "B".equals(donorABO) || "O".equals(donorABO);
            case "AB" -> true; // Can receive from any ABO
            case "O" -> "O".equals(donorABO);
            default -> false;
        };

        // Rh compatibility (Rh- can only receive from Rh-)
        boolean rhCompatible = "+".equals(patientRh) || "-".equals(donorRh);

        return aboCompatible && rhCompatible;
    }

    /**
     * Calculate distance between donor and patient locations
     */
    private double calculateDistance(Donor donor, Patient patient) {
        // Simplified distance calculation based on city/state
        // In production, this would use actual GPS coordinates
        
        if (donor.getCity().equals(patient.getCity()) && donor.getState().equals(patient.getState())) {
            return 10.0; // Same city
        } else if (donor.getState().equals(patient.getState())) {
            return 150.0; // Same state
        } else if (donor.getCountry().equals(patient.getCountry())) {
            return 500.0; // Same country
        } else {
            return 2000.0; // Different country
        }
    }

    /**
     * Calculate urgency factor based on patient condition
     */
    private double calculateUrgencyFactor(Patient patient) {
        double urgencyScore = switch (patient.getUrgencyLevel()) {
            case EMERGENCY -> 1.0;
            case CRITICAL -> 0.8;
            case HIGH -> 0.6;
            case MEDIUM -> 0.4;
            case LOW -> 0.2;
        };

        // Add waiting time factor
        if (patient.getWaitingListDate() != null) {
            long daysWaiting = java.time.temporal.ChronoUnit.DAYS.between(
                patient.getWaitingListDate(), java.time.LocalDate.now());
            double waitingBonus = Math.min(0.3, daysWaiting * 0.001); // Max 30% bonus
            urgencyScore += waitingBonus;
        }

        return Math.min(1.0, urgencyScore);
    }

    /**
     * Apply policy weights to the match
     */
    private double applyPolicyWeights(Donor donor, Patient patient, List<Policy> activePolicies) {
        double policyScore = 0.5; // Base score

        for (Policy policy : activePolicies) {
            // Apply policy-specific scoring logic
            // This would be implemented based on actual policy rules
            policyScore += 0.1; // Simplified for now
        }

        return Math.min(1.0, policyScore);
    }

    /**
     * Calculate final weighted score
     */
    private double calculateFinalScore(double compatibility, double distance, double urgency, double policyWeight) {
        // Distance penalty (closer is better)
        double distanceFactor = Math.max(0.1, 1.0 - (distance / 1000.0));

        // Weighted combination
        return (compatibility * 0.4) + (distanceFactor * 0.2) + (urgency * 0.3) + (policyWeight * 0.1);
    }

    /**
     * Calculate medical compatibility
     */
    private double calculateMedicalCompatibility(Donor donor, Patient patient) {
        // Simplified medical compatibility check
        // In production, this would analyze medical histories, allergies, etc.
        return 0.8; // Default good compatibility
    }

    /**
     * Calculate lifestyle compatibility
     */
    private double calculateLifestyleCompatibility(Donor donor, Patient patient) {
        // Simplified lifestyle compatibility
        // Consider smoking, alcohol, exercise habits
        return 0.7; // Default moderate compatibility
    }

    /**
     * Create detailed match information
     */
    private Map<String, Object> createMatchDetails(Donor donor, Patient patient, 
                                                 double compatibility, double distance, 
                                                 double urgency, double policyWeight) {
        Map<String, Object> details = new HashMap<>();
        details.put("donorAge", donor.getAge());
        details.put("patientAge", patient.getAge());
        details.put("bloodTypeMatch", isBloodTypeCompatible(donor.getBloodType(), patient.getBloodType()));
        details.put("distanceKm", distance);
        details.put("compatibilityScore", compatibility);
        details.put("urgencyFactor", urgency);
        details.put("policyWeightedScore", policyWeight);
        details.put("calculatedAt", LocalDateTime.now().toString());
        return details;
    }

    /**
     * Generate unique match ID
     */
    private String generateMatchId() {
        return "MATCH-" + System.currentTimeMillis() + "-" + 
               String.format("%04d", new Random().nextInt(10000));
    }

    /**
     * Get match by ID
     */
    public Optional<Match> getMatchById(String matchId) {
        return matchRepository.findByMatchId(matchId);
    }

    /**
     * Accept a match
     */
    public Match acceptMatch(String matchId) {
        Optional<Match> matchOpt = matchRepository.findByMatchId(matchId);
        if (matchOpt.isEmpty()) {
            throw new RuntimeException("Match not found: " + matchId);
        }

        Match match = matchOpt.get();
        match.setStatus(MatchStatus.ACCEPTED);
        match.setAcceptedDate(LocalDateTime.now());

        // Update donor and patient availability
        match.getDonor().setAvailabilityStatus(AvailabilityStatus.MATCHED);
        match.getPatient().setStatus(PatientStatus.MATCHED);

        return matchRepository.save(match);
    }

    /**
     * Reject a match
     */
    public Match rejectMatch(String matchId, String reason) {
        Optional<Match> matchOpt = matchRepository.findByMatchId(matchId);
        if (matchOpt.isEmpty()) {
            throw new RuntimeException("Match not found: " + matchId);
        }

        Match match = matchOpt.get();
        match.setStatus(MatchStatus.REJECTED);
        match.setRejectedDate(LocalDateTime.now());
        match.setRejectionReason(reason);

        return matchRepository.save(match);
    }
}
