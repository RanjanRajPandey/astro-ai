package com.astroai.chart;

import com.astroai.birth.BirthProfileResponse;
import com.astroai.house.AscendantSummaryDto;
import com.astroai.house.HouseDetailDto;
import com.astroai.planet.PlanetPositionDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record KundliChartResponseDto(
        UUID chartId,
        String vargaCode,
        String vargaName,
        BirthProfileResponse birthProfile,
        String ayanamshaType,
        BigDecimal ayanamshaValue,
        String houseSystem,
        String nodeType,
        AscendantSummaryDto ascendant,
        String moonSign,
        String moonSanskritSign,
        String moonNakshatra,
        int moonPada,
        String sunSign,
        List<PlanetPositionDto> planets,
        List<HouseDetailDto> houses
) {
}
