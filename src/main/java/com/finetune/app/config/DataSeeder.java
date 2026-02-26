package com.finetune.app.config;

import com.finetune.app.model.Staff;
import com.finetune.app.model.Shop;
import com.finetune.app.repository.sql.StaffSqlRepository;
import com.finetune.app.repository.sql.ShopSqlRepository;
import java.util.List;
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

    @Autowired
    private ShopSqlRepository shopRepository;

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

        // Create default shops if they don't exist
        try {
            List<Shop> shops = shopRepository.findAll();

            boolean hasAlta = shops.stream().anyMatch(s -> "alta".equals(s.getSlug()));
            if (!hasAlta) {
                Shop alta = new Shop();
                alta.setName("Alta");
                alta.setSlug("alta");
                alta.setLogoUrl(null);
                shopRepository.insert(alta);
                System.out.println("Created shop: Alta (slug: alta)");
            }

            boolean has7evens = shops.stream().anyMatch(s -> "7evenskis".equals(s.getSlug()));
            if (!has7evens) {
                Shop sev = new Shop();
                sev.setName("7evenskis");
                sev.setSlug("7evenskis");
                sev.setLogoUrl(null);
                shopRepository.insert(sev);
                System.out.println("Created shop: 7evenskis (slug: 7evenskis)");
            }
        } catch (Exception e) {
            System.err.println("Error seeding shops: " + e.getMessage());
        }

        // Ensure seeded staff user (id=1) belongs to at least one shop
        try {
            var staffOpt = staffRepository.findById(1L);
            if (staffOpt.isPresent()) {
                Integer count = shopRepository.getJdbcTemplate().queryForObject(
                        "SELECT COUNT(*) FROM staff_shops WHERE staff_id = ?", Integer.class, 1L);
                if (count == null || count == 0) {
                    // Determine a shop id to assign: prefer shop id=1 if it exists, otherwise first shop
                    Long assignShopId = null;
                    var shopOneOpt = shopRepository.findById(1L);
                    if (shopOneOpt.isPresent()) {
                        assignShopId = shopOneOpt.get().getId();
                    } else {
                        var shopsAll = shopRepository.findAll();
                        if (!shopsAll.isEmpty()) assignShopId = shopsAll.get(0).getId();
                    }

                    if (assignShopId != null) {
                        try {
                            shopRepository.getJdbcTemplate().update(
                                    "INSERT INTO staff_shops (staff_id, shop_id) VALUES (?, ?)",
                                    1L, assignShopId);
                            System.out.println("Assigned staff id=1 to shop id=" + assignShopId);
                        } catch (Exception ex) {
                            System.err.println("Failed to assign staff to shop: " + ex.getMessage());
                        }
                    } else {
                        System.err.println("No shop available to assign to staff id=1");
                    }
                }
            }
        } catch (Exception ignore) {
            // ignore errors (table may not exist in some environments)
        }
    }
}