package com.finetune.app.repository.jdbc;

import com.finetune.app.model.SkiModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class SkiModelJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public SkiModelJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SkiModel> skiModelRowMapper = (rs, rowNum) -> {
        SkiModel s = new SkiModel();
        s.setId(rs.getLong("id"));
        s.setName(rs.getString("name"));
        s.setBrandId(rs.getLong("brand_id"));
        return s;
    };

    public List<SkiModel> findByBrandId(Long brandId) {
        return jdbcTemplate.query("SELECT * FROM ski_models WHERE brand_id = ?", skiModelRowMapper, brandId);
    }

    public Optional<SkiModel> findById(Long id) {
        List<SkiModel> models = jdbcTemplate.query("SELECT * FROM ski_models WHERE id = ?", skiModelRowMapper, id);
        return models.stream().findFirst();
    }

    public List<SkiModel> findAll() {
        return jdbcTemplate.query("SELECT * FROM ski_models", skiModelRowMapper);
    }

    public int save(SkiModel model) {
        return jdbcTemplate.update(
            "INSERT INTO ski_models (name, brand_id) VALUES (?, ?)",
            model.getName(), model.getBrandId()
        );
    }

    public int update(SkiModel model) {
        return jdbcTemplate.update(
            "UPDATE ski_models SET name = ?, brand_id = ? WHERE id = ?",
            model.getName(), model.getBrandId(), model.getId()
        );
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM ski_models WHERE id = ?", id);
    }
}
