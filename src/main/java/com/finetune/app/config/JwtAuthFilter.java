package com.finetune.app.config;

import com.finetune.app.model.Staff;
import com.finetune.app.repository.sql.StaffSqlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import com.finetune.app.repository.sql.ShopSqlRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;

@Component
@Order(2)
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

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

        logger.debug("JwtAuthFilter: incoming request {}", path);

        // Early exits: skip filter for static resources and public pages/APIs BEFORE reading cookies
        if (path == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String lower = path.toLowerCase();

        // Static assets - allow through without authentication
        if (lower.endsWith(".js") || lower.endsWith(".css") || lower.endsWith(".png") || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg") || lower.endsWith(".gif") || lower.endsWith(".svg") || lower.endsWith(".ico")
                || "/favicon.ico".equals(lower) || lower.startsWith("/images/") || lower.startsWith("/webjars/")
                || lower.startsWith("/static/") || lower.startsWith("/public/") || lower.startsWith("/resources/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Public UI pages
        if ("/login.html".equals(lower) || "/index.html".equals(lower) || "/create-workorder.html".equals(lower)
                || "/customer-workorder.html".equals(lower) || lower.startsWith("/api/public/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Log cookie names for troubleshooting (do not log token values)
        try {
            Cookie[] debugCookies = request.getCookies();
            if (debugCookies != null) {
                StringBuilder names = new StringBuilder();
                for (Cookie dc : debugCookies) {
                    if (dc == null) continue;
                    names.append(dc.getName()).append(',');
                }
                logger.debug("JwtAuthFilter: cookies present=[{}]", names.toString());
            } else {
                logger.debug("JwtAuthFilter: no cookies present on request");
            }
        } catch (Exception ignore) {}

        try {
            // Read token from cookie named 'access_token' only
            String token = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if (c != null && "access_token".equals(c.getName())) {
                        token = c.getValue();
                        break;
                    }
                }
            }

            if (token != null && jwtUtils.validateToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.debug("JwtAuthFilter: token present and validated");
                String email = jwtUtils.getEmailFromToken(token);
                logger.debug("JwtAuthFilter: token subject/email={}", email);
                if (email != null) {
                    // Load full staff record (with shops) from DB
                    Optional<Staff> staffOpt = staffRepository.findByEmailWithShops(email);
                    Staff staff = staffOpt.orElse(null);

                    if (staff != null) {
                        logger.debug("JwtAuthFilter: loaded staff id={} email={}", staff.getId(), staff.getEmail());
                        try {
                            io.jsonwebtoken.Claims claims = jwtUtils.getClaims(token);
                            Object staffIdObj = claims == null ? null : claims.get("staffUserId");
                            if (staffIdObj != null) {
                                if (staffIdObj instanceof Number) staff.setId(((Number) staffIdObj).longValue());
                                else {
                                    try { staff.setId(Long.parseLong(staffIdObj.toString())); } catch (Exception ignore) {}
                                }
                            }
                            Object singleShop = claims == null ? null : claims.get("shopId");
                            if (singleShop != null) {
                                Long sid = null;
                                if (singleShop instanceof Number) sid = ((Number) singleShop).longValue();
                                else {
                                    try { sid = Long.parseLong(singleShop.toString()); } catch (Exception ignore) { sid = null; }
                                }
                                if (sid != null) request.setAttribute("tokenShopId", sid);
                            }
                        } catch (Exception ignore) {}

                        GrantedAuthority authority = new SimpleGrantedAuthority(staff.getRole() == null ? "ROLE_STAFF" : staff.getRole());
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                staff, null, Collections.singletonList(authority));
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.debug("JwtAuthFilter: authentication set for staff id={}", staff.getId());

                        // Enforce active_shop cookie presence and membership
                        try {
                            // Read active_shop cookie
                            Long cookieShopId = null;
                            Cookie[] allCookies = request.getCookies();
                            if (allCookies != null) {
                                for (Cookie ck : allCookies) {
                                    if (ck != null && "active_shop".equals(ck.getName())) {
                                        try { cookieShopId = Long.parseLong(ck.getValue()); } catch (Exception ignore) { cookieShopId = null; }
                                        break;
                                    }
                                }
                            }

                            if (cookieShopId == null) {
                                // No active shop selected
                                logger.debug("JwtAuthFilter: no active_shop cookie; returning 409");
                                response.setStatus(409);
                                response.getWriter().write("SHOP_NOT_SELECTED");
                                return;
                            }

                            // Resolve shop from subdomain
                            Long subdomainShopId = extractShopIdFromRequest(request);
                            if (subdomainShopId != null && !subdomainShopId.equals(cookieShopId)) {
                                // Update active_shop cookie to subdomain shop
                                cookieShopId = subdomainShopId;
                                ResponseCookie updated = ResponseCookie.from("active_shop", String.valueOf(cookieShopId))
                                        .httpOnly(true)
                                        .path("/")
                                        .sameSite("Lax")
                                        .maxAge(2 * 60 * 60)
                                        .build();
                                response.addHeader(HttpHeaders.SET_COOKIE, updated.toString());
                            }

                            // Verify staff belongs to active_shop
                            boolean belongs = false;
                            if (staff.getShops() != null) {
                                for (com.finetune.app.model.Shop s : staff.getShops()) {
                                    if (s != null && Long.valueOf(s.getId()).equals(cookieShopId)) { belongs = true; break; }
                                }
                            }

                            if (!belongs) {
                                logger.debug("JwtAuthFilter: staff id={} does not belong to active shop id={}", staff.getId(), cookieShopId);
                                response.setStatus(403);
                                response.getWriter().write("Forbidden: staff does not belong to active shop");
                                return;
                            }

                            // Attach shop object to request
                            Optional<com.finetune.app.model.Shop> shopOpt = shopRepository.findById(cookieShopId);
                            if (shopOpt.isPresent()) {
                                request.setAttribute("ACTIVE_SHOP", shopOpt.get());
                                logger.debug("JwtAuthFilter: attached ACTIVE_SHOP id={}", shopOpt.get().getId());
                            } else {
                                logger.debug("JwtAuthFilter: active shop id={} not found; returning 403", cookieShopId);
                                response.setStatus(403);
                                response.getWriter().write("Forbidden: active shop not found");
                                return;
                            }
                        } catch (IOException ioe) {
                            logger.warn("Error enforcing active_shop: {}", ioe.getMessage());
                            // On IO errors, continue without blocking
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // Token invalid or other error: do NOT block the request. Continue without authentication.
            logger.warn("JwtAuthFilter error while processing token: {}", ex.getMessage());
        }

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
                // Support both shop.localhost and shop.localhost.com for local development
                if (host.endsWith(".localhost") || host.endsWith(".localhost.com")) {
                    String suffix = host.endsWith(".localhost.com") ? ".localhost.com" : ".localhost";
                    String prefix = host.substring(0, host.length() - suffix.length());
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
