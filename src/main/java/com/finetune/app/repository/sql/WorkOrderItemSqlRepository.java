package com.finetune.app.repository.sql;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WorkOrderItemSqlRepository {
    private final JdbcTemplate jdbcTemplate;

    public WorkOrderItemSqlRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int addAssociation(Long workOrderId, Long equipmentId) {
        return jdbcTemplate.update("INSERT INTO work_order_items (work_order_id, equipment_id) VALUES (?, ?)", workOrderId, equipmentId);
    }

    /**
     * Public API: add equipment to a work order by inserting into work_order_items.
     * Kept for clearer naming and to match refactor requirements.
     */
    public int addEquipmentToWorkOrder(Long workOrderId, Long equipmentId) {
        return addAssociation(workOrderId, equipmentId);
    }

    public int deleteByWorkOrderId(Long workOrderId) {
        return jdbcTemplate.update("DELETE FROM work_order_items WHERE work_order_id = ?", workOrderId);
    }

    public List<Long> findEquipmentIdsByWorkOrderId(Long workOrderId) {
        return jdbcTemplate.queryForList("SELECT equipment_id FROM work_order_items WHERE work_order_id = ? ORDER BY id ASC", Long.class, workOrderId);
    }

    /**
     * Mark work_order_items rows for a given work order as completed by setting completed_at.
     */
    public int markWorkOrderItemsCompleted(Long workOrderId, java.sql.Timestamp completedAt) {
        return jdbcTemplate.update("UPDATE work_order_items SET completed_at = ? WHERE work_order_id = ?", completedAt, workOrderId);
    }
}
