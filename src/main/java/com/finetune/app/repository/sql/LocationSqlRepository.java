package com.finetune.app.repository.sql;

import com.finetune.app.model.Location;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class LocationSqlRepository {
    private final JdbcTemplate jdbcTemplate;

    public LocationSqlRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Location> locationRowMapper = (rs, rowNum) -> {
        Location location = new Location();
        location.setId(rs.getLong("id"));
        location.setName(rs.getString("name"));
        location.setAddress(rs.getString("address"));
        location.setCity(rs.getString("city"));
        location.setState(rs.getString("state"));
        location.setZipCode(rs.getString("zipCode"));
        return location;
    };

    public List<Location> findAll() {
        return jdbcTemplate.query("SELECT * FROM locations", locationRowMapper);
    }

    public Optional<Location> findById(Long id) {
        List<Location> locations = jdbcTemplate.query("SELECT * FROM locations WHERE id = ?", locationRowMapper, id);
        return locations.stream().findFirst();
    }

    public int insert(Location location) {
        return jdbcTemplate.update(
            "INSERT INTO locations (name, address, city, state, zipCode) VALUES (?, ?, ?, ?, ?)",
            location.getName(), location.getAddress(), location.getCity(), location.getState(), location.getZipCode()
        );
    }

    public int update(Location location) {
        return jdbcTemplate.update(
            "UPDATE locations SET name = ?, address = ?, city = ?, state = ?, zipCode = ? WHERE id = ?",
            location.getName(), location.getAddress(), location.getCity(), location.getState(), location.getZipCode(), location.getId()
        );
    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM locations WHERE id = ?", id);
    }
}
