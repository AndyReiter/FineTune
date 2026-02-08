package com.finetune.app.service;

import com.finetune.app.model.entity.Customer;
import com.finetune.app.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer findOrCreateCustomer(
        String firstName,
        String lastName,
        String email,
        String phone
    ) {
        List<Customer> existingCustomers = customerRepository.findByEmailOrPhone(email, phone);
        
        Customer customer;
        if (existingCustomers.isEmpty()) {
            // No existing customer found, create new one
            customer = new Customer();
            customer.setEmail(email);
            customer.setPhone(phone);
        } else {
            // Use the first matching customer (prefer exact email match if available)
            customer = existingCustomers.stream()
                .filter(c -> email.equals(c.getEmail()))
                .findFirst()
                .orElse(existingCustomers.get(0));
        }

        // Always update names to most recent input
        customer.setFirstName(firstName);
        customer.setLastName(lastName);

        return customerRepository.save(customer);
    }
}
