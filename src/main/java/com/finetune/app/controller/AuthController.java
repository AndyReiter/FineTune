package com.finetune.app.controller;

import com.finetune.app.model.dto.LoginRequest;
import com.finetune.app.model.dto.LoginResponse;
import com.finetune.app.model.entity.Staff;
import com.finetune.app.repository.StaffRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * AuthController handles authentication endpoints (simple version without JWT).
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Simple authentication without JWT (for testing).
     * 
     * @param loginRequest the login credentials
     * @return LoginResponse with success/failure
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Find staff by email
            Optional<Staff> staffOpt = staffRepository.findByEmail(loginRequest.getEmail());
            
            if (staffOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }
            
            Staff staff = staffOpt.get();
            
            // Check password
            if (!passwordEncoder.matches(loginRequest.getPassword(), staff.getPassword())) {
                return ResponseEntity.badRequest().body("Invalid password");
            }

            // Create simple response (no JWT yet)
            LoginResponse response = new LoginResponse("temporary-token", staff.getEmail(), staff.getRole());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }
}