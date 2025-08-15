package com.organlink.service;

import com.organlink.entity.Hospital;
import com.organlink.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Location service for country, state, city, and hospital data
 * Provides location hierarchy for frontend forms
 */
@Service
public class LocationService {

    @Autowired
    private HospitalRepository hospitalRepository;

    /**
     * Get all countries from hospitals in database
     */
    public List<Map<String, String>> getCountries() {
        System.out.println("🌍 Fetching countries from database...");
        List<String> dbCountries = hospitalRepository.findDistinctCountries();
        System.out.println("Found " + dbCountries.size() + " countries in database: " + dbCountries);

        List<Map<String, String>> countries = new ArrayList<>();
        for (String countryName : dbCountries) {
            String countryId = countryName.toUpperCase().replaceAll("\\s+", "_");
            countries.add(Map.of("id", countryId, "name", countryName));
        }

        // If no countries in database, return default
        if (countries.isEmpty()) {
            System.out.println("⚠️ No countries found in database, returning defaults");
            return Arrays.asList(
                Map.of("id", "INDIA", "name", "India"),
                Map.of("id", "US", "name", "United States")
            );
        }

        return countries;
    }

    /**
     * Get states by country from hospitals in database
     */
    public List<Map<String, String>> getStatesByCountry(String countryId) {
        System.out.println("🏛️ Fetching states for country: " + countryId);

        // Convert countryId back to country name for database lookup
        String countryName = countryId.replaceAll("_", " ");
        List<String> dbStates = hospitalRepository.findDistinctStatesByCountry(countryName);
        System.out.println("Found " + dbStates.size() + " states in database: " + dbStates);

        List<Map<String, String>> states = new ArrayList<>();
        for (String stateName : dbStates) {
            String stateId = stateName.toUpperCase().replaceAll("\\s+", "_");
            states.add(Map.of("id", stateId, "name", stateName, "countryId", countryId));
        }

        // If no states found, return fallback based on country
        if (states.isEmpty()) {
            System.out.println("⚠️ No states found in database for " + countryName + ", returning fallback");
            return getFallbackStates(countryId);
        }

        return states;
    }

    private List<Map<String, String>> getFallbackStates(String countryId) {
        return switch (countryId.toUpperCase()) {
            case "INDIA" -> Arrays.asList(
                Map.of("id", "TAMIL_NADU", "name", "Tamil Nadu", "countryId", countryId),
                Map.of("id", "KARNATAKA", "name", "Karnataka", "countryId", countryId),
                Map.of("id", "MAHARASHTRA", "name", "Maharashtra", "countryId", countryId),
                Map.of("id", "DELHI", "name", "Delhi", "countryId", countryId)
            );
            case "US" -> Arrays.asList(
                Map.of("id", "AL", "name", "Alabama", "countryId", "US"),
                Map.of("id", "AK", "name", "Alaska", "countryId", "US"),
                Map.of("id", "AZ", "name", "Arizona", "countryId", "US"),
                Map.of("id", "AR", "name", "Arkansas", "countryId", "US"),
                Map.of("id", "CA", "name", "California", "countryId", "US"),
                Map.of("id", "CO", "name", "Colorado", "countryId", "US"),
                Map.of("id", "CT", "name", "Connecticut", "countryId", "US"),
                Map.of("id", "DE", "name", "Delaware", "countryId", "US"),
                Map.of("id", "FL", "name", "Florida", "countryId", "US"),
                Map.of("id", "GA", "name", "Georgia", "countryId", "US"),
                Map.of("id", "HI", "name", "Hawaii", "countryId", "US"),
                Map.of("id", "ID", "name", "Idaho", "countryId", "US"),
                Map.of("id", "IL", "name", "Illinois", "countryId", "US"),
                Map.of("id", "IN", "name", "Indiana", "countryId", "US"),
                Map.of("id", "IA", "name", "Iowa", "countryId", "US"),
                Map.of("id", "KS", "name", "Kansas", "countryId", "US"),
                Map.of("id", "KY", "name", "Kentucky", "countryId", "US"),
                Map.of("id", "LA", "name", "Louisiana", "countryId", "US"),
                Map.of("id", "ME", "name", "Maine", "countryId", "US"),
                Map.of("id", "MD", "name", "Maryland", "countryId", "US"),
                Map.of("id", "MA", "name", "Massachusetts", "countryId", "US"),
                Map.of("id", "MI", "name", "Michigan", "countryId", "US"),
                Map.of("id", "MN", "name", "Minnesota", "countryId", "US"),
                Map.of("id", "MS", "name", "Mississippi", "countryId", "US"),
                Map.of("id", "MO", "name", "Missouri", "countryId", "US"),
                Map.of("id", "MT", "name", "Montana", "countryId", "US"),
                Map.of("id", "NE", "name", "Nebraska", "countryId", "US"),
                Map.of("id", "NV", "name", "Nevada", "countryId", "US"),
                Map.of("id", "NH", "name", "New Hampshire", "countryId", "US"),
                Map.of("id", "NJ", "name", "New Jersey", "countryId", "US"),
                Map.of("id", "NM", "name", "New Mexico", "countryId", "US"),
                Map.of("id", "NY", "name", "New York", "countryId", "US"),
                Map.of("id", "NC", "name", "North Carolina", "countryId", "US"),
                Map.of("id", "ND", "name", "North Dakota", "countryId", "US"),
                Map.of("id", "OH", "name", "Ohio", "countryId", "US"),
                Map.of("id", "OK", "name", "Oklahoma", "countryId", "US"),
                Map.of("id", "OR", "name", "Oregon", "countryId", "US"),
                Map.of("id", "PA", "name", "Pennsylvania", "countryId", "US"),
                Map.of("id", "RI", "name", "Rhode Island", "countryId", "US"),
                Map.of("id", "SC", "name", "South Carolina", "countryId", "US"),
                Map.of("id", "SD", "name", "South Dakota", "countryId", "US"),
                Map.of("id", "TN", "name", "Tennessee", "countryId", "US"),
                Map.of("id", "TX", "name", "Texas", "countryId", "US"),
                Map.of("id", "UT", "name", "Utah", "countryId", "US"),
                Map.of("id", "VT", "name", "Vermont", "countryId", "US"),
                Map.of("id", "VA", "name", "Virginia", "countryId", "US"),
                Map.of("id", "WA", "name", "Washington", "countryId", "US"),
                Map.of("id", "WV", "name", "West Virginia", "countryId", "US"),
                Map.of("id", "WI", "name", "Wisconsin", "countryId", "US"),
                Map.of("id", "WY", "name", "Wyoming", "countryId", "US")
            );
            case "CA" -> Arrays.asList(
                Map.of("id", "AB", "name", "Alberta", "countryId", "CA"),
                Map.of("id", "BC", "name", "British Columbia", "countryId", "CA"),
                Map.of("id", "MB", "name", "Manitoba", "countryId", "CA"),
                Map.of("id", "NB", "name", "New Brunswick", "countryId", "CA"),
                Map.of("id", "NL", "name", "Newfoundland and Labrador", "countryId", "CA"),
                Map.of("id", "NS", "name", "Nova Scotia", "countryId", "CA"),
                Map.of("id", "ON", "name", "Ontario", "countryId", "CA"),
                Map.of("id", "PE", "name", "Prince Edward Island", "countryId", "CA"),
                Map.of("id", "QC", "name", "Quebec", "countryId", "CA"),
                Map.of("id", "SK", "name", "Saskatchewan", "countryId", "CA")
            );
            case "IN" -> Arrays.asList(
                Map.of("id", "AP", "name", "Andhra Pradesh", "countryId", "IN"),
                Map.of("id", "AR", "name", "Arunachal Pradesh", "countryId", "IN"),
                Map.of("id", "AS", "name", "Assam", "countryId", "IN"),
                Map.of("id", "BR", "name", "Bihar", "countryId", "IN"),
                Map.of("id", "CT", "name", "Chhattisgarh", "countryId", "IN"),
                Map.of("id", "DL", "name", "Delhi", "countryId", "IN"),
                Map.of("id", "GA", "name", "Goa", "countryId", "IN"),
                Map.of("id", "GJ", "name", "Gujarat", "countryId", "IN"),
                Map.of("id", "HR", "name", "Haryana", "countryId", "IN"),
                Map.of("id", "HP", "name", "Himachal Pradesh", "countryId", "IN"),
                Map.of("id", "JK", "name", "Jammu and Kashmir", "countryId", "IN"),
                Map.of("id", "JH", "name", "Jharkhand", "countryId", "IN"),
                Map.of("id", "KA", "name", "Karnataka", "countryId", "IN"),
                Map.of("id", "KL", "name", "Kerala", "countryId", "IN"),
                Map.of("id", "MP", "name", "Madhya Pradesh", "countryId", "IN"),
                Map.of("id", "MH", "name", "Maharashtra", "countryId", "IN"),
                Map.of("id", "MN", "name", "Manipur", "countryId", "IN"),
                Map.of("id", "ML", "name", "Meghalaya", "countryId", "IN"),
                Map.of("id", "MZ", "name", "Mizoram", "countryId", "IN"),
                Map.of("id", "NL", "name", "Nagaland", "countryId", "IN"),
                Map.of("id", "OR", "name", "Odisha", "countryId", "IN"),
                Map.of("id", "PB", "name", "Punjab", "countryId", "IN"),
                Map.of("id", "RJ", "name", "Rajasthan", "countryId", "IN"),
                Map.of("id", "SK", "name", "Sikkim", "countryId", "IN"),
                Map.of("id", "TN", "name", "Tamil Nadu", "countryId", "IN"),
                Map.of("id", "TG", "name", "Telangana", "countryId", "IN"),
                Map.of("id", "TR", "name", "Tripura", "countryId", "IN"),
                Map.of("id", "UP", "name", "Uttar Pradesh", "countryId", "IN"),
                Map.of("id", "UT", "name", "Uttarakhand", "countryId", "IN"),
                Map.of("id", "WB", "name", "West Bengal", "countryId", "IN")
            );
            default -> Arrays.asList(
                Map.of("id", "STATE1", "name", "State 1", "countryId", countryId),
                Map.of("id", "STATE2", "name", "State 2", "countryId", countryId)
            );
        };
    }

    /**
     * Get cities by state from hospitals in database
     */
    public List<String> getCitiesByState(String stateId) {
        System.out.println("🏙️ Fetching cities for state: " + stateId);

        // Convert stateId back to state name for database lookup
        String stateName = stateId.replaceAll("_", " ");
        List<String> dbCities = hospitalRepository.findDistinctCitiesByState(stateName);

        // Also try with the original stateId in case some hospitals were saved with state codes
        if (dbCities.isEmpty()) {
            dbCities = hospitalRepository.findDistinctCitiesByState(stateId);
        }

        System.out.println("Found " + dbCities.size() + " cities in database for state: " + stateName);

        // If no cities found, return message
        if (dbCities.isEmpty()) {
            return Arrays.asList("No cities available for " + stateName);
        }

        return dbCities;
    }

    /**
     * Get hospitals by city and state
     */
    public List<Hospital> getHospitalsByCity(String city, String stateId) {
        System.out.println("🔍 LocationService: Looking for hospitals");
        System.out.println("City: " + city);
        System.out.println("StateId: " + stateId);

        // Convert state ID to full state name for database lookup
        String stateName = getStateNameById(stateId);
        System.out.println("Converted to state name: " + stateName);

        // First try to get from database using full state name
        List<Hospital> hospitalsFromDb = hospitalRepository.findByCityAndState(city, stateName);
        System.out.println("Found " + hospitalsFromDb.size() + " hospitals in database");

        if (!hospitalsFromDb.isEmpty()) {
            return hospitalsFromDb;
        }

        // Also try with the original stateId in case some hospitals were saved with state codes
        List<Hospital> hospitalsFromDbWithCode = hospitalRepository.findByCityAndState(city, stateId);
        System.out.println("Found " + hospitalsFromDbWithCode.size() + " hospitals with state code");

        if (!hospitalsFromDbWithCode.isEmpty()) {
            return hospitalsFromDbWithCode;
        }

        // If no hospitals in database, return empty list
        return Arrays.asList();
    }

    /**
     * Convert state ID to full state name
     */
    private String getStateNameById(String stateId) {
        return switch (stateId.toUpperCase()) {
            case "CA" -> "California";
            case "NY" -> "New York";
            case "TX" -> "Texas";
            case "FL" -> "Florida";
            case "IL" -> "Illinois";
            case "PA" -> "Pennsylvania";
            case "OH" -> "Ohio";
            case "GA" -> "Georgia";
            case "NC" -> "North Carolina";
            case "MI" -> "Michigan";
            case "TN", "TAMIL_NADU" -> "Tamil Nadu";
            case "KA", "KARNATAKA" -> "Karnataka";
            case "MH", "MAHARASHTRA" -> "Maharashtra";
            case "DL", "DELHI" -> "Delhi";
            case "WB", "WEST_BENGAL" -> "West Bengal";
            case "RJ", "RAJASTHAN" -> "Rajasthan";
            case "UP", "UTTAR_PRADESH" -> "Uttar Pradesh";
            case "GJ", "GUJARAT" -> "Gujarat";
            case "AP", "ANDHRA_PRADESH" -> "Andhra Pradesh";
            case "KL", "KERALA" -> "Kerala";
            case "UNITED_STATES" -> "United States";
            case "CALIFORNIA" -> "California";
            default -> stateId.replaceAll("_", " "); // Convert underscores to spaces
        };
    }

    /**
     * Get all hospitals (for admin purposes)
     */
    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findActiveHospitals();
    }
}
