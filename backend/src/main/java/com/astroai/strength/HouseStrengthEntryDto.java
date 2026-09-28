package com.astroai.strength;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HouseStrengthEntryDto(
        @JsonAlias("house_number") int houseNumber,
        @JsonAlias("sign") String sign,
        @JsonAlias("sanskrit_sign") String sanskritSign,
        @JsonAlias("lord_planet") String lordPlanet,
        @JsonAlias("sign_nature") String signNature,
        @JsonAlias("purushartha") String purushartha,
        @JsonAlias("domain_title") String domainTitle,
        @JsonAlias("occupants") List<String> occupants,
        @JsonAlias("bhavadhipati_bala") double bhavadhipatiBala,
        @JsonAlias("bhava_dig_bala") double bhavaDigBala,
        @JsonAlias("bhava_drishti_bala") double bhavaDrishtiBala,
        @JsonAlias("occupant_factor") double occupantFactor,
        @JsonAlias("total_bhava_bala_virupas") double totalBhavaBalaVirupas,
        @JsonAlias("total_bhava_bala_rupas") double totalBhavaBalaRupas,
        @JsonAlias("strength_grade") String strengthGrade,
        @JsonAlias("rank") int rank
) {
}
