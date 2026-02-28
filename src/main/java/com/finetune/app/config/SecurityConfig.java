package com.finetune.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import com.finetune.app.repository.sql.StaffSqlRepository;
import org.springframework.security.web.AuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Security configuration: registers JWT filter, disables CSRF, and configures route authorization.
 */
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private ShopSubdomainResolver shopSubdomainResolver;

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public: static assets, public APIs, and public UI pages - must be checked first
                .requestMatchers(
                    "/**/*.js",
                    "/**/*.css",
                    "/**/*.png",
                    "/**/*.jpg",
                    "/**/*.jpeg",
                    "/**/*.gif",
                    "/**/*.svg",
                    "/**/*.ico",
                    "/favicon.ico",
                    "/images/**",
                    "/webjars/**",
                    "/static/**",
                    "/public/**",
                    "/resources/**",
                    "/login.html",
                    "/index.html",
                    "/create-workorder.html",
                    "/customer-workorder.html",
                    "/customer-workorder.js",
                    "/shared.css",
                    "/assets/**",
                    "/auth/login",
                    "/auth/logout",
                    "/auth/refresh",
                    "/api/public/**"
                ).permitAll()

                // All other API endpoints require authentication
                .requestMatchers("/api/**").authenticated()

                // Protected UI pages (frontend will handle redirects)
                .requestMatchers(
                    "/dashboard.html",
                    "/customers.html",
                    "/workorders.html",
                    "/settings.html",
                    "/analytics.html"
                ).authenticated()

                // Default: authenticated
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint))
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable())
            .addFilterBefore(shopSubdomainResolver, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        // Allow credentials and explicit local dev subdomains (host-only / port-aware)
        // Keep production patterns for finetune.com but restrict to those domains.
        // Allow local dev origins (explicit port-aware patterns) and production patterns
        configuration.setAllowedOriginPatterns(java.util.List.of(
            "http://localhost:8080",
            "http://*.localhost:8080",
            "http://*.finetune.com",
            "https://*.finetune.com"
        ));
        configuration.addAllowedHeader("*");
        // Restrict to the methods required for the API surface in local dev
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(StaffSqlRepository staffRepo) {
        return username -> {
            var staffOpt = staffRepo.findByEmail(username);
            var staff = staffOpt.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(staff.getRole() == null ? "ROLE_STAFF" : staff.getRole()));
            return User.withUsername(staff.getEmail()).password(staff.getPassword()).authorities(authorities).build();
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    
}