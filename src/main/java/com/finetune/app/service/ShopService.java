package com.finetune.app.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.finetune.app.model.Shop;
import com.finetune.app.repository.ShopJpaRepository;

@Service
public class ShopService {
    private final ShopJpaRepository shopRepository;

    public ShopService(ShopJpaRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public List<Shop> getAllShops() {
        return shopRepository.findAll();
    }

    public Shop getShop(Long id) {
        return shopRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found with id: " + id));
    }

    private void validateUniqueLocation(Shop shop) {
        if (shop.getLocation() != null) {
            String address = shop.getLocation().getAddress();
            String city = shop.getLocation().getCity();
            String state = shop.getLocation().getState();
            String zipCode = shop.getLocation().getZipCode();
            
            if (shopRepository.findByLocationAddress(address, city, state, zipCode).isPresent()) {
                throw new IllegalArgumentException("A shop already exists at location: " + address + ", " + city + ", " + state + " " + zipCode);
            }
        }
    }

    public Shop createShop(Shop shop) {
        validateUniqueLocation(shop);
        return shopRepository.save(shop);
    }

    public Shop updateShop(Long id, Shop shop) {
        Shop existingShop = shopRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found with id: " + id));
        
        // Only check for duplicates if the location changed
        if (shop.getLocation() != null && 
            (existingShop.getLocation() == null || 
             !isSameLocation(existingShop.getLocation(), shop.getLocation()))) {
            validateUniqueLocation(shop);
        }
        
        shop.setId(id);
        return shopRepository.save(shop);
    }

    private boolean isSameLocation(com.finetune.app.model.Location loc1, com.finetune.app.model.Location loc2) {
        return loc1.getId() != null && loc1.getId().equals(loc2.getId());
    }

    public void deleteShop(Long id) {
        if (!shopRepository.existsById(id)) {
            throw new IllegalArgumentException("Shop not found with id: " + id);
        }
        shopRepository.deleteById(id);
    }

    public String checkIn() {
        return "Item successfully checked in";
    }

    public String getStatus() {
        return "Item is currently in progress";
    }
}