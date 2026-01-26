package com.finetune.app.repository;

import com.finetune.app.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShopJpaRepository extends JpaRepository<Shop, Long> {
    @Query("SELECT s FROM Shop s JOIN s.location l WHERE LOWER(TRIM(l.address)) = LOWER(TRIM(:address)) AND LOWER(TRIM(l.city)) = LOWER(TRIM(:city)) AND LOWER(TRIM(l.state)) = LOWER(TRIM(:state)) AND TRIM(l.zipCode) = TRIM(:zipCode)")
    Optional<Shop> findByLocationAddress(@Param("address") String address, @Param("city") String city, @Param("state") String state, @Param("zipCode") String zipCode);
}
