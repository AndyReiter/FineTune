package com.finetune.app.repository.jdbc;

import com.finetune.app.model.Shop;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class ShopJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public ShopJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Shop> shopRowMapper = (rs, rowNum) -> {
        Shop s = new Shop();
        s.setId(rs.getLong("id"));
        s.setName(rs.getString("name"));
        s.setStatus(rs.getString("status"));
        s.setLogoUrl(rs.getString("logoUrl"));
        s.setLocationId(rs.getLong("location_id"));
        return s;
    };

    public List<Shop> findAll() {
        return jdbcTemplate.query("SELECT * FROM shops", shopRowMapper);
    }

    public Optional<Shop> findById(Long id) {
        List<Shop> shops = jdbcTemplate.query("SELECT * FROM shops WHERE id = ?", shopRowMapper, id);
        return shops.stream().findFirst();
    }

    public int save(Shop shop) {
        return jdbcTemplate.update(
            "INSERT INTO shops (name, status, logoUrl, location_id) VALUES (?, ?, ?, ?)",
            shop.getName(), shop.getStatus(), shop.getLogoUrl(), shop.getLocationId()
        );
    }

    public int update(Shop shop) {
        return jdbcTemplate.update(
            "UPDATE shops SET name = ?, status = ?, logoUrl = ?, location_id = ? WHERE id = ?",
            shop.getName(), shop.getStatus(), shop.getLogoUrl(), shop.getLocationId(), shop.getId()
        );
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM shops WHERE id = ?", id);
    }
}
