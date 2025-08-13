package com.organlink.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * Blockchain transaction record entity
 */
@Entity
@Table(name = "blockchain_transactions")
@EntityListeners(AuditingEntityListener.class)
public class BlockchainTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "transaction_hash", unique = true, nullable = false)
    private String transactionHash;
    
    @Column(name = "block_number")
    private BigInteger blockNumber;
    
    @Column(name = "block_hash")
    private String blockHash;
    
    @NotBlank
    @Column(name = "from_address", nullable = false)
    private String fromAddress;
    
    @Column(name = "to_address")
    private String toAddress;
    
    @Column(name = "contract_address")
    private String contractAddress;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private BlockchainEventType eventType;
    
    @Column(name = "gas_used")
    private BigInteger gasUsed;
    
    @Column(name = "gas_price")
    private BigInteger gasPrice;
    
    @Column(name = "transaction_fee")
    private BigInteger transactionFee;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(name = "confirmations")
    private Integer confirmations = 0;
    
    @Column(name = "event_data", columnDefinition = "JSON")
    private String eventData; // JSON containing event-specific data
    
    @Column(name = "error_message")
    private String errorMessage;
    
    // Related Entity Information
    @Column(name = "entity_type")
    private String entityType; // POLICY, SIGNATURE, DONOR, PATIENT, etc.
    
    @Column(name = "entity_id")
    private String entityId;
    
    @Column(name = "initiator_type")
    private String initiatorType; // ADMIN, HOSPITAL, ORGANIZATION
    
    @Column(name = "initiator_id")
    private String initiatorId;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public BlockchainTransaction() {}
    
    public BlockchainTransaction(String transactionHash, BlockchainEventType eventType, String fromAddress) {
        this.transactionHash = transactionHash;
        this.eventType = eventType;
        this.fromAddress = fromAddress;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }
    
    public BigInteger getBlockNumber() { return blockNumber; }
    public void setBlockNumber(BigInteger blockNumber) { this.blockNumber = blockNumber; }
    
    public String getBlockHash() { return blockHash; }
    public void setBlockHash(String blockHash) { this.blockHash = blockHash; }
    
    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }
    
    public String getToAddress() { return toAddress; }
    public void setToAddress(String toAddress) { this.toAddress = toAddress; }
    
    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String contractAddress) { this.contractAddress = contractAddress; }
    
    public BlockchainEventType getEventType() { return eventType; }
    public void setEventType(BlockchainEventType eventType) { this.eventType = eventType; }
    
    public BigInteger getGasUsed() { return gasUsed; }
    public void setGasUsed(BigInteger gasUsed) { this.gasUsed = gasUsed; }
    
    public BigInteger getGasPrice() { return gasPrice; }
    public void setGasPrice(BigInteger gasPrice) { this.gasPrice = gasPrice; }
    
    public BigInteger getTransactionFee() { return transactionFee; }
    public void setTransactionFee(BigInteger transactionFee) { this.transactionFee = transactionFee; }
    
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    
    public Integer getConfirmations() { return confirmations; }
    public void setConfirmations(Integer confirmations) { this.confirmations = confirmations; }
    
    public String getEventData() { return eventData; }
    public void setEventData(String eventData) { this.eventData = eventData; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    
    public String getInitiatorType() { return initiatorType; }
    public void setInitiatorType(String initiatorType) { this.initiatorType = initiatorType; }
    
    public String getInitiatorId() { return initiatorId; }
    public void setInitiatorId(String initiatorId) { this.initiatorId = initiatorId; }
    
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Utility methods
    public boolean isConfirmed() {
        return status == TransactionStatus.CONFIRMED;
    }
    
    public boolean isFailed() {
        return status == TransactionStatus.FAILED;
    }
    
    public String getGasPriceInGwei() {
        if (gasPrice == null) return "0";
        return gasPrice.divide(BigInteger.valueOf(1_000_000_000)).toString();
    }
}

// Enums moved to separate files
