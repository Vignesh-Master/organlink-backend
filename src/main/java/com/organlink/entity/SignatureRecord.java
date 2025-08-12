package com.organlink.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Signature verification record entity
 */
@Entity
@Table(name = "signature_records")
@EntityListeners(AuditingEntityListener.class)
public class SignatureRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "file_id", unique = true, nullable = false)
    private String fileId;
    
    @NotBlank
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;
    
    @NotBlank
    @Column(name = "file_path", nullable = false)
    private String filePath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "file_type")
    private String fileType;
    
    @Column(name = "file_hash")
    private String fileHash;
    
    // IPFS Integration
    @Column(name = "ipfs_hash")
    private String ipfsHash;
    
    @Column(name = "ipfs_url")
    private String ipfsUrl;
    
    // OCR Results
    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;
    
    @Column(name = "extracted_name")
    private String extractedName;
    
    @Column(name = "expected_name")
    private String expectedName;
    
    @Column(name = "ocr_confidence")
    private Double ocrConfidence;
    
    @Column(name = "name_match_confidence")
    private Double nameMatchConfidence;
    
    // Verification Results
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    
    @Column(name = "verification_details", columnDefinition = "JSON")
    private String verificationDetails;
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    // Signer Information
    @NotBlank
    @Column(name = "signer_name", nullable = false)
    private String signerName;
    
    @NotBlank
    @Column(name = "signer_type", nullable = false)
    private String signerType; // DONOR, PATIENT, HOSPITAL_STAFF, etc.
    
    @Column(name = "signer_id")
    private String signerId;
    
    // Blockchain Integration
    @Column(name = "blockchain_tx_hash")
    private String blockchainTxHash;
    
    @Column(name = "smart_contract_address")
    private String smartContractAddress;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public SignatureRecord() {}
    
    public SignatureRecord(String fileId, String originalFilename, String filePath, String signerName, String signerType) {
        this.fileId = fileId;
        this.originalFilename = originalFilename;
        this.filePath = filePath;
        this.signerName = signerName;
        this.signerType = signerType;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
    
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }
    
    public String getIpfsHash() { return ipfsHash; }
    public void setIpfsHash(String ipfsHash) { this.ipfsHash = ipfsHash; }
    
    public String getIpfsUrl() { return ipfsUrl; }
    public void setIpfsUrl(String ipfsUrl) { this.ipfsUrl = ipfsUrl; }
    
    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }
    
    public String getExtractedName() { return extractedName; }
    public void setExtractedName(String extractedName) { this.extractedName = extractedName; }
    
    public String getExpectedName() { return expectedName; }
    public void setExpectedName(String expectedName) { this.expectedName = expectedName; }
    
    public Double getOcrConfidence() { return ocrConfidence; }
    public void setOcrConfidence(Double ocrConfidence) { this.ocrConfidence = ocrConfidence; }
    
    public Double getNameMatchConfidence() { return nameMatchConfidence; }
    public void setNameMatchConfidence(Double nameMatchConfidence) { this.nameMatchConfidence = nameMatchConfidence; }
    
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
    
    public String getVerificationDetails() { return verificationDetails; }
    public void setVerificationDetails(String verificationDetails) { this.verificationDetails = verificationDetails; }
    
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    
    public String getSignerName() { return signerName; }
    public void setSignerName(String signerName) { this.signerName = signerName; }
    
    public String getSignerType() { return signerType; }
    public void setSignerType(String signerType) { this.signerType = signerType; }
    
    public String getSignerId() { return signerId; }
    public void setSignerId(String signerId) { this.signerId = signerId; }
    
    public String getBlockchainTxHash() { return blockchainTxHash; }
    public void setBlockchainTxHash(String blockchainTxHash) { this.blockchainTxHash = blockchainTxHash; }
    
    public String getSmartContractAddress() { return smartContractAddress; }
    public void setSmartContractAddress(String smartContractAddress) { this.smartContractAddress = smartContractAddress; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Utility methods
    public boolean isVerified() {
        return verificationStatus == VerificationStatus.VERIFIED;
    }
    
    public boolean isNameMatched() {
        return nameMatchConfidence != null && nameMatchConfidence >= 0.8;
    }
}
