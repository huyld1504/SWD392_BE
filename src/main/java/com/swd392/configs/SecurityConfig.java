package com.swd392.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain (HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        sm ->
                                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(
                        request ->
                                request.requestMatchers(
                                        "/hello/",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**"
                                )
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated()
                )
                .exceptionHandling(ex ->
                        ex
                                .accessDeniedHandler(((request, response, accessDeniedException) -> {
                                    //Write the exception here - 403 (Access Denied)
                                }))
                                .authenticationEntryPoint(((request, response, authException) -> {
                                    //Write the exception here - (401 - Unauthorized)
                                }))
                );
        return http.build();
    }
}
