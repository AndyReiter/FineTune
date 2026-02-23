package com.finetune.app.repository.sql;

import com.finetune.app.model.WorkOrder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class WorkOrderSqlRepository {
    private final JdbcTemplate jdbcTemplate;

    public WorkOrderSqlRepository(JdbcTemplate jdbcTemplate) {
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

    public List<WorkOrder> findOpenWorkOrdersByCustomer(Long customerId) {
        return jdbcTemplate.query("SELECT * FROM work_orders WHERE customer_id = ? AND status != 'PICKED_UP' ORDER BY createdAt DESC", workOrderRowMapper, customerId);
    }
    public Optional<WorkOrder> findByIdWithEquipment(Long id) {
        // This is a stub. You may want to join with equipment table if needed.
        return findById(id);
    }
        public List<WorkOrder> findByCustomerIdAndStatusIn(Long customerId, List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        String inSql = String.join(",", java.util.Collections.nCopies(statuses.size(), "?"));
        String sql = "SELECT * FROM work_orders WHERE customer_id = ? AND status IN (" + inSql + ")";
        Object[] params = new Object[statuses.size() + 1];
        params[0] = customerId;
        for (int i = 0; i < statuses.size(); i++) {
            params[i + 1] = statuses.get(i);
        }
        return jdbcTemplate.query(sql, params, workOrderRowMapper);
    }

    public int save(WorkOrder workOrder) {
        if (workOrder.getId() == null) {
            return jdbcTemplate.update(
                "INSERT INTO work_orders (customer_id, status, createdAt, promised_by, completed_date, customer_created, notes) VALUES (?, ?, ?, ?, ?, ?, ?)",
                workOrder.getCustomerId(), workOrder.getStatus(), workOrder.getCreatedAt(), workOrder.getPromisedBy(), workOrder.getCompletedDate(), workOrder.getCustomerCreated(), workOrder.getNotes()
            );
        } else {
            return jdbcTemplate.update(
                "UPDATE work_orders SET customer_id = ?, status = ?, createdAt = ?, promised_by = ?, completed_date = ?, customer_created = ?, notes = ? WHERE id = ?",
                workOrder.getCustomerId(), workOrder.getStatus(), workOrder.getCreatedAt(), workOrder.getPromisedBy(), workOrder.getCompletedDate(), workOrder.getCustomerCreated(), workOrder.getNotes(), workOrder.getId()
            );
        }
    }

    public List<WorkOrder> findCompletedWorkOrdersOrderByCompletedDateDesc() {
        return jdbcTemplate.query("SELECT * FROM work_orders WHERE status = 'PICKED_UP' ORDER BY completed_date DESC", workOrderRowMapper);
    }

    public List<WorkOrder> findAllOrderByCreatedAtAsc() {
        return jdbcTemplate.query("SELECT * FROM work_orders ORDER BY createdAt ASC", workOrderRowMapper);
    }

    public List<WorkOrder> findByStatusOrderByCreatedAtAsc(String status) {
        return jdbcTemplate.query("SELECT * FROM work_orders WHERE status = ? ORDER BY createdAt ASC", workOrderRowMapper, status);
    }
    public List<WorkOrder> findByStatusInOrderByCreatedAtAsc(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        String inSql = String.join(",", java.util.Collections.nCopies(statuses.size(), "?"));
        String sql = "SELECT * FROM work_orders WHERE status IN (" + inSql + ") ORDER BY createdAt ASC";
        Object[] params = new Object[statuses.size()];
        for (int i = 0; i < statuses.size(); i++) {
            params[i] = statuses.get(i);
        }
        return jdbcTemplate.query(sql, params, workOrderRowMapper);
    }

    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM work_orders WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM work_orders WHERE id = ?", id);
    }
}
