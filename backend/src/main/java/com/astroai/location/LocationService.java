package com.astroai.location;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        register("New Delhi", "Delhi", "IN", 28.6139, 77.2090, "Asia/Kolkata", "delhi");
        register("Mumbai", "Maharashtra", "IN", 19.0760, 72.8777, "Asia/Kolkata", "bombay");
        register("Bengaluru", "Karnataka", "IN", 12.9716, 77.5946, "Asia/Kolkata", "bangalore");
        register("Kolkata", "West Bengal", "IN", 22.5726, 88.3639, "Asia/Kolkata", "calcutta");
        register("Chennai", "Tamil Nadu", "IN", 13.0827, 80.2707, "Asia/Kolkata", "madras");
        register("Hyderabad", "Telangana", "IN", 17.3850, 78.4867, "Asia/Kolkata");
        register("Pune", "Maharashtra", "IN", 18.5204, 73.8567, "Asia/Kolkata");
        register("Ahmedabad", "Gujarat", "IN", 23.0225, 72.5714, "Asia/Kolkata");
        register("Jaipur", "Rajasthan", "IN", 26.9124, 75.7873, "Asia/Kolkata");
        register("Lucknow", "Uttar Pradesh", "IN", 26.8467, 80.9462, "Asia/Kolkata");
        register("Varanasi", "Uttar Pradesh", "IN", 25.3176, 82.9739, "Asia/Kolkata", "kashi", "benares");
        register("Patna", "Bihar", "IN", 25.5941, 85.1376, "Asia/Kolkata");
        register("Ranchi", "Jharkhand", "IN", 23.3441, 85.3096, "Asia/Kolkata");
        register("Bhopal", "Madhya Pradesh", "IN", 23.2599, 77.4126, "Asia/Kolkata");
        register("Ujjain", "Madhya Pradesh", "IN", 23.1765, 75.7885, "Asia/Kolkata");
        register("Indore", "Madhya Pradesh", "IN", 22.7196, 75.8577, "Asia/Kolkata");
        register("Chandigarh", "Chandigarh", "IN", 30.7333, 76.7794, "Asia/Kolkata");
        register("Amritsar", "Punjab", "IN", 31.6340, 74.8723, "Asia/Kolkata");
        register("Prayagraj", "Uttar Pradesh", "IN", 25.4358, 81.8463, "Asia/Kolkata", "allahabad");
        register("Ayodhya", "Uttar Pradesh", "IN", 26.7922, 82.1998, "Asia/Kolkata");
        register("Surat", "Gujarat", "IN", 21.1702, 72.8311, "Asia/Kolkata");
        register("Kochi", "Kerala", "IN", 9.9312, 76.2673, "Asia/Kolkata");
        register("Thiruvananthapuram", "Kerala", "IN", 8.5241, 76.9366, "Asia/Kolkata");
        register("Guwahati", "Assam", "IN", 26.1445, 91.7362, "Asia/Kolkata");
        register("Bhubaneswar", "Odisha", "IN", 20.2961, 85.8245, "Asia/Kolkata");
        register("Dehradun", "Uttarakhand", "IN", 30.3165, 78.0322, "Asia/Kolkata");
        register("Kathmandu", "Bagmati", "NP", 27.7172, 85.3240, "Asia/Kathmandu");
        register("Colombo", "Western", "LK", 6.9271, 79.8612, "Asia/Colombo");
        register("London", "England", "GB", 51.5074, -0.1278, "Europe/London");
        register("New York", "NY", "US", 40.7128, -74.0060, "America/New_York");
        register("San Francisco", "CA", "US", 37.7749, -122.4194, "America/Los_Angeles");
        register("Los Angeles", "CA", "US", 34.0522, -118.2437, "America/Los_Angeles");
        register("Chicago", "IL", "US", 41.8781, -87.6298, "America/Chicago");
        register("Toronto", "Ontario", "CA", 43.6532, -79.3832, "America/Toronto");
        register("Sydney", "NSW", "AU", -33.8688, 151.2093, "Australia/Sydney");
        register("Singapore", "Singapore", "SG", 1.3521, 103.8198, "Asia/Singapore");
        register("Dubai", "Dubai", "AE", 25.2048, 55.2708, "Asia/Dubai");
        register("Tokyo", "Kanto", "JP", 35.6762, 139.6503, "Asia/Tokyo");
    }

    private static void register(String name, String state, String country, double lat, double lon, String tz, String... aliases) {
        GazetteerCity city = new GazetteerCity(name, state, country, lat, lon, tz);
        GAZETTEER.put(name.toLowerCase(Locale.ROOT), city);
        for (String alias : aliases) {
            GAZETTEER.put(alias.toLowerCase(Locale.ROOT), city);
        }
    }

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<GazetteerCity> searchCities(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        Set<String> seen = new HashSet<>();
        List<GazetteerCity> results = new ArrayList<>();
        for (GazetteerCity city : GAZETTEER.values()) {
            String key = city.name() + "|" + city.countryCode();
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
            countryCode = matched.countryCode();
            tzId = (explicitTimezone != null && !explicitTimezone.isBlank())
                    ? explicitTimezone.trim()
                    : matched.timezoneId();
        } else {
            throw new IllegalArgumentException(
                    "Could not resolve coordinates for place '" + placeOfBirth + "'. " +
                    "Please provide valid latitude and longitude or select a recognized city."
            );
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

    private void validateCoordinates(double latitude, double longitude) {
        if (Double.isNaN(latitude) || latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90.0 and 90.0 degrees.");
        }
        if (Double.isNaN(longitude) || longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180.0 and 180.0 degrees.");
        }
    }

    private String inferTimezoneFromCoordinates(double lat, double lon) {
        // India bounding box check as primary fast fallback when explicit coordinates are in India
        if (lat >= 6.0 && lat <= 37.5 && lon >= 68.0 && lon <= 97.5) {
            return "Asia/Kolkata";
        }
        return "UTC";
    }
}
