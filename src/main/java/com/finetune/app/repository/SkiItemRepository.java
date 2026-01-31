package com.finetune.app.repository;

import com.finetune.app.model.entity.SkiItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * SkiItemRepository provides data access for SkiItem entities.
 */
@Repository
public interface SkiItemRepository extends JpaRepository<SkiItem, Long> {
}
