package com.organlink.repository;

import com.organlink.entity.Hospital;
import com.organlink.entity.HospitalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Hospital entity
 */
@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    
    Optional<Hospital> findByHospitalId(String hospitalId);
    
    Optional<Hospital> findByEmail(String email);
    
    Optional<Hospital> findByLicenseNumber(String licenseNumber);
    
    boolean existsByHospitalId(String hospitalId);
    
    boolean existsByEmail(String email);
    
    boolean existsByLicenseNumber(String licenseNumber);
    
    List<Hospital> findByStatus(HospitalStatus status);
    
    List<Hospital> findByCountry(String country);
    
    List<Hospital> findByCountryAndState(String country, String state);
    
    List<Hospital> findByCountryAndStateAndCity(String country, String state, String city);
    
    @Query("SELECT h FROM Hospital h WHERE h.city = :city AND h.state = :state")
    List<Hospital> findByCityAndState(@Param("city") String city, @Param("state") String state);
    
    @Query("SELECT h FROM Hospital h WHERE h.hospitalName LIKE %:name%")
    Page<Hospital> findByHospitalNameContaining(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT h FROM Hospital h WHERE h.status = 'ACTIVE'")
    List<Hospital> findActiveHospitals();
    
    @Query("SELECT COUNT(h) FROM Hospital h WHERE h.status = :status")
    long countByStatus(@Param("status") HospitalStatus status);
    
    @Query("SELECT h FROM Hospital h WHERE h.specializations LIKE %:specialization%")
    List<Hospital> findBySpecialization(@Param("specialization") String specialization);
}
