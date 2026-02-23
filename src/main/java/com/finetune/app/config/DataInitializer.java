package com.finetune.app.config;

import com.finetune.app.model.SkiBrand;
import com.finetune.app.model.SkiModel;
import com.finetune.app.repository.sql.SkiBrandSqlRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SkiBrandSqlRepository brandRepository;
    private final ResourceLoader resourceLoader;

    public DataInitializer(SkiBrandSqlRepository brandRepository, ResourceLoader resourceLoader) {
        this.brandRepository = brandRepository;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(String... args) {
        System.out.println("Analyzing SkiModels.json and comparing with database...");
        syncDatabaseWithJson();
        System.out.println("Database sync completed!");
    }

    /**
     * Compare JSON file content with database and update only what's different
     */
    @Transactional
    private void syncDatabaseWithJson() {
        try {
            // Load JSON data
            List<BrandData> jsonBrands = loadBrandsFromJson();
            
            // Get existing brands from database with models eagerly loaded
            List<SkiBrand> dbBrands = brandRepository.findAllWithModels();
            
            // Compare and sync
            boolean hasChanges = false;
            
            for (BrandData jsonBrand : jsonBrands) {
                SkiBrand existingBrand = findBrandByName(dbBrands, jsonBrand.name);
                
                if (existingBrand == null) {
                    // Brand doesn't exist - add it
                    System.out.println("Adding new brand: " + jsonBrand.name + " with " + jsonBrand.models.size() + " models");
                    addBrandWithModels(jsonBrand.name, jsonBrand.models);
                    hasChanges = true;
                } else {
                    // Brand exists - check if models have changed
                    if (modelsHaveChanged(existingBrand, jsonBrand.models)) {
                        System.out.println("Updating models for brand: " + jsonBrand.name);
                        updateBrandModels(existingBrand, jsonBrand.models);
                        hasChanges = true;
                    }
                }
            }
            
            // Remove brands that are no longer in JSON
            for (SkiBrand dbBrand : dbBrands) {
                if (findBrandDataByName(jsonBrands, dbBrand.getName()) == null) {
                    System.out.println("Removing brand no longer in JSON: " + dbBrand.getName());
                    brandRepository.delete(dbBrand);
                    hasChanges = true;
                }
            }
            
            if (!hasChanges) {
                System.out.println("Database is already in sync with SkiModels.json - no changes needed");
            }
            
        } catch (Exception e) {
            System.err.println("Error syncing database with JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load brands from JSON file into memory for comparison
     */
    private List<BrandData> loadBrandsFromJson() throws Exception {
        List<BrandData> brands = new ArrayList<>();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            resourceLoader.getResource("classpath:SkiModels.json").getInputStream(), 
            StandardCharsets.UTF_8));
        
        StringBuilder jsonContent = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonContent.append(line);
        }
        reader.close();
        
        // Normalize the JSON content to fix Unicode issues
        String normalizedJson = normalizeJsonContent(jsonContent.toString());
        
        // Parse JSON content
        Pattern brandPattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"[^\\[]*\\[([^\\]]+)\\]");
        Matcher brandMatcher = brandPattern.matcher(normalizedJson);
        
        while (brandMatcher.find()) {
            String brandName = brandMatcher.group(1);
            String makesString = brandMatcher.group(2);
            
            List<String> models = new ArrayList<>();
            String[] makesParts = makesString.split(",");
            for (String make : makesParts) {
                String cleanMake = make.replaceAll("\"", "").trim();
                if (!cleanMake.isEmpty()) {
                    models.add(cleanMake);
                }
            }
            
            brands.add(new BrandData(brandName, models));
        }
        
        return brands;
    }
    
    /**
     * Normalize Unicode characters that cause display issues
     */
    private String normalizeJsonContent(String content) {
        return content
            .replace("‑", "-")  // Replace non-breaking hyphen with regular hyphen
            .replace("–", "-")  // Replace en dash with regular hyphen
            .replace("—", "-"); // Replace em dash with regular hyphen
    }
    
    private SkiBrand findBrandByName(List<SkiBrand> brands, String name) {
        return brands.stream().filter(b -> b.getName().equals(name)).findFirst().orElse(null);
    }
    
    private BrandData findBrandDataByName(List<BrandData> brands, String name) {
        return brands.stream().filter(b -> b.name.equals(name)).findFirst().orElse(null);
    }
    
    private boolean modelsHaveChanged(SkiBrand existingBrand, List<String> newModels) {
        List<String> existingModels = existingBrand.getModels().stream()
            .map(SkiModel::getName).sorted().toList();
        List<String> sortedNewModels = newModels.stream().sorted().toList();
        return !existingModels.equals(sortedNewModels);
    }
    
    private void updateBrandModels(SkiBrand brand, List<String> newModels) {
        // Clear existing models
        brand.getModels().clear();
        // Add new models
        for (String modelName : newModels) {
            brand.addModel(new SkiModel(modelName));
        }
        brandRepository.save(brand);
    }
    
    // Helper class to hold JSON data
    private static class BrandData {
        final String name;
        final List<String> models;
        
        BrandData(String name, List<String> models) {
            this.name = name;
            this.models = models;
        }
    }

    /**
     * Load ski brands and models from SkiModels.json file using simple string parsing
     */
    private void loadSkiDataFromJson() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                resourceLoader.getResource("classpath:SkiModels.json").getInputStream()));
            
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            reader.close();
            
            parseJsonContent(jsonContent.toString());
            
        } catch (Exception e) {
            System.err.println("Error loading ski data from JSON: " + e.getMessage());
            e.printStackTrace();
            // Fallback to a minimal dataset if JSON loading fails
            System.out.println("Falling back to minimal dataset...");
            addBrandWithModels("Atomic", Arrays.asList("Bent 90", "Bent 100"));
            addBrandWithModels("Rossignol", Arrays.asList("Experience 88", "Black Ops 98"));
        }
    }
    
    /**
     * Simple JSON parser for the specific structure of SkiModels.json
     */
    private void parseJsonContent(String jsonContent) {
        // Pattern to match brand objects: "name": "BrandName", "makes": [...]
        Pattern brandPattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"[^\\[]*\\[([^\\]]+)\\]");
        Matcher brandMatcher = brandPattern.matcher(jsonContent);
        
        while (brandMatcher.find()) {
            String brandName = brandMatcher.group(1);
            String makesString = brandMatcher.group(2);
            
            // Parse the makes array - split by comma and clean quotes
            List<String> makes = new ArrayList<>();
            String[] makesParts = makesString.split(",");
            for (String make : makesParts) {
                // Remove quotes and trim whitespace
                String cleanMake = make.replaceAll("\"", "").trim();
                if (!cleanMake.isEmpty()) {
                    makes.add(cleanMake);
                }
            }
            
            addBrandWithModels(brandName, makes);
        }
    }

    private void addBrandWithModels(String brandName, List<String> modelNames) {
        System.out.println("Adding brand '" + brandName + "' with " + modelNames.size() + " models");
        SkiBrand brand = new SkiBrand(brandName);
        for (String m : modelNames) {
            brand.addModel(new SkiModel(m));
        }
        brandRepository.save(brand);
    }
}
