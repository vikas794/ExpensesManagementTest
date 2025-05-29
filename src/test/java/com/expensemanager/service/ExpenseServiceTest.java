package com.expensemanager.service;

import com.expensemanager.dto.CreateExpenseDto;
import com.expensemanager.dto.ExpenseDto;
import com.expensemanager.entity.Expense;
import com.expensemanager.entity.User;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.repository.ExpenseRepository;
import com.expensemanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private User user;
    private Expense expense;
    private CreateExpenseDto createExpenseDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "testuser", "password", "test@example.com");
        createExpenseDto = new CreateExpenseDto("Test Expense", BigDecimal.valueOf(100.00), LocalDate.now(), "Food");
        expense = new Expense(1L, "Test Expense", BigDecimal.valueOf(100.00), LocalDate.now(), "Food", user);
    }

    @Test
    void createExpense_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        ExpenseDto result = expenseService.createExpense(createExpenseDto, "testuser");

        assertNotNull(result);
        assertEquals(expense.getDescription(), result.getDescription());
        assertEquals(expense.getAmount(), result.getAmount());
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    void createExpense_userNotFound() {
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            expenseService.createExpense(createExpenseDto, "unknownuser");
        });
        assertEquals("User not found: unknownuser", exception.getMessage());
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void getExpensesByUsername_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(expenseRepository.findByUserId(user.getId())).thenReturn(Collections.singletonList(expense));

        List<ExpenseDto> results = expenseService.getExpensesByUsername("testuser");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(expense.getDescription(), results.get(0).getDescription());
    }
    
    @Test
    void getExpensesByUsername_userNotFound() {
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            expenseService.getExpensesByUsername("unknownuser");
        });
        assertEquals("User not found: unknownuser", exception.getMessage());
        verify(expenseRepository, never()).findByUserId(anyLong());
    }


    @Test
    void getExpenseByIdAndUsername_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));

        ExpenseDto result = expenseService.getExpenseByIdAndUsername(expense.getId(), "testuser");

        assertNotNull(result);
        assertEquals(expense.getDescription(), result.getDescription());
    }

    @Test
    void getExpenseByIdAndUsername_expenseNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(expenseRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            expenseService.getExpenseByIdAndUsername(99L, "testuser");
        });
        assertEquals("Expense not found with id: 99", exception.getMessage());
    }

    @Test
    void getExpenseByIdAndUsername_accessDenied() {
        User otherUser = new User(2L, "otheruser", "password", "other@example.com");
        Expense otherUsersExpense = new Expense(2L, "Other Expense", BigDecimal.ONE, LocalDate.now(), "Other", otherUser);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user)); // Current user
        when(expenseRepository.findById(otherUsersExpense.getId())).thenReturn(Optional.of(otherUsersExpense)); // Expense belongs to otherUser

        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            expenseService.getExpenseByIdAndUsername(otherUsersExpense.getId(), "testuser");
        });
        assertEquals("You are not authorized to view this expense", exception.getMessage());
    }
    
    @Test
    void updateExpense_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense); // mock save returning the updated expense

        CreateExpenseDto updateDto = new CreateExpenseDto("Updated Expense", BigDecimal.valueOf(150.00), LocalDate.now().plusDays(1), "Travel");
        ExpenseDto result = expenseService.updateExpense(expense.getId(), updateDto, "testuser");

        assertNotNull(result);
        assertEquals("Updated Expense", result.getDescription());
        assertEquals(BigDecimal.valueOf(150.00), result.getAmount());
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    void updateExpense_accessDenied() {
        User otherUser = new User(2L, "otheruser", "password", "other@example.com");
        Expense otherUsersExpense = new Expense(2L, "Other Expense", BigDecimal.ONE, LocalDate.now(), "Other", otherUser);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(expenseRepository.findById(otherUsersExpense.getId())).thenReturn(Optional.of(otherUsersExpense));

        CreateExpenseDto updateDto = new CreateExpenseDto("Attempted Update", BigDecimal.TEN, LocalDate.now(), "Fraud");

        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            expenseService.updateExpense(otherUsersExpense.getId(), updateDto, "testuser");
        });
        assertEquals("You are not authorized to update this expense", exception.getMessage());
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void deleteExpense_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));
        doNothing().when(expenseRepository).delete(any(Expense.class));

        assertDoesNotThrow(() -> {
            expenseService.deleteExpense(expense.getId(), "testuser");
        });
        verify(expenseRepository, times(1)).delete(expense);
    }

    @Test
    void deleteExpense_accessDenied() {
        User otherUser = new User(2L, "otheruser", "password", "other@example.com");
        Expense otherUsersExpense = new Expense(2L, "Other Expense", BigDecimal.ONE, LocalDate.now(), "Other", otherUser);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(expenseRepository.findById(otherUsersExpense.getId())).thenReturn(Optional.of(otherUsersExpense));

        Exception exception = assertThrows(AccessDeniedException.class, () -> {
            expenseService.deleteExpense(otherUsersExpense.getId(), "testuser");
        });
        assertEquals("You are not authorized to delete this expense", exception.getMessage());
        verify(expenseRepository, never()).delete(any(Expense.class));
    }
}
