package com.organlink.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for interacting with the OrganLinkRegistry smart contract
 * Handles hospital authorization, donor/patient registration, and signature verification
 */
@Service
public class OrganLinkRegistryService {

    private static final Logger logger = LoggerFactory.getLogger(OrganLinkRegistryService.class);

    private final Web3j web3j;
    private final Credentials credentials;
    private final StaticGasProvider gasProvider;

    @Value("${blockchain.contracts.signature-verification-address:}")
    private String contractAddress;

    public OrganLinkRegistryService(Web3j web3j, Credentials credentials, StaticGasProvider gasProvider) {
        this.web3j = web3j;
        this.credentials = credentials;
        this.gasProvider = gasProvider;
    }

    /**
     * Authorize a hospital on the blockchain
     */
    public CompletableFuture<String> authorizeHospital(String hospitalAddress, String hospitalId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Authorizing hospital: {} with ID: {}", hospitalAddress, hospitalId);

                Function function = new Function(
                    "authorizeHospital",
                    Arrays.asList(
                        new org.web3j.abi.datatypes.Address(hospitalAddress),
                        new Utf8String(hospitalId)
                    ),
                    Arrays.asList()
                );

                String encodedFunction = FunctionEncoder.encode(function);
                
                EthSendTransaction transactionResponse = web3j.ethSendTransaction(
                    Transaction.createFunctionCallTransaction(
                        credentials.getAddress(),
                        null,
                        gasProvider.getGasPrice(),
                        gasProvider.getGasLimit(),
                        contractAddress,
                        encodedFunction
                    )
                ).send();

                if (transactionResponse.hasError()) {
                    throw new RuntimeException("Transaction failed: " + transactionResponse.getError().getMessage());
                }

                String transactionHash = transactionResponse.getTransactionHash();
                logger.info("Hospital authorized successfully. Transaction hash: {}", transactionHash);
                return transactionHash;

            } catch (Exception e) {
                logger.error("Failed to authorize hospital", e);
                throw new RuntimeException("Failed to authorize hospital: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Register a donor on the blockchain
     */
    public CompletableFuture<String> registerDonor(String donorId, String fullName, String bloodType, String signatureIpfsHash) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Registering donor: {} on blockchain", donorId);

                Function function = new Function(
                    "registerDonor",
                    Arrays.asList(
                        new Utf8String(donorId),
                        new Utf8String(fullName),
                        new Utf8String(bloodType),
                        new Utf8String(signatureIpfsHash)
                    ),
                    Arrays.asList()
                );

                String encodedFunction = FunctionEncoder.encode(function);
                
                EthSendTransaction transactionResponse = web3j.ethSendTransaction(
                    Transaction.createFunctionCallTransaction(
                        credentials.getAddress(),
                        null,
                        gasProvider.getGasPrice(),
                        gasProvider.getGasLimit(),
                        contractAddress,
                        encodedFunction
                    )
                ).send();

                if (transactionResponse.hasError()) {
                    throw new RuntimeException("Transaction failed: " + transactionResponse.getError().getMessage());
                }

                String transactionHash = transactionResponse.getTransactionHash();
                logger.info("Donor registered successfully. Transaction hash: {}", transactionHash);
                return transactionHash;

            } catch (Exception e) {
                logger.error("Failed to register donor", e);
                throw new RuntimeException("Failed to register donor: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Register a patient on the blockchain
     */
    public CompletableFuture<String> registerPatient(String patientId, String fullName, String bloodType, 
                                                   String organNeeded, String urgencyLevel, String signatureIpfsHash) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Registering patient: {} on blockchain", patientId);

                Function function = new Function(
                    "registerPatient",
                    Arrays.asList(
                        new Utf8String(patientId),
                        new Utf8String(fullName),
                        new Utf8String(bloodType),
                        new Utf8String(organNeeded),
                        new Utf8String(urgencyLevel),
                        new Utf8String(signatureIpfsHash)
                    ),
                    Arrays.asList()
                );

                String encodedFunction = FunctionEncoder.encode(function);
                
                EthSendTransaction transactionResponse = web3j.ethSendTransaction(
                    Transaction.createFunctionCallTransaction(
                        credentials.getAddress(),
                        null,
                        gasProvider.getGasPrice(),
                        gasProvider.getGasLimit(),
                        contractAddress,
                        encodedFunction
                    )
                ).send();

                if (transactionResponse.hasError()) {
                    throw new RuntimeException("Transaction failed: " + transactionResponse.getError().getMessage());
                }

                String transactionHash = transactionResponse.getTransactionHash();
                logger.info("Patient registered successfully. Transaction hash: {}", transactionHash);
                return transactionHash;

            } catch (Exception e) {
                logger.error("Failed to register patient", e);
                throw new RuntimeException("Failed to register patient: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Verify donor signature on the blockchain
     */
    public CompletableFuture<String> verifyDonorSignature(String donorId, boolean verified) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Verifying donor signature for: {} as {}", donorId, verified);

                Function function = new Function(
                    "verifyDonorSignature",
                    Arrays.asList(
                        new Utf8String(donorId),
                        new Bool(verified)
                    ),
                    Arrays.asList()
                );

                String encodedFunction = FunctionEncoder.encode(function);
                
                EthSendTransaction transactionResponse = web3j.ethSendTransaction(
                    Transaction.createFunctionCallTransaction(
                        credentials.getAddress(),
                        null,
                        gasProvider.getGasPrice(),
                        gasProvider.getGasLimit(),
                        contractAddress,
                        encodedFunction
                    )
                ).send();

                if (transactionResponse.hasError()) {
                    throw new RuntimeException("Transaction failed: " + transactionResponse.getError().getMessage());
                }

                String transactionHash = transactionResponse.getTransactionHash();
                logger.info("Donor signature verification completed. Transaction hash: {}", transactionHash);
                return transactionHash;

            } catch (Exception e) {
                logger.error("Failed to verify donor signature", e);
                throw new RuntimeException("Failed to verify donor signature: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Verify patient signature on the blockchain
     */
    public CompletableFuture<String> verifyPatientSignature(String patientId, boolean verified) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Verifying patient signature for: {} as {}", patientId, verified);

                Function function = new Function(
                    "verifyPatientSignature",
                    Arrays.asList(
                        new Utf8String(patientId),
                        new Bool(verified)
                    ),
                    Arrays.asList()
                );

                String encodedFunction = FunctionEncoder.encode(function);
                
                EthSendTransaction transactionResponse = web3j.ethSendTransaction(
                    Transaction.createFunctionCallTransaction(
                        credentials.getAddress(),
                        null,
                        gasProvider.getGasPrice(),
                        gasProvider.getGasLimit(),
                        contractAddress,
                        encodedFunction
                    )
                ).send();

                if (transactionResponse.hasError()) {
                    throw new RuntimeException("Transaction failed: " + transactionResponse.getError().getMessage());
                }

                String transactionHash = transactionResponse.getTransactionHash();
                logger.info("Patient signature verification completed. Transaction hash: {}", transactionHash);
                return transactionHash;

            } catch (Exception e) {
                logger.error("Failed to verify patient signature", e);
                throw new RuntimeException("Failed to verify patient signature: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Check if a hospital is authorized
     */
    public CompletableFuture<Boolean> isHospitalAuthorized(String hospitalAddress) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Function function = new Function(
                    "isAuthorized",
                    Arrays.asList(new org.web3j.abi.datatypes.Address(hospitalAddress)),
                    Arrays.asList(new TypeReference<Bool>() {})
                );

                String encodedFunction = FunctionEncoder.encode(function);
                
                EthCall response = web3j.ethCall(
                    Transaction.createEthCallTransaction(
                        credentials.getAddress(),
                        contractAddress,
                        encodedFunction
                    ),
                    DefaultBlockParameterName.LATEST
                ).send();

                List<Type> result = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
                return ((Bool) result.get(0)).getValue();

            } catch (Exception e) {
                logger.error("Failed to check hospital authorization", e);
                return false;
            }
        });
    }

    /**
     * Get donor details from blockchain
     */
    public CompletableFuture<DonorBlockchainData> getDonor(String donorId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Function function = new Function(
                    "getDonor",
                    Arrays.asList(new Utf8String(donorId)),
                    Arrays.asList(
                        new TypeReference<Utf8String>() {},  // donorId
                        new TypeReference<Utf8String>() {},  // fullName
                        new TypeReference<Utf8String>() {},  // bloodType
                        new TypeReference<Utf8String>() {},  // signatureIpfsHash
                        new TypeReference<org.web3j.abi.datatypes.Address>() {},  // hospitalAddress
                        new TypeReference<Uint256>() {},     // registrationTime
                        new TypeReference<Bool>() {},        // isActive
                        new TypeReference<Bool>() {}         // signatureVerified
                    )
                );

                String encodedFunction = FunctionEncoder.encode(function);
                
                EthCall response = web3j.ethCall(
                    Transaction.createEthCallTransaction(
                        credentials.getAddress(),
                        contractAddress,
                        encodedFunction
                    ),
                    DefaultBlockParameterName.LATEST
                ).send();

                List<Type> result = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
                
                return new DonorBlockchainData(
                    ((Utf8String) result.get(0)).getValue(),
                    ((Utf8String) result.get(1)).getValue(),
                    ((Utf8String) result.get(2)).getValue(),
                    ((Utf8String) result.get(3)).getValue(),
                    ((org.web3j.abi.datatypes.Address) result.get(4)).getValue(),
                    ((Uint256) result.get(5)).getValue(),
                    ((Bool) result.get(6)).getValue(),
                    ((Bool) result.get(7)).getValue()
                );

            } catch (Exception e) {
                logger.error("Failed to get donor from blockchain", e);
                throw new RuntimeException("Failed to get donor: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Get patient details from blockchain
     */
    public CompletableFuture<PatientBlockchainData> getPatient(String patientId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Function function = new Function(
                    "getPatient",
                    Arrays.asList(new Utf8String(patientId)),
                    Arrays.asList(
                        new TypeReference<Utf8String>() {},  // patientId
                        new TypeReference<Utf8String>() {},  // fullName
                        new TypeReference<Utf8String>() {},  // bloodType
                        new TypeReference<Utf8String>() {},  // organNeeded
                        new TypeReference<Utf8String>() {},  // urgencyLevel
                        new TypeReference<Utf8String>() {},  // signatureIpfsHash
                        new TypeReference<org.web3j.abi.datatypes.Address>() {},  // hospitalAddress
                        new TypeReference<Uint256>() {},     // registrationTime
                        new TypeReference<Bool>() {},        // isActive
                        new TypeReference<Bool>() {}         // signatureVerified
                    )
                );

                String encodedFunction = FunctionEncoder.encode(function);
                
                EthCall response = web3j.ethCall(
                    Transaction.createEthCallTransaction(
                        credentials.getAddress(),
                        contractAddress,
                        encodedFunction
                    ),
                    DefaultBlockParameterName.LATEST
                ).send();

                List<Type> result = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
                
                return new PatientBlockchainData(
                    ((Utf8String) result.get(0)).getValue(),
                    ((Utf8String) result.get(1)).getValue(),
                    ((Utf8String) result.get(2)).getValue(),
                    ((Utf8String) result.get(3)).getValue(),
                    ((Utf8String) result.get(4)).getValue(),
                    ((Utf8String) result.get(5)).getValue(),
                    ((org.web3j.abi.datatypes.Address) result.get(6)).getValue(),
                    ((Uint256) result.get(7)).getValue(),
                    ((Bool) result.get(8)).getValue(),
                    ((Bool) result.get(9)).getValue()
                );

            } catch (Exception e) {
                logger.error("Failed to get patient from blockchain", e);
                throw new RuntimeException("Failed to get patient: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Get statistics from the smart contract
     */
    public CompletableFuture<ContractStats> getStats() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Function function = new Function(
                    "getStats",
                    Arrays.asList(),
                    Arrays.asList(
                        new TypeReference<Uint256>() {},  // totalDonors
                        new TypeReference<Uint256>() {}   // totalPatients
                    )
                );

                String encodedFunction = FunctionEncoder.encode(function);
                
                EthCall response = web3j.ethCall(
                    Transaction.createEthCallTransaction(
                        credentials.getAddress(),
                        contractAddress,
                        encodedFunction
                    ),
                    DefaultBlockParameterName.LATEST
                ).send();

                List<Type> result = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
                
                return new ContractStats(
                    ((Uint256) result.get(0)).getValue(),
                    ((Uint256) result.get(1)).getValue()
                );

            } catch (Exception e) {
                logger.error("Failed to get stats from blockchain", e);
                return new ContractStats(BigInteger.ZERO, BigInteger.ZERO);
            }
        });
    }

    // Data classes for blockchain responses
    public static class DonorBlockchainData {
        private final String donorId;
        private final String fullName;
        private final String bloodType;
        private final String signatureIpfsHash;
        private final String hospitalAddress;
        private final BigInteger registrationTime;
        private final boolean isActive;
        private final boolean signatureVerified;

        public DonorBlockchainData(String donorId, String fullName, String bloodType, String signatureIpfsHash,
                                 String hospitalAddress, BigInteger registrationTime, boolean isActive, boolean signatureVerified) {
            this.donorId = donorId;
            this.fullName = fullName;
            this.bloodType = bloodType;
            this.signatureIpfsHash = signatureIpfsHash;
            this.hospitalAddress = hospitalAddress;
            this.registrationTime = registrationTime;
            this.isActive = isActive;
            this.signatureVerified = signatureVerified;
        }

        // Getters
        public String getDonorId() { return donorId; }
        public String getFullName() { return fullName; }
        public String getBloodType() { return bloodType; }
        public String getSignatureIpfsHash() { return signatureIpfsHash; }
        public String getHospitalAddress() { return hospitalAddress; }
        public BigInteger getRegistrationTime() { return registrationTime; }
        public boolean isActive() { return isActive; }
        public boolean isSignatureVerified() { return signatureVerified; }
    }

    public static class PatientBlockchainData {
        private final String patientId;
        private final String fullName;
        private final String bloodType;
        private final String organNeeded;
        private final String urgencyLevel;
        private final String signatureIpfsHash;
        private final String hospitalAddress;
        private final BigInteger registrationTime;
        private final boolean isActive;
        private final boolean signatureVerified;

        public PatientBlockchainData(String patientId, String fullName, String bloodType, String organNeeded,
                                   String urgencyLevel, String signatureIpfsHash, String hospitalAddress,
                                   BigInteger registrationTime, boolean isActive, boolean signatureVerified) {
            this.patientId = patientId;
            this.fullName = fullName;
            this.bloodType = bloodType;
            this.organNeeded = organNeeded;
            this.urgencyLevel = urgencyLevel;
            this.signatureIpfsHash = signatureIpfsHash;
            this.hospitalAddress = hospitalAddress;
            this.registrationTime = registrationTime;
            this.isActive = isActive;
            this.signatureVerified = signatureVerified;
        }

        // Getters
        public String getPatientId() { return patientId; }
        public String getFullName() { return fullName; }
        public String getBloodType() { return bloodType; }
        public String getOrganNeeded() { return organNeeded; }
        public String getUrgencyLevel() { return urgencyLevel; }
        public String getSignatureIpfsHash() { return signatureIpfsHash; }
        public String getHospitalAddress() { return hospitalAddress; }
        public BigInteger getRegistrationTime() { return registrationTime; }
        public boolean isActive() { return isActive; }
        public boolean isSignatureVerified() { return signatureVerified; }
    }

    public static class ContractStats {
        private final BigInteger totalDonors;
        private final BigInteger totalPatients;

        public ContractStats(BigInteger totalDonors, BigInteger totalPatients) {
            this.totalDonors = totalDonors;
            this.totalPatients = totalPatients;
        }

        public BigInteger getTotalDonors() { return totalDonors; }
        public BigInteger getTotalPatients() { return totalPatients; }
    }
}
