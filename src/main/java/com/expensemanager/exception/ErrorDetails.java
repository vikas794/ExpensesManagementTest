package com.expensemanager.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ErrorDetails {
    private LocalDateTime timestamp;
    private String message;
    private String details;
    private Map<String, String> validationErrors; // For validation errors

    public ErrorDetails(LocalDateTime timestamp, String message, String details) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }

    public ErrorDetails(LocalDateTime timestamp, String message, String details, Map<String, String> validationErrors) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
        this.validationErrors = validationErrors;
    }

    // Getters
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getMessage() { return message; }
    public String getDetails() { return details; }
    public Map<String, String> getValidationErrors() { return validationErrors; }
}
