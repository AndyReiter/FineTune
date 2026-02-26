package com.finetune.app.controller;

import com.finetune.app.model.dto.LoginRequest;
import com.finetune.app.model.dto.LoginResponse;
import com.finetune.app.model.Staff;
import com.finetune.app.repository.sql.StaffSqlRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.finetune.app.config.JwtUtils;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

/**
 * AuthController handles authentication endpoints (simple version without JWT).
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private StaffSqlRepository staffRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Simple authentication without JWT (for testing).
     * 
     * @param loginRequest the login credentials
     * @return LoginResponse with success/failure
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate using AuthenticationManager
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Load staff with shops for token claims
            Optional<Staff> staffWithShops = staffRepository.findByEmailWithShops(loginRequest.getEmail());
            Staff sForToken = staffWithShops.orElseGet(() -> staffRepository.findByEmail(loginRequest.getEmail()).orElse(null));

            if (sForToken == null) {
                return ResponseEntity.badRequest().body("User not found after authentication");
            }

            String token = jwtUtils.generateToken(sForToken.getId(), sForToken.getEmail(), sForToken.getShopIds());

            Map<String, Object> resp = new HashMap<>();
            resp.put("token", token);
            resp.put("shopIds", sForToken.getShopIds() != null ? sForToken.getShopIds() : java.util.List.of());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Login failed: " + e.getMessage());
        }
    }
}