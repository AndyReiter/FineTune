    
package com.finetune.app.repository.sql;

import com.finetune.app.model.Staff;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public class StaffSqlRepository {
    private final JdbcTemplate jdbcTemplate;
    @org.springframework.beans.factory.annotation.Autowired
    private com.finetune.app.repository.sql.ShopSqlRepository shopRepository;

    public StaffSqlRepository(JdbcTemplate jdbcTemplate) {
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

    /**
     * Find staff by email and load associated shops via staff_shops join
     */
    public Optional<Staff> findByEmailWithShops(String email) {
        Optional<Staff> sOpt = findByEmail(email);
        if (sOpt.isEmpty()) return sOpt;
        Staff s = sOpt.get();

        // Load shops for this staff
        try {
            List<com.finetune.app.model.Shop> shops = jdbcTemplate.query(
                "SELECT sh.id, sh.name, sh.slug, sh.logo_url, sh.created_at FROM shop sh JOIN staff_shops ss ON sh.id = ss.shop_id WHERE ss.staff_id = ?",
                (rs, rowNum) -> {
                    com.finetune.app.model.Shop shop = new com.finetune.app.model.Shop();
                    shop.setId(rs.getLong("id"));
                    shop.setName(rs.getString("name"));
                    try { shop.setSlug(rs.getString("slug")); } catch (Exception ignore) {}
                    try { shop.setLogoUrl(rs.getString("logo_url")); } catch (Exception ignore) {}
                    try { shop.setCreatedAt(rs.getTimestamp("created_at").toInstant()); } catch (Exception ignore) {}
                    return shop;
                }, s.getId());

            s.setShops(shops);
        } catch (Exception ignore) {}

        return Optional.of(s);
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM staff WHERE email = ?", Integer.class, email);
        return count != null && count > 0;
    }

    public Optional<Staff> findById(Long id) {
        List<Staff> staff = jdbcTemplate.query("SELECT * FROM staff WHERE id = ?", staffRowMapper, id);
        return staff.stream().findFirst();
    }
    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM staff", Long.class);
        return count != null ? count : 0;
    }

    public Staff save(Staff staff) {
        if (staff.getId() == null) {
            // Insert new staff
            jdbcTemplate.update("INSERT INTO staff (email, password, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                staff.getEmail(), staff.getPassword(), staff.getRole(),
                java.sql.Timestamp.valueOf(staff.getCreatedAt()),
                java.sql.Timestamp.valueOf(staff.getUpdatedAt()));
            // Retrieve inserted staff (assume unique email)
            List<Staff> inserted = jdbcTemplate.query("SELECT * FROM staff WHERE email = ?", staffRowMapper, staff.getEmail());
            return inserted.isEmpty() ? staff : inserted.get(0);
        } else {
            // Update existing staff
            jdbcTemplate.update("UPDATE staff SET email = ?, password = ?, role = ?, created_at = ?, updated_at = ? WHERE id = ?",
                staff.getEmail(), staff.getPassword(), staff.getRole(),
                java.sql.Timestamp.valueOf(staff.getCreatedAt()),
                java.sql.Timestamp.valueOf(staff.getUpdatedAt()),
                staff.getId());
            return staff;
        }
    }
}
