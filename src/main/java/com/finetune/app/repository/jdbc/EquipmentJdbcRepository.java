package com.finetune.app.repository.jdbc;

import com.finetune.app.model.Equipment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class EquipmentJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public EquipmentJdbcRepository(JdbcTemplate jdbcTemplate) {
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
        e.setBootId(rs.getLong("boot_id"));
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
     * Legacy callers should migrate to `WorkOrderItemSqlRepository.findEquipmentIdsByWorkOrderId`
     * then load equipment by id. Kept for backward compatibility during migration.
     */
    @Deprecated
    public List<Equipment> findByWorkOrderId(Long workOrderId) {
        String sql = "SELECT e.* FROM equipment e JOIN work_order_items woi ON woi.equipment_id = e.id WHERE woi.work_order_id = ? ORDER BY e.id ASC";
        return jdbcTemplate.query(sql, equipmentRowMapper, workOrderId);
    }

    public Optional<Equipment> findById(Long id) {
        List<Equipment> equipment = jdbcTemplate.query("SELECT * FROM equipment WHERE id = ?", equipmentRowMapper, id);
        return equipment.stream().findFirst();
    }

    public int save(Equipment equipment) {
        return jdbcTemplate.update(
            "INSERT INTO equipment (type, brand, model, length, serviceType, condition, bindingBrand, bindingModel, heightInches, weight, age, abilityLevel, boot_id, status, last_serviced_date, last_service_type, customer_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            equipment.getType(), equipment.getBrand(), equipment.getModel(), equipment.getLength(), equipment.getServiceType(), equipment.getCondition(), equipment.getBindingBrand(), equipment.getBindingModel(), equipment.getHeightInches(), equipment.getWeight(), equipment.getAge(), equipment.getAbilityLevel(), equipment.getBootId(), equipment.getStatus(), equipment.getLastServicedDate(), equipment.getLastServiceType(), equipment.getCustomerId()
        );
    }

    public int update(Equipment equipment) {
        return jdbcTemplate.update(
            "UPDATE equipment SET type = ?, brand = ?, model = ?, length = ?, serviceType = ?, condition = ?, bindingBrand = ?, bindingModel = ?, heightInches = ?, weight = ?, age = ?, abilityLevel = ?, boot_id = ?, status = ?, last_serviced_date = ?, last_service_type = ?, customer_id = ? WHERE id = ?",
            equipment.getType(), equipment.getBrand(), equipment.getModel(), equipment.getLength(), equipment.getServiceType(), equipment.getCondition(), equipment.getBindingBrand(), equipment.getBindingModel(), equipment.getHeightInches(), equipment.getWeight(), equipment.getAge(), equipment.getAbilityLevel(), equipment.getBootId(), equipment.getStatus(), equipment.getLastServicedDate(), equipment.getLastServiceType(), equipment.getCustomerId(), equipment.getId()
        );
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM equipment WHERE id = ?", id);
    }
}
