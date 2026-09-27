package com.astroai.divisional;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DivisionalCalculationResponseDto(
        UUID birthProfileId,
        UUID chartId,
        @JsonAlias("utc_datetime_iso") String utcDatetimeIso,
        @JsonAlias("julian_day_ut") double julianDayUt,
        @JsonAlias("ayanamsha_type") String ayanamshaType,
        @JsonAlias("ayanamsha_value") BigDecimal ayanamshaValue,
        @JsonAlias("d1_ascendant_sign") String d1AscendantSign,
        @JsonAlias("vargottama_Swapna_summary") List<String> d9VargottamaSummary,
        @JsonAlias("charts") List<DivisionalChartDto> charts
) {
}
