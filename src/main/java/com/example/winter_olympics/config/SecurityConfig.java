package com.example.winter_olympics.config;

import com.example.winter_olympics.user.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/auth/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/statistics/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/athletes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/competitions/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/athletes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/athletes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/athletes/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/competitions").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/competitions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/competitions/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/competitions/*/slalom/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/competitions/*/biathlon/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/competitions/*/registrations/**")
                        .hasAnyRole("ADMIN", "ATHLETE")
                        .requestMatchers(HttpMethod.DELETE, "/api/competitions/*/registrations/**")
                        .hasAnyRole("ADMIN", "ATHLETE")

                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}