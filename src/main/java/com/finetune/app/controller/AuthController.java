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
import jakarta.servlet.http.HttpServletRequest;
import com.finetune.app.repository.sql.ShopSqlRepository;
import java.util.ArrayList;
import java.util.List;
import com.finetune.app.config.JwtUtils;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

/**
 * AuthController handles authentication endpoints (simple version without JWT).
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private StaffSqlRepository staffRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ShopSqlRepository shopRepository;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        // Read access_token cookie
        String token = null;
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie c : cookies) {
                if (c != null && "access_token".equals(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }

        if (token == null || !jwtUtils.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        // Extract claims to rebuild token
        io.jsonwebtoken.Claims claims = jwtUtils.getClaims(token);
        if (claims == null) return ResponseEntity.status(401).build();

        String email = claims.getSubject();
        Object staffIdObj = claims.get("staffUserId");
        Long staffUserId = null;
        if (staffIdObj != null) {
            if (staffIdObj instanceof Number) staffUserId = ((Number) staffIdObj).longValue();
            else {
                try { staffUserId = Long.parseLong(staffIdObj.toString()); } catch (Exception ignore) { staffUserId = null; }
            }
        }

        // extract shopIds claim (may be a List<Integer> or List<Long>)
        java.util.List<Long> shopIds = null;
        try {
            Object raw = claims.get("shopIds");
            if (raw instanceof java.util.List) {
                shopIds = new java.util.ArrayList<>();
                for (Object o : (java.util.List<?>) raw) {
                    if (o instanceof Number) shopIds.add(((Number)o).longValue());
                    else {
                        try { shopIds.add(Long.parseLong(o.toString())); } catch (Exception ignore) {}
                    }
                }
            }
        } catch (Exception ignore) { shopIds = null; }

        String newToken;
        if (staffUserId != null) {
            newToken = jwtUtils.generateToken(staffUserId, email, shopIds);
        } else if (shopIds != null) {
            newToken = jwtUtils.generateToken(email, shopIds);
        } else {
            newToken = jwtUtils.generateToken(email);
        }

        // Decide SameSite/Domain based on origin/host (local dev vs production)
        String originHeader = null;
        try { originHeader = request.getHeader("Origin"); } catch (Exception ignore) { originHeader = null; }
        String hostHeader = null;
        if (originHeader != null) {
            try { hostHeader = new java.net.URI(originHeader).getHost(); } catch (Exception ignore) { hostHeader = null; }
        }
        if (hostHeader == null) {
            try { hostHeader = request.getHeader("Host"); } catch (Exception ignore) { hostHeader = null; }
            if (hostHeader != null && hostHeader.contains(":")) hostHeader = hostHeader.split(":")[0];
        }
        boolean isLocal = hostHeader != null && hostHeader.toLowerCase().endsWith("localhost");

        org.springframework.http.ResponseCookie.ResponseCookieBuilder accessBuilder = org.springframework.http.ResponseCookie.from("access_token", newToken)
                .httpOnly(true)
                .path("/")
                .maxAge(2 * 60 * 60);

        if (isLocal) {
            accessBuilder = accessBuilder.sameSite("Lax");
        } else {
            accessBuilder = accessBuilder.sameSite("None").secure(true);
            if (hostHeader != null && hostHeader.toLowerCase().contains("finetune.com")) {
                accessBuilder = accessBuilder.domain(".finetune.com");
            }
        }

        org.springframework.http.ResponseCookie accessCookie = accessBuilder.build();

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, accessCookie.toString())
                .build();
    }

    /**
     * Simple authentication without JWT (for testing).
     * 
     * @param loginRequest the login credentials
     * @return LoginResponse with success/failure
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
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

                    // Create HttpOnly cookie with token (access_token), 2 hours lifetime
                    // Determine host/origin and whether we're running on local dev (localhost or subdomain)
                    String originHeader = null;
                    try { originHeader = request.getHeader("Origin"); } catch (Exception ignore) { originHeader = null; }
                    String hostHeader = null;
                    if (originHeader != null) {
                        try { hostHeader = new java.net.URI(originHeader).getHost(); } catch (Exception ignore) { hostHeader = null; }
                    }
                    if (hostHeader == null) {
                        try { hostHeader = request.getHeader("Host"); } catch (Exception ignore) { hostHeader = null; }
                        if (hostHeader != null && hostHeader.contains(":")) hostHeader = hostHeader.split(":")[0];
                    }
                    boolean isLocal = hostHeader != null && hostHeader.toLowerCase().endsWith("localhost");

                    org.springframework.http.ResponseCookie.ResponseCookieBuilder accessBuilder = org.springframework.http.ResponseCookie.from("access_token", token)
                        .httpOnly(true)
                        .path("/")
                        .maxAge(2 * 60 * 60);

                    // SameSite and Secure: local -> Lax (no Secure), production -> None + Secure
                    if (isLocal) {
                        accessBuilder = accessBuilder.sameSite("Lax");
                    } else {
                        accessBuilder = accessBuilder.sameSite("None").secure(true);
                    }

                    // For production subdomains, set cookie domain to allow cross-subdomain sharing.
                    if (!isLocal && hostHeader != null && hostHeader.toLowerCase().contains("finetune.com")) {
                        accessBuilder = accessBuilder.domain(".finetune.com");
                    }

                    org.springframework.http.ResponseCookie accessCookie = accessBuilder.build();

                    // Load shops for the staff user (reuse earlier lookup)
                    java.util.List<com.finetune.app.model.Shop> shops = staffWithShops.map(Staff::getShops).orElse(java.util.List.of());

                    // If exactly one shop, set active_shop HttpOnly cookie and return 200 OK
                    if (shops.size() == 1) {
                    Long shopId = shops.get(0).getId();
                    org.springframework.http.ResponseCookie.ResponseCookieBuilder shopBuilder = org.springframework.http.ResponseCookie.from("active_shop", String.valueOf(shopId))
                        .httpOnly(true)
                        .path("/")
                        .maxAge(2 * 60 * 60);

                    if (isLocal) {
                        shopBuilder = shopBuilder.sameSite("Lax");
                    } else {
                        shopBuilder = shopBuilder.sameSite("None").secure(true);
                        if (hostHeader != null && hostHeader.toLowerCase().contains("finetune.com")) {
                            shopBuilder = shopBuilder.domain(".finetune.com");
                        }
                    }

                    org.springframework.http.ResponseCookie shopCookie = shopBuilder.build();

                    return org.springframework.http.ResponseEntity.ok()
                        .header(org.springframework.http.HttpHeaders.SET_COOKIE, accessCookie.toString())
                        .header(org.springframework.http.HttpHeaders.SET_COOKIE, shopCookie.toString())
                        .build();
                    }

                    // If multiple shops, return JSON containing shop list (do not include token in JSON)
                    if (shops.size() > 1) {
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("shops", shops);
                    return org.springframework.http.ResponseEntity.ok()
                        .header(org.springframework.http.HttpHeaders.SET_COOKIE, accessCookie.toString())
                        .body(resp);
                    }

                    // No shops found: just set access_token cookie and return 200 OK
                    return org.springframework.http.ResponseEntity.ok()
                        .header(org.springframework.http.HttpHeaders.SET_COOKIE, accessCookie.toString())
                        .build();
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Login failed: " + e.getMessage());
        }
    }

        @PostMapping("/logout")
        public ResponseEntity<?> logout(HttpServletRequest request) {
        // Clear access_token (HttpOnly) and active_shop cookies by setting maxAge=0
        // Respect domain and SameSite/Secure semantics based on host used when setting cookies
        String hostHeader = null;
        try { hostHeader = request.getHeader("Host"); } catch (Exception ignore) { hostHeader = null; }
        boolean isLocal = hostHeader != null && hostHeader.toLowerCase().contains("localhost");

        org.springframework.http.ResponseCookie.ResponseCookieBuilder clearAccessBuilder = org.springframework.http.ResponseCookie.from("access_token", "")
            .httpOnly(true)
            .path("/")
            .maxAge(0);

        org.springframework.http.ResponseCookie.ResponseCookieBuilder clearShopBuilder = org.springframework.http.ResponseCookie.from("active_shop", "")
            .httpOnly(true)
            .path("/")
            .maxAge(0);

        if (isLocal) {
            clearAccessBuilder = clearAccessBuilder.sameSite("Lax");
            clearShopBuilder = clearShopBuilder.sameSite("Lax");
        } else {
            clearAccessBuilder = clearAccessBuilder.sameSite("None").secure(true);
            clearShopBuilder = clearShopBuilder.sameSite("None").secure(true);
        }

        if (hostHeader != null && hostHeader.toLowerCase().contains("finetune.com") && !isLocal) {
            clearAccessBuilder = clearAccessBuilder.domain(".finetune.com");
            clearShopBuilder = clearShopBuilder.domain(".finetune.com");
        }

        org.springframework.http.ResponseCookie clearAccess = clearAccessBuilder.build();
        org.springframework.http.ResponseCookie clearShop = clearShopBuilder.build();

        return org.springframework.http.ResponseEntity.ok()
            .header(org.springframework.http.HttpHeaders.SET_COOKIE, clearAccess.toString())
            .header(org.springframework.http.HttpHeaders.SET_COOKIE, clearShop.toString())
            .build();
        }

    @PostMapping("/select-shop")
    public ResponseEntity<?> selectShop(@RequestBody Map<String, Object> body) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Staff)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Staff staff = (Staff) principal;

        Object shopIdObj = body.get("shopId");
        if (shopIdObj == null) {
            return ResponseEntity.badRequest().body("Missing shopId");
        }

        Long shopId = null;
        if (shopIdObj instanceof Number) {
            shopId = ((Number) shopIdObj).longValue();
        } else {
            try {
                shopId = Long.parseLong(shopIdObj.toString());
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Invalid shopId");
            }
        }

        boolean belongs = false;
        if (staff.getShops() != null) {
            for (com.finetune.app.model.Shop s : staff.getShops()) {
                if (s != null && shopId.equals(s.getId())) {
                    belongs = true;
                    break;
                }
            }
        }

        if (!belongs) {
            return ResponseEntity.status(403).body("Staff does not belong to shop");
        }

        org.springframework.http.ResponseCookie shopCookie = org.springframework.http.ResponseCookie.from("active_shop", String.valueOf(shopId))
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(2 * 60 * 60)
                .build();

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, shopCookie.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("/auth/me called - authentication present={}", authentication != null);
        if (authentication != null) {
            try { logger.debug("/auth/me principal={}", authentication.getPrincipal()); } catch (Exception ignore) {}
        }
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Staff)) {
            return ResponseEntity.status(401).build();
        }

        Staff staff = (Staff) principal;

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", staff.getId());
        resp.put("email", staff.getEmail());
        resp.put("role", staff.getRole());

        // shops list
        List<Map<String, Object>> shopsList = new ArrayList<>();
        if (staff.getShops() != null) {
            for (com.finetune.app.model.Shop s : staff.getShops()) {
                if (s == null) continue;
                Map<String, Object> sm = new HashMap<>();
                sm.put("id", s.getId());
                sm.put("name", s.getName());
                shopsList.add(sm);
            }
        }
        resp.put("shops", shopsList);

        // active shop: prefer request attribute set by filter
        Object activeAttr = request.getAttribute("ACTIVE_SHOP");
        if (activeAttr instanceof com.finetune.app.model.Shop) {
            com.finetune.app.model.Shop as = (com.finetune.app.model.Shop) activeAttr;
            Map<String, Object> am = new HashMap<>();
            am.put("id", as.getId());
            am.put("name", as.getName());
            resp.put("activeShop", am);
        } else {
            // try cookie
            Long cookieShopId = null;
            jakarta.servlet.http.Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (jakarta.servlet.http.Cookie c : cookies) {
                    if (c != null && "active_shop".equals(c.getName())) {
                        try { cookieShopId = Long.parseLong(c.getValue()); } catch (Exception ignore) { cookieShopId = null; }
                        break;
                    }
                }
            }
            if (cookieShopId != null) {
                var shopOpt = shopRepository.findById(cookieShopId);
                if (shopOpt.isPresent()) {
                    var s = shopOpt.get();
                    Map<String, Object> am = new HashMap<>();
                    am.put("id", s.getId());
                    am.put("name", s.getName());
                    resp.put("activeShop", am);
                } else {
                    resp.put("activeShop", null);
                }
            } else {
                resp.put("activeShop", null);
            }
        }

        return ResponseEntity.ok(resp);
    }
}