package com.astroai.nakshatra;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NakshatraCalculationResponseDto(
        UUID birthProfileId,
        UUID chartId,
        @JsonAlias("utc_datetime_iso") String utcDatetimeIso,
        @JsonAlias("julian_day_ut") double julianDayUt,
        @JsonAlias("ayanamsha_type") String ayanamshaType,
        @JsonAlias("ayanamsha_value") BigDecimal ayanamshaValue,
        @JsonAlias("janma_nakshatra") String janmaNakshatra,
        @JsonAlias("janma_nakshatra_index") int janmaNakshatraIndex,
        @JsonAlias("janma_pada") int janmaPada,
        @JsonAlias("janma_nakshatra_lord") String janmaNakshatraLord,
        @JsonAlias("janma_rashi") String janmaRashi,
        @JsonAlias("moon_elapsed_fraction") BigDecimal moonElapsedFraction,
        @JsonAlias("moon_remaining_fraction") BigDecimal moonRemainingFraction,
        @JsonAlias("placements") List<NakshatraPlacementDto> placements
) {
}
