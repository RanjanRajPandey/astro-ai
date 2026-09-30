package com.astroai.temporal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TemporalForecastWindowDto(
        @JsonAlias("window_index") int windowIndex,
        @JsonAlias("window_label") String windowLabel,
        @JsonAlias("start_utc") String startUtc,
        @JsonAlias("end_utc") String endUtc,
        @JsonAlias("midpoint_utc") String midpointUtc,
        @JsonAlias("mahadasha_lord") String mahadashaLord,
        @JsonAlias("antardasha_lord") String antardashaLord,
        @JsonAlias("pratyantardasha_lord") String pratyantardashaLord,
        @JsonAlias("jupiter_transit_sign") String jupiterTransitSign,
        @JsonAlias("saturn_transit_sign") String saturnTransitSign,
        @JsonAlias("sade_sati_phase") String sadeSatiPhase,
        @JsonAlias("double_transit_houses") List<Integer> doubleTransitHouses,
        @JsonAlias("overall_window_score") double overallWindowScore,
        @JsonAlias("dominant_domain") String dominantDomain,
        @JsonAlias("domain_evaluations") List<DomainWindowEvaluationDto> domainEvaluations
) {
}
