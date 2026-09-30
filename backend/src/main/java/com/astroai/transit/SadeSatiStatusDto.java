package com.astroai.transit;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SadeSatiStatusDto(
        @JsonAlias("sade_sati_active") boolean sadeSatiActive,
        @JsonAlias("dhaiya_active") boolean dhaiyaActive,
        @JsonAlias("phase") String phase,
        @JsonAlias("saturn_transit_sign") String saturnTransitSign,
        @JsonAlias("saturn_house_from_moon") int saturnHouseFromMoon,
        @JsonAlias("saturn_house_from_lagna") int saturnHouseFromLagna,
        @JsonAlias("description") String description
) {
}
