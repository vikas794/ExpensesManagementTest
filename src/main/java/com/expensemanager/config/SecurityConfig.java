package com.expensemanager.config;

import com.expensemanager.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless APIs if not using forms directly, or configure properly
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                        new AntPathRequestMatcher("/api/auth/register"),
                        new AntPathRequestMatcher("/api/auth/login"),
                        new AntPathRequestMatcher("/error"), // Permit error pages
                        new AntPathRequestMatcher("/h2-console/**") // Permit H2 console access for dev
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(formLogin -> formLogin
                .loginProcessingUrl("/api/auth/login") // Endpoint that processes the login
                .usernameParameter("username") // Parameter name for username in login request
                .passwordParameter("password") // Parameter name for password in login request
                .defaultSuccessUrl("/api/users/profile", true) // Redirect after successful login
                .failureUrl("/api/auth/login?error=true") // URL to redirect to on login failure
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessUrl("/api/auth/login?logout=true") // Redirect after logout
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID") // Ensure session cookie is deleted
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Spring Security will create a session if needed
                .invalidSessionUrl("/api/auth/login?invalid=true") // URL for invalid session
                .maximumSessions(1) // Prevent concurrent logins with the same user
                    .expiredUrl("/api/auth/login?expired=true") // URL if session expires due to concurrent login
            )
            // For H2 console to work with Spring Security, frame options need to be disabled
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));


        return http.build();
    }
}
