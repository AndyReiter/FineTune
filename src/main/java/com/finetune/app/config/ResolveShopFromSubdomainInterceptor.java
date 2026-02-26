package com.finetune.app.config;

import com.finetune.app.model.Shop;
import com.finetune.app.repository.sql.ShopSqlRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

@Component
public class ResolveShopFromSubdomainInterceptor implements HandlerInterceptor {

    private final ShopSqlRepository shopRepository;

    public ResolveShopFromSubdomainInterceptor(ShopSqlRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String hostHeader = request.getHeader("Host");
        if (hostHeader == null || hostHeader.isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }

        // Remove port if present (ignore localhost ports)
        String host = hostHeader.split(":" )[0].toLowerCase().trim();

        // Extract subdomain:
        // - For hosts like "7evenskis.finetune.app" -> subdomain is first label
        // - For localhost development like "alta.localhost" or "a.b.localhost" -> take first label before ".localhost"
        String subdomain = null;
        if (host.endsWith(".localhost")) {
            String prefix = host.substring(0, host.length() - ".localhost".length());
            if (!prefix.isBlank()) {
                String[] prefixParts = prefix.split("\\.");
                subdomain = prefixParts.length > 0 ? prefixParts[0] : null;
            }
        } else if (host.contains(".")) {
            String[] parts = host.split("\\.");
            if (parts.length >= 3) {
                subdomain = parts[0];
            }
        }

        if (subdomain == null || subdomain.isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }

        Optional<Shop> shopOpt = shopRepository.findBySlug(subdomain);
        if (shopOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }

        // Attach shop to request context as "shop"
        request.setAttribute("shop", shopOpt.get());
        return true;
    }
}
