package com.organlink.repository;

import com.organlink.entity.Donor;
import com.organlink.entity.DonorStatus;
import com.organlink.entity.AvailabilityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Donor entity
 */
@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {
    
    Optional<Donor> findByDonorId(String donorId);
    
    Optional<Donor> findByEmail(String email);
    
    boolean existsByDonorId(String donorId);
    
    boolean existsByEmail(String email);
    
    List<Donor> findByStatus(DonorStatus status);
    
    List<Donor> findByAvailabilityStatus(AvailabilityStatus availabilityStatus);
    
    List<Donor> findByBloodType(String bloodType);
    
    List<Donor> findByHospitalId(Long hospitalId);
    
    @Query("SELECT d FROM Donor d WHERE d.hospital.hospitalId = :hospitalId")
    Page<Donor> findByHospitalHospitalId(@Param("hospitalId") String hospitalId, Pageable pageable);
    
    @Query("SELECT d FROM Donor d WHERE d.firstName LIKE %:name% OR d.lastName LIKE %:name%")
    Page<Donor> findByNameContaining(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT d FROM Donor d WHERE d.organTypes LIKE %:organType%")
    List<Donor> findByOrganType(@Param("organType") String organType);
    
    @Query("SELECT d FROM Donor d WHERE d.status = 'ACTIVE' AND d.availabilityStatus = 'AVAILABLE'")
    List<Donor> findAvailableDonors();
    
    @Query("SELECT d FROM Donor d WHERE d.organTypes LIKE %:organType% AND d.bloodType = :bloodType AND d.status = 'ACTIVE' AND d.availabilityStatus = 'AVAILABLE'")
    List<Donor> findAvailableDonorsByOrganTypeAndBloodType(@Param("organType") String organType, @Param("bloodType") String bloodType);
    
    @Query("SELECT COUNT(d) FROM Donor d WHERE d.hospital.id = :hospitalId")
    long countByHospitalId(@Param("hospitalId") Long hospitalId);
    
    @Query("SELECT COUNT(d) FROM Donor d WHERE d.status = :status")
    long countByStatus(@Param("status") DonorStatus status);
    
    @Query("SELECT d FROM Donor d WHERE d.city = :city AND d.state = :state")
    List<Donor> findByCityAndState(@Param("city") String city, @Param("state") String state);
}
