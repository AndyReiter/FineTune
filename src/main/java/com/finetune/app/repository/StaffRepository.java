package com.finetune.app.repository;

import com.finetune.app.model.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Staff entity.
 */
@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    /**
     * Find staff by email address.
     * Used for authentication.
     * 
     * @param email the email to search for
     * @return Optional containing staff if found
     */
    Optional<Staff> findByEmail(String email);

    /**
     * Check if staff exists by email.
     * 
     * @param email the email to check
     * @return true if staff exists with this email
     */
    boolean existsByEmail(String email);
}