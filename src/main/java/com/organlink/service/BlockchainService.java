package com.organlink.service;

import com.organlink.entity.*;
import com.organlink.repository.BlockchainTransactionRepository;
import com.organlink.repository.SignatureRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import jakarta.annotation.PostConstruct;
import java.math.BigInteger;

@Service
public class BlockchainService {

    @Value("${blockchain.ethereum.network-url}")
    private String networkUrl;

    @Value("${blockchain.ethereum.private-key}")
    private String privateKey;

    @Value("${blockchain.contracts.signature-verification-address:}")
    private String signatureContractAddress;

    @Value("${blockchain.contracts.policy-voting-address:}")
    private String policyContractAddress;

    @Autowired
    private BlockchainTransactionRepository transactionRepository;

    @Autowired
    private SignatureRecordRepository signatureRecordRepository;

    private Web3j web3j;
    private Credentials credentials;

    @PostConstruct
    public void init() {
        this.web3j = Web3j.build(new HttpService(networkUrl));
        this.credentials = Credentials.create(privateKey);
        System.out.println("✅ BlockchainService initialized. Connected to: " + networkUrl);
    }

    @Async
    public void recordDonorRegistration(Donor donor) {
        try {
            // This is a placeholder until the contract is deployed and ABI is provided
            System.out.println("Simulating blockchain transaction for Donor: " + donor.getFullName());
            String txHash = "0x" + new java.math.BigInteger(130, new java.security.SecureRandom()).toString(16);
            
            // Update the donor entity with the simulated hash
            donor.setBlockchainTxHash(txHash);
            // In a real scenario, you would save the donor entity here after getting the hash

            logTransaction(txHash, "DONOR_REGISTERED", "CONFIRMED", donor.getDonorId());
        } catch (Exception e) {
            System.err.println("Blockchain transaction failed for Donor " + donor.getFullName() + ": " + e.getMessage());
            logTransaction(null, "DONOR_REGISTERED", "FAILED", e.getMessage());
        }
    }

    @Async
    public void recordPatientRegistration(Patient patient) {
        try {
            System.out.println("Simulating blockchain transaction for Patient: " + patient.getFullName());
            String txHash = "0x" + new java.math.BigInteger(130, new java.security.SecureRandom()).toString(16);
            patient.setBlockchainTxHash(txHash);
            logTransaction(txHash, "PATIENT_REGISTERED", "CONFIRMED", patient.getPatientId());
        } catch (Exception e) {
            System.err.println("Blockchain transaction failed for Patient " + patient.getFullName() + ": " + e.getMessage());
            logTransaction(null, "PATIENT_REGISTERED", "FAILED", e.getMessage());
        }
    }

    @Async
    public void recordPolicyCreation(Policy policy) {
        try {
            System.out.println("Simulating blockchain transaction for Policy: " + policy.getTitle());
            String txHash = "0x" + new java.math.BigInteger(130, new java.security.SecureRandom()).toString(16);
            policy.setBlockchainTxHash(txHash);
            logTransaction(txHash, "POLICY_CREATED", "CONFIRMED", policy.getPolicyId());
        } catch (Exception e) {
            System.err.println("Blockchain transaction failed for Policy " + policy.getTitle() + ": " + e.getMessage());
            logTransaction(null, "POLICY_CREATED", "FAILED", e.getMessage());
        }
    }

    @Async
    public void recordVote(Vote vote) {
        try {
            System.out.println("Simulating blockchain transaction for Vote on Policy: " + vote.getPolicy().getTitle());
            String txHash = "0x" + new java.math.BigInteger(130, new java.security.SecureRandom()).toString(16);
            vote.setBlockchainTxHash(txHash);
            logTransaction(txHash, "POLICY_VOTED", "CONFIRMED", "Policy: " + vote.getPolicy().getPolicyId());
        } catch (Exception e) {
            System.err.println("Blockchain transaction failed for Vote on Policy " + vote.getPolicy().getTitle() + ": " + e.getMessage());
            logTransaction(null, "POLICY_VOTED", "FAILED", e.getMessage());
        }
    }

    private void logTransaction(String txHash, String type, String status, String details) {
        BlockchainTransaction transaction = new BlockchainTransaction();
        transaction.setTransactionHash(txHash != null ? txHash : "failed-tx-" + System.currentTimeMillis());
        transaction.setEventType(BlockchainEventType.valueOf(type));
        transaction.setStatus(TransactionStatus.valueOf(status));
        transaction.setEventData(details);
        transaction.setFromAddress(credentials.getAddress());
        transactionRepository.save(transaction);
    }
}
