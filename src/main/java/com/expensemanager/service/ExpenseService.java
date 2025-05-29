package com.expensemanager.service;

import com.expensemanager.dto.CreateExpenseDto;
import com.expensemanager.dto.ExpenseDto;
import com.expensemanager.entity.Expense;
import com.expensemanager.entity.User;
import com.expensemanager.exception.ResourceNotFoundException; // Added import
import com.expensemanager.repository.ExpenseRepository;
import com.expensemanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException; // For authorization checks
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ExpenseDto createExpense(CreateExpenseDto expenseDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Expense expense = new Expense();
        expense.setDescription(expenseDto.getDescription());
        expense.setAmount(expenseDto.getAmount());
        expense.setDate(expenseDto.getDate());
        expense.setCategory(expenseDto.getCategory());
        expense.setUser(user);

        Expense savedExpense = expenseRepository.save(expense);
        return mapToDto(savedExpense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseDto> getExpensesByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return expenseRepository.findByUserId(user.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExpenseDto getExpenseByIdAndUsername(Long expenseId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to view this expense");
        }
        return mapToDto(expense);
    }

    @Transactional
    public ExpenseDto updateExpense(Long expenseId, CreateExpenseDto expenseDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to update this expense");
        }

        expense.setDescription(expenseDto.getDescription());
        expense.setAmount(expenseDto.getAmount());
        expense.setDate(expenseDto.getDate());
        expense.setCategory(expenseDto.getCategory());

        Expense updatedExpense = expenseRepository.save(expense);
        return mapToDto(updatedExpense);
    }

    @Transactional
    public void deleteExpense(Long expenseId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this expense");
        }
        expenseRepository.delete(expense);
    }

    // Helper method to map Expense entity to ExpenseDto
    private ExpenseDto mapToDto(Expense expense) {
        return new ExpenseDto(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getDate(),
                expense.getCategory()
        );
    }
}
