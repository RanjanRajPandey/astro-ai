package com.astroai.strength;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SthanaBalaBreakdownDto(
        @JsonAlias("uchcha_bala") double uchchaBala,
        @JsonAlias("saptavargaja_bala") double saptavargajaBala,
        @JsonAlias("ojhayugmarasyamsa_bala") double ojhayugmarasyamsaBala,
        @JsonAlias("kendradi_bala") double kendradiBala,
        @JsonAlias("drekkana_bala") double drekkanaBala,
        @JsonAlias("total") double total
) {
}
