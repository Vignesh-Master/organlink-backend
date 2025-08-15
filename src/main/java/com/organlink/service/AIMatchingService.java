package com.organlink.service;

import com.organlink.entity.Match;

import java.util.List;
import java.util.Optional;

public interface AIMatchingService {
    List<Match> findBestMatchesForPatient(Long patientId) throws Exception;
    void trainModels() throws Exception;
    
    // Additional methods for AIMatchingController
    List<Match> findMatchesForPatient(Long patientId) throws Exception;
    Optional<Match> getMatchById(String matchId) throws Exception;
    Match acceptMatch(String matchId) throws Exception;
    Match rejectMatch(String matchId, String reason) throws Exception;
    
    // Cross-hospital matching methods
    List<Match> getMatchesForHospital(String hospitalId) throws Exception;
    int triggerMatchingForHospital(String hospitalId) throws Exception;
}
