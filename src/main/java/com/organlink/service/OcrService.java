package com.organlink.service;

import org.springframework.web.multipart.MultipartFile;

public interface OcrService {
    boolean verifySignature(MultipartFile signatureImage, String expectedName) throws Exception;
}
