package com.finetune.app.repository.sql;

import com.finetune.app.model.AgreementTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class AgreementTemplateSqlRepository {
    private final JdbcTemplate jdbcTemplate;

    public AgreementTemplateSqlRepository(JdbcTemplate jdbcTemplate) {
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
    public Optional<AgreementTemplate> findById(Long id) {
        List<AgreementTemplate> templates = jdbcTemplate.query("SELECT * FROM agreement_templates WHERE id = ?", agreementTemplateRowMapper, id);
        return templates.stream().findFirst();
    }

    public AgreementTemplate save(AgreementTemplate template) {
        if (template.getId() == null) {
            jdbcTemplate.update(
                "INSERT INTO agreement_templates (shop_id, title, agreement_text, logo_url, jurisdiction_state, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                template.getShopId(), template.getTitle(), template.getAgreementText(), template.getLogoUrl(), template.getJurisdictionState(), template.getIsActive(), template.getCreatedAt()
            );
            // Optionally fetch and set the generated ID
        } else {
            jdbcTemplate.update(
                "UPDATE agreement_templates SET shop_id = ?, title = ?, agreement_text = ?, logo_url = ?, jurisdiction_state = ?, is_active = ?, created_at = ? WHERE id = ?",
                template.getShopId(), template.getTitle(), template.getAgreementText(), template.getLogoUrl(), template.getJurisdictionState(), template.getIsActive(), template.getCreatedAt(), template.getId()
            );
        }
        return template;
    }

    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM agreement_templates WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM agreement_templates WHERE id = ?", id);
    }

    public List<AgreementTemplate> findByIsActiveTrue() {
        return jdbcTemplate.query("SELECT * FROM agreement_templates WHERE is_active = TRUE", agreementTemplateRowMapper);
    }
}
