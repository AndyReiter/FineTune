        
    
package com.finetune.app.repository.sql;

import com.finetune.app.model.SkiBrand;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class SkiBrandSqlRepository {
    private final JdbcTemplate jdbcTemplate;

    public SkiBrandSqlRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SkiBrand> skiBrandRowMapper = (rs, rowNum) -> {
        SkiBrand b = new SkiBrand();
        b.setId(rs.getLong("id"));
        b.setName(rs.getString("name"));
        return b;
    };

    public boolean existsByName(String name) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ski_brands WHERE name = ?", Integer.class, name);
        return count != null && count > 0;
    }

    public List<SkiBrand> findAll() {
        return jdbcTemplate.query("SELECT * FROM ski_brands", skiBrandRowMapper);
    }

    public List<SkiBrand> findAllWithModels() {
        String sql = "SELECT b.*, m.id as model_id, m.name as model_name FROM ski_brands b LEFT JOIN ski_models m ON b.id = m.brand_id";
        // Map models manually if needed
        return jdbcTemplate.query(sql, skiBrandRowMapper);
    }
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM ski_brands WHERE id = ?", id);
    }

    public void delete(SkiBrand brand) {
        if (brand != null && brand.getId() != null) {
            deleteById(brand.getId());
        }
    }
    public SkiBrand save(SkiBrand brand) {
        if (brand.getId() == null) {
            // Insert new brand
            jdbcTemplate.update("INSERT INTO ski_brands (name) VALUES (?)", brand.getName());
            // Retrieve inserted brand (assume unique name)
            List<SkiBrand> inserted = jdbcTemplate.query("SELECT * FROM ski_brands WHERE name = ?", skiBrandRowMapper, brand.getName());
            return inserted.isEmpty() ? brand : inserted.get(0);
        } else {
            // Update existing brand
            jdbcTemplate.update("UPDATE ski_brands SET name = ? WHERE id = ?", brand.getName(), brand.getId());
            return brand;
        }
    }
}
