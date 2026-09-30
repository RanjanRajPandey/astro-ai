package com.astroai.yoga;

import com.astroai.common.ApiResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/yogas")
public class YogaController {

    private final YogaService yogaService;

    public YogaController(YogaService yogaService) {
        this.yogaService = yogaService;
    }

    @GetMapping("/{birthProfileId}")
    public ApiResponse<YogaCalculationResponseDto> getYogasAndDoshas(
            @PathVariable UUID birthProfileId
    ) {
        return ApiResponse.ok(yogaService.calculateAndPersistYogas(birthProfileId));
    }
}
