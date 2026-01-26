package com.finetune.app.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.finetune.app.model.Location;
import com.finetune.app.repository.LocationJpaRepository;
import com.finetune.app.repository.ShopJpaRepository;

@Service
public class LocationService {
    private final LocationJpaRepository locationRepository;
    private final ShopJpaRepository shopRepository;

    public LocationService(LocationJpaRepository locationRepository, ShopJpaRepository shopRepository) {
        this.locationRepository = locationRepository;
        this.shopRepository = shopRepository;
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public Location getLocation(Long id) {
        return locationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + id));
    }

    public Location createLocation(Location location) {
        // Check if a shop already exists at this address
        if (shopRepository.findByLocationAddress(location.getAddress(), location.getCity(), location.getState(), location.getZipCode()).isPresent()) {
            throw new IllegalArgumentException("A shop already exists at location: " + location.getAddress() + ", " + location.getCity() + ", " + location.getState() + " " + location.getZipCode());
        }
        return locationRepository.save(location);
    }

    public Location updateLocation(Long id, Location location) {
        Location existingLocation = locationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + id));
        
        // Only check for duplicates if the address changed
        if (!isSameAddress(existingLocation, location)) {
            if (shopRepository.findByLocationAddress(location.getAddress(), location.getCity(), location.getState(), location.getZipCode()).isPresent()) {
                throw new IllegalArgumentException("A shop already exists at location: " + location.getAddress() + ", " + location.getCity() + ", " + location.getState() + " " + location.getZipCode());
            }
        }
        
        location.setId(id);
        return locationRepository.save(location);
    }

    private boolean isSameAddress(Location loc1, Location loc2) {
        return loc1.getAddress().equalsIgnoreCase(loc2.getAddress()) &&
               loc1.getCity().equalsIgnoreCase(loc2.getCity()) &&
               loc1.getState().equalsIgnoreCase(loc2.getState()) &&
               loc1.getZipCode().equals(loc2.getZipCode());
    }

    public void deleteLocation(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new IllegalArgumentException("Location not found with id: " + id);
        }
        locationRepository.deleteById(id);
    }
}
