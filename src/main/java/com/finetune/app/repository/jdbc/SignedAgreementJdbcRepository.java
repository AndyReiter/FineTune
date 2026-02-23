package com.finetune.app.repository.jdbc;

import com.finetune.app.model.SignedAgreement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class SignedAgreementJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public SignedAgreementJdbcRepository(JdbcTemplate jdbcTemplate) {
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

    public Optional<SignedAgreement> findById(String id) {
        List<SignedAgreement> agreements = jdbcTemplate.query("SELECT * FROM signed_agreements WHERE id = ?", signedAgreementRowMapper, id);
        return agreements.stream().findFirst();
    }

    public int save(SignedAgreement agreement) {
        return jdbcTemplate.update(
            "INSERT INTO signed_agreements (id, work_order_id, customer_id, agreement_template_id, pdf_storage_key, signature_name, signature_ip, signature_user_agent, signed_at, document_hash, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            agreement.getId(), agreement.getWorkOrderId(), agreement.getCustomerId(), agreement.getAgreementTemplateId(), agreement.getPdfStorageKey(), agreement.getSignatureName(), agreement.getSignatureIp(), agreement.getSignatureUserAgent(), agreement.getSignedAt(), agreement.getDocumentHash(), agreement.getCreatedAt()
        );
    }

    public int update(SignedAgreement agreement) {
        return jdbcTemplate.update(
            "UPDATE signed_agreements SET work_order_id = ?, customer_id = ?, agreement_template_id = ?, pdf_storage_key = ?, signature_name = ?, signature_ip = ?, signature_user_agent = ?, signed_at = ?, document_hash = ?, created_at = ? WHERE id = ?",
            agreement.getWorkOrderId(), agreement.getCustomerId(), agreement.getAgreementTemplateId(), agreement.getPdfStorageKey(), agreement.getSignatureName(), agreement.getSignatureIp(), agreement.getSignatureUserAgent(), agreement.getSignedAt(), agreement.getDocumentHash(), agreement.getCreatedAt(), agreement.getId()
        );
    }

    public int deleteById(String id) {
        return jdbcTemplate.update("DELETE FROM signed_agreements WHERE id = ?", id);
    }
}
