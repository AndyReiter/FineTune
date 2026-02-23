
package com.finetune.app.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.finetune.app.model.Shop;
import com.finetune.app.model.Location;
import com.finetune.app.repository.sql.ShopSqlRepository;

@Service
public class ShopService {
    private final ShopSqlRepository shopRepository;

    public ShopService(ShopSqlRepository shopRepository) {
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
            List<Shop> shopsAtLocation = shopRepository.findByLocation(shop.getLocation());
            if (!shopsAtLocation.isEmpty()) {
                throw new IllegalArgumentException("A shop already exists at location: " +
                    shop.getLocation().getAddress() + ", " +
                    shop.getLocation().getCity() + ", " +
                    shop.getLocation().getState() + " " +
                    shop.getLocation().getZipCode());
            }
        }
    }

    public Shop createShop(Shop shop) {
        validateUniqueLocation(shop);
        shopRepository.save(shop);
        // Return the saved shop (with generated ID)
        return shopRepository.findLastInserted().orElse(shop);
    }

    public Shop getOrCreateMainShop() {
        List<Shop> shops = getAllShops();
        if (!shops.isEmpty()) {
            return shops.get(0);
        } else {
            Shop newShop = new Shop();
            shopRepository.save(newShop);
            return shopRepository.findLastInserted().orElse(newShop);
        }
    }

    public Shop saveShop(Shop shop) {
        shopRepository.save(shop);
        return shopRepository.findLastInserted().orElse(shop);
    }

    public Shop updateShop(Long id, Shop shop) {
        Shop existingShop = getShop(id);
        shop.setId(id);
        shopRepository.update(shop);
        return getShop(id);
    }

    public void deleteShop(Long id) {
        if (!shopRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("Shop not found with id: " + id);
        }
        shopRepository.delete(id);
    }

    public String checkIn() {
        return "Item successfully checked in";
    }

    public String getStatus() {
        return "Item is currently in progress";
    }
}