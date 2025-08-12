package com.organlink.service;

import com.organlink.entity.Hospital;
import com.organlink.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
     * Get all countries
     */
    public List<Map<String, String>> getCountries() {
        return Arrays.asList(
            Map.of("id", "US", "name", "United States"),
            Map.of("id", "CA", "name", "Canada"),
            Map.of("id", "UK", "name", "United Kingdom"),
            Map.of("id", "IN", "name", "India"),
            Map.of("id", "AU", "name", "Australia"),
            Map.of("id", "DE", "name", "Germany"),
            Map.of("id", "FR", "name", "France"),
            Map.of("id", "JP", "name", "Japan"),
            Map.of("id", "BR", "name", "Brazil"),
            Map.of("id", "MX", "name", "Mexico")
        );
    }

    /**
     * Get states by country
     */
    public List<Map<String, String>> getStatesByCountry(String countryId) {
        return switch (countryId.toUpperCase()) {
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
     * Get cities by state
     */
    public List<String> getCitiesByState(String stateId) {
        return switch (stateId.toUpperCase()) {
            case "CA" -> Arrays.asList("Los Angeles", "San Francisco", "San Diego", "Sacramento", "San Jose", "Fresno", "Long Beach", "Oakland", "Bakersfield", "Anaheim");
            case "NY" -> Arrays.asList("New York", "Buffalo", "Rochester", "Yonkers", "Syracuse", "Albany", "New Rochelle", "Mount Vernon", "Schenectady", "Utica");
            case "TX" -> Arrays.asList("Houston", "San Antonio", "Dallas", "Austin", "Fort Worth", "El Paso", "Arlington", "Corpus Christi", "Plano", "Laredo");
            case "FL" -> Arrays.asList("Jacksonville", "Miami", "Tampa", "Orlando", "St. Petersburg", "Hialeah", "Tallahassee", "Fort Lauderdale", "Port St. Lucie", "Cape Coral");
            case "IL" -> Arrays.asList("Chicago", "Aurora", "Rockford", "Joliet", "Naperville", "Springfield", "Peoria", "Elgin", "Waukegan", "Cicero");
            case "ON" -> Arrays.asList("Toronto", "Ottawa", "Mississauga", "Brampton", "Hamilton", "London", "Markham", "Vaughan", "Kitchener", "Windsor");
            case "BC" -> Arrays.asList("Vancouver", "Surrey", "Burnaby", "Richmond", "Abbotsford", "Coquitlam", "Kelowna", "Saanich", "Delta", "Langley");
            case "MH" -> Arrays.asList("Mumbai", "Pune", "Nagpur", "Thane", "Nashik", "Aurangabad", "Solapur", "Amravati", "Kolhapur", "Sangli");
            case "KA" -> Arrays.asList("Bangalore", "Mysore", "Hubli", "Mangalore", "Belgaum", "Gulbarga", "Davanagere", "Bellary", "Bijapur", "Shimoga");
            case "TN" -> Arrays.asList("Chennai", "Coimbatore", "Madurai", "Tiruchirappalli", "Salem", "Tirunelveli", "Erode", "Vellore", "Thoothukudi", "Dindigul");
            default -> Arrays.asList("City 1", "City 2", "City 3", "City 4", "City 5");
        };
    }

    /**
     * Get hospitals by city and state
     */
    public List<Hospital> getHospitalsByCity(String city, String stateId) {
        // First try to get from database
        List<Hospital> hospitalsFromDb = hospitalRepository.findByCityAndState(city, stateId);
        
        if (!hospitalsFromDb.isEmpty()) {
            return hospitalsFromDb;
        }
        
        // If no hospitals in database, return empty list
        // In a real system, you might want to return some default hospitals
        return Arrays.asList();
    }

    /**
     * Get all hospitals (for admin purposes)
     */
    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findActiveHospitals();
    }
}
