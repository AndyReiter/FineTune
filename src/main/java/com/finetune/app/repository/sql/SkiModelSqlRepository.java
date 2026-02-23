package com.finetune.app.repository.sql;

import com.finetune.app.model.SkiModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class SkiModelSqlRepository {
    private final JdbcTemplate jdbcTemplate;

    public SkiModelSqlRepository(JdbcTemplate jdbcTemplate) {
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
}
