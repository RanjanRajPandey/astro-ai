package com.astroai.house;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record HouseCalculationResponseDto(
        UUID birthProfileId,
        UUID chartId,
        @JsonAlias("utc_datetime_iso")
        String utcDatetimeIso,
        @JsonAlias("julian_day_ut")
        BigDecimal julianDayUt,
        @JsonAlias("ayanamsha_type")
        String ayanamshaType,
        @JsonAlias("ayanamsha_value")
        BigDecimal ayanamshaValue,
        @JsonAlias("house_system")
        String houseSystem,
        AscendantSummaryDto ascendant,
        List<HouseDetailDto> houses
) {
}
