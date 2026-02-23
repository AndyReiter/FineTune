package com.finetune.app.repository.sql;

import com.finetune.app.model.SignedAgreement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class SignedAgreementSqlRepository {
    public List<SignedAgreement> findByAgreementTemplate(Long agreementTemplateId) {
        return jdbcTemplate.query("SELECT * FROM signed_agreements WHERE agreement_template_id = ?", signedAgreementRowMapper, agreementTemplateId);
    }
    public List<SignedAgreement> findByWorkOrder(Long workOrderId) {
        return jdbcTemplate.query("SELECT * FROM signed_agreements WHERE work_order_id = ?", signedAgreementRowMapper, workOrderId);
    }
    public List<SignedAgreement> findByCustomer(Long customerId) {
        return jdbcTemplate.query("SELECT * FROM signed_agreements WHERE customer_id = ?", signedAgreementRowMapper, customerId);
    }
            public SignedAgreement findById(String id) {
                List<SignedAgreement> agreements = jdbcTemplate.query("SELECT * FROM signed_agreements WHERE id = ?", signedAgreementRowMapper, id);
                return agreements.isEmpty() ? null : agreements.get(0);
            }

            public List<SignedAgreement> findByDocumentHash(String documentHash) {
                return jdbcTemplate.query("SELECT * FROM signed_agreements WHERE document_hash = ?", signedAgreementRowMapper, documentHash);
            }

            public List<SignedAgreement> findByCustomerAndSignedAtBetween(Long customerId, java.time.LocalDateTime start, java.time.LocalDateTime end) {
                return jdbcTemplate.query("SELECT * FROM signed_agreements WHERE customer_id = ? AND signed_at BETWEEN ? AND ?", signedAgreementRowMapper, customerId, start, end);
            }

            public List<SignedAgreement> findBySignedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end) {
                return jdbcTemplate.query("SELECT * FROM signed_agreements WHERE signed_at BETWEEN ? AND ?", signedAgreementRowMapper, start, end);
            }

            public java.util.Optional<SignedAgreement> findFirstByCustomerOrderBySignedAtDesc(Long customerId) {
                List<SignedAgreement> agreements = jdbcTemplate.query("SELECT * FROM signed_agreements WHERE customer_id = ? ORDER BY signed_at DESC LIMIT 1", signedAgreementRowMapper, customerId);
                return agreements.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(agreements.get(0));
            }

            public boolean existsByWorkOrder(Long workOrderId) {
                List<SignedAgreement> agreements = jdbcTemplate.query("SELECT * FROM signed_agreements WHERE work_order_id = ?", signedAgreementRowMapper, workOrderId);
                return !agreements.isEmpty();
            }

            public java.util.Optional<SignedAgreement> findFirstByWorkOrderId(Long workOrderId) {
                List<SignedAgreement> agreements = jdbcTemplate.query("SELECT * FROM signed_agreements WHERE work_order_id = ? ORDER BY signed_at DESC LIMIT 1", signedAgreementRowMapper, workOrderId);
                return agreements.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(agreements.get(0));
            }

            public List<SignedAgreement> findAll() {
                return jdbcTemplate.query("SELECT * FROM signed_agreements ORDER BY id ASC", signedAgreementRowMapper);
            }
        public SignedAgreement save(SignedAgreement agreement) {
            jdbcTemplate.update(
                "INSERT INTO signed_agreements (id, work_order_id, customer_id, agreement_template_id, pdf_storage_key, signature_name, signature_ip, signature_user_agent, signed_at, document_hash, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                agreement.getId(),
                agreement.getWorkOrderId(),
                agreement.getCustomerId(),
                agreement.getAgreementTemplateId(),
                agreement.getPdfStorageKey(),
                agreement.getSignatureName(),
                agreement.getSignatureIp(),
                agreement.getSignatureUserAgent(),
                agreement.getSignedAt(),
                agreement.getDocumentHash(),
                agreement.getCreatedAt()
            );
            return agreement;
        }

    private final JdbcTemplate jdbcTemplate;

    public SignedAgreementSqlRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SignedAgreement> signedAgreementRowMapper = (rs, rowNum) -> {
        SignedAgreement s = new SignedAgreement();
        s.setId(rs.getString("id"));
        s.setWorkOrderId(rs.getLong("work_order_id"));
        s.setCustomerId(rs.getLong("customer_id"));
        s.setAgreementTemplateId(rs.getString("agreement_template_id"));
        s.setPdfStorageKey(rs.getString("pdf_storage_key"));
        s.setSignatureName(rs.getString("signature_name"));
        s.setSignatureIp(rs.getString("signature_ip"));
        s.setSignatureUserAgent(rs.getString("signature_user_agent"));
        s.setSignedAt(rs.getTimestamp("signed_at").toLocalDateTime());
        s.setDocumentHash(rs.getString("document_hash"));
        s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return s;
    };

    public List<SignedAgreement> findByWorkOrderId(Long workOrderId) {
        return jdbcTemplate.query("SELECT * FROM signed_agreements WHERE work_order_id = ?", signedAgreementRowMapper, workOrderId);
    }

    public List<SignedAgreement> findByCustomerId(Long customerId) {
        return jdbcTemplate.query("SELECT * FROM signed_agreements WHERE customer_id = ?", signedAgreementRowMapper, customerId);
    }

    public List<SignedAgreement> findByAgreementTemplateId(String agreementTemplateId) {
        return jdbcTemplate.query("SELECT * FROM signed_agreements WHERE agreement_template_id = ?", signedAgreementRowMapper, agreementTemplateId);
    }
}
