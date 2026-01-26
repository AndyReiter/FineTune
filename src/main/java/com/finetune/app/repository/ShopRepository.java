package com.finetune.app.repository;

import com.finetune.app.model.Shop;
import java.util.List;
import java.util.Optional;

public interface ShopRepository {
    List<Shop> findAll();
    Optional<Shop> findById(Long id);
    Shop save(Shop shop);
    void deleteById(Long id);
}
