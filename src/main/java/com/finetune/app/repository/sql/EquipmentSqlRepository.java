package com.finetune.app.repository.sql;

import com.finetune.app.model.Equipment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class EquipmentSqlRepository {
        public List<Equipment> findAll() {
            return jdbcTemplate.query("SELECT * FROM equipment ORDER BY id ASC", equipmentRowMapper);
        }

        public java.util.Optional<Equipment> findById(Long id) {
            List<Equipment> equipmentList = jdbcTemplate.query("SELECT * FROM equipment WHERE id = ?", equipmentRowMapper, id);
            return equipmentList.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(equipmentList.get(0));
        }
    private final JdbcTemplate jdbcTemplate;

    public EquipmentSqlRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Equipment> equipmentRowMapper = (rs, rowNum) -> {
        Equipment e = new Equipment();
        e.setId(rs.getLong("id"));
        e.setType(Equipment.EquipmentType.valueOf(rs.getString("type")));
        e.setBrand(rs.getString("brand"));
        e.setModel(rs.getString("model"));
        e.setLength(rs.getInt("length"));
        e.setServiceType(rs.getString("serviceType"));
        e.setCondition(rs.getString("condition"));
        e.setBindingBrand(rs.getString("bindingBrand"));
        e.setBindingModel(rs.getString("bindingModel"));
        e.setHeightInches(rs.getInt("heightInches"));
        e.setWeight(rs.getInt("weight"));
        e.setAge(rs.getInt("age"));
        e.setAbilityLevel(rs.getString("abilityLevel"));
        // boot_id may be NULL in the DB; use getObject to preserve nullability
        Object bootIdObj = null;
        try {
            bootIdObj = rs.getObject("boot_id");
        } catch (Exception ignore) {
            bootIdObj = null;
        }
        if (bootIdObj != null) {
            e.setBootId(rs.getLong("boot_id"));
        }
        e.setStatus(rs.getString("status"));
        e.setLastServicedDate(rs.getDate("last_serviced_date"));
        e.setLastServiceType(rs.getString("last_service_type"));
        e.setWorkOrderId(rs.getLong("work_order_id"));
        e.setCustomerId(rs.getLong("customer_id"));
        return e;
    };

    public List<Equipment> findByCustomerId(Long customerId) {
        return jdbcTemplate.query("SELECT * FROM equipment WHERE customer_id = ? ORDER BY id ASC", equipmentRowMapper, customerId);
    }

    public List<Equipment> findByWorkOrderId(Long workOrderId) {
        return jdbcTemplate.query("SELECT * FROM equipment WHERE work_order_id = ? ORDER BY id ASC", equipmentRowMapper, workOrderId);
    }
    public int save(Equipment equipment) {
        String conditionValue = equipment.getCondition() != null ? equipment.getCondition().toString() : null;
        if (equipment.getId() == null) {
            return jdbcTemplate.update(
                "INSERT INTO equipment (shop_id, type, brand, model, length, serviceType, `condition`, bindingBrand, bindingModel, heightInches, weight, age, abilityLevel, boot_id, status, last_serviced_date, last_service_type, work_order_id, customer_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                1, equipment.getType().toString(), equipment.getBrand(), equipment.getModel(), equipment.getLength(), equipment.getServiceType(), conditionValue, equipment.getBindingBrand(), equipment.getBindingModel(), equipment.getHeightInches(), equipment.getWeight(), equipment.getAge(),
                (equipment.getAbilityLevel() != null ? equipment.getAbilityLevel().name() : null), equipment.getBootId(), equipment.getStatus(), equipment.getLastServicedDate(), equipment.getLastServiceType(), equipment.getWorkOrderId(), equipment.getCustomerId()
            );
        } else {
            return jdbcTemplate.update(
                "UPDATE equipment SET shop_id = ?, type = ?, brand = ?, model = ?, length = ?, serviceType = ?, `condition` = ?, bindingBrand = ?, bindingModel = ?, heightInches = ?, weight = ?, age = ?, abilityLevel = ?, boot_id = ?, status = ?, last_serviced_date = ?, last_service_type = ?, work_order_id = ?, customer_id = ? WHERE id = ?",
                1, equipment.getType().toString(), equipment.getBrand(), equipment.getModel(), equipment.getLength(), equipment.getServiceType(), conditionValue, equipment.getBindingBrand(), equipment.getBindingModel(), equipment.getHeightInches(), equipment.getWeight(), equipment.getAge(),
                (equipment.getAbilityLevel() != null ? equipment.getAbilityLevel().name() : null), equipment.getBootId(), equipment.getStatus(), equipment.getLastServicedDate(), equipment.getLastServiceType(), equipment.getWorkOrderId(), equipment.getCustomerId(), equipment.getId()
            );
        }
    }
}
