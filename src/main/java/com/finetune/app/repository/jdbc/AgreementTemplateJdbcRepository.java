package com.finetune.app.repository.jdbc;

import com.finetune.app.model.AgreementTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class AgreementTemplateJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public AgreementTemplateJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AgreementTemplate> agreementTemplateRowMapper = (rs, rowNum) -> {
        AgreementTemplate a = new AgreementTemplate();
        a.setId(rs.getLong("id"));
        a.setShopId(rs.getLong("shop_id"));
        a.setTitle(rs.getString("title"));
        a.setAgreementText(rs.getString("agreement_text"));
        a.setLogoUrl(rs.getString("logo_url"));
        a.setJurisdictionState(rs.getString("jurisdiction_state"));
        a.setIsActive(rs.getBoolean("is_active"));
        a.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return a;
    };

    public Optional<AgreementTemplate> findByShopAndIsActiveTrue(Long shopId) {
        List<AgreementTemplate> templates = jdbcTemplate.query("SELECT * FROM agreement_templates WHERE shop_id = ? AND is_active = TRUE", agreementTemplateRowMapper, shopId);
        return templates.stream().findFirst();
    }

    public List<AgreementTemplate> findByShop(Long shopId) {
        return jdbcTemplate.query("SELECT * FROM agreement_templates WHERE shop_id = ?", agreementTemplateRowMapper, shopId);
    }

    public List<AgreementTemplate> findByShopAndIsActive(Long shopId, Boolean isActive) {
        return jdbcTemplate.query("SELECT * FROM agreement_templates WHERE shop_id = ? AND is_active = ?", agreementTemplateRowMapper, shopId, isActive);
    }

    public Optional<AgreementTemplate> findById(String id) {
        List<AgreementTemplate> templates = jdbcTemplate.query("SELECT * FROM agreement_templates WHERE id = ?", agreementTemplateRowMapper, id);
        return templates.stream().findFirst();
    }

    public int save(AgreementTemplate template) {
        return jdbcTemplate.update(
            "INSERT INTO agreement_templates (id, shop_id, title, agreement_text, logo_url, jurisdiction_state, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            template.getId(), template.getShopId(), template.getTitle(), template.getAgreementText(), template.getLogoUrl(), template.getJurisdictionState(), template.getIsActive(), template.getCreatedAt()
        );
    }

    public int update(AgreementTemplate template) {
        return jdbcTemplate.update(
            "UPDATE agreement_templates SET shop_id = ?, title = ?, agreement_text = ?, logo_url = ?, jurisdiction_state = ?, is_active = ?, created_at = ? WHERE id = ?",
            template.getShopId(), template.getTitle(), template.getAgreementText(), template.getLogoUrl(), template.getJurisdictionState(), template.getIsActive(), template.getCreatedAt(), template.getId()
        );
    }

    public int deleteById(String id) {
        return jdbcTemplate.update("DELETE FROM agreement_templates WHERE id = ?", id);
    }
}
