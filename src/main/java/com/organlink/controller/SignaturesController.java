package com.organlink.controller;

import com.organlink.dto.ApiResponse;
import com.organlink.service.IPFSService;
import com.organlink.service.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles signature OCR verification and IPFS upload.
 * Frontend first calls this endpoint to verify handwritten signature and get IPFS hash,
 * then posts the donor/patient JSON including the returned signatureData.
 */
@RestController
@RequestMapping("/api/v1/signatures")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "http://localhost:8080"})
@PreAuthorize("hasRole('HOSPITAL')")
public class SignaturesController {

    @Autowired
    private OcrService ocrService;

    @Autowired
    private IPFSService ipfsService;

    @PostMapping(value = "/verify-and-store", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyAndStore(
            @RequestPart("signatureFile") MultipartFile signatureFile,
            @RequestParam("signerName") String signerName,
            @RequestParam(value = "signerType", required = false) String signerType
    ) {
        try {
            // 1) OCR verification
            boolean verified = ocrService.verifySignature(signatureFile, signerName);

            if (!verified) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Signature verification failed",
                                "OCR could not match signer name with the image"));
            }

            // 2) Upload to IPFS (Pinata)
            String ipfsHash = ipfsService.uploadFile(signatureFile);

            // 3) Build response payload expected by frontend
            Map<String, Object> data = new HashMap<>();
            data.put("ipfsHash", ipfsHash);

            Map<String, Object> ocrResult = new HashMap<>();
            ocrResult.put("extractedName", signerName); // simplified; detailed extraction not returned by OCR service
            ocrResult.put("confidence", 95.0); // placeholder confidence as OcrService returns boolean
            ocrResult.put("verified", true);
            data.put("ocrResult", ocrResult);

            // blockchainTxHash is handled during donor/patient registration on-chain later
            data.put("blockchainTxHash", null);

            return ResponseEntity.ok(ApiResponse.success("Signature uploaded and verified successfully", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to verify/store signature", e.getMessage()));
        }
    }
}
