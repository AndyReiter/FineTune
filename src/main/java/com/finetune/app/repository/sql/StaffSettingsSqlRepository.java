package com.finetune.app.repository.sql;

import com.finetune.app.model.StaffSettings;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public class StaffSettingsSqlRepository {
    private final JdbcTemplate jdbcTemplate;

    public StaffSettingsSqlRepository(JdbcTemplate jdbcTemplate) {
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
}
