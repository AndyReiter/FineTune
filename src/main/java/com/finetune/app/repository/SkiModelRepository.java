package com.finetune.app.repository;

import com.finetune.app.model.entity.SkiModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkiModelRepository extends JpaRepository<SkiModel, Long> {
    List<SkiModel> findByBrand_Id(Long brandId);
}
