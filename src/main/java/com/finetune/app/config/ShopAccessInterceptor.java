package com.finetune.app.config;

import com.finetune.app.model.Shop;
import com.finetune.app.model.Staff;
import com.finetune.app.repository.sql.ShopSqlRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;

@Component
public class ShopAccessInterceptor implements HandlerInterceptor {

    private final ShopSqlRepository shopRepository;

    public ShopAccessInterceptor(ShopSqlRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // Only enforce for workorders path (registered accordingly in WebConfig)

        // 1) Try query parameter/shop header
        String shopIdParam = request.getParameter("shopId");
        if (shopIdParam == null || shopIdParam.isBlank()) {
            shopIdParam = request.getHeader("X-Shop-Id");
        }

        Long shopId = null;
        if (shopIdParam != null && !shopIdParam.isBlank()) {
            try { shopId = Long.parseLong(shopIdParam); } catch (Exception ignore) { shopId = null; }
        }

        // 2) Try subdomain
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
                    Optional<Shop> shopOpt = shopRepository.findBySlug(subdomain);
                    if (shopOpt.isPresent()) shopId = shopOpt.get().getId();
                }
            }
        }

        // If we couldn't determine shopId, allow request through (can't verify)
        if (shopId == null) {
            // Check if a shopId was embedded in the token and set as request attribute by JwtAuthFilter
            Object tokenShop = request.getAttribute("tokenShopId");
            if (tokenShop instanceof Number) {
                shopId = ((Number) tokenShop).longValue();
            } else if (tokenShop != null) {
                try { shopId = Long.parseLong(tokenShop.toString()); } catch (Exception ignore) { shopId = null; }
            }
        }

        // If still couldn't determine shopId, allow request through (can't verify)
        if (shopId == null) return true;

        // Ensure user is authenticated
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;

        if (!(principal instanceof Staff)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        Staff staff = (Staff) principal;
        List<Long> staffShopIds = staff.getShopIds();
        if (staffShopIds == null || !staffShopIds.contains(shopId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        // Authorized for the shop
        return true;
    }
}
