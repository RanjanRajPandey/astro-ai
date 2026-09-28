package com.astroai.strength;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlanetStrengthEntryDto(
        @JsonAlias("planet") String planet,
        @JsonAlias("sign") String sign,
        @JsonAlias("house") int house,
        @JsonAlias("d1_dignity") String d1Dignity,
        @JsonAlias("is_retrograde") boolean isRetrograde,
        @JsonAlias("sthana_bala") double sthanaBala,
        @JsonAlias("sthana_breakdown") SthanaBalaBreakdownDto sthanaBreakdown,
        @JsonAlias("dig_bala") double digBala,
        @JsonAlias("kala_bala") double kalaBala,
        @JsonAlias("kala_breakdown") KalaBalaBreakdownDto kalaBreakdown,
        @JsonAlias("chesta_bala") double chestaBala,
        @JsonAlias("naisargika_bala") double naisargikaBala,
        @JsonAlias("drik_bala") double drikBala,
        @JsonAlias("total_shadbala_virupas") double totalShadbalaVirupas,
        @JsonAlias("total_shadbala_rupas") double totalShadbalaRupas,
        @JsonAlias("required_minimum_rupas") double requiredMinimumRupas,
        @JsonAlias("shadbala_ratio") double shadbalaRatio,
        @JsonAlias("vimshopaka_bala") double vimshopakaBala,
        @JsonAlias("vimshopaka_percentage") double vimshopakaPercentage,
        @JsonAlias("strength_grade") String strengthGrade,
        @JsonAlias("rank") int rank
) {
}
