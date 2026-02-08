package com.finetune.app.repository;

import com.finetune.app.model.entity.Boot;
import com.finetune.app.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BootRepository extends JpaRepository<Boot, Long> {

    /**
     * Find all boots belonging to a specific customer.
     */
    List<Boot> findByCustomer(Customer customer);

    /**
     * Find a boot by customer and exact match of brand, model, and BSL.
     */
    @Query("SELECT b FROM Boot b WHERE b.customer = :customer AND " +
           "(:brand IS NULL AND b.brand IS NULL OR b.brand = :brand) AND " +
           "(:model IS NULL AND b.model IS NULL OR b.model = :model) AND " +
           "(:bsl IS NULL AND b.bsl IS NULL OR b.bsl = :bsl)")
    Optional<Boot> findByCustomerAndExactMatch(@Param("customer") Customer customer,
                                               @Param("brand") String brand,
                                               @Param("model") String model,
                                               @Param("bsl") Integer bsl);
}