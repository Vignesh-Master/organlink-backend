package com.organlink.service.impl;

import com.organlink.service.OcrService;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class OcrServiceImpl implements OcrService {

    @Value("${ocr.tesseract.data-path}")
    private String tesseractDataPath;

    @Value("${ocr.confidence-threshold}")
    private double confidenceThreshold;

    @Override
    public boolean verifySignature(MultipartFile signatureImage, String expectedName) throws Exception {
        if (signatureImage.isEmpty()) {
            throw new IllegalArgumentException("Signature image cannot be empty.");
        }

        Path tempFile = null;
        try {
            // Create a temporary file to store the uploaded image
            tempFile = Files.createTempFile("signature_", ".tmp");
            Files.copy(signatureImage.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(tesseractDataPath);
            tesseract.setLanguage("eng"); // Assuming English for signature names

            String extractedText = tesseract.doOCR(tempFile.toFile());

            // Basic verification: check if the extracted text contains the expected name
            // You might want to implement more sophisticated fuzzy matching or AI models here
            boolean nameMatches = extractedText.toLowerCase().contains(expectedName.toLowerCase());

            // Optional: Check confidence level if Tesseract provides it (requires further configuration)
            // For simplicity, we're just checking if the name is present.

            return nameMatches;

        } catch (TesseractException e) {
            throw new Exception("OCR processing failed: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new Exception("Failed to read signature image file: " + e.getMessage(), e);
        } finally {
            if (tempFile != null) {
                Files.deleteIfExists(tempFile); // Clean up the temporary file
            }
        }
    }
}
