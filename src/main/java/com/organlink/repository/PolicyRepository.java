package com.organlink.repository;

import com.organlink.entity.Policy;
import com.organlink.entity.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    long countByStatus(PolicyStatus status);
    Optional<Policy> findByPolicyId(String policyId);
    List<Policy> findByProposedByOrganizationId(Long organizationId);

    @Query("SELECT p FROM Policy p WHERE p.status = 'VOTING' AND p.votingEndDate > ?1")
    List<Policy> findActiveVotingPolicies(LocalDateTime now);

    @Query("SELECT p FROM Policy p ORDER BY p.createdAt DESC")
    Page<Policy> findAllOrderByCreatedAtDesc(Pageable pageable);

    Page<Policy> findByTitleContaining(String title, Pageable pageable);

    // Method needed by AiMatchingService
    @Query("SELECT p FROM Policy p WHERE p.organType = ?1 AND p.status = ?2")
    List<Policy> findActivePoliciesForOrgan(String organType, PolicyStatus status);
}
