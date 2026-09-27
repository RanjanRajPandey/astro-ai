package com.astroai.house;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;

public record AscendantSummaryDto(
        BigDecimal longitude,
        String sign,
        @JsonAlias("sanskrit_sign")
        String sanskritSign,
        @JsonAlias("sign_index")
        int signIndex,
        @JsonAlias("degree_in_sign")
        BigDecimal degreeInSign,
        @JsonAlias("degree_dms")
        String degreeDms,
        String nakshatra,
        @JsonAlias("nakshatra_index")
        int nakshatraIndex,
        @JsonAlias("nakshatra_lord")
        String nakshatraLord,
        int pada,
        @JsonAlias("lagna_lord")
        String lagnaLord,
        @JsonAlias("lagna_lord_sign")
        String lagnaLordSign,
        @JsonAlias("lagna_lord_house")
        int lagnaLordHouse,
        @JsonAlias("lagna_lord_dignity")
        String lagnaLordDignity,
        @JsonAlias("chandra_lagna_sign")
        String chandraLagnaSign,
        @JsonAlias("surya_lagna_sign")
        String suryaLagnaSign,
        @JsonAlias("arudha_lagna_sign")
        String arudhaLagnaSign,
        @JsonAlias("arudha_lagna_house")
        int arudhaLagnaHouse,
        @JsonAlias("upapada_lagna_sign")
        String upapadaLagnaSign,
        @JsonAlias("upapada_lagna_house")
        int upapadaLagnaHouse
) {
}
