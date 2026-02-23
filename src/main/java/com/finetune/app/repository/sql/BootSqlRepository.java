        
    
package com.finetune.app.repository.sql;

import com.finetune.app.model.Boot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class BootSqlRepository {
    private final JdbcTemplate jdbcTemplate;

    public BootSqlRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Boot> bootRowMapper = (rs, rowNum) -> {
        Boot b = new Boot();
        b.setId(rs.getLong("id"));
        b.setBrand(rs.getString("brand"));
        b.setModel(rs.getString("model"));
        b.setBsl(rs.getInt("bsl"));
        b.setHeightInches(rs.getInt("heightInches"));
        b.setWeight(rs.getInt("weight"));
        b.setAge(rs.getInt("age"));
        b.setAbilityLevel(rs.getString("abilityLevel"));
        b.setActive(rs.getBoolean("active"));
        b.setCustomerId(rs.getLong("customer_id"));
        return b;
    };

    public List<Boot> findByCustomerId(Long customerId) {
        return jdbcTemplate.query("SELECT * FROM boots WHERE customer_id = ?", bootRowMapper, customerId);
    }

    public Optional<Boot> findByCustomerAndExactMatch(Long customerId, String brand, String model, Integer bsl) {
        List<Boot> boots = jdbcTemplate.query(
            "SELECT * FROM boots WHERE customer_id = ? AND (brand = ? OR brand IS NULL) AND (model = ? OR model IS NULL) AND (bsl = ? OR bsl IS NULL)",
            bootRowMapper, customerId, brand, model, bsl
        );
        return boots.stream().findFirst();
    }
    public Optional<Boot> findById(Long id) {
        List<Boot> boots = jdbcTemplate.query("SELECT * FROM boots WHERE id = ?", bootRowMapper, id);
        return boots.stream().findFirst();
    }
    public int save(Boot boot) {
            if (boot.getId() == null) {
                return jdbcTemplate.update(
                    "INSERT INTO boots (brand, model, bsl, heightInches, weight, age, abilityLevel, active, customer_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    boot.getBrand(), boot.getModel(), boot.getBsl(), boot.getHeightInches(), boot.getWeight(), boot.getAge(),
                    (boot.getAbilityLevel() != null ? boot.getAbilityLevel().name() : null), boot.isActive(), boot.getCustomerId()
                );
            } else {
                return jdbcTemplate.update(
                    "UPDATE boots SET brand = ?, model = ?, bsl = ?, heightInches = ?, weight = ?, age = ?, abilityLevel = ?, active = ?, customer_id = ? WHERE id = ?",
                    boot.getBrand(), boot.getModel(), boot.getBsl(), boot.getHeightInches(), boot.getWeight(), boot.getAge(),
                    (boot.getAbilityLevel() != null ? boot.getAbilityLevel().name() : null), boot.isActive(), boot.getCustomerId(), boot.getId()
                );
            }
        }
}
