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
        // New design: equipment no longer stores work_order_id. Relationship is via work_order_items.
        // last_work_order_id may be NULL; preserve nullability
        Object lastWoIdObj = null;
        try {
            lastWoIdObj = rs.getObject("last_work_order_id");
        } catch (Exception ignore) {
            lastWoIdObj = null;
        }
        if (lastWoIdObj != null) {
            e.setLastWorkOrderId(rs.getLong("last_work_order_id"));
        }
        e.setStatus(rs.getString("status"));
        e.setLastServicedDate(rs.getDate("last_serviced_date"));
        e.setLastServiceType(rs.getString("last_service_type"));
        e.setCustomerId(rs.getLong("customer_id"));
        return e;
    };

    public List<Equipment> findByCustomerId(Long customerId) {
        return jdbcTemplate.query("SELECT * FROM equipment WHERE customer_id = ? ORDER BY id ASC", equipmentRowMapper, customerId);
    }

    /**
     * Deprecated: returns equipment associated with a work order via the join table.
     * Legacy callers should migrate to using `WorkOrderItemSqlRepository.findEquipmentIdsByWorkOrderId`
     * and then load equipment by id. This method is kept for API compatibility during migration.
     */
    @Deprecated
    public List<Equipment> findByWorkOrderId(Long workOrderId) {
        String sql = "SELECT e.* FROM equipment e JOIN work_order_items woi ON woi.equipment_id = e.id WHERE woi.work_order_id = ? ORDER BY e.id ASC";
        return jdbcTemplate.query(sql, equipmentRowMapper, workOrderId);
    }
    public int save(Equipment equipment) {
        String conditionValue = equipment.getCondition() != null ? equipment.getCondition().toString() : null;
        if (equipment.getId() == null) {
            java.sql.Date lastServiced = equipment.getLastServicedDate() != null ? java.sql.Date.valueOf(equipment.getLastServicedDate()) : null;
            org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
            int rows = jdbcTemplate.update(connection -> {
                java.sql.PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO equipment (shop_id, type, brand, model, length, serviceType, `condition`, bindingBrand, bindingModel, heightInches, weight, age, abilityLevel, boot_id, status, last_serviced_date, last_service_type, last_work_order_id, customer_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
                );
                ps.setObject(1, 1);
                ps.setString(2, equipment.getType().toString());
                ps.setString(3, equipment.getBrand());
                ps.setString(4, equipment.getModel());
                ps.setObject(5, equipment.getLength());
                ps.setString(6, equipment.getServiceType());
                ps.setString(7, conditionValue);
                ps.setString(8, equipment.getBindingBrand());
                ps.setString(9, equipment.getBindingModel());
                if (equipment.getHeightInches() != null) ps.setInt(10, equipment.getHeightInches()); else ps.setNull(10, java.sql.Types.INTEGER);
                if (equipment.getWeight() != null) ps.setInt(11, equipment.getWeight()); else ps.setNull(11, java.sql.Types.INTEGER);
                if (equipment.getAge() != null) ps.setInt(12, equipment.getAge()); else ps.setNull(12, java.sql.Types.INTEGER);
                ps.setString(13, (equipment.getAbilityLevel() != null ? equipment.getAbilityLevel().name() : null));
                ps.setObject(14, equipment.getBootId());
                ps.setString(15, equipment.getStatus());
                if (lastServiced != null) ps.setDate(16, lastServiced); else ps.setDate(16, null);
                ps.setString(17, equipment.getLastServiceType());
                ps.setObject(18, equipment.getLastWorkOrderId());
                ps.setObject(19, equipment.getCustomerId());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key != null) {
                equipment.setId(key.longValue());
            }
            return rows;
        } else {
            java.sql.Date lastServiced = equipment.getLastServicedDate() != null ? java.sql.Date.valueOf(equipment.getLastServicedDate()) : null;
            return jdbcTemplate.update(
                "UPDATE equipment SET shop_id = ?, type = ?, brand = ?, model = ?, length = ?, serviceType = ?, `condition` = ?, bindingBrand = ?, bindingModel = ?, heightInches = ?, weight = ?, age = ?, abilityLevel = ?, boot_id = ?, status = ?, last_serviced_date = ?, last_service_type = ?, last_work_order_id = ?, customer_id = ? WHERE id = ?",
                1, equipment.getType().toString(), equipment.getBrand(), equipment.getModel(), equipment.getLength(), equipment.getServiceType(), conditionValue, equipment.getBindingBrand(), equipment.getBindingModel(), equipment.getHeightInches(), equipment.getWeight(), equipment.getAge(),
                (equipment.getAbilityLevel() != null ? equipment.getAbilityLevel().name() : null), equipment.getBootId(), equipment.getStatus(), lastServiced, equipment.getLastServiceType(), equipment.getLastWorkOrderId(), equipment.getCustomerId(), equipment.getId()
            );
        }
    }
}
