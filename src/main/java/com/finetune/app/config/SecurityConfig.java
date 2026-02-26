package com.finetune.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Permit authentication endpoint and static assets
                .requestMatchers("/login.html").permitAll()
                .requestMatchers("/auth/**").permitAll()
                // Allow UI pages to be served so client-side JS can attach JWT and call APIs
                .requestMatchers("/dashboard.html").permitAll()
                .requestMatchers("/customers.html").permitAll()
                .requestMatchers("/settings.html").permitAll()
                .requestMatchers("/analytics.html").permitAll()
                .requestMatchers("/shops.html").permitAll()
                .requestMatchers("/api/public/workorders/**").permitAll()
                .requestMatchers("/shared.js").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                .requestMatchers("/css/**").permitAll()
                .requestMatchers("/js/**").permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(loginRedirectEntryPoint()))
            .httpBasic(httpBasic -> httpBasic.disable())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
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

    @Bean
    public AuthenticationEntryPoint loginRedirectEntryPoint() {
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response,
                                 org.springframework.security.core.AuthenticationException authException) throws IOException {
                String accept = request.getHeader("Accept");
                String uri = request.getRequestURI();
                // For API calls return 401, for page requests redirect to login page
                if (uri.startsWith("/api/") || (accept != null && accept.contains("application/json"))) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                } else {
                    response.sendRedirect("/login.html");
                }
            }
        };
    }
}