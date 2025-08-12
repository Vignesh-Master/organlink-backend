package com.organlink.service;

import com.organlink.entity.*;
import com.organlink.repository.BlockchainTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Blockchain service for Ethereum integration
 * Handles smart contract interactions and transaction recording
 */
@Service
@Transactional
public class BlockchainService {

    @Autowired
    private Web3j web3j;

    @Autowired
    private Credentials credentials;

    @Autowired
    private StaticGasProvider gasProvider;

    @Autowired
    private BlockchainTransactionRepository blockchainTransactionRepository;

    @Value("${blockchain.contracts.policy-voting:}")
    private String policyVotingContractAddress;

    @Value("${blockchain.contracts.signature-verification:}")
    private String signatureVerificationContractAddress;

    /**
     * Record policy creation on blockchain
     */
    @Async
    public CompletableFuture<String> recordPolicyCreation(Policy policy) {
        try {
            // Create blockchain transaction record
            BlockchainTransaction transaction = new BlockchainTransaction();
            transaction.setEventType(BlockchainEventType.POLICY_CREATED);
            transaction.setFromAddress(credentials.getAddress());
            transaction.setEntityType("POLICY");
            transaction.setEntityId(policy.getPolicyId());
            transaction.setInitiatorType("ORGANIZATION");
            transaction.setInitiatorId(policy.getProposedByOrganization().getOrganizationId());
            transaction.setStatus(TransactionStatus.PENDING);

            // For now, simulate blockchain transaction
            // In production, this would interact with deployed smart contracts
            String simulatedTxHash = generateSimulatedTxHash();
            transaction.setTransactionHash(simulatedTxHash);
            transaction.setStatus(TransactionStatus.CONFIRMED);
            transaction.setConfirmedAt(LocalDateTime.now());
            transaction.setConfirmations(1);

            blockchainTransactionRepository.save(transaction);

            // Update policy with blockchain transaction hash
            policy.setBlockchainTxHash(simulatedTxHash);

            return CompletableFuture.completedFuture(simulatedTxHash);

        } catch (Exception e) {
            throw new RuntimeException("Failed to record policy creation on blockchain", e);
        }
    }

    /**
     * Record vote on blockchain
     */
    @Async
    public CompletableFuture<String> recordVote(Vote vote) {
        try {
            // Create blockchain transaction record
            BlockchainTransaction transaction = new BlockchainTransaction();
            transaction.setEventType(BlockchainEventType.POLICY_VOTED);
            transaction.setFromAddress(credentials.getAddress());
            transaction.setEntityType("VOTE");
            transaction.setEntityId(vote.getId().toString());
            transaction.setInitiatorType("ORGANIZATION");
            transaction.setInitiatorId(vote.getOrganization().getOrganizationId());
            transaction.setStatus(TransactionStatus.PENDING);

            // Create event data
            String eventData = String.format(
                "{\"policyId\":\"%s\",\"organizationId\":\"%s\",\"voteType\":\"%s\",\"votingPower\":%d}",
                vote.getPolicy().getPolicyId(),
                vote.getOrganization().getOrganizationId(),
                vote.getVoteType().name(),
                vote.getVotingPower()
            );
            transaction.setEventData(eventData);

            // Simulate blockchain transaction
            String simulatedTxHash = generateSimulatedTxHash();
            transaction.setTransactionHash(simulatedTxHash);
            transaction.setStatus(TransactionStatus.CONFIRMED);
            transaction.setConfirmedAt(LocalDateTime.now());
            transaction.setConfirmations(1);

            blockchainTransactionRepository.save(transaction);

            // Update vote with blockchain transaction hash
            vote.setBlockchainTxHash(simulatedTxHash);

            return CompletableFuture.completedFuture(simulatedTxHash);

        } catch (Exception e) {
            throw new RuntimeException("Failed to record vote on blockchain", e);
        }
    }

    /**
     * Record donor registration on blockchain
     */
    @Async
    public CompletableFuture<String> recordDonorRegistration(Donor donor) {
        try {
            BlockchainTransaction transaction = new BlockchainTransaction();
            transaction.setEventType(BlockchainEventType.DONOR_REGISTERED);
            transaction.setFromAddress(credentials.getAddress());
            transaction.setEntityType("DONOR");
            transaction.setEntityId(donor.getDonorId());
            transaction.setInitiatorType("HOSPITAL");
            transaction.setInitiatorId(donor.getHospital().getHospitalId());
            transaction.setStatus(TransactionStatus.PENDING);

            String eventData = String.format(
                "{\"donorId\":\"%s\",\"hospitalId\":\"%s\",\"organTypes\":%s,\"bloodType\":\"%s\"}",
                donor.getDonorId(),
                donor.getHospital().getHospitalId(),
                donor.getOrganTypes().toString(),
                donor.getBloodType()
            );
            transaction.setEventData(eventData);

            String simulatedTxHash = generateSimulatedTxHash();
            transaction.setTransactionHash(simulatedTxHash);
            transaction.setStatus(TransactionStatus.CONFIRMED);
            transaction.setConfirmedAt(LocalDateTime.now());
            transaction.setConfirmations(1);

            blockchainTransactionRepository.save(transaction);
            donor.setBlockchainTxHash(simulatedTxHash);

            return CompletableFuture.completedFuture(simulatedTxHash);

        } catch (Exception e) {
            throw new RuntimeException("Failed to record donor registration on blockchain", e);
        }
    }

    /**
     * Record patient registration on blockchain
     */
    @Async
    public CompletableFuture<String> recordPatientRegistration(Patient patient) {
        try {
            BlockchainTransaction transaction = new BlockchainTransaction();
            transaction.setEventType(BlockchainEventType.PATIENT_REGISTERED);
            transaction.setFromAddress(credentials.getAddress());
            transaction.setEntityType("PATIENT");
            transaction.setEntityId(patient.getPatientId());
            transaction.setInitiatorType("HOSPITAL");
            transaction.setInitiatorId(patient.getHospital().getHospitalId());
            transaction.setStatus(TransactionStatus.PENDING);

            String eventData = String.format(
                "{\"patientId\":\"%s\",\"hospitalId\":\"%s\",\"organNeeded\":\"%s\",\"bloodType\":\"%s\",\"urgencyLevel\":\"%s\"}",
                patient.getPatientId(),
                patient.getHospital().getHospitalId(),
                patient.getOrganNeeded(),
                patient.getBloodType(),
                patient.getUrgencyLevel().name()
            );
            transaction.setEventData(eventData);

            String simulatedTxHash = generateSimulatedTxHash();
            transaction.setTransactionHash(simulatedTxHash);
            transaction.setStatus(TransactionStatus.CONFIRMED);
            transaction.setConfirmedAt(LocalDateTime.now());
            transaction.setConfirmations(1);

            blockchainTransactionRepository.save(transaction);
            patient.setBlockchainTxHash(simulatedTxHash);

            return CompletableFuture.completedFuture(simulatedTxHash);

        } catch (Exception e) {
            throw new RuntimeException("Failed to record patient registration on blockchain", e);
        }
    }

    /**
     * Record signature verification on blockchain
     */
    @Async
    public CompletableFuture<String> recordSignatureVerification(SignatureRecord signatureRecord) {
        try {
            BlockchainTransaction transaction = new BlockchainTransaction();
            transaction.setEventType(BlockchainEventType.SIGNATURE_VERIFIED);
            transaction.setFromAddress(credentials.getAddress());
            transaction.setEntityType("SIGNATURE");
            transaction.setEntityId(signatureRecord.getFileId());
            transaction.setInitiatorType("HOSPITAL");
            transaction.setStatus(TransactionStatus.PENDING);

            String eventData = String.format(
                "{\"fileId\":\"%s\",\"ipfsHash\":\"%s\",\"signerName\":\"%s\",\"verificationStatus\":\"%s\",\"confidence\":%.2f}",
                signatureRecord.getFileId(),
                signatureRecord.getIpfsHash(),
                signatureRecord.getSignerName(),
                signatureRecord.getVerificationStatus().name(),
                signatureRecord.getNameMatchConfidence()
            );
            transaction.setEventData(eventData);

            String simulatedTxHash = generateSimulatedTxHash();
            transaction.setTransactionHash(simulatedTxHash);
            transaction.setStatus(TransactionStatus.CONFIRMED);
            transaction.setConfirmedAt(LocalDateTime.now());
            transaction.setConfirmations(1);

            blockchainTransactionRepository.save(transaction);
            signatureRecord.setBlockchainTxHash(simulatedTxHash);

            return CompletableFuture.completedFuture(simulatedTxHash);

        } catch (Exception e) {
            throw new RuntimeException("Failed to record signature verification on blockchain", e);
        }
    }

    /**
     * Get blockchain transactions
     */
    public List<BlockchainTransaction> getTransactions() {
        return blockchainTransactionRepository.findAll();
    }

    /**
     * Get transaction by hash
     */
    public Optional<BlockchainTransaction> getTransactionByHash(String hash) {
        return blockchainTransactionRepository.findByTransactionHash(hash);
    }

    /**
     * Get transactions by entity
     */
    public List<BlockchainTransaction> getTransactionsByEntity(String entityType, String entityId) {
        return blockchainTransactionRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    /**
     * Check if Web3 connection is healthy
     */
    public boolean isBlockchainConnected() {
        try {
            web3j.web3ClientVersion().send();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get current block number
     */
    public BigInteger getCurrentBlockNumber() {
        try {
            return web3j.ethBlockNumber().send().getBlockNumber();
        } catch (Exception e) {
            return BigInteger.ZERO;
        }
    }

    // Helper methods
    private String generateSimulatedTxHash() {
        // Generate a realistic-looking transaction hash for simulation
        return "0x" + java.util.UUID.randomUUID().toString().replace("-", "") + 
               java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
