package com.finetune.app.repository.jdbc;

import com.finetune.app.model.Customer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class CustomerJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public CustomerJdbcRepository(JdbcTemplate jdbcTemplate) {
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

    public Optional<Customer> findByEmailAndPhone(String email, String phone) {
        List<Customer> customers = jdbcTemplate.query("SELECT * FROM customers WHERE email = ? AND phone = ?", customerRowMapper, email, phone);
        return customers.stream().findFirst();
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

    public List<Customer> findAll() {
        return jdbcTemplate.query("SELECT * FROM customers", customerRowMapper);
    }

    public int save(Customer customer) {
        return jdbcTemplate.update(
            "INSERT INTO customers (firstName, lastName, email, phone, heightInches, weight, skiAbilityLevel) VALUES (?, ?, ?, ?, ?, ?, ?)",
            customer.getFirstName(), customer.getLastName(), customer.getEmail(), customer.getPhone(), customer.getHeightInches(), customer.getWeight(), customer.getSkiAbilityLevel()
        );
    }

    public int update(Customer customer) {
        return jdbcTemplate.update(
            "UPDATE customers SET firstName = ?, lastName = ?, email = ?, phone = ?, heightInches = ?, weight = ?, skiAbilityLevel = ? WHERE id = ?",
            customer.getFirstName(), customer.getLastName(), customer.getEmail(), customer.getPhone(), customer.getHeightInches(), customer.getWeight(), customer.getSkiAbilityLevel(), customer.getId()
        );
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM customers WHERE id = ?", id);
    }
}
