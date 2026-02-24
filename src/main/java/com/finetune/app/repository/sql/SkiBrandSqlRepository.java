        
    
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
        String sql = "SELECT b.id as brand_id, b.name as brand_name, m.id as model_id, m.name as model_name " +
                     "FROM ski_brands b LEFT JOIN ski_models m ON b.id = m.brand_id ORDER BY b.name, m.name";

        return jdbcTemplate.query(sql, rs -> {
            java.util.Map<Long, SkiBrand> map = new java.util.LinkedHashMap<>();
            while (rs.next()) {
                Long brandId = rs.getLong("brand_id");
                SkiBrand brand = map.get(brandId);
                if (brand == null) {
                    brand = new SkiBrand();
                    brand.setId(brandId);
                    brand.setName(rs.getString("brand_name"));
                    map.put(brandId, brand);
                }
                Long modelId = rs.getObject("model_id") != null ? rs.getLong("model_id") : null;
                if (modelId != null) {
                    String modelName = rs.getString("model_name");
                    com.finetune.app.model.SkiModel model = new com.finetune.app.model.SkiModel();
                    model.setId(modelId);
                    model.setName(modelName);
                    model.setBrand(brand);
                    brand.getModels().add(model);
                }
            }
            return new java.util.ArrayList<>(map.values());
        });
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
            SkiBrand saved = inserted.isEmpty() ? brand : inserted.get(0);
            // Persist models if present
            if (brand.getModels() != null && !brand.getModels().isEmpty()) {
                for (com.finetune.app.model.SkiModel m : brand.getModels()) {
                    jdbcTemplate.update("INSERT INTO ski_models (brand_id, name) VALUES (?, ?)", saved.getId(), m.getName());
                }
            }
            return saved;
        } else {
            // Update existing brand
            jdbcTemplate.update("UPDATE ski_brands SET name = ? WHERE id = ?", brand.getName(), brand.getId());
            // Replace models: delete existing and insert provided models
            jdbcTemplate.update("DELETE FROM ski_models WHERE brand_id = ?", brand.getId());
            if (brand.getModels() != null && !brand.getModels().isEmpty()) {
                for (com.finetune.app.model.SkiModel m : brand.getModels()) {
                    jdbcTemplate.update("INSERT INTO ski_models (brand_id, name) VALUES (?, ?)", brand.getId(), m.getName());
                }
            }
            return brand;
        }
    }
}
