package com.finetune.app.service;

import com.finetune.app.model.entity.Customer;
import com.finetune.app.repository.CustomerRepository;
import org.springframework.stereotype.Service;

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
        Customer customer = customerRepository
            .findByEmailOrPhone(email, phone)
            .orElseGet(() -> {
                Customer c = new Customer();
                c.setEmail(email);
                c.setPhone(phone);
                return c;
            });

        // Always update names to most recent input
        customer.setFirstName(firstName);
        customer.setLastName(lastName);

        return customerRepository.save(customer);
    }
}
