// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "@openzeppelin/contracts/access/Ownable.sol";

/**
 * @title SignatureVerification
 * @dev Contract for storing and verifying signature hashes on IPFS
 */
contract SignatureVerification is Ownable {
    
    struct VerificationRecord {
        string ipfsHash;
        uint256 timestamp;
        bool verified;
    }
    
    mapping(uint256 => VerificationRecord) public verificationRecords;
    
    event VerificationStored(
        uint256 indexed recordId,
        string ipfsHash,
        bool verified
    );
    
    constructor() Ownable() {}
    
    /**
     * @dev Store verification record with IPFS hash
     * @param recordId Unique identifier for the record
     * @param ipfsHash IPFS hash of the signature file
     */
    function storeVerification(uint256 recordId, string memory ipfsHash) external onlyOwner {
        verificationRecords[recordId] = VerificationRecord({
            ipfsHash: ipfsHash,
            timestamp: block.timestamp,
            verified: true
        });
        
        emit VerificationStored(recordId, ipfsHash, true);
    }
    
    /**
     * @dev Get verification record by ID
     * @param recordId The record ID to query
     * @return timestamp When the record was created
     * @return verified Whether the signature is verified
     */
    function getVerification(uint256 recordId) external view returns (
        uint256 timestamp,
        uint256 verified,
        bool status
    ) {
        VerificationRecord memory record = verificationRecords[recordId];
        return (record.timestamp, record.timestamp, record.verified);
    }
}
