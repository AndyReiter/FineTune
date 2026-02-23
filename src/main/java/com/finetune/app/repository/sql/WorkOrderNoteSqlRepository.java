package com.finetune.app.repository.sql;

import com.finetune.app.model.WorkOrderNote;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class WorkOrderNoteSqlRepository {
    private final JdbcTemplate jdbcTemplate;

    public WorkOrderNoteSqlRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<WorkOrderNote> workOrderNoteRowMapper = (rs, rowNum) -> {
        WorkOrderNote n = new WorkOrderNote();
        n.setId(rs.getLong("id"));
        n.setWorkOrderId(rs.getLong("work_order_id"));
        n.setNoteText(rs.getString("note_text"));
        n.setCreatedBy(rs.getString("created_by"));
        n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return n;
    };

    public List<WorkOrderNote> findByWorkOrderIdOrderByCreatedAtDesc(Long workOrderId) {
        return jdbcTemplate.query("SELECT * FROM work_order_notes WHERE work_order_id = ? ORDER BY created_at DESC", workOrderNoteRowMapper, workOrderId);
    }
    public WorkOrderNote insert(Long workOrderId, String noteText, String createdBy) {
        String sql = "INSERT INTO work_order_notes (work_order_id, note_text, created_by, created_at) VALUES (?, ?, ?, ?)";
        org.springframework.jdbc.support.GeneratedKeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, workOrderId);
            ps.setString(2, noteText);
            ps.setString(3, createdBy);
            ps.setTimestamp(4, java.sql.Timestamp.valueOf(now));
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKey().longValue();
        WorkOrderNote note = new WorkOrderNote();
        note.setId(id);
        note.setWorkOrderId(workOrderId);
        note.setNoteText(noteText);
        note.setCreatedBy(createdBy);
        note.setCreatedAt(now);
        return note;
    }
}
