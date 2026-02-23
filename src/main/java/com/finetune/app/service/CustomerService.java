
package com.finetune.app.service;

import com.finetune.app.model.Customer;
import com.finetune.app.model.Equipment;
import com.finetune.app.model.dto.EquipmentRequest;
import com.finetune.app.model.dto.CustomerResponseDTO;
import com.finetune.app.repository.sql.CustomerSqlRepository;
import com.finetune.app.util.PhoneNumberUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerSqlRepository customerRepository;

    public CustomerService(CustomerSqlRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

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
        String normalizedPhone = PhoneNumberUtils.normalize(phone);

        List<Customer> existingCustomers = customerRepository.findByEmailOrPhone(email, normalizedPhone);

        Customer customer;
        if (existingCustomers.isEmpty()) {
            customer = new Customer();
            customer.setEmail(email);
            customer.setPhone(normalizedPhone);
        } else {
            customer = existingCustomers.stream()
                .filter(c -> email.equals(c.getEmail()))
                .findFirst()
                .orElse(existingCustomers.get(0));
        }

        customer.setFirstName(firstName);
        customer.setLastName(lastName);

        return customerRepository.save(customer);
    }

    public Customer findOrCreatePublicCustomer(String name, String email, String phone) {
        String normalizedPhone = PhoneNumberUtils.normalize(phone);

        String firstName = "";
        String lastName = "";
        if (name != null && !name.trim().isEmpty()) {
            String[] nameParts = name.trim().split("\\s+", 2);
            firstName = nameParts[0];
            lastName = nameParts.length > 1 ? nameParts[1] : "";
        }

        List<Customer> candidatesByContact = customerRepository.findByEmailOrPhone(
            email != null ? email.toLowerCase() : "",
            normalizedPhone
        );

        if (!candidatesByContact.isEmpty()) {
            Customer existingCustomer = candidatesByContact.get(0);
            existingCustomer.setFirstName(firstName);
            existingCustomer.setLastName(lastName);
            if (email != null && !email.trim().isEmpty()) {
                existingCustomer.setEmail(email.toLowerCase());
            }
            existingCustomer.setPhone(normalizedPhone);
            return customerRepository.save(existingCustomer);
        }

        if (!firstName.isEmpty()) {
            List<Customer> candidatesByName = customerRepository.findByNameContaining(firstName);

            String fullNameLower = (firstName + " " + lastName).toLowerCase().trim();
            Optional<Customer> nameMatch = candidatesByName.stream()
                .filter(c -> {
                    String customerFullName = (c.getFirstName() + " " + c.getLastName()).toLowerCase().trim();
                    return customerFullName.equals(fullNameLower);
                })
                .findFirst();

            if (nameMatch.isPresent()) {
                Customer existingCustomer = nameMatch.get();
                if (email != null && !email.trim().isEmpty()) {
                    existingCustomer.setEmail(email.toLowerCase());
                }
                existingCustomer.setPhone(normalizedPhone);
                return customerRepository.save(existingCustomer);
            }
        }

        Customer newCustomer = new Customer();
        newCustomer.setFirstName(firstName);
        newCustomer.setLastName(lastName);
        newCustomer.setEmail(email != null ? email.toLowerCase() : null);
        newCustomer.setPhone(normalizedPhone);

        return customerRepository.save(newCustomer);
    }

    public Equipment createEquipment(Long customerId, EquipmentRequest request) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        Equipment equipment = new Equipment();
        equipment.setBrand(request.getBrand());
        equipment.setModel(request.getModel());
        equipment.setLength(request.getLength());
        equipment.setCondition(request.getCondition());
        equipment.setAbilityLevel(request.getAbilityLevel());

        if (request.getType() == null) {
            equipment.setType(Equipment.EquipmentType.SKI);
        } else {
            equipment.setType(request.getType());
        }

        customer.addEquipment(equipment);

        customerRepository.save(customer);

        return equipment;
    }
}
