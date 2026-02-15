package com.finetune.app.service;

import com.finetune.app.model.entity.Customer;
import com.finetune.app.model.entity.Equipment;
import com.finetune.app.model.dto.EquipmentRequest;
import com.finetune.app.model.dto.CustomerResponseDTO;
import com.finetune.app.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Get all customers with aggregate counts.
     * 
     * @return List of CustomerResponseDTO objects
     */
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
            .map(CustomerResponseDTO::fromEntity)
            .collect(Collectors.toList());
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

    /**
     * Creates equipment and attaches it to a customer profile.
     * Equipment is persisted via Customer cascade.
     * 
     * @param customerId ID of the customer to attach equipment to
     * @param request Equipment details
     * @return The created Equipment entity
     * @throws RuntimeException if customer not found
     */
    public Equipment createEquipment(Long customerId, EquipmentRequest request) {
        // 1. Fetch customer
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
        
        // 2. Create equipment entity
        Equipment equipment = new Equipment();
        
        // 3. Populate fields from request
        equipment.setBrand(request.getBrand());
        equipment.setModel(request.getModel());
        equipment.setLength(request.getLength());
        equipment.setCondition(request.getCondition());
        equipment.setAbilityLevel(request.getAbilityLevel());
        
        // 4. Default type to SKI if null
        if (request.getType() == null) {
            equipment.setType(Equipment.EquipmentType.SKI);
        } else {
            equipment.setType(request.getType());
        }
        
        // 5. Attach to customer (sets bidirectional relationship)
        customer.addEquipment(equipment);
        
        // 6. Save customer (cascades to equipment)
        customerRepository.save(customer);
        
        // 7. Return saved equipment
        return equipment;
    }
}
