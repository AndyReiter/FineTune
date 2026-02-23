package com.finetune.app.repository.jdbc;

import com.finetune.app.model.SkiBrand;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class SkiBrandJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public SkiBrandJdbcRepository(JdbcTemplate jdbcTemplate) {
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

    public Optional<SkiBrand> findById(Long id) {
        List<SkiBrand> brands = jdbcTemplate.query("SELECT * FROM ski_brands WHERE id = ?", skiBrandRowMapper, id);
        return brands.stream().findFirst();
    }

    public int save(SkiBrand brand) {
        return jdbcTemplate.update(
            "INSERT INTO ski_brands (name) VALUES (?)",
            brand.getName()
        );
    }

    public int update(SkiBrand brand) {
        return jdbcTemplate.update(
            "UPDATE ski_brands SET name = ? WHERE id = ?",
            brand.getName(), brand.getId()
        );
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM ski_brands WHERE id = ?", id);
    }
}
