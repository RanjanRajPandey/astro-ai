package com.astroai.strength;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PurusharthaSummaryDto(
        @JsonAlias("purushartha") String purushartha,
        @JsonAlias("houses") List<Integer> houses,
        @JsonAlias("average_rupas") double averageRupas,
        @JsonAlias("dominant_house") int dominantHouse
) {
}
