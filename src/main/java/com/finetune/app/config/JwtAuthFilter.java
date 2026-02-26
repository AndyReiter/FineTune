package com.finetune.app.config;

import com.finetune.app.model.Staff;
import com.finetune.app.repository.sql.StaffSqlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.finetune.app.repository.sql.ShopSqlRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private StaffSqlRepository staffRepository;

    @Autowired
    private ShopSqlRepository shopRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        // Skip JWT validation for public workorder endpoints
        if (path != null && path.startsWith("/api/public/workorders")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7).trim();

                if (jwtUtils.validateToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String email = jwtUtils.getEmailFromToken(token);
                    if (email != null) {
                        // Try to avoid loading shops on every request: prefer shop IDs from token
                        java.util.List<Long> tokenShopIds = jwtUtils.getShopIdsFromToken(token);

                        Optional<Staff> staffOpt;
                        Staff staff;

                        if (tokenShopIds != null && !tokenShopIds.isEmpty()) {
                            // Load staff WITHOUT shops (lightweight) and attach shops from token
                            staffOpt = staffRepository.findByEmail(email);
                            if (staffOpt.isPresent()) {
                                staff = staffOpt.get();
                                java.util.List<com.finetune.app.model.Shop> shops = new java.util.ArrayList<>();
                                for (Long id : tokenShopIds) {
                                    com.finetune.app.model.Shop sh = new com.finetune.app.model.Shop();
                                    sh.setId(id);
                                    shops.add(sh);
                                }
                                staff.setShops(shops);
                            } else {
                                staff = null;
                            }
                        } else {
                            // No shop IDs in token: load staff with shops (may hit DB)
                            staffOpt = staffRepository.findByEmailWithShops(email);
                            staff = staffOpt.orElse(null);
                        }

                        if (staff != null) {
                            // If token included staffUserId, set it on staff
                            try {
                                io.jsonwebtoken.Claims claims = jwtUtils.getClaims(token);
                                Object staffIdObj = claims == null ? null : claims.get("staffUserId");
                                if (staffIdObj != null) {
                                    if (staffIdObj instanceof Number) staff.setId(((Number)staffIdObj).longValue());
                                    else {
                                        try { staff.setId(Long.parseLong(staffIdObj.toString())); } catch (Exception ignore) {}
                                    }
                                }
                                Object singleShop = claims == null ? null : claims.get("shopId");
                                if (singleShop != null) {
                                    Long sid = null;
                                    if (singleShop instanceof Number) sid = ((Number)singleShop).longValue();
                                    else {
                                        try { sid = Long.parseLong(singleShop.toString()); } catch (Exception ignore) { sid = null; }
                                    }
                                    if (sid != null) {
                                        // expose as request attribute for downstream checks
                                        request.setAttribute("tokenShopId", sid);
                                    }
                                }
                            } catch (Exception ignore) {}
                            GrantedAuthority authority = new SimpleGrantedAuthority(staff.getRole() == null ? "ROLE_STAFF" : staff.getRole());
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    staff, null, Collections.singletonList(authority));
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // On any exception, clear context and continue filter chain
            SecurityContextHolder.clearContext();
        }

        // UI page protection is handled by Spring Security configuration and client-side logic.
        filterChain.doFilter(request, response);
    }

    private Long extractShopIdFromRequest(HttpServletRequest request) {
        // 1) query param or header
        String shopIdParam = request.getParameter("shopId");
        if (shopIdParam == null || shopIdParam.isBlank()) {
            shopIdParam = request.getHeader("X-Shop-Id");
        }
        Long shopId = null;
        if (shopIdParam != null && !shopIdParam.isBlank()) {
            try { shopId = Long.parseLong(shopIdParam); } catch (Exception ignore) { shopId = null; }
        }

        // 2) subdomain
        if (shopId == null) {
            String hostHeader = request.getHeader("Host");
            if (hostHeader != null && !hostHeader.isBlank()) {
                String host = hostHeader.split(":")[0].toLowerCase().trim();
                String subdomain = null;
                if (host.endsWith(".localhost")) {
                    String prefix = host.substring(0, host.length() - ".localhost".length());
                    if (!prefix.isBlank()) subdomain = prefix.split("\\.")[0];
                } else if (host.contains(".")) {
                    String[] parts = host.split("\\.");
                    if (parts.length >= 3) subdomain = parts[0];
                }
                if (subdomain != null) {
                    try {
                        Optional<com.finetune.app.model.Shop> shopOpt = shopRepository.findBySlug(subdomain);
                        if (shopOpt.isPresent()) shopId = shopOpt.get().getId();
                    } catch (Exception ignore) {}
                }
            }
        }

        return shopId;
    }
}
