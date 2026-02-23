package com.finetune.app.repository.jdbc;

import com.finetune.app.model.Location;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class LocationJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public LocationJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Location> locationRowMapper = (rs, rowNum) -> {
        Location l = new Location();
        l.setId(rs.getLong("id"));
        l.setName(rs.getString("name"));
        l.setAddress(rs.getString("address"));
        l.setCity(rs.getString("city"));
        l.setState(rs.getString("state"));
        l.setZipCode(rs.getString("zipCode"));
        return l;
    };

    public List<Location> findAll() {
        return jdbcTemplate.query("SELECT * FROM locations", locationRowMapper);
    }

    public Optional<Location> findById(Long id) {
        List<Location> locations = jdbcTemplate.query("SELECT * FROM locations WHERE id = ?", locationRowMapper, id);
        return locations.stream().findFirst();
    }

    public int save(Location location) {
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

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM locations WHERE id = ?", id);
    }
}
