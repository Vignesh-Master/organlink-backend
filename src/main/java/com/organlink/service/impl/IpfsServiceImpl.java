package com.organlink.service.impl;

import com.organlink.service.IpfsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class IpfsServiceImpl implements IpfsService {

    @Value("${ipfs.api-url}")
    private String apiUrl;

    @Value("${ipfs.jwt-token}")
    private String jwtToken;

    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file to IPFS.");
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(jwtToken);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl + "/pinning/pinFileToIPFS", requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().containsKey("IpfsHash")) {
                return (String) response.getBody().get("IpfsHash");
            } else {
                throw new Exception("Failed to upload file to IPFS. Response: " + response.getBody());
            }
        } catch (Exception e) {
            throw new Exception("Error while communicating with IPFS service: " + e.getMessage(), e);
        }
    }
}
