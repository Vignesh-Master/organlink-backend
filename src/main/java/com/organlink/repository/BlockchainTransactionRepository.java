package com.organlink.repository;

import com.organlink.entity.BlockchainTransaction;
import com.organlink.entity.BlockchainEventType;
import com.organlink.entity.TransactionStatus;
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
 * Repository interface for BlockchainTransaction entity
 */
@Repository
public interface BlockchainTransactionRepository extends JpaRepository<BlockchainTransaction, Long> {
    
    Optional<BlockchainTransaction> findByTransactionHash(String transactionHash);
    
    List<BlockchainTransaction> findByStatus(TransactionStatus status);
    
    List<BlockchainTransaction> findByEventType(BlockchainEventType eventType);
    
    List<BlockchainTransaction> findByEntityTypeAndEntityId(String entityType, String entityId);
    
    List<BlockchainTransaction> findByInitiatorTypeAndInitiatorId(String initiatorType, String initiatorId);
    
    @Query("SELECT bt FROM BlockchainTransaction bt WHERE bt.createdAt BETWEEN :startDate AND :endDate")
    List<BlockchainTransaction> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT bt FROM BlockchainTransaction bt ORDER BY bt.createdAt DESC")
    Page<BlockchainTransaction> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT COUNT(bt) FROM BlockchainTransaction bt WHERE bt.status = :status")
    long countByStatus(@Param("status") TransactionStatus status);
    
    @Query("SELECT COUNT(bt) FROM BlockchainTransaction bt WHERE bt.eventType = :eventType")
    long countByEventType(@Param("eventType") BlockchainEventType eventType);
    
    @Query("SELECT bt FROM BlockchainTransaction bt WHERE bt.status = 'PENDING' AND bt.createdAt < :cutoffTime")
    List<BlockchainTransaction> findStaleTransactions(@Param("cutoffTime") LocalDateTime cutoffTime);
}
