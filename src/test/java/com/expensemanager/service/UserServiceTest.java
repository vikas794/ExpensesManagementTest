package com.expensemanager.service;

import com.expensemanager.dto.UserRegistrationDto;
import com.expensemanager.dto.UserViewDto;
import com.expensemanager.entity.User;
import com.expensemanager.exception.UserAlreadyExistsException;
import com.expensemanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDto registrationDto;
    private User user;

    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto("testuser", "password123", "test@example.com");
        user = new User(1L, "testuser", "encodedPassword", "test@example.com");
    }

    @Test
    void registerUser_success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserViewDto result = userService.registerUser(registrationDto);

        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_usernameExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        Exception exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.registerUser(registrationDto);
        });
        assertEquals("Username already exists: testuser", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_emailExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        Exception exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.registerUser(registrationDto);
        });
        assertEquals("Email already exists: test@example.com", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByUsername_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        User foundUser = userService.findByUsername("testuser");
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
    }

    @Test
    void findByUsername_notFound() {
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.findByUsername("unknownuser");
        });
        assertEquals("User not found with username: unknownuser", exception.getMessage());
    }

    @Test
    void getCurrentUserProfile_success() {
        // Mock Spring Security Context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserViewDto result = userService.getCurrentUserProfile();

        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
    }
    
    @Test
    void getCurrentUserProfile_userNotFoundInRepoThoughAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.getCurrentUserProfile();
        });
        assertEquals("User not found with username: testuser", exception.getMessage());
    }
}
