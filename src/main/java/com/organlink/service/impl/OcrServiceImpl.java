package com.organlink.service.impl;

import com.organlink.service.OcrService;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.regex.Pattern;

@Service
public class OcrServiceImpl implements OcrService {

    private static final Logger logger = LoggerFactory.getLogger(OcrServiceImpl.class);
    
    @Value("${ocr.tesseract.data-path}")
    private String tesseractDataPath;

    @Value("${ocr.confidence-threshold}")
    private double confidenceThreshold;

    @Override
    public boolean verifySignature(MultipartFile signatureImage, String expectedName) throws Exception {
        if (signatureImage.isEmpty()) {
            throw new IllegalArgumentException("Signature image cannot be empty.");
        }

        if (expectedName == null || expectedName.trim().isEmpty()) {
            throw new IllegalArgumentException("Expected name cannot be empty.");
        }

        Path tempFile = null;
        try {
            logger.info("🔍 Starting OCR verification for expected name: {}", expectedName);
            
            // Create a temporary file to store the uploaded image
            tempFile = Files.createTempFile("signature_", ".tmp");
            Files.copy(signatureImage.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            ITesseract tesseract = new Tesseract();
            
            // Configure Tesseract for better handwriting recognition
            if (tesseractDataPath != null && !tesseractDataPath.isEmpty()) {
                tesseract.setDatapath(tesseractDataPath);
            }
            tesseract.setLanguage("eng");
            
            // Optimize for handwritten text
            tesseract.setPageSegMode(8); // Single word mode
            tesseract.setOcrEngineMode(1); // Neural nets LSTM engine
            
            String extractedText = tesseract.doOCR(tempFile.toFile());
            logger.info("📝 Extracted text: '{}'", extractedText);

            // Enhanced verification with fuzzy matching
            boolean isVerified = performFuzzyNameMatching(extractedText, expectedName);
            
            logger.info("✅ Signature verification result: {} for name '{}'", isVerified, expectedName);
            return isVerified;

        } catch (TesseractException e) {
            logger.error("❌ OCR processing failed: {}", e.getMessage());
            throw new Exception("OCR processing failed: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("❌ Failed to read signature image: {}", e.getMessage());
            throw new Exception("Failed to read signature image file: " + e.getMessage(), e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    logger.warn("⚠️ Failed to delete temp file: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Performs fuzzy matching between extracted text and expected name
     * Handles common OCR errors in handwritten signatures
     */
    private boolean performFuzzyNameMatching(String extractedText, String expectedName) {
        if (extractedText == null || extractedText.trim().isEmpty()) {
            logger.warn("⚠️ No text extracted from signature image");
            return false;
        }

        // Clean and normalize both texts
        String cleanExtracted = cleanText(extractedText);
        String cleanExpected = cleanText(expectedName);
        
        logger.debug("🔍 Comparing cleaned extracted: '{}' with expected: '{}'", cleanExtracted, cleanExpected);

        // Strategy 1: Direct substring match (case insensitive)
        if (cleanExtracted.contains(cleanExpected)) {
            logger.info("✅ Direct match found");
            return true;
        }

        // Strategy 2: Check individual name parts
        String[] expectedParts = cleanExpected.split("\\s+");
        String[] extractedParts = cleanExtracted.split("\\s+");
        
        int matchedParts = 0;
        for (String expectedPart : expectedParts) {
            if (expectedPart.length() >= 2) { // Only check meaningful parts
                for (String extractedPart : extractedParts) {
                    if (extractedPart.length() >= 2) {
                        // Check if parts match with some tolerance for OCR errors
                        if (calculateSimilarity(expectedPart, extractedPart) > 0.7) {
                            matchedParts++;
                            break;
                        }
                    }
                }
            }
        }

        boolean partialMatch = matchedParts >= Math.max(1, expectedParts.length / 2);
        if (partialMatch) {
            logger.info("✅ Partial match found: {}/{} parts matched", matchedParts, expectedParts.length);
        }

        // Strategy 3: Character sequence matching for short names
        if (!partialMatch && cleanExpected.length() <= 15) {
            double similarity = calculateSimilarity(cleanExpected, cleanExtracted);
            boolean similarMatch = similarity > 0.6;
            if (similarMatch) {
                logger.info("✅ Character similarity match: {}", similarity);
            }
            return similarMatch;
        }

        return partialMatch;
    }

    /**
     * Cleans text by removing special characters and normalizing whitespace
     */
    private String cleanText(String text) {
        return text.toLowerCase()
                  .replaceAll("[^a-zA-Z0-9\\s]", "") // Remove special characters
                  .replaceAll("\\s+", " ")           // Normalize whitespace
                  .trim();
    }

    /**
     * Calculates similarity between two strings using Levenshtein distance
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - (double) distance / maxLen;
    }

    /**
     * Calculates Levenshtein distance between two strings
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1),     // insertion
                    dp[i - 1][j - 1] + cost // substitution
                );
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
}
