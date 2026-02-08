package com.finetune.app.repository;

import com.finetune.app.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    List<Customer> findByEmailOrPhone(String email, String phone);

    Optional<Customer> findByEmailAndPhone(String email, String phone);
}
