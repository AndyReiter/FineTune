package com.finetune.app.controller;

import com.finetune.app.model.SkiBrand;
import com.finetune.app.model.SkiModel;
import com.finetune.app.repository.sql.SkiBrandSqlRepository;
import com.finetune.app.repository.sql.SkiModelSqlRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brands")
@CrossOrigin(origins = "*")
public class BrandController {

    private final SkiBrandSqlRepository brandRepository;
    private final SkiModelSqlRepository modelRepository;

    public BrandController(SkiBrandSqlRepository brandRepository, SkiModelSqlRepository modelRepository) {
        this.brandRepository = brandRepository;
        this.modelRepository = modelRepository;
    }

    // GET /brands → returns list of ski brands (id + name)
    @GetMapping
    public List<SkiBrand> getBrands() {
        return brandRepository.findAll();
    }

    // GET /brands/{id}/models → returns models for a brand (id + name)
    @GetMapping("/{id}/models")
    public List<SkiModel> getModelsByBrand(@PathVariable Long id) {
        return modelRepository.findByBrandId(id);
    }
}
