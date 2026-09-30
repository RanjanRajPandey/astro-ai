package com.astroai.transit;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DoubleTransitHouseEntryDto(
        @JsonAlias("house_number") int houseNumber,
        @JsonAlias("sign") String sign,
        @JsonAlias("sanskrit_sign") String sanskritSign,
        @JsonAlias("domain_title") String domainTitle,
        @JsonAlias("jupiter_influence") String jupiterInfluence,
        @JsonAlias("saturn_influence") String saturnInfluence,
        @JsonAlias("is_activated") boolean isActivated
) {
}
