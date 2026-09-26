package com.astroai.location;

import com.astroai.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/search")
    public ApiResponse<List<LocationService.GazetteerCity>> search(@RequestParam(name = "q", defaultValue = "") String q) {
        return ApiResponse.ok(locationService.searchCities(q));
    }
}
