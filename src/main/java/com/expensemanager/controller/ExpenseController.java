package com.expensemanager.controller;

import com.expensemanager.dto.CreateExpenseDto;
import com.expensemanager.dto.ExpenseDto;
import com.expensemanager.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // This case should ideally not be reached if Spring Security is configured correctly
            // as endpoints are protected.
            throw new IllegalStateException("User not authenticated or authentication details not found.");
        }
        return authentication.getName();
    }

    @PostMapping
    public ResponseEntity<ExpenseDto> createExpense(@Valid @RequestBody CreateExpenseDto createExpenseDto) {
        String username = getCurrentUsername();
        ExpenseDto createdExpense = expenseService.createExpense(createExpenseDto, username);
        return new ResponseEntity<>(createdExpense, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> getAllExpensesForCurrentUser() {
        String username = getCurrentUsername();
        List<ExpenseDto> expenses = expenseService.getExpensesByUsername(username);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto> getExpenseById(@PathVariable Long id) {
        String username = getCurrentUsername();
        ExpenseDto expenseDto = expenseService.getExpenseByIdAndUsername(id, username);
        return ResponseEntity.ok(expenseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDto> updateExpense(@PathVariable Long id, @Valid @RequestBody CreateExpenseDto createExpenseDto) {
        String username = getCurrentUsername();
        ExpenseDto updatedExpense = expenseService.updateExpense(id, createExpenseDto, username);
        return ResponseEntity.ok(updatedExpense);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        String username = getCurrentUsername();
        expenseService.deleteExpense(id, username);
        return ResponseEntity.noContent().build();
    }
}
