package com.astroai.transit;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransitPlanetEntryDto(
        @JsonAlias("planet") String planet,
        @JsonAlias("natal_sign") String natalSign,
        @JsonAlias("natal_house_from_lagna") int natalHouseFromLagna,
        @JsonAlias("transit_longitude") double transitLongitude,
        @JsonAlias("transit_sign") String transitSign,
        @JsonAlias("transit_sanskrit_sign") String transitSanskritSign,
        @JsonAlias("transit_degree_dms") String transitDegreeDms,
        @JsonAlias("transit_nakshatra") String transitNakshatra,
        @JsonAlias("transit_pada") int transitPada,
        @JsonAlias("is_retrograde") boolean isRetrograde,
        @JsonAlias("house_from_moon") int houseFromMoon,
        @JsonAlias("house_from_lagna") int houseFromLagna,
        @JsonAlias("is_benefic_from_moon") boolean isBeneficFromMoon,
        @JsonAlias("vedha_obstructed") boolean vedhaObstructed,
        @JsonAlias("vedha_obstructor") String vedhaObstructor,
        @JsonAlias("gochar_status") String gocharStatus,
        @JsonAlias("tara_bala_category") String taraBalaCategory,
        @JsonAlias("is_tara_favorable") boolean isTaraFavorable,
        @JsonAlias("classical_summary") String classicalSummary
) {
}
