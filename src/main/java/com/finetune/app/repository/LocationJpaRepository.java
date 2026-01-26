package com.finetune.app.repository;

import com.finetune.app.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationJpaRepository extends JpaRepository<Location, Long> {
}
