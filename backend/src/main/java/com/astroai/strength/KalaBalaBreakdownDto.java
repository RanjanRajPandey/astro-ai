package com.astroai.strength;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KalaBalaBreakdownDto(
        @JsonAlias("nathonnatha_bala") double nathonnathaBala,
        @JsonAlias("paksha_bala") double pakshaBala,
        @JsonAlias("tribhaga_bala") double tribhagaBala,
        @JsonAlias("vara_bala") double varaBala,
        @JsonAlias("ayana_bala") double ayanaBala,
        @JsonAlias("total") double total
) {
}
