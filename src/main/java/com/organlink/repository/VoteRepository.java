package com.organlink.repository;

import com.organlink.entity.Vote;
import com.organlink.entity.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Vote entity
 */
@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    
    List<Vote> findByPolicyId(Long policyId);
    
    List<Vote> findByOrganizationId(Long organizationId);
    
    List<Vote> findByVoteType(VoteType voteType);
    
    Optional<Vote> findByPolicyIdAndOrganizationId(Long policyId, Long organizationId);
    
    boolean existsByPolicyIdAndOrganizationId(Long policyId, Long organizationId);
    
    @Query("SELECT v FROM Vote v WHERE v.policy.policyId = :policyId")
    List<Vote> findByPolicyPolicyId(@Param("policyId") String policyId);
    
    @Query("SELECT v FROM Vote v WHERE v.organization.organizationId = :organizationId")
    List<Vote> findByOrganizationOrganizationId(@Param("organizationId") String organizationId);
    
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.policy.id = :policyId AND v.voteType = :voteType")
    long countByPolicyIdAndVoteType(@Param("policyId") Long policyId, @Param("voteType") VoteType voteType);
    
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.policy.id = :policyId")
    long countByPolicyId(@Param("policyId") Long policyId);
    
    @Query("SELECT SUM(v.votingPower) FROM Vote v WHERE v.policy.id = :policyId AND v.voteType = :voteType")
    Long sumVotingPowerByPolicyIdAndVoteType(@Param("policyId") Long policyId, @Param("voteType") VoteType voteType);
    
    @Query("SELECT v FROM Vote v WHERE v.organization.id = :organizationId ORDER BY v.createdAt DESC")
    List<Vote> findByOrganizationIdOrderByCreatedAtDesc(@Param("organizationId") Long organizationId);
}
