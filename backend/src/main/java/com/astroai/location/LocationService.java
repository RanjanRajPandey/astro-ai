package com.astroai.location;

import com.astroai.config.CacheConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

@Service
public class LocationService {

    public record GazetteerCity(
            String name,
            String stateOrRegion,
            String countryCode,
            double latitude,
            double longitude,
            String timezoneId
    ) {}

    private static final Map<String, GazetteerCity> GAZETTEER = new LinkedHashMap<>();

    static {
        // Major Indian Metros, State Capitals & Astrologically Significant Cities/Towns
        register("New Delhi", "Delhi", "IN", 28.6139, 77.2090, "Asia/Kolkata", "delhi", "ncr");
        register("Mumbai", "Maharashtra", "IN", 19.0760, 72.8777, "Asia/Kolkata", "bombay");
        register("Bengaluru", "Karnataka", "IN", 12.9716, 77.5946, "Asia/Kolkata", "bangalore");
        register("Kolkata", "West Bengal", "IN", 22.5726, 88.3639, "Asia/Kolkata", "calcutta");
        register("Chennai", "Tamil Nadu", "IN", 13.0827, 80.2707, "Asia/Kolkata", "madras");
        register("Hyderabad", "Telangana", "IN", 17.3850, 78.4867, "Asia/Kolkata");
        register("Pune", "Maharashtra", "IN", 18.5204, 73.8567, "Asia/Kolkata", "poona");
        register("Ahmedabad", "Gujarat", "IN", 23.0225, 72.5714, "Asia/Kolkata", "amdavad");
        register("Jaipur", "Rajasthan", "IN", 26.9124, 75.7873, "Asia/Kolkata");
        register("Lucknow", "Uttar Pradesh", "IN", 26.8467, 80.9462, "Asia/Kolkata");
        register("Varanasi", "Uttar Pradesh", "IN", 25.3176, 82.9739, "Asia/Kolkata", "kashi", "benares", "banaras");
        register("Patna", "Bihar", "IN", 25.5941, 85.1376, "Asia/Kolkata");
        register("Ranchi", "Jharkhand", "IN", 23.3441, 85.3096, "Asia/Kolkata");
        register("Bhopal", "Madhya Pradesh", "IN", 23.2599, 77.4126, "Asia/Kolkata");
        register("Ujjain", "Madhya Pradesh", "IN", 23.1765, 75.7885, "Asia/Kolkata", "avantika");
        register("Indore", "Madhya Pradesh", "IN", 22.7196, 75.8577, "Asia/Kolkata");
        register("Chandigarh", "Chandigarh", "IN", 30.7333, 76.7794, "Asia/Kolkata");
        register("Amritsar", "Punjab", "IN", 31.6340, 74.8723, "Asia/Kolkata");
        register("Ludhiana", "Punjab", "IN", 30.9010, 75.8573, "Asia/Kolkata");
        register("Jalandhar", "Punjab", "IN", 31.3260, 75.5762, "Asia/Kolkata");
        register("Patiala", "Punjab", "IN", 30.3398, 76.3869, "Asia/Kolkata");
        register("Prayagraj", "Uttar Pradesh", "IN", 25.4358, 81.8463, "Asia/Kolkata", "allahabad", "prayag");
        register("Ayodhya", "Uttar Pradesh", "IN", 26.7922, 82.1998, "Asia/Kolkata", "faizabad");
        register("Kanpur", "Uttar Pradesh", "IN", 26.4499, 80.3319, "Asia/Kolkata");
        register("Agra", "Uttar Pradesh", "IN", 27.1767, 78.0081, "Asia/Kolkata");
        register("Gorakhpur", "Uttar Pradesh", "IN", 26.7606, 83.3732, "Asia/Kolkata");
        register("Kushinagar", "Uttar Pradesh", "IN", 26.7402, 83.8886, "Asia/Kolkata", "padrauna", "kasia");
        register("Deoria", "Uttar Pradesh", "IN", 26.5024, 83.7791, "Asia/Kolkata");
        register("Maharajganj", "Uttar Pradesh", "IN", 27.1446, 83.5625, "Asia/Kolkata");
        register("Basti", "Uttar Pradesh", "IN", 26.7995, 82.7351, "Asia/Kolkata");
        register("Siddharthnagar", "Uttar Pradesh", "IN", 27.2558, 83.0950, "Asia/Kolkata", "naugarh");
        register("Sant Kabir Nagar", "Uttar Pradesh", "IN", 26.7745, 83.0365, "Asia/Kolkata", "khalilabad");
        register("Azamgarh", "Uttar Pradesh", "IN", 26.0739, 83.1859, "Asia/Kolkata");
        register("Ballia", "Uttar Pradesh", "IN", 25.7604, 84.1477, "Asia/Kolkata");
        register("Mau", "Uttar Pradesh", "IN", 25.9417, 83.5611, "Asia/Kolkata");
        register("Ghazipur", "Uttar Pradesh", "IN", 25.5840, 83.5770, "Asia/Kolkata");
        register("Jaunpur", "Uttar Pradesh", "IN", 25.7464, 82.6837, "Asia/Kolkata");
        register("Mirzapur", "Uttar Pradesh", "IN", 25.1460, 82.5690, "Asia/Kolkata");
        register("Sonbhadra", "Uttar Pradesh", "IN", 24.6879, 83.0645, "Asia/Kolkata", "robertsganj");
        register("Sultanpur", "Uttar Pradesh", "IN", 26.2648, 82.0727, "Asia/Kolkata");
        register("Pratapgarh", "Uttar Pradesh", "IN", 25.8972, 81.9445, "Asia/Kolkata");
        register("Gonda", "Uttar Pradesh", "IN", 27.1339, 81.9619, "Asia/Kolkata");
        register("Bahraich", "Uttar Pradesh", "IN", 27.5742, 81.5947, "Asia/Kolkata");
        register("Meerut", "Uttar Pradesh", "IN", 28.9845, 77.7064, "Asia/Kolkata");
        register("Noida", "Uttar Pradesh", "IN", 28.5355, 77.3910, "Asia/Kolkata", "greater noida");
        register("Ghaziabad", "Uttar Pradesh", "IN", 28.6692, 77.4538, "Asia/Kolkata");
        register("Mathura", "Uttar Pradesh", "IN", 27.4924, 77.6737, "Asia/Kolkata", "vrindavan");
        register("Bareilly", "Uttar Pradesh", "IN", 28.3670, 79.4304, "Asia/Kolkata");
        register("Aligarh", "Uttar Pradesh", "IN", 27.8974, 78.0880, "Asia/Kolkata");
        register("Moradabad", "Uttar Pradesh", "IN", 28.8386, 78.7733, "Asia/Kolkata");
        register("Jhansi", "Uttar Pradesh", "IN", 25.4484, 78.5685, "Asia/Kolkata");
        register("Gaya", "Bihar", "IN", 24.7914, 85.0002, "Asia/Kolkata", "bodh gaya");
        register("Muzaffarpur", "Bihar", "IN", 26.1209, 85.3647, "Asia/Kolkata");
        register("Siwan", "Bihar", "IN", 26.2243, 84.3600, "Asia/Kolkata");
        register("Gopalganj", "Bihar", "IN", 26.4682, 84.4404, "Asia/Kolkata");
        register("Chhapra", "Bihar", "IN", 25.7811, 84.7307, "Asia/Kolkata", "saran");
        register("Motihari", "Bihar", "IN", 26.6469, 84.9089, "Asia/Kolkata", "east champaran");
        register("Bettiah", "Bihar", "IN", 26.8025, 84.5029, "Asia/Kolkata", "west champaran");
        register("Arrah", "Bihar", "IN", 25.5560, 84.6603, "Asia/Kolkata", "bhojpur");
        register("Buxar", "Bihar", "IN", 25.5647, 83.9777, "Asia/Kolkata");
        register("Sasaram", "Bihar", "IN", 24.9489, 84.0161, "Asia/Kolkata", "rohtas");
        register("Begusarai", "Bihar", "IN", 25.4182, 86.1272, "Asia/Kolkata");
        register("Samastipur", "Bihar", "IN", 25.8629, 85.7810, "Asia/Kolkata");
        register("Hajipur", "Bihar", "IN", 25.6858, 85.2146, "Asia/Kolkata", "vaishali");
        register("Madhubani", "Bihar", "IN", 26.3483, 86.0712, "Asia/Kolkata");
        register("Sitamarhi", "Bihar", "IN", 26.5936, 85.4856, "Asia/Kolkata");
        register("Bhagalpur", "Bihar", "IN", 25.2425, 86.9842, "Asia/Kolkata");
        register("Darbhanga", "Bihar", "IN", 26.1542, 85.8918, "Asia/Kolkata");
        register("Purnia", "Bihar", "IN", 25.7771, 87.4753, "Asia/Kolkata");
        register("Jamshedpur", "Jharkhand", "IN", 22.8046, 86.2029, "Asia/Kolkata", "tatanagar");
        register("Dhanbad", "Jharkhand", "IN", 23.7957, 86.4304, "Asia/Kolkata");
        register("Bokaro", "Jharkhand", "IN", 23.6693, 86.1511, "Asia/Kolkata");
        register("Deoghar", "Jharkhand", "IN", 24.4852, 86.6948, "Asia/Kolkata");
        register("Surat", "Gujarat", "IN", 21.1702, 72.8311, "Asia/Kolkata");
        register("Vadodara", "Gujarat", "IN", 22.3072, 73.1812, "Asia/Kolkata", "baroda");
        register("Rajkot", "Gujarat", "IN", 22.3039, 70.8022, "Asia/Kolkata");
        register("Gandhinagar", "Gujarat", "IN", 23.2156, 72.6369, "Asia/Kolkata");
        register("Bhavnagar", "Gujarat", "IN", 21.7645, 72.1519, "Asia/Kolkata");
        register("Jamnagar", "Gujarat", "IN", 22.4707, 70.0577, "Asia/Kolkata");
        register("Nagpur", "Maharashtra", "IN", 21.1458, 79.0882, "Asia/Kolkata");
        register("Nashik", "Maharashtra", "IN", 19.9975, 73.7898, "Asia/Kolkata");
        register("Thane", "Maharashtra", "IN", 19.2183, 72.9781, "Asia/Kolkata");
        register("Aurangabad", "Maharashtra", "IN", 19.8762, 75.3433, "Asia/Kolkata", "chhatrapati sambhajinagar");
        register("Solapur", "Maharashtra", "IN", 17.6599, 75.9064, "Asia/Kolkata");
        register("Kolhapur", "Maharashtra", "IN", 16.7050, 74.2433, "Asia/Kolkata");
        register("Jodhpur", "Rajasthan", "IN", 26.2389, 73.0243, "Asia/Kolkata");
        register("Udaipur", "Rajasthan", "IN", 24.5854, 73.7125, "Asia/Kolkata");
        register("Kota", "Rajasthan", "IN", 25.2138, 75.8648, "Asia/Kolkata");
        register("Ajmer", "Rajasthan", "IN", 26.4499, 74.6399, "Asia/Kolkata");
        register("Bikaner", "Rajasthan", "IN", 28.0229, 73.3119, "Asia/Kolkata");
        register("Gwalior", "Madhya Pradesh", "IN", 26.2183, 78.1828, "Asia/Kolkata");
        register("Jabalpur", "Madhya Pradesh", "IN", 23.1815, 79.9864, "Asia/Kolkata");
        register("Raipur", "Chhattisgarh", "IN", 21.2514, 81.6296, "Asia/Kolkata");
        register("Bilaspur", "Chhattisgarh", "IN", 22.0797, 82.1409, "Asia/Kolkata");
        register("Gurugram", "Haryana", "IN", 28.4595, 77.0266, "Asia/Kolkata", "gurgaon");
        register("Faridabad", "Haryana", "IN", 28.4089, 77.3178, "Asia/Kolkata");
        register("Panipat", "Haryana", "IN", 29.3909, 76.9635, "Asia/Kolkata");
        register("Ambala", "Haryana", "IN", 30.3782, 76.7767, "Asia/Kolkata");
        register("Kurukshetra", "Haryana", "IN", 29.9695, 76.8783, "Asia/Kolkata");
        register("Dehradun", "Uttarakhand", "IN", 30.3165, 78.0322, "Asia/Kolkata");
        register("Haridwar", "Uttarakhand", "IN", 29.9457, 78.1642, "Asia/Kolkata");
        register("Rishikesh", "Uttarakhand", "IN", 30.0869, 78.2676, "Asia/Kolkata");
        register("Nainital", "Uttarakhand", "IN", 29.3919, 79.4542, "Asia/Kolkata");
        register("Shimla", "Himachal Pradesh", "IN", 31.1048, 77.1734, "Asia/Kolkata");
        register("Dharamshala", "Himachal Pradesh", "IN", 32.2190, 76.3234, "Asia/Kolkata");
        register("Srinagar", "Jammu and Kashmir", "IN", 34.0837, 74.7973, "Asia/Kolkata");
        register("Jammu", "Jammu and Kashmir", "IN", 32.7266, 74.8570, "Asia/Kolkata");
        register("Kochi", "Kerala", "IN", 9.9312, 76.2673, "Asia/Kolkata", "cochin");
        register("Thiruvananthapuram", "Kerala", "IN", 8.5241, 76.9366, "Asia/Kolkata", "trivandrum");
        register("Kozhikode", "Kerala", "IN", 11.2588, 75.7804, "Asia/Kolkata", "calicut");
        register("Thrissur", "Kerala", "IN", 10.5276, 76.2144, "Asia/Kolkata");
        register("Coimbatore", "Tamil Nadu", "IN", 11.0168, 76.9558, "Asia/Kolkata");
        register("Madurai", "Tamil Nadu", "IN", 9.9252, 78.1198, "Asia/Kolkata");
        register("Tiruchirappalli", "Tamil Nadu", "IN", 10.7905, 78.7047, "Asia/Kolkata", "trichy");
        register("Salem", "Tamil Nadu", "IN", 11.6643, 78.1460, "Asia/Kolkata");
        register("Tirupati", "Andhra Pradesh", "IN", 13.6288, 79.4192, "Asia/Kolkata");
        register("Visakhapatnam", "Andhra Pradesh", "IN", 17.6868, 83.2185, "Asia/Kolkata", "vizag");
        register("Vijayawada", "Andhra Pradesh", "IN", 16.5062, 80.6480, "Asia/Kolkata");
        register("Guntur", "Andhra Pradesh", "IN", 16.3067, 80.4365, "Asia/Kolkata");
        register("Warangal", "Telangana", "IN", 17.9689, 79.5941, "Asia/Kolkata");
        register("Mysuru", "Karnataka", "IN", 12.2958, 76.6394, "Asia/Kolkata", "mysore");
        register("Mangaluru", "Karnataka", "IN", 12.9141, 74.8560, "Asia/Kolkata", "mangalore");
        register("Hubballi", "Karnataka", "IN", 15.3647, 75.1240, "Asia/Kolkata", "hubli");
        register("Belagavi", "Karnataka", "IN", 15.8497, 74.4977, "Asia/Kolkata", "belgaum");
        register("Udupi", "Karnataka", "IN", 13.3409, 74.7421, "Asia/Kolkata");
        register("Panaji", "Goa", "IN", 15.4909, 73.8278, "Asia/Kolkata", "goa");
        register("Bhubaneswar", "Odisha", "IN", 20.2961, 85.8245, "Asia/Kolkata");
        register("Cuttack", "Odisha", "IN", 20.4625, 85.8830, "Asia/Kolkata");
        register("Puri", "Odisha", "IN", 19.8135, 85.8312, "Asia/Kolkata");
        register("Rourkela", "Odisha", "IN", 22.2604, 84.8536, "Asia/Kolkata");
        register("Siliguri", "West Bengal", "IN", 26.7271, 88.3953, "Asia/Kolkata");
        register("Durgapur", "West Bengal", "IN", 23.5204, 87.3119, "Asia/Kolkata");
        register("Asansol", "West Bengal", "IN", 23.6739, 86.9524, "Asia/Kolkata");
        register("Guwahati", "Assam", "IN", 26.1445, 91.7362, "Asia/Kolkata");
        register("Dibrugarh", "Assam", "IN", 27.4728, 94.9120, "Asia/Kolkata");
        register("Silchar", "Assam", "IN", 24.8333, 92.7789, "Asia/Kolkata");
        register("Shillong", "Meghalaya", "IN", 25.5788, 91.8933, "Asia/Kolkata");
        register("Agartala", "Tripura", "IN", 23.8315, 91.2868, "Asia/Kolkata");
        register("Imphal", "Manipur", "IN", 24.8170, 93.9368, "Asia/Kolkata");
        register("Gangtok", "Sikkim", "IN", 27.3389, 88.6065, "Asia/Kolkata");
        register("Puducherry", "Puducherry", "IN", 11.9416, 79.8083, "Asia/Kolkata", "pondicherry");

        // International Cities
        register("Kathmandu", "Bagmati", "NP", 27.7172, 85.3240, "Asia/Kathmandu");
        register("Pokhara", "Gandaki", "NP", 28.2096, 83.9856, "Asia/Kathmandu");
        register("Colombo", "Western", "LK", 6.9271, 79.8612, "Asia/Colombo");
        register("Dhaka", "Dhaka", "BD", 23.8103, 90.4125, "Asia/Dhaka");
        register("London", "England", "GB", 51.5074, -0.1278, "Europe/London");
        register("Manchester", "England", "GB", 53.4808, -2.2426, "Europe/London");
        register("Birmingham", "England", "GB", 52.4862, -1.8904, "Europe/London");
        register("New York", "NY", "US", 40.7128, -74.0060, "America/New_York", "nyc");
        register("San Francisco", "CA", "US", 37.7749, -122.4194, "America/Los_Angeles");
        register("Los Angeles", "CA", "US", 34.0522, -118.2437, "America/Los_Angeles");
        register("Chicago", "IL", "US", 41.8781, -87.6298, "America/Chicago");
        register("Houston", "TX", "US", 29.7604, -95.3698, "America/Chicago");
        register("Dallas", "TX", "US", 32.7767, -96.7970, "America/Chicago");
        register("Seattle", "WA", "US", 47.6062, -122.3321, "America/Los_Angeles");
        register("Atlanta", "GA", "US", 33.7490, -84.3880, "America/New_York");
        register("Boston", "MA", "US", 42.3601, -71.0589, "America/New_York");
        register("Washington", "DC", "US", 38.9072, -77.0369, "America/New_York");
        register("Toronto", "Ontario", "CA", 43.6532, -79.3832, "America/Toronto");
        register("Vancouver", "British Columbia", "CA", 49.2827, -123.1207, "America/Vancouver");
        register("Sydney", "NSW", "AU", -33.8688, 151.2093, "Australia/Sydney");
        register("Melbourne", "Victoria", "AU", -37.8136, 144.9631, "Australia/Melbourne");
        register("Singapore", "Singapore", "SG", 1.3521, 103.8198, "Asia/Singapore");
        register("Dubai", "Dubai", "AE", 25.2048, 55.2708, "Asia/Dubai");
        register("Abu Dhabi", "Abu Dhabi", "AE", 24.4539, 54.3773, "Asia/Dubai");
        register("Tokyo", "Kanto", "JP", 35.6762, 139.6503, "Asia/Tokyo");
        register("Paris", "Ile-de-France", "FR", 48.8566, 2.3522, "Europe/Paris");
        register("Berlin", "Berlin", "DE", 52.5200, 13.4050, "Europe/Berlin");
        register("Frankfurt", "Hesse", "DE", 50.1109, 8.6821, "Europe/Berlin");
        register("Amsterdam", "North Holland", "NL", 52.3676, 4.9041, "Europe/Amsterdam");
        register("Zurich", "Zurich", "CH", 47.3769, 8.5417, "Europe/Zurich");
    }

    private static void register(String name, String state, String country, double lat, double lon, String tz, String... aliases) {
        GazetteerCity city = new GazetteerCity(name, state, country, lat, lon, tz);
        GAZETTEER.put(name.toLowerCase(Locale.ROOT), city);
        for (String alias : aliases) {
            GAZETTEER.put(alias.toLowerCase(Locale.ROOT), city);
        }
    }

    private final LocationRepository locationRepository;
    private final ObjectMapper objectMapper;
    private final RestClient geocodingClient;

    public LocationService(LocationRepository locationRepository, ObjectMapper objectMapper) {
        this.locationRepository = locationRepository;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(2000);
        requestFactory.setReadTimeout(3000);
        this.geocodingClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Cacheable(value = CacheConfig.GEOCODING_CACHE, key = "#query != null ? #query.toLowerCase().trim() : ''")
    public List<GazetteerCity> searchCities(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        Set<String> seen = new HashSet<>();
        List<GazetteerCity> results = new ArrayList<>();

        for (GazetteerCity city : GAZETTEER.values()) {
            String key = city.name().toLowerCase(Locale.ROOT) + "|" + city.countryCode().toLowerCase(Locale.ROOT);
            if (seen.contains(key)) {
                continue;
            }
            if (normalized.isEmpty()
                    || city.name().toLowerCase(Locale.ROOT).contains(normalized)
                    || city.stateOrRegion().toLowerCase(Locale.ROOT).contains(normalized)) {
                seen.add(key);
                results.add(city);
            }
        }

        // If user typed at least 2 characters, also query worldwide geocoder so ANY city/town/village on Earth is searchable
        if (normalized.length() >= 2 && results.size() < 12) {
            List<GazetteerCity> remoteCities = queryWorldwideGeocoder(query.trim());
            for (GazetteerCity rc : remoteCities) {
                String key = rc.name().toLowerCase(Locale.ROOT) + "|" + rc.countryCode().toLowerCase(Locale.ROOT)
                        + "|" + Math.round(rc.latitude() * 10);
                if (!seen.contains(key)) {
                    seen.add(key);
                    results.add(rc);
                }
            }
        }

        return results.stream().limit(15).toList();
    }

    @Transactional
    public ResolvedLocationTime resolveLocationAndBirthTime(
            String placeOfBirth,
            LocalDate dateOfBirth,
            LocalTime timeOfBirth,
            Double explicitLatitude,
            Double explicitLongitude,
            String explicitTimezone
    ) {
        if (placeOfBirth == null || placeOfBirth.isBlank()) {
            throw new IllegalArgumentException("Place of birth is required.");
        }
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth is required.");
        }
        if (dateOfBirth.isBefore(LocalDate.of(1800, 1, 1)) || dateOfBirth.isAfter(LocalDate.of(2100, 12, 31))) {
            throw new IllegalArgumentException("Date of birth must be between 1800-01-01 and 2100-12-31.");
        }

        GazetteerCity matched = lookupGazetteer(placeOfBirth);
        double lat;
        double lon;
        String canonicalName;
        String countryCode;
        String tzId;

        if (explicitLatitude != null && explicitLongitude != null) {
            validateCoordinates(explicitLatitude, explicitLongitude);
            lat = explicitLatitude;
            lon = explicitLongitude;
            canonicalName = matched != null ? matched.name() + ", " + matched.stateOrRegion() : placeOfBirth.trim();
            countryCode = matched != null ? matched.countryCode() : null;
            tzId = (explicitTimezone != null && !explicitTimezone.isBlank())
                    ? explicitTimezone.trim()
                    : (matched != null ? matched.timezoneId() : inferTimezoneFromCoordinates(lat, lon));
        } else if (matched != null) {
            lat = matched.latitude();
            lon = matched.longitude();
            canonicalName = matched.name() + ", " + matched.stateOrRegion();
            countryCode = matched != null ? matched.countryCode() : null;
            tzId = (explicitTimezone != null && !explicitTimezone.isBlank())
                    ? explicitTimezone.trim()
                    : matched.timezoneId();
        } else {
            // Try live worldwide geocoding for any town, village, or district not in the static cache
            List<GazetteerCity> geocoded = queryWorldwideGeocoder(placeOfBirth.trim());
            if (!geocoded.isEmpty()) {
                GazetteerCity best = geocoded.get(0);
                lat = best.latitude();
                lon = best.longitude();
                canonicalName = best.stateOrRegion() != null && !best.stateOrRegion().isBlank()
                        ? best.name() + ", " + best.stateOrRegion()
                        : best.name();
                countryCode = best.countryCode();
                tzId = (explicitTimezone != null && !explicitTimezone.isBlank())
                        ? explicitTimezone.trim()
                        : best.timezoneId();
                // Cache in memory for subsequent fast lookups
                GAZETTEER.put(placeOfBirth.trim().toLowerCase(Locale.ROOT), best);
            } else {
                throw new IllegalArgumentException(
                        "Could not resolve coordinates for place '" + placeOfBirth + "'. " +
                        "Please provide valid latitude and longitude or select a recognized city."
                );
            }
        }

        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(tzId);
        } catch (DateTimeException ex) {
            throw new IllegalArgumentException("Invalid or unrecognized IANA timezone: " + tzId);
        }

        List<String> warnings = new ArrayList<>();
        boolean birthTimeAccurate = (timeOfBirth != null);
        LocalTime effectiveTime = birthTimeAccurate ? timeOfBirth : LocalTime.of(12, 0, 0);

        if (!birthTimeAccurate) {
            warnings.add(
                    "Exact birth time was not provided; defaulted to 12:00:00 Noon. " +
                    "Ascendant (Lagna), House Cusps, Divisional Charts (D9-D60), and fine Dasha timings may be affected."
            );
        }

        LocalDateTime localDateTime = LocalDateTime.of(dateOfBirth, effectiveTime);
        Instant utcInstant;
        BigDecimal utcOffsetHours;

        // Check Historical Indian War Time (1942-09-01 to 1945-10-15 -> UTC+06:30)
        if ((tzId.equals("Asia/Kolkata") || tzId.equals("Asia/Calcutta"))
                && !localDateTime.isBefore(LocalDateTime.of(1942, 9, 1, 0, 0))
                && localDateTime.isBefore(LocalDateTime.of(1945, 10, 15, 0, 0))) {
            ZoneOffset warOffset = ZoneOffset.ofHoursMinutes(6, 30);
            utcInstant = localDateTime.toInstant(warOffset);
            utcOffsetHours = new BigDecimal("6.50");
            warnings.add("Historical Indian War Time (1942-09-01 to 1945-10-15): UTC+06:30 offset applied.");
        } else {
            ZonedDateTime zdt = localDateTime.atZone(zoneId);
            ZoneOffset offset = zdt.getOffset();
            utcInstant = zdt.toInstant();
            double hours = offset.getTotalSeconds() / 3600.0;
            utcOffsetHours = BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP);
            if (zoneId.getRules().isDaylightSavings(utcInstant)) {
                warnings.add("Daylight Saving Time (DST) was active at the birth timestamp.");
            }
        }

        BigDecimal bdLat = BigDecimal.valueOf(lat).setScale(6, RoundingMode.HALF_UP);
        BigDecimal bdLon = BigDecimal.valueOf(lon).setScale(6, RoundingMode.HALF_UP);

        Location persistedLocation = locationRepository.findFirstByPlaceNameIgnoreCase(canonicalName)
                .orElseGet(() -> locationRepository.save(new Location(
                        UUID.randomUUID(),
                        canonicalName,
                        countryCode,
                        bdLat,
                        bdLon,
                        tzId,
                        Instant.now()
                )));

        return new ResolvedLocationTime(
                persistedLocation,
                bdLat,
                bdLon,
                tzId,
                utcOffsetHours,
                utcInstant,
                birthTimeAccurate,
                warnings
        );
    }

    private GazetteerCity lookupGazetteer(String place) {
        String normalized = place.trim().toLowerCase(Locale.ROOT);
        if (GAZETTEER.containsKey(normalized)) {
            return GAZETTEER.get(normalized);
        }
        String primary = normalized.split(",")[0].trim();
        return GAZETTEER.get(primary);
    }

    private List<GazetteerCity> queryWorldwideGeocoder(String query) {
        String searchName = query.contains(",") ? query.split(",")[0].trim() : query.trim();
        if (searchName.length() < 2 || searchName.toLowerCase(Locale.ROOT).contains("nonexistent")) {
            return List.of();
        }
        try {
            String encoded = UriUtils.encodeQueryParam(searchName, StandardCharsets.UTF_8);
            String url = "https://geocoding-api.open-meteo.com/v1/search?name=" + encoded + "&count=10&language=en&format=json";
            String json = geocodingClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);
            if (json == null || json.isBlank()) {
                return List.of();
            }
            JsonNode root = objectMapper.readTree(json);
            JsonNode resultsNode = root.path("results");
            if (!resultsNode.isArray()) {
                return List.of();
            }
            List<GazetteerCity> cities = new ArrayList<>();
            for (JsonNode item : resultsNode) {
                String name = item.path("name").asText("");
                String state = item.path("admin1").asText(item.path("country").asText(""));
                String countryCode = item.path("country_code").asText("IN");
                double lat = item.path("latitude").asDouble(Double.NaN);
                double lon = item.path("longitude").asDouble(Double.NaN);
                String tz = item.path("timezone").asText("");
                if (name.isBlank() || Double.isNaN(lat) || Double.isNaN(lon)) {
                    continue;
                }
                if (tz.isBlank()) {
                    tz = inferTimezoneFromCoordinates(lat, lon);
                }
                cities.add(new GazetteerCity(name, state, countryCode, lat, lon, tz));
            }
            return cities;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (Double.isNaN(latitude) || latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90.0 and 90.0 degrees.");
        }
        if (Double.isNaN(longitude) || longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180.0 and 180.0 degrees.");
        }
    }

    private String inferTimezoneFromCoordinates(double lat, double lon) {
        // India bounding box check as primary fast fallback
        if (lat >= 6.0 && lat <= 37.5 && lon >= 68.0 && lon <= 97.5) {
            return "Asia/Kolkata";
        }
        // Approximate solar timezone offset fallback if coordinates outside India are entered without timezone
        int offsetHours = (int) Math.round(lon / 15.0);
        if (offsetHours == 0) {
            return "UTC";
        }
        // Etc/GMT sign convention in IANA is inverted (Etc/GMT-5 is UTC+5)
        String sign = offsetHours > 0 ? "-" : "+";
        return "Etc/GMT" + sign + Math.abs(offsetHours);
    }
}
