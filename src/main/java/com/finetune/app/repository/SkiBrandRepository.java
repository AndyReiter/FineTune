package com.finetune.app.repository;

import com.finetune.app.model.entity.SkiBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkiBrandRepository extends JpaRepository<SkiBrand, Long> {
    boolean existsByName(String name);
    
    @Query("SELECT b FROM SkiBrand b LEFT JOIN FETCH b.models")
    List<SkiBrand> findAllWithModels();
}
