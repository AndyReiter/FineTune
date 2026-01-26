package com.finetune.app.controller;

import com.finetune.app.model.Location;
import com.finetune.app.service.LocationService;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/locations")
public class LocationController {
    private final LocationService locationService;

    // Constructor injection (preferred)
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping()
    public List<Location> getLocations() {
        return locationService.getAllLocations();
    }

    @GetMapping("/{id}")
    public Location getLocation(@PathVariable Long id) {
        return locationService.getLocation(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Location createLocation(@RequestBody Location location) {
        return locationService.createLocation(location);
    }

    @PutMapping("/{id}")
    public Location updateLocation(@PathVariable Long id, @RequestBody Location location) {
        return locationService.updateLocation(id, location);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
    }
}
