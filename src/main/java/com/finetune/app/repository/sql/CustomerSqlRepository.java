    
package com.finetune.app.repository.sql;

import com.finetune.app.model.Customer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class CustomerSqlRepository {
    private final JdbcTemplate jdbcTemplate;

    public CustomerSqlRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Customer> customerRowMapper = (rs, rowNum) -> {
        Customer c = new Customer();
        c.setId(rs.getLong("id"));
        c.setFirstName(rs.getString("firstName"));
        c.setLastName(rs.getString("lastName"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setHeightInches(rs.getInt("heightInches"));
        c.setWeight(rs.getInt("weight"));
        c.setSkiAbilityLevel(rs.getString("skiAbilityLevel"));
        return c;
    };

    public List<Customer> findAll() {
        return jdbcTemplate.query("SELECT * FROM customers", customerRowMapper);
    }

    public Customer save(Customer customer) {
        if (customer.getId() == null) {
            // Insert new customer
            jdbcTemplate.update(
                "INSERT INTO customers (firstName, lastName, email, phone, heightInches, weight, skiAbilityLevel) VALUES (?, ?, ?, ?, ?, ?, ?)",
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getHeightInches(),
                customer.getWeight(),
                (customer.getSkiAbilityLevel() != null ? customer.getSkiAbilityLevel().name() : null)
            );
            // Retrieve inserted customer (assume unique email/phone)
            List<Customer> inserted = findByEmailOrPhone(customer.getEmail(), customer.getPhone());
            return inserted.isEmpty() ? customer : inserted.get(0);
        } else {
            // Update existing customer
            jdbcTemplate.update(
                "UPDATE customers SET firstName = ?, lastName = ?, email = ?, phone = ?, heightInches = ?, weight = ?, skiAbilityLevel = ? WHERE id = ?",
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getHeightInches(),
                customer.getWeight(),
                (customer.getSkiAbilityLevel() != null ? customer.getSkiAbilityLevel().name() : null),
                customer.getId()
            );
            return customer;
        }
    }

    public Optional<Customer> findById(Long id) {
        List<Customer> customers = jdbcTemplate.query("SELECT * FROM customers WHERE id = ?", customerRowMapper, id);
        return customers.stream().findFirst();
    }

    public Optional<Customer> findByEmail(String email) {
        List<Customer> customers = jdbcTemplate.query("SELECT * FROM customers WHERE email = ?", customerRowMapper, email);
        return customers.stream().findFirst();
    }

    public List<Customer> findByEmailOrPhone(String email, String phone) {
        return jdbcTemplate.query("SELECT * FROM customers WHERE email = ? OR phone = ?", customerRowMapper, email, phone);
    }

    public List<Customer> findByEmailContainingIgnoreCase(String email) {
        return jdbcTemplate.query("SELECT * FROM customers WHERE LOWER(email) LIKE LOWER(?)", customerRowMapper, "%" + email + "%");
    }

    public List<Customer> findByPhone(String phone) {
        return jdbcTemplate.query("SELECT * FROM customers WHERE phone = ?", customerRowMapper, phone);
    }

    public List<Customer> findByNameContaining(String name) {
        return jdbcTemplate.query("SELECT * FROM customers WHERE LOWER(firstName) LIKE LOWER(?) OR LOWER(lastName) LIKE LOWER(?)", customerRowMapper, "%" + name + "%", "%" + name + "%");
    }
    public Optional<Customer> findByEmailAndPhone(String email, String phone) {
        List<Customer> customers = jdbcTemplate.query("SELECT * FROM customers WHERE email = ? AND phone = ?", customerRowMapper, email, phone);
        return customers.stream().findFirst();
    }
}
