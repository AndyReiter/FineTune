package com.finetune.app.repository.jdbc;

import com.finetune.app.model.Staff;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public class StaffJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public StaffJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Staff> staffRowMapper = (rs, rowNum) -> {
        Staff s = new Staff();
        s.setId(rs.getLong("id"));
        s.setEmail(rs.getString("email"));
        s.setPassword(rs.getString("password"));
        s.setRole(rs.getString("role"));
        s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        s.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return s;
    };

    public Optional<Staff> findByEmail(String email) {
        List<Staff> staff = jdbcTemplate.query("SELECT * FROM staff WHERE email = ?", staffRowMapper, email);
        return staff.stream().findFirst();
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM staff WHERE email = ?", Integer.class, email);
        return count != null && count > 0;
    }

    public Optional<Staff> findById(Long id) {
        List<Staff> staff = jdbcTemplate.query("SELECT * FROM staff WHERE id = ?", staffRowMapper, id);
        return staff.stream().findFirst();
    }

    public List<Staff> findAll() {
        return jdbcTemplate.query("SELECT * FROM staff", staffRowMapper);
    }

    public int save(Staff staff) {
        return jdbcTemplate.update(
            "INSERT INTO staff (email, password, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
            staff.getEmail(), staff.getPassword(), staff.getRole(), staff.getCreatedAt(), staff.getUpdatedAt()
        );
    }

    public int update(Staff staff) {
        return jdbcTemplate.update(
            "UPDATE staff SET email = ?, password = ?, role = ?, created_at = ?, updated_at = ? WHERE id = ?",
            staff.getEmail(), staff.getPassword(), staff.getRole(), staff.getCreatedAt(), staff.getUpdatedAt(), staff.getId()
        );
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM staff WHERE id = ?", id);
    }
}
