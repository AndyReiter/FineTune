
package com.finetune.app.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.finetune.app.model.Location;
import com.finetune.app.repository.sql.LocationSqlRepository;
import com.finetune.app.repository.sql.ShopSqlRepository;

@Service
public class LocationService {
    private final LocationSqlRepository locationRepository;
    private final ShopSqlRepository shopRepository;

    public LocationService(LocationSqlRepository locationRepository, ShopSqlRepository shopRepository) {
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
        String sql = "SELECT s.id FROM shops s JOIN locations l ON s.location_id = l.id WHERE l.address = ? AND l.city = ? AND l.state = ? AND l.zipCode = ?";
        List<Long> shopIds = shopRepository.getJdbcTemplate().query(sql, (rs, rowNum) -> rs.getLong("id"), location.getAddress(), location.getCity(), location.getState(), location.getZipCode());
        if (!shopIds.isEmpty()) {
            throw new IllegalArgumentException("A shop already exists at location: " + location.getAddress() + ", " + location.getCity() + ", " + location.getState() + " " + location.getZipCode());
        }
        locationRepository.insert(location);
        return locationRepository.findAll().stream().reduce((first, second) -> second).orElse(location);
    }

    public Location updateLocation(Long id, Location location) {
        Location existingLocation = locationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + id));
        if (!isSameAddress(existingLocation, location)) {
            String sql = "SELECT s.id FROM shops s JOIN locations l ON s.location_id = l.id WHERE l.address = ? AND l.city = ? AND l.state = ? AND l.zipCode = ?";
            List<Long> shopIds = shopRepository.getJdbcTemplate().query(sql, (rs, rowNum) -> rs.getLong("id"), location.getAddress(), location.getCity(), location.getState(), location.getZipCode());
            if (!shopIds.isEmpty()) {
                throw new IllegalArgumentException("A shop already exists at location: " + location.getAddress() + ", " + location.getCity() + ", " + location.getState() + " " + location.getZipCode());
            }
        }
        location.setId(id);
        locationRepository.update(location);
        return locationRepository.findById(id).orElse(location);
    }

    private boolean isSameAddress(Location loc1, Location loc2) {
        return loc1.getAddress().equalsIgnoreCase(loc2.getAddress()) &&
               loc1.getCity().equalsIgnoreCase(loc2.getCity()) &&
               loc1.getState().equalsIgnoreCase(loc2.getState()) &&
               loc1.getZipCode().equals(loc2.getZipCode());
    }

    public void deleteLocation(Long id) {
        if (!locationRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("Location not found with id: " + id);
        }
        locationRepository.delete(id);
    }
}
