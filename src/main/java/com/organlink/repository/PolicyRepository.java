package com.organlink.repository;

import com.organlink.entity.Policy;
import com.organlink.entity.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Policy entity
 */
@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    
    Optional<Policy> findByPolicyId(String policyId);
    
    List<Policy> findByStatus(PolicyStatus status);
    
    List<Policy> findByOrganType(String organType);
    
    List<Policy> findByProposedByOrganizationId(Long organizationId);
    
    @Query("SELECT p FROM Policy p WHERE p.proposedByOrganization.organizationId = :organizationId")
    Page<Policy> findByProposedByOrganizationOrganizationId(@Param("organizationId") String organizationId, Pageable pageable);
    
    @Query("SELECT p FROM Policy p WHERE p.title LIKE %:title%")
    Page<Policy> findByTitleContaining(@Param("title") String title, Pageable pageable);
    
    @Query("SELECT p FROM Policy p WHERE p.status = 'VOTING' AND p.votingEndDate > :currentTime")
    List<Policy> findActiveVotingPolicies(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT p FROM Policy p WHERE p.status = 'APPROVED' AND p.implementationDate <= :currentTime")
    List<Policy> findPoliciesReadyForImplementation(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT p FROM Policy p WHERE p.expiryDate <= :currentTime AND p.status = 'IMPLEMENTED'")
    List<Policy> findExpiredPolicies(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT COUNT(p) FROM Policy p WHERE p.status = :status")
    long countByStatus(@Param("status") PolicyStatus status);
    
    @Query("SELECT p FROM Policy p WHERE p.organType = :organType AND p.status = 'IMPLEMENTED'")
    List<Policy> findImplementedPoliciesByOrganType(@Param("organType") String organType);
    
    @Query("SELECT p FROM Policy p ORDER BY p.createdAt DESC")
    Page<Policy> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT p FROM Policy p WHERE p.status IN ('PENDING', 'VOTING') ORDER BY p.createdAt DESC")
    List<Policy> findActivePolicies();
}
