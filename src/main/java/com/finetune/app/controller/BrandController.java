package com.finetune.app.controller;

import com.finetune.app.model.entity.SkiBrand;
import com.finetune.app.model.entity.SkiModel;
import com.finetune.app.repository.SkiBrandRepository;
import com.finetune.app.repository.SkiModelRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brands")
@CrossOrigin(origins = "*")
public class BrandController {

    private final SkiBrandRepository brandRepository;
    private final SkiModelRepository modelRepository;

    public BrandController(SkiBrandRepository brandRepository, SkiModelRepository modelRepository) {
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
        return modelRepository.findByBrand_Id(id);
    }
}
