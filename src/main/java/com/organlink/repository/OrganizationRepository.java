package com.organlink.repository;

import com.organlink.entity.Organization;
import com.organlink.entity.OrganizationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Organization entity
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    
    Optional<Organization> findByOrganizationId(String organizationId);
    
    Optional<Organization> findByEmail(String email);
    
    Optional<Organization> findByRegistrationNumber(String registrationNumber);
    
    boolean existsByOrganizationId(String organizationId);
    
    boolean existsByEmail(String email);
    
    boolean existsByRegistrationNumber(String registrationNumber);
    
    List<Organization> findByStatus(OrganizationStatus status);
    
    List<Organization> findByCountry(String country);
    
    List<Organization> findByOrganizationType(String organizationType);
    
    @Query("SELECT o FROM Organization o WHERE o.organizationName LIKE %:name%")
    Page<Organization> findByOrganizationNameContaining(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT o FROM Organization o WHERE o.status = 'ACTIVE'")
    List<Organization> findActiveOrganizations();
    
    @Query("SELECT COUNT(o) FROM Organization o WHERE o.status = :status")
    long countByStatus(@Param("status") OrganizationStatus status);
    
    @Query("SELECT o FROM Organization o WHERE o.focusAreas LIKE %:focusArea%")
    List<Organization> findByFocusArea(@Param("focusArea") String focusArea);
    
    @Query("SELECT SUM(o.votingPower) FROM Organization o WHERE o.status = 'ACTIVE'")
    Long getTotalVotingPower();

    Page<Organization> findByOrganizationNameContainingIgnoreCaseOrOrganizationIdContainingIgnoreCase(
            String organizationName, String organizationId, Pageable pageable);
}
