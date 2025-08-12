package com.organlink.repository;

import com.organlink.entity.Match;
import com.organlink.entity.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Match entity
 */
@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    
    Optional<Match> findByMatchId(String matchId);
    
    List<Match> findByStatus(MatchStatus status);
    
    List<Match> findByDonorId(Long donorId);
    
    List<Match> findByPatientId(Long patientId);
    
    @Query("SELECT m FROM Match m WHERE m.donor.hospital.id = :hospitalId OR m.patient.hospital.id = :hospitalId")
    List<Match> findByHospitalId(@Param("hospitalId") Long hospitalId);
    
    @Query("SELECT COUNT(m) FROM Match m WHERE m.donor.hospital.id = :hospitalId OR m.patient.hospital.id = :hospitalId")
    long countByHospitalId(@Param("hospitalId") Long hospitalId);
    
    @Query("SELECT COUNT(m) FROM Match m WHERE (m.donor.hospital.id = :hospitalId OR m.patient.hospital.id = :hospitalId) AND m.status = 'COMPLETED'")
    long countCompletedByHospitalId(@Param("hospitalId") Long hospitalId);
    
    @Query("SELECT m FROM Match m WHERE m.status = 'PENDING' ORDER BY m.finalScore DESC")
    List<Match> findPendingMatchesOrderedByScore();
    
    @Query("SELECT m FROM Match m WHERE m.patient.id = :patientId ORDER BY m.finalScore DESC")
    List<Match> findByPatientIdOrderedByScore(@Param("patientId") Long patientId);
    
    @Query("SELECT m FROM Match m WHERE m.donor.id = :donorId ORDER BY m.finalScore DESC")
    List<Match> findByDonorIdOrderedByScore(@Param("donorId") Long donorId);
    
    @Query("SELECT COUNT(m) FROM Match m WHERE m.status = :status")
    long countByStatus(@Param("status") MatchStatus status);
    
    @Query("SELECT m FROM Match m WHERE m.expiryDate < CURRENT_TIMESTAMP AND m.status = 'PENDING'")
    List<Match> findExpiredMatches();
    
    @Query("SELECT m FROM Match m WHERE m.donor.hospital.hospitalId = :hospitalId OR m.patient.hospital.hospitalId = :hospitalId")
    Page<Match> findByHospitalHospitalId(@Param("hospitalId") String hospitalId, Pageable pageable);
}
