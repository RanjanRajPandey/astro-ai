package com.astroai.location;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ResolvedLocationTime(
        Location location,
        BigDecimal latitude,
        BigDecimal longitude,
        String timezoneId,
        BigDecimal utcOffsetHours,
        Instant utcBirthTime,
        boolean birthTimeAccurate,
        List<String> calculationWarnings
) {
}
