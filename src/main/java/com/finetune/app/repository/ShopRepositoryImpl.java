package com.finetune.app.repository;

import com.finetune.app.model.Shop;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.ArrayList;

@Repository
public class ShopRepositoryImpl implements ShopRepository {
    private final Map<Long, Shop> shopStore = new HashMap<>();
    private long nextId = 1;

    @Override
    public List<Shop> findAll() {
        return new ArrayList<>(shopStore.values());
    }

    @Override
    public Optional<Shop> findById(Long id) {
        return Optional.ofNullable(shopStore.get(id));
    }

    @Override
    public Shop save(Shop shop) {
        if (shop.getId() == null || shop.getId() == 0) {
            shop.setId(nextId++);
        }
        shopStore.put(shop.getId(), shop);
        return shop;
    }

    @Override
    public void deleteById(Long id) {
        if (!shopStore.containsKey(id)) {
            throw new IllegalArgumentException("Shop not found with id: " + id);
        }
        shopStore.remove(id);
    }
}
