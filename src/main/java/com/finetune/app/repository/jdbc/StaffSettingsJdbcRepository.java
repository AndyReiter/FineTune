package com.finetune.app.repository.jdbc;

import com.finetune.app.model.StaffSettings;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public class StaffSettingsJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public StaffSettingsJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<StaffSettings> staffSettingsRowMapper = (rs, rowNum) -> {
        StaffSettings s = new StaffSettings();
        s.setId(rs.getLong("id"));
        s.setMaxCustomerWorkOrdersPerDay(rs.getInt("max_customer_work_orders_per_day"));
        s.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return s;
    };

    public Optional<StaffSettings> findFirstByOrderByIdAsc() {
        List<StaffSettings> settings = jdbcTemplate.query("SELECT * FROM staff_settings ORDER BY id ASC LIMIT 1", staffSettingsRowMapper);
        return settings.stream().findFirst();
    }

    public Optional<StaffSettings> findById(Long id) {
        List<StaffSettings> settings = jdbcTemplate.query("SELECT * FROM staff_settings WHERE id = ?", staffSettingsRowMapper, id);
        return settings.stream().findFirst();
    }

    public int save(StaffSettings settings) {
        return jdbcTemplate.update(
            "INSERT INTO staff_settings (max_customer_work_orders_per_day, updated_at, created_at) VALUES (?, ?, ?)",
            settings.getMaxCustomerWorkOrdersPerDay(), settings.getUpdatedAt(), settings.getCreatedAt()
        );
    }

    public int update(StaffSettings settings) {
        return jdbcTemplate.update(
            "UPDATE staff_settings SET max_customer_work_orders_per_day = ?, updated_at = ?, created_at = ? WHERE id = ?",
            settings.getMaxCustomerWorkOrdersPerDay(), settings.getUpdatedAt(), settings.getCreatedAt(), settings.getId()
        );
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM staff_settings WHERE id = ?", id);
    }
}
