package com.organlink.repository;

import com.organlink.entity.Patient;
import com.organlink.entity.PatientStatus;
import com.organlink.entity.UrgencyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Patient entity
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    Optional<Patient> findByPatientId(String patientId);
    
    Optional<Patient> findByEmail(String email);
    
    boolean existsByPatientId(String patientId);
    
    boolean existsByEmail(String email);
    
    List<Patient> findByStatus(PatientStatus status);
    
    List<Patient> findByUrgencyLevel(UrgencyLevel urgencyLevel);
    
    List<Patient> findByBloodType(String bloodType);
    
    List<Patient> findByOrganNeeded(String organNeeded);
    
    List<Patient> findByHospitalId(Long hospitalId);
    
    @Query("SELECT p FROM Patient p WHERE p.hospital.hospitalId = :hospitalId")
    Page<Patient> findByHospitalHospitalId(@Param("hospitalId") String hospitalId, Pageable pageable);
    
    @Query("SELECT p FROM Patient p WHERE p.firstName LIKE %:name% OR p.lastName LIKE %:name%")
    Page<Patient> findByNameContaining(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT p FROM Patient p WHERE p.status = 'WAITING' ORDER BY p.urgencyLevel DESC, p.waitingListDate ASC")
    List<Patient> findWaitingPatientsOrderedByUrgency();
    
    @Query("SELECT p FROM Patient p WHERE p.organNeeded = :organType AND p.bloodType = :bloodType AND p.status = 'WAITING'")
    List<Patient> findWaitingPatientsByOrganTypeAndBloodType(@Param("organType") String organType, @Param("bloodType") String bloodType);
    
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.hospital.id = :hospitalId")
    long countByHospitalId(@Param("hospitalId") Long hospitalId);
    
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.status = :status")
    long countByStatus(@Param("status") PatientStatus status);
    
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.urgencyLevel = :urgencyLevel")
    long countByUrgencyLevel(@Param("urgencyLevel") UrgencyLevel urgencyLevel);
    
    @Query("SELECT p FROM Patient p WHERE p.city = :city AND p.state = :state")
    List<Patient> findByCityAndState(@Param("city") String city, @Param("state") String state);
    
    @Query("SELECT p FROM Patient p WHERE p.status = 'WAITING' AND p.urgencyLevel IN ('CRITICAL', 'EMERGENCY')")
    List<Patient> findCriticalPatients();
    
    List<Patient> findByHospitalHospitalIdAndStatus(String hospitalId, PatientStatus status);
}
