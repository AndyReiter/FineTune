package com.finetune.app.repository.jdbc;

import com.finetune.app.model.WorkOrder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class WorkOrderJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public WorkOrderJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<WorkOrder> workOrderRowMapper = (rs, rowNum) -> {
        WorkOrder w = new WorkOrder();
        w.setId(rs.getLong("id"));
        w.setCustomerId(rs.getLong("customer_id"));
        w.setStatus(rs.getString("status"));
        java.sql.Timestamp createdAtTs = rs.getTimestamp("createdAt");
        w.setCreatedAt(createdAtTs != null ? createdAtTs.toLocalDateTime() : null);
        java.sql.Date promisedByDate = rs.getDate("promised_by");
        w.setPromisedBy(promisedByDate != null ? promisedByDate.toLocalDate() : null);
        java.sql.Timestamp completedDateTs = rs.getTimestamp("completed_date");
        w.setCompletedDate(completedDateTs != null ? completedDateTs.toLocalDateTime() : null);
        w.setCustomerCreated(rs.getBoolean("customer_created"));
        w.setNotes(rs.getString("notes"));
        return w;
    };

    public Optional<WorkOrder> findById(Long id) {
        List<WorkOrder> workOrders = jdbcTemplate.query("SELECT * FROM work_orders WHERE id = ?", workOrderRowMapper, id);
        return workOrders.stream().findFirst();
    }

    public List<WorkOrder> findByCustomerId(Long customerId) {
        return jdbcTemplate.query("SELECT * FROM work_orders WHERE customer_id = ?", workOrderRowMapper, customerId);
    }

    public List<WorkOrder> findOpenWorkOrdersByCustomer(Long customerId) {
        return jdbcTemplate.query("SELECT * FROM work_orders WHERE customer_id = ? AND status != 'PICKED_UP' ORDER BY createdAt DESC", workOrderRowMapper, customerId);
    }

    public List<WorkOrder> findAll() {
        return jdbcTemplate.query("SELECT * FROM work_orders", workOrderRowMapper);
    }

    public int save(WorkOrder workOrder) {
        return jdbcTemplate.update(
            "INSERT INTO work_orders (customer_id, status, createdAt, promised_by, completed_date, customer_created, notes) VALUES (?, ?, ?, ?, ?, ?, ?)",
            workOrder.getCustomerId(), workOrder.getStatus(), workOrder.getCreatedAt(), workOrder.getPromisedBy(), workOrder.getCompletedDate(), workOrder.getCustomerCreated(), workOrder.getNotes()
        );
    }

    public int update(WorkOrder workOrder) {
        return jdbcTemplate.update(
            "UPDATE work_orders SET customer_id = ?, status = ?, createdAt = ?, promised_by = ?, completed_date = ?, customer_created = ?, notes = ? WHERE id = ?",
            workOrder.getCustomerId(), workOrder.getStatus(), workOrder.getCreatedAt(), workOrder.getPromisedBy(), workOrder.getCompletedDate(), workOrder.getCustomerCreated(), workOrder.getNotes(), workOrder.getId()
        );
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM work_orders WHERE id = ?", id);
    }
}
