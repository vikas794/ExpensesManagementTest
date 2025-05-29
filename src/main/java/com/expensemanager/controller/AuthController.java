package com.expensemanager.controller;

import com.expensemanager.dto.UserRegistrationDto;
import com.expensemanager.dto.UserViewDto;
import com.expensemanager.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserViewDto> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        UserViewDto registeredUser = userService.registerUser(registrationDto);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    // Login endpoint is handled by Spring Security's formLogin().
    // This is a placeholder or can be used if custom login logic beyond Spring Security's default is needed.
    // For now, Spring Security's /api/auth/login (as configured in SecurityConfig) will be used.
    // If we want to return user data upon login, we might need a custom success handler or this endpoint.
    /*
    @PostMapping("/login")
    public ResponseEntity<String> loginUser() {
        // Spring Security handles authentication. If successful, this method might not even be called
        // directly if using formLogin(). A custom AuthenticationSuccessHandler is a better way
        // to customize post-login behavior like returning user data.
        return ResponseEntity.ok("Login successful (handled by Spring Security)");
    }
    */

    // Logout endpoint is effectively handled by Spring Security's logout().
    // This custom mapping provides a POST way to logout if preferred over a GET.
    // SecurityConfig handles the actual logout process.
    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.ok("Logout successful");
    }
}
