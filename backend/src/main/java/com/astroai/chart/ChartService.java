package com.astroai.chart;

import com.astroai.birth.BirthProfileResponse;
import com.astroai.birth.BirthProfileService;
import com.astroai.house.HouseCalculationResponseDto;
import com.astroai.house.HouseService;
import com.astroai.planet.PlanetPositionDto;
import com.astroai.planet.PlanetService;
import com.astroai.planet.PlanetaryCalculationResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ChartService {

    private final BirthProfileService birthProfileService;
    private final PlanetService planetService;
    private final HouseService houseService;

    public ChartService(
            BirthProfileService birthProfileService,
            PlanetService planetService,
            HouseService houseService
    ) {
        this.birthProfileService = birthProfileService;
        this.planetService = planetService;
        this.houseService = houseService;
    }

    public KundliChartResponseDto getD1KundliChart(UUID birthProfileId) {
        BirthProfileResponse profile = birthProfileService.getProfile(birthProfileId);
        PlanetaryCalculationResponseDto planetsRes = planetService.calculateAndPersistPlanets(birthProfileId);
        HouseCalculationResponseDto housesRes = houseService.calculateAndPersistHouses(birthProfileId);

        PlanetPositionDto moon = planetsRes.planets().stream()
                .filter(p -> "Moon".equalsIgnoreCase(p.planet()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Moon position missing from calculation"));

        PlanetPositionDto sun = planetsRes.planets().stream()
                .filter(p -> "Sun".equalsIgnoreCase(p.planet()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Sun position missing from calculation"));

        return new KundliChartResponseDto(
                planetsRes.chartId(),
                "D1",
                "Rashi Chart (D1 - Natal Kundli)",
                profile,
                planetsRes.ayanamshaType(),
                planetsRes.ayanamshaValue(),
                housesRes.houseSystem(),
                planetsRes.nodeType(),
                housesRes.ascendant(),
                moon.sign(),
                moon.sanskritSign(),
                moon.nakshatra(),
                moon.pada(),
                sun.sign(),
                planetsRes.planets(),
                housesRes.houses()
        );
    }
}
