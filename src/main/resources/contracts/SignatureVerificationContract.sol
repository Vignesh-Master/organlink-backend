// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract SignatureVerificationContract {

    struct Signature {
        uint256 id;
        string ipfsHash;
        string documentType; // e.g., "Donor Consent", "Patient Consent"
        address uploader; // Hospital's address
        uint256 timestamp;
    }

    Signature[] public signatures;
    uint256 public signatureCount = 0;

    mapping(string => bool) private ipfsHashExists;

    event SignatureStored(uint256 id, string ipfsHash, address uploader);

    modifier onlyUploader() {
        // In a real application, you would have a role-based access control system
        // to ensure only authorized hospitals can upload signatures.
        // For this project, we'll keep it simple and allow any address to upload.
        _;
    }

    function storeSignature(string memory _ipfsHash, string memory _documentType) public onlyUploader {
        require(!ipfsHashExists[_ipfsHash], "IPFS hash already exists.");

        signatureCount++;
        signatures.push(Signature(
            signatureCount,
            _ipfsHash,
            _documentType,
            msg.sender,
            block.timestamp
        ));

        ipfsHashExists[_ipfsHash] = true;

        emit SignatureStored(signatureCount, _ipfsHash, msg.sender);
    }

    function getSignature(uint256 _signatureId) public view returns (uint256, string memory, string memory, address, uint256) {
        require(_signatureId > 0 && _signatureId <= signatureCount, "Invalid signature ID");
        Signature storage signature = signatures[_signatureId - 1];
        return (signature.id, signature.ipfsHash, signature.documentType, signature.uploader, signature.timestamp);
    }

    function getSignatureCount() public view returns (uint256) {
        return signatureCount;
    }
}
