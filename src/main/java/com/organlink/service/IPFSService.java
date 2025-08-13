package com.organlink.service;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * IPFS Service for file storage using Pinata
 * Handles file uploads to IPFS via Pinata API
 */
@Service
public class IPFSService {

    @Value("${ipfs.pinata.api-key:your_pinata_api_key}")
    private String pinataApiKey;

    @Value("${ipfs.pinata.secret-key:your_pinata_secret_key}")
    private String pinataSecretKey;

    @Value("${ipfs.pinata.jwt:your_pinata_jwt_token}")
    private String pinataJWT;

    @Value("${ipfs.pinata.gateway-url:https://gateway.pinata.cloud/ipfs/}")
    private String gatewayUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Upload file to IPFS via Pinata
     */
    public Map<String, Object> uploadFile(MultipartFile file, String fileName) throws IOException {
        if (pinataJWT.equals("your_pinata_jwt_token")) {
            // Return mock response if Pinata is not configured
            return createMockResponse(fileName);
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost uploadFile = new HttpPost("https://api.pinata.cloud/pinning/pinFileToIPFS");
            
            // Set headers
            uploadFile.setHeader("Authorization", "Bearer " + pinataJWT);
            
            // Build multipart entity
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", file.getInputStream(), 
                    org.apache.http.entity.ContentType.DEFAULT_BINARY, fileName);
            
            // Add metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("name", fileName);
            metadata.put("keyvalues", Map.of(
                "uploadedBy", "OrganLink",
                "timestamp", System.currentTimeMillis(),
                "fileSize", file.getSize()
            ));
            
            builder.addTextBody("pinataMetadata", objectMapper.writeValueAsString(metadata),
                    org.apache.http.entity.ContentType.APPLICATION_JSON);
            
            uploadFile.setEntity(builder.build());
            
            try (CloseableHttpResponse response = httpClient.execute(uploadFile)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                
                if (response.getStatusLine().getStatusCode() == 200) {
                    JsonNode jsonResponse = objectMapper.readTree(result);
                    return createSuccessResponse(jsonResponse, fileName, file.getSize());
                } else {
                    throw new IOException("Failed to upload to IPFS: " + result);
                }
            }
        }
    }

    /**
     * Get file from IPFS
     */
    public String getFileUrl(String ipfsHash) {
        return gatewayUrl + ipfsHash;
    }

    /**
     * Pin existing file by hash
     */
    public Map<String, Object> pinByHash(String ipfsHash) throws IOException {
        if (pinataJWT.equals("your_pinata_jwt_token")) {
            return createMockPinResponse(ipfsHash);
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost pinByHash = new HttpPost("https://api.pinata.cloud/pinning/pinByHash");
            
            pinByHash.setHeader("Authorization", "Bearer " + pinataJWT);
            pinByHash.setHeader("Content-Type", "application/json");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("hashToPin", ipfsHash);
            requestBody.put("pinataMetadata", Map.of(
                "name", "Pinned by OrganLink",
                "keyvalues", Map.of("pinnedBy", "OrganLink")
            ));
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            pinByHash.setEntity(new org.apache.http.entity.StringEntity(jsonBody));
            
            try (CloseableHttpResponse response = httpClient.execute(pinByHash)) {
                String result = EntityUtils.toString(response.getEntity());
                
                if (response.getStatusLine().getStatusCode() == 200) {
                    JsonNode jsonResponse = objectMapper.readTree(result);
                    return Map.of(
                        "success", true,
                        "ipfsHash", jsonResponse.get("ipfsHash").asText(),
                        "pinSize", jsonResponse.get("pinSize").asLong(),
                        "timestamp", jsonResponse.get("timestamp").asText()
                    );
                } else {
                    throw new IOException("Failed to pin hash: " + result);
                }
            }
        }
    }

    /**
     * Unpin file from IPFS
     */
    public boolean unpinFile(String ipfsHash) throws IOException {
        if (pinataJWT.equals("your_pinata_jwt_token")) {
            return true; // Mock success
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            org.apache.http.client.methods.HttpDelete unpinFile = 
                new org.apache.http.client.methods.HttpDelete("https://api.pinata.cloud/pinning/unpin/" + ipfsHash);
            
            unpinFile.setHeader("Authorization", "Bearer " + pinataJWT);
            
            try (CloseableHttpResponse response = httpClient.execute(unpinFile)) {
                return response.getStatusLine().getStatusCode() == 200;
            }
        }
    }

    /**
     * Get pinned files list
     */
    public Map<String, Object> getPinnedFiles(int limit, int offset) throws IOException {
        if (pinataJWT.equals("your_pinata_jwt_token")) {
            return createMockPinnedFilesResponse();
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String url = String.format("https://api.pinata.cloud/data/pinList?status=pinned&pageLimit=%d&pageOffset=%d", 
                    limit, offset);
            org.apache.http.client.methods.HttpGet getPinned = 
                new org.apache.http.client.methods.HttpGet(url);
            
            getPinned.setHeader("Authorization", "Bearer " + pinataJWT);
            
            try (CloseableHttpResponse response = httpClient.execute(getPinned)) {
                String result = EntityUtils.toString(response.getEntity());
                
                if (response.getStatusLine().getStatusCode() == 200) {
                    return objectMapper.readValue(result, Map.class);
                } else {
                    throw new IOException("Failed to get pinned files: " + result);
                }
            }
        }
    }

    // Helper methods for mock responses
    private Map<String, Object> createMockResponse(String fileName) {
        String mockHash = "Qm" + System.currentTimeMillis() + "MockHash";
        return Map.of(
            "success", true,
            "ipfsHash", mockHash,
            "pinSize", 1024L,
            "timestamp", java.time.LocalDateTime.now().toString(),
            "fileName", fileName,
            "gatewayUrl", gatewayUrl + mockHash
        );
    }

    private Map<String, Object> createSuccessResponse(JsonNode jsonResponse, String fileName, long fileSize) {
        String ipfsHash = jsonResponse.get("IpfsHash").asText();
        return Map.of(
            "success", true,
            "ipfsHash", ipfsHash,
            "pinSize", jsonResponse.get("PinSize").asLong(),
            "timestamp", jsonResponse.get("Timestamp").asText(),
            "fileName", fileName,
            "fileSize", fileSize,
            "gatewayUrl", gatewayUrl + ipfsHash
        );
    }

    private Map<String, Object> createMockPinResponse(String ipfsHash) {
        return Map.of(
            "success", true,
            "ipfsHash", ipfsHash,
            "pinSize", 2048L,
            "timestamp", java.time.LocalDateTime.now().toString()
        );
    }

    private Map<String, Object> createMockPinnedFilesResponse() {
        return Map.of(
            "count", 5,
            "rows", java.util.List.of(
                Map.of("ipfs_pin_hash", "QmMockHash1", "size", 1024, "date_pinned", "2024-01-15T10:30:00Z"),
                Map.of("ipfs_pin_hash", "QmMockHash2", "size", 2048, "date_pinned", "2024-01-14T15:45:00Z")
            )
        );
    }

    /**
     * Test IPFS connection
     */
    public Map<String, Object> testConnection() {
        try {
            if (pinataJWT.equals("your_pinata_jwt_token")) {
                return Map.of(
                    "connected", false,
                    "message", "Pinata JWT not configured",
                    "status", "MOCK_MODE"
                );
            }

            // Test with a simple API call
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                org.apache.http.client.methods.HttpGet testAuth = 
                    new org.apache.http.client.methods.HttpGet("https://api.pinata.cloud/data/testAuthentication");
                
                testAuth.setHeader("Authorization", "Bearer " + pinataJWT);
                
                try (CloseableHttpResponse response = httpClient.execute(testAuth)) {
                    if (response.getStatusLine().getStatusCode() == 200) {
                        return Map.of(
                            "connected", true,
                            "message", "Successfully connected to Pinata",
                            "status", "CONNECTED"
                        );
                    } else {
                        return Map.of(
                            "connected", false,
                            "message", "Authentication failed",
                            "status", "AUTH_FAILED"
                        );
                    }
                }
            }
        } catch (Exception e) {
            return Map.of(
                "connected", false,
                "message", "Connection error: " + e.getMessage(),
                "status", "ERROR"
            );
        }
    }
}
