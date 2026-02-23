package com.finetune.app.repository.jdbc;

import com.finetune.app.model.WorkOrderNote;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class WorkOrderNoteJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public WorkOrderNoteJdbcRepository(JdbcTemplate jdbcTemplate) {
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

    public Optional<WorkOrderNote> findById(Long id) {
        List<WorkOrderNote> notes = jdbcTemplate.query("SELECT * FROM work_order_notes WHERE id = ?", workOrderNoteRowMapper, id);
        return notes.stream().findFirst();
    }

    public int save(WorkOrderNote note) {
        return jdbcTemplate.update(
            "INSERT INTO work_order_notes (work_order_id, note_text, created_by, created_at) VALUES (?, ?, ?, ?)",
            note.getWorkOrderId(), note.getNoteText(), note.getCreatedBy(), note.getCreatedAt()
        );
    }

    public int update(WorkOrderNote note) {
        return jdbcTemplate.update(
            "UPDATE work_order_notes SET work_order_id = ?, note_text = ?, created_by = ?, created_at = ? WHERE id = ?",
            note.getWorkOrderId(), note.getNoteText(), note.getCreatedBy(), note.getCreatedAt(), note.getId()
        );
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM work_order_notes WHERE id = ?", id);
    }
}
