package com.example.winter_olympics.config;

import com.example.winter_olympics.user.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
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

                        .requestMatchers(HttpMethod.POST,   "/api/athletes/**").hasAnyRole("ADMIN", "ATHLETE")
                        .requestMatchers(HttpMethod.PUT,    "/api/athletes/**").hasAnyRole("ADMIN", "ATHLETE")
                        .requestMatchers(HttpMethod.DELETE, "/api/athletes/**").hasAnyRole("ADMIN", "ATHLETE")

                                .requestMatchers(HttpMethod.POST, "/api/competitions").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/competitions/**").hasRole("ADMIN")

                                .requestMatchers(HttpMethod.POST, "/api/competitions/*/registrations/**")
                                .hasAnyRole("ADMIN", "ATHLETE")
                                .requestMatchers(HttpMethod.DELETE, "/api/competitions/*/registrations/**")
                                .hasAnyRole("ADMIN", "ATHLETE")

                                .requestMatchers(HttpMethod.DELETE, "/api/competitions/**").hasRole("ADMIN")

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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}