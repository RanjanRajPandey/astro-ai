package com.astroai.house;

import com.astroai.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/houses")
public class HouseController {

    private final HouseService houseService;

    public HouseController(HouseService houseService) {
        this.houseService = houseService;
    }

    @GetMapping("/{id}")
    public ApiResponse<HouseCalculationResponseDto> getHouses(@PathVariable("id") UUID birthProfileId) {
        return ApiResponse.ok(houseService.calculateAndPersistHouses(birthProfileId));
    }
}
