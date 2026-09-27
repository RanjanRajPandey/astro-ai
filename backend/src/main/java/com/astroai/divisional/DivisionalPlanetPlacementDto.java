package com.astroai.divisional;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DivisionalPlanetPlacementDto(
        @JsonAlias("planet") String planet,
        @JsonAlias("d1_longitude") BigDecimal d1Longitude,
        @JsonAlias("d1_sign") String d1Sign,
        @JsonAlias("d1_house") int d1House,
        @JsonAlias("varga_sign") String vargaSign,
        @JsonAlias("varga_sanskrit_sign") String vargaSanskritSign,
        @JsonAlias("varga_sign_index") int vargaSignIndex,
        @JsonAlias("varga_sign_lord") String vargaSignLord,
        @JsonAlias("varga_house") int vargaHouse,
        @JsonAlias("part_number") int partNumber,
        @JsonAlias("is_vargottama") boolean isVargottama,
        @JsonAlias("dignity_in_varga") String dignityInVarga,
        @JsonAlias("retrograde") boolean retrograde,
        @JsonAlias("combust") boolean combust,
        @JsonAlias("shashtiamsha_name") String shashtiamshaName,
        @JsonAlias("shashtiamsha_quality") String shashtiamshaQuality
) {
}
