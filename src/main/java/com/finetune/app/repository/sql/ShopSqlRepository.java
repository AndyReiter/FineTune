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
        // slug may exist
        try {
            shop.setSlug(rs.getString("slug"));
        } catch (Exception ignore) {}
        // logo_url may exist
        try {
            shop.setLogoUrl(rs.getString("logo_url"));
        } catch (Exception ignore) {}
        // created_at may exist
        try {
            shop.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        } catch (Exception ignore) {}
        return shop;
    };

    public Optional<Shop> findById(Long id) {
        List<Shop> shops = jdbcTemplate.query("SELECT id, name, slug, logo_url, created_at FROM shop WHERE id = ?", shopRowMapper, id);
        return shops.stream().findFirst();
    }

    public List<Shop> findAll() {
        return jdbcTemplate.query("SELECT id, name, slug, logo_url, created_at FROM shop ORDER BY id ASC", shopRowMapper);
    }

    public List<Shop> findAllByShopId(Long shopId) {
        return jdbcTemplate.query("SELECT id, name, slug, logo_url, created_at FROM shop WHERE id = ?", shopRowMapper, shopId);
    }

    public int insert(Shop shop) {
        return jdbcTemplate.update(
            "INSERT INTO shop (name, slug, logo_url) VALUES (?, ?, ?)",
            shop.getName(), shop.getSlug(), shop.getLogoUrl()
        );
    }

    public int update(Shop shop) {
        return jdbcTemplate.update(
            "UPDATE shop SET name = ?, slug = ?, logo_url = ? WHERE id = ?",
            shop.getName(), shop.getSlug(), shop.getLogoUrl(),
            shop.getId()
        );
    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM shop WHERE id = ?", id);
    }
    public List<Shop> findByLocation(com.finetune.app.model.Location location) {
        if (location == null || location.getId() == null) {
            return List.of();
        }
        return jdbcTemplate.query("SELECT id, name, slug, logo_url, created_at FROM shop WHERE location_id = ?", shopRowMapper, location.getId());
    }

    public int save(Shop shop) {
        if (shop.getId() == null) {
            return insert(shop);
        } else {
            return update(shop);
        }
    }

    public java.util.Optional<Shop> findLastInserted() {
        List<Shop> shops = jdbcTemplate.query("SELECT id, name, slug, logo_url, created_at FROM shop ORDER BY id DESC LIMIT 1", shopRowMapper);
        return shops.stream().findFirst();
    }
}
