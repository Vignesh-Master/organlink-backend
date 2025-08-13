package com.organlink.service;

import com.organlink.entity.Donor;
import com.organlink.entity.Patient;
import com.organlink.entity.Match;

import java.util.List;

public interface AiMatchingService {
    List<Match> findBestMatchesForPatient(Long patientId) throws Exception;
    void trainModels() throws Exception;
}
