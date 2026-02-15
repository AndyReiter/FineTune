-- Work Order Notes Table
-- Simple append-only notes for work orders
-- CASCADE DELETE: Notes are automatically deleted when parent work order is deleted

CREATE TABLE work_order_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    note_text TEXT NOT NULL,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_work_order_notes_work_order
        FOREIGN KEY (work_order_id) 
        REFERENCES work_orders(id) 
        ON DELETE CASCADE
);

-- Index for efficient lookup of notes by work order
CREATE INDEX idx_work_order_notes_work_order_id ON work_order_notes(work_order_id);
