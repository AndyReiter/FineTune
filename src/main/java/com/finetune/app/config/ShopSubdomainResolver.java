package com.finetune.app.config;

import com.finetune.app.model.Shop;
import com.finetune.app.repository.sql.ShopSqlRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@Order(1)
public class ShopSubdomainResolver extends OncePerRequestFilter {

    @Autowired
    private ShopSqlRepository shopRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String hostHeader = request.getHeader("Host");
            String subdomain = null;
            if (hostHeader != null && !hostHeader.isBlank()) {
                String host = hostHeader.split(":")[0].toLowerCase().trim();
                // Support both shop.localhost and shop.localhost.com for local development
                if (host.endsWith(".localhost") || host.endsWith(".localhost.com")) {
                    String suffix = host.endsWith(".localhost.com") ? ".localhost.com" : ".localhost";
                    String prefix = host.substring(0, host.length() - suffix.length());
                    if (!prefix.isBlank()) subdomain = prefix.split("\\.")[0];
                } else if (host.contains(".")) {
                    String[] parts = host.split("\\.");
                    if (parts.length >= 3) subdomain = parts[0];
                }
            }

            if (subdomain != null) {
                try {
                    Optional<Shop> shopOpt = shopRepository.findBySlug(subdomain);
                    if (shopOpt.isPresent()) {
                        Shop shop = shopOpt.get();
                        // Attach shop to request for downstream consumers
                        request.setAttribute("ACTIVE_SHOP", shop);

                        // If user is authenticated, ensure active_shop cookie is set/updated
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        if (auth != null && auth.isAuthenticated()) {
                            // check existing cookie
                            Long cookieShopId = null;
                            Cookie[] cookies = request.getCookies();
                            if (cookies != null) {
                                for (Cookie c : cookies) {
                                    if (c != null && "active_shop".equals(c.getName())) {
                                        try { cookieShopId = Long.parseLong(c.getValue()); } catch (Exception ignore) { cookieShopId = null; }
                                        break;
                                    }
                                }
                            }

                            if (cookieShopId == null || !cookieShopId.equals(shop.getId())) {
                                ResponseCookie updated = ResponseCookie.from("active_shop", String.valueOf(shop.getId()))
                                        .httpOnly(true)
                                        .path("/")
                                        .sameSite("Lax")
                                        .maxAge(2 * 60 * 60)
                                        .build();
                                response.addHeader(HttpHeaders.SET_COOKIE, updated.toString());
                            }
                        }
                    }
                } catch (Exception ignore) {}
            }
        } catch (Exception ignore) {}

        filterChain.doFilter(request, response);
    }
}
