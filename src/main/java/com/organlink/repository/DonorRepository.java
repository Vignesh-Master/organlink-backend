package com.organlink.repository;

import com.organlink.entity.AvailabilityStatus;
import com.organlink.entity.Donor;
import com.organlink.entity.DonorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {
    long countByStatus(DonorStatus status);
    long countByHospitalId(Long hospitalId);
    List<Donor> findByHospitalId(Long hospitalId);
    Page<Donor> findByHospitalHospitalId(String hospitalId, Pageable pageable);
    Page<Donor> findByHospitalHospitalNameContainingIgnoreCaseOrDonorIdContainingIgnoreCase(String name, String donorId, Pageable pageable);

    // Method needed by AiMatchingService
    List<Donor> findAllByOrganTypesContainingAndAvailabilityStatus(String organType, AvailabilityStatus availabilityStatus);
}
