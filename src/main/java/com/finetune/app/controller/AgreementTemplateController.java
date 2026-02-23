package com.finetune.app.controller;

import com.finetune.app.model.Shop;
import com.finetune.app.model.AgreementTemplate;
import com.finetune.app.service.AgreementTemplateService;
import com.finetune.app.service.ShopService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agreement-templates")
public class AgreementTemplateController {
    private final AgreementTemplateService agreementTemplateService;
    private final ShopService shopService;

    public AgreementTemplateController(AgreementTemplateService agreementTemplateService, ShopService shopService) {
        this.agreementTemplateService = agreementTemplateService;
        this.shopService = shopService;
    }

    // List all templates for a shop
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<AgreementTemplate>> getTemplatesForShop(@PathVariable Long shopId) {
        Shop shop = shopService.getShop(shopId);
        return ResponseEntity.ok(agreementTemplateService.getAllTemplates(shop));
    }

    // Create a new template for a shop
    @PostMapping("/shop/{shopId}")
    public ResponseEntity<AgreementTemplate> createTemplate(
            @PathVariable Long shopId,
            @RequestBody AgreementTemplate template) {
        Shop shop = shopService.getShop(shopId);
        template.setShop(shop);
        template.setIsActive(true); // Make active by default
        AgreementTemplate created = agreementTemplateService.createTemplate(template);
        return ResponseEntity.ok(created);
    }

    // Activate a template
    @PostMapping("/{templateId}/activate")
    public ResponseEntity<AgreementTemplate> activateTemplate(@PathVariable String templateId) {
        return agreementTemplateService.activateTemplate(templateId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}