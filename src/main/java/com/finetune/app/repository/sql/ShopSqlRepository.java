package com.finetune.app.repository.sql;

import com.finetune.app.model.Shop;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class ShopSqlRepository {
    private final JdbcTemplate jdbcTemplate;

    public ShopSqlRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    private final RowMapper<Shop> shopRowMapper = (rs, rowNum) -> {
        Shop shop = new Shop();
        shop.setId(rs.getLong("id"));
        shop.setName(rs.getString("name"));
        shop.setStatus(rs.getString("status"));
        shop.setLogoUrl(rs.getString("logo_url"));
        // Map location
        com.finetune.app.model.Location location = new com.finetune.app.model.Location();
        location.setId(rs.getLong("location_id"));
        shop.setLocation(location);
        return shop;
    };

    public Optional<Shop> findById(Long id) {
        List<Shop> shops = jdbcTemplate.query("SELECT * FROM shops WHERE id = ?", shopRowMapper, id);
        return shops.stream().findFirst();
    }

    public List<Shop> findAll() {
        return jdbcTemplate.query("SELECT * FROM shops", shopRowMapper);
    }

    public List<Shop> findAllByShopId(Long shopId) {
        return jdbcTemplate.query("SELECT * FROM shops WHERE id = ?", shopRowMapper, shopId);
    }

    public int insert(Shop shop) {
        return jdbcTemplate.update(
            "INSERT INTO shops (name, status, logo_url, location_id) VALUES (?, ?, ?, ?)",
            shop.getName(), shop.getStatus(), shop.getLogoUrl(),
            shop.getLocation() != null ? shop.getLocation().getId() : null
        );
    }

    public int update(Shop shop) {
        return jdbcTemplate.update(
            "UPDATE shops SET name = ?, status = ?, logo_url = ?, location_id = ? WHERE id = ?",
            shop.getName(), shop.getStatus(), shop.getLogoUrl(),
            shop.getLocation() != null ? shop.getLocation().getId() : null,
            shop.getId()
        );
    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM shops WHERE id = ?", id);
    }
    public List<Shop> findByLocation(com.finetune.app.model.Location location) {
        if (location == null || location.getId() == null) {
            return List.of();
        }
        return jdbcTemplate.query("SELECT * FROM shops WHERE location_id = ?", shopRowMapper, location.getId());
    }

    public int save(Shop shop) {
        if (shop.getId() == null) {
            return insert(shop);
        } else {
            return update(shop);
        }
    }

    public java.util.Optional<Shop> findLastInserted() {
        List<Shop> shops = jdbcTemplate.query("SELECT * FROM shops ORDER BY id DESC LIMIT 1", shopRowMapper);
        return shops.stream().findFirst();
    }
}
