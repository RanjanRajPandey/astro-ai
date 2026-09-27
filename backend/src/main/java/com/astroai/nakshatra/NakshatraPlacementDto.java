package com.astroai.nakshatra;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NakshatraPlacementDto(
        @JsonAlias("body_name") String bodyName,
        @JsonAlias("longitude") BigDecimal longitude,
        @JsonAlias("rashi_sign") String rashiSign,
        @JsonAlias("nakshatra_name") String nakshatraName,
        @JsonAlias("nakshatra_index") int nakshatraIndex,
        @JsonAlias("pada") int pada,
        @JsonAlias("pada_navamsha_sign") String padaNavamshaSign,
        @JsonAlias("ruler_planet") String rulerPlanet,
        @JsonAlias("degree_in_nakshatra") BigDecimal degreeInNakshatra,
        @JsonAlias("degree_in_nakshatra_dms") String degreeInNakshatraDms,
        @JsonAlias("elapsed_fraction") BigDecimal elapsedFraction,
        @JsonAlias("remaining_fraction") BigDecimal remainingFraction,
        @JsonAlias("deity") String deity,
        @JsonAlias("gana") String gana,
        @JsonAlias("nadi") String nadi,
        @JsonAlias("yoni") String yoni,
        @JsonAlias("symbol") String symbol,
        @JsonAlias("tara_number_from_moon") int taraNumberFromMoon,
        @JsonAlias("tara_name_from_moon") String taraNameFromMoon,
        @JsonAlias("tara_quality") String taraQuality,
        @JsonAlias("relationship_to_nakshatra_lord") String relationshipToNakshatraLord
) {
}
