package com.astroai.temporal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TemporalAnalysisResponseDto(
        @JsonAlias("birth_profile_id") UUID birthProfileId,
        @JsonAlias("natal_utc_datetime_iso") String natalUtcDatetimeIso,
        @JsonAlias("anchor_utc_datetime_iso") String anchorUtcDatetimeIso,
        @JsonAlias("natal_ascendant_sign") String natalAscendantSign,
        @JsonAlias("natal_moon_sign") String natalMoonSign,
        @JsonAlias("ayanamsha_type") String ayanamshaType,
        @JsonAlias("window_count") int windowCount,
        @JsonAlias("best_overall_window_label") String bestOverallWindowLabel,
        @JsonAlias("strongest_domain_code") String strongestDomainCode,
        @JsonAlias("domain_summaries") List<DomainTimelineSummaryDto> domainSummaries,
        @JsonAlias("timeline_windows") List<TemporalForecastWindowDto> timelineWindows
) {
}
