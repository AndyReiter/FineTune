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
    private final CustomerSqlRepository customerRepository;
    private final EquipmentSqlRepository equipmentRepository;
    private final BootSqlRepository bootRepository;
    private final WorkOrderNoteSqlRepository workOrderNoteRepository;

    public WorkOrderSqlRepository(JdbcTemplate jdbcTemplate, CustomerSqlRepository customerRepository, EquipmentSqlRepository equipmentRepository, BootSqlRepository bootRepository, WorkOrderNoteSqlRepository workOrderNoteRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.customerRepository = customerRepository;
        this.equipmentRepository = equipmentRepository;
        this.bootRepository = bootRepository;
        this.workOrderNoteRepository = workOrderNoteRepository;
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
        if (workOrders.isEmpty()) return workOrders.stream().findFirst();
        WorkOrder wo = workOrders.get(0);
        enrichWorkOrder(wo);
        return java.util.Optional.of(wo);
    }

    public List<WorkOrder> findOpenWorkOrdersByCustomer(Long customerId) {
        return jdbcTemplate.query("SELECT * FROM work_orders WHERE customer_id = ? AND status != 'PICKED_UP' ORDER BY createdAt DESC", workOrderRowMapper, customerId);
    }
    public Optional<WorkOrder> findByIdWithEquipment(Long id) {
        Optional<WorkOrder> maybe = findById(id);
        return maybe;
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
        List<WorkOrder> list = jdbcTemplate.query(sql, params, workOrderRowMapper);
        enrichWorkOrders(list);
        return list;
    }

    public int save(WorkOrder workOrder) {
        if (workOrder.getId() == null) {
            // Insert and capture generated key
            org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                java.sql.PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO work_orders (customer_id, status, createdAt, promised_by, completed_date, customer_created, notes) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
                );
                ps.setObject(1, workOrder.getCustomerId());
                ps.setString(2, workOrder.getStatus());
                if (workOrder.getCreatedAt() != null) ps.setTimestamp(3, java.sql.Timestamp.valueOf(workOrder.getCreatedAt())); else ps.setTimestamp(3, null);
                if (workOrder.getPromisedBy() != null) ps.setDate(4, java.sql.Date.valueOf(workOrder.getPromisedBy())); else ps.setDate(4, null);
                if (workOrder.getCompletedDate() != null) ps.setTimestamp(5, java.sql.Timestamp.valueOf(workOrder.getCompletedDate())); else ps.setTimestamp(5, null);
                ps.setObject(6, workOrder.getCustomerCreated());
                ps.setString(7, workOrder.getNotes());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key != null) {
                workOrder.setId(key.longValue());
                return 1;
            }
            return 0;
        } else {
            return jdbcTemplate.update(
                "UPDATE work_orders SET customer_id = ?, status = ?, createdAt = ?, promised_by = ?, completed_date = ?, customer_created = ?, notes = ? WHERE id = ?",
                workOrder.getCustomerId(), workOrder.getStatus(), workOrder.getCreatedAt(), workOrder.getPromisedBy(), workOrder.getCompletedDate(), workOrder.getCustomerCreated(), workOrder.getNotes(), workOrder.getId()
            );
        }
    }

    public List<WorkOrder> findCompletedWorkOrdersOrderByCompletedDateDesc() {
        List<WorkOrder> list = jdbcTemplate.query("SELECT * FROM work_orders WHERE status = 'PICKED_UP' ORDER BY completed_date DESC", workOrderRowMapper);
        enrichWorkOrders(list);
        return list;
    }

    public List<WorkOrder> findAllOrderByCreatedAtAsc() {
        List<WorkOrder> list = jdbcTemplate.query("SELECT * FROM work_orders ORDER BY createdAt ASC", workOrderRowMapper);
        enrichWorkOrders(list);
        return list;
    }

    public List<WorkOrder> findByStatusOrderByCreatedAtAsc(String status) {
        List<WorkOrder> list = jdbcTemplate.query("SELECT * FROM work_orders WHERE status = ? ORDER BY createdAt ASC", workOrderRowMapper, status);
        enrichWorkOrders(list);
        return list;
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
        List<WorkOrder> list = jdbcTemplate.query(sql, params, workOrderRowMapper);
        enrichWorkOrders(list);
        return list;
    }

    // Helper to enrich a single work order with customer, equipment, and notes
    private void enrichWorkOrder(WorkOrder wo) {
        if (wo == null) return;
        Long cid = wo.getCustomerId();
        if (cid != null) {
            customerRepository.findById(cid).ifPresent(wo::setCustomer);
        }
        // Load equipment list
        try {
            java.util.List<com.finetune.app.model.Equipment> equipmentList = equipmentRepository.findByWorkOrderId(wo.getId());
            // For mount service types, load the boot details for each equipment that has a boot_id
            if (equipmentList != null) {
                for (com.finetune.app.model.Equipment eq : equipmentList) {
                    Long bid = eq.getBootId();
                    if (bid != null) {
                        try {
                            bootRepository.findById(bid).ifPresent(eq::setBoot);
                        } catch (Exception ex) {
                            // ignore individual boot load failures
                        }
                    }
                }
            }
            wo.setEquipment(equipmentList);
        } catch (Exception e) {
            // ignore if equipment repo not available
        }
        // Load notes list
        try {
            wo.setNotesList(workOrderNoteRepository.findByWorkOrderIdOrderByCreatedAtDesc(wo.getId()));
        } catch (Exception e) {
            // ignore if notes repo not available
        }
    }

    private void enrichWorkOrders(List<WorkOrder> list) {
        if (list == null || list.isEmpty()) return;
        for (WorkOrder wo : list) {
            enrichWorkOrder(wo);
        }
    }

    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM work_orders WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM work_orders WHERE id = ?", id);
    }
}
