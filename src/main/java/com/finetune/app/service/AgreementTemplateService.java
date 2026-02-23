package com.finetune.app.service;

import com.finetune.app.model.Shop;
import com.finetune.app.model.AgreementTemplate;
import com.finetune.app.repository.sql.AgreementTemplateSqlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing agreement templates.
 * Enforces the business rule that only one active template per shop should exist.
 */
@Service
public class AgreementTemplateService {

    private final AgreementTemplateSqlRepository agreementTemplateRepository;

    public AgreementTemplateService(AgreementTemplateSqlRepository agreementTemplateRepository) {
        this.agreementTemplateRepository = agreementTemplateRepository;
    }

    /**
     * Get the active agreement template for a shop.
     * 
     * @param shop The shop to get the active template for
     * @return Optional containing the active template if found
     */
    public Optional<AgreementTemplate> getActiveTemplate(Shop shop) {
        return agreementTemplateRepository.findByShopAndIsActiveTrue(shop.getId());
    }

    /**
     * Get all templates for a shop (active and inactive).
     * 
     * @param shop The shop to get templates for
     * @return List of all templates for the shop
     */
    public List<AgreementTemplate> getAllTemplates(Shop shop) {
        return agreementTemplateRepository.findByShop(shop.getId());
    }

    /**
     * Get a template by its ID.
     * 
     * @param id The template ID
     * @return Optional containing the template if found
     */
    public Optional<AgreementTemplate> getTemplateById(String id) {
        return agreementTemplateRepository.findById(Long.valueOf(id));
    }

    /**
     * Create a new agreement template.
     * If the template is active, automatically deactivates any existing active templates for the shop.
     * 
     * @param template The template to create
     * @return The created template
     */
    @Transactional
    public AgreementTemplate createTemplate(AgreementTemplate template) {
        if (template.getIsActive()) {
            deactivateExistingTemplates(template.getShop());
        }
        return agreementTemplateRepository.save(template);
    }

    /**
     * Update an existing agreement template.
     * If the template is being set to active, automatically deactivates other active templates for the shop.
     * 
     * @param id The template ID
     * @param updatedTemplate The updated template data
     * @return Optional containing the updated template if found
     */
    @Transactional
    public Optional<AgreementTemplate> updateTemplate(String id, AgreementTemplate updatedTemplate) {
        return agreementTemplateRepository.findById(Long.valueOf(id)).map(existing -> {
            existing.setTitle(updatedTemplate.getTitle());
            existing.setAgreementText(updatedTemplate.getAgreementText());
            existing.setLogoUrl(updatedTemplate.getLogoUrl());
            existing.setJurisdictionState(updatedTemplate.getJurisdictionState());
            
            // If setting this template to active, deactivate others
            if (updatedTemplate.getIsActive() && !existing.getIsActive()) {
                deactivateExistingTemplates(existing.getShop());
            }
            
            existing.setIsActive(updatedTemplate.getIsActive());
            
            return agreementTemplateRepository.save(existing);
        });
    }

    /**
     * Set a template as active for its shop.
     * Automatically deactivates any other active templates for the same shop.
     * 
     * @param id The template ID to activate
     * @return Optional containing the activated template if found
     */
    @Transactional
    public Optional<AgreementTemplate> activateTemplate(String id) {
        return agreementTemplateRepository.findById(Long.valueOf(id)).map(template -> {
            deactivateExistingTemplates(template.getShop());
            template.setIsActive(true);
            return agreementTemplateRepository.save(template);
        });
    }

    /**
     * Deactivate a template.
     * 
     * @param id The template ID to deactivate
     * @return Optional containing the deactivated template if found
     */
    @Transactional
    public Optional<AgreementTemplate> deactivateTemplate(String id) {
        return agreementTemplateRepository.findById(Long.valueOf(id)).map(template -> {
            template.setIsActive(false);
            return agreementTemplateRepository.save(template);
        });
    }

    /**
     * Delete a template.
     * 
     * @param id The template ID to delete
     * @return true if the template was deleted, false if not found
     */
    @Transactional
    public boolean deleteTemplate(String id) {
        Long longId = Long.valueOf(id);
        if (agreementTemplateRepository.existsById(longId)) {
            agreementTemplateRepository.deleteById(longId);
            return true;
        }
        return false;
    }

    /**
     * Deactivate all active templates for a specific shop.
     * This is called internally to enforce the business rule:
     * only one active template per shop.
     * 
     * @param shop The shop to deactivate templates for
     */
    @Transactional
    protected void deactivateExistingTemplates(Shop shop) {
        List<AgreementTemplate> activeTemplates = agreementTemplateRepository.findByShopAndIsActive(shop.getId(), true);
        activeTemplates.forEach(template -> {
            template.setIsActive(false);
            agreementTemplateRepository.save(template);
        });
    }

    /**
     * Get all active templates across all shops.
     * 
     * @return List of all active templates
     */
    public List<AgreementTemplate> getAllActiveTemplates() {
        return agreementTemplateRepository.findByIsActiveTrue();
    }
}
