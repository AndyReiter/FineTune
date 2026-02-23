package com.finetune.app.config;

import com.finetune.app.model.Staff;
import com.finetune.app.repository.sql.StaffSqlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data seeder to create initial staff user for testing.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private StaffSqlRepository staffRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create default staff user if none exists
        if (staffRepository.count() == 0) {
            Staff staff = new Staff();
            staff.setEmail("andyreiter5@gmail.com");
            staff.setPassword(passwordEncoder.encode("password123"));
            staff.setRole("ROLE_STAFF");
            staff.setCreatedAt(java.time.LocalDateTime.now());
            staff.setUpdatedAt(java.time.LocalDateTime.now());
            staffRepository.save(staff);
            
            System.out.println("=== DATA SEEDER ===");
            System.out.println("Created default staff user:");
            System.out.println("Email: andyreiter5@gmail.com");
            System.out.println("Password: password123");
            System.out.println("===================");
        }
    }
}