package com.assessment.exception;

import com.assessment.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the Meeting Calendar Assistant application
 * Provides consistent error responses for all exceptions
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle DuplicateResourceException
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResource(
            DuplicateResourceException ex, WebRequest request) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
    
    /**
     * Handle BusinessLogicException
     */
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessLogic(
            BusinessLogicException ex, WebRequest request) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        String errorMessage = "Validation failed: " + 
                errors.entrySet().stream()
                     .map(entry -> entry.getKey() + " - " + entry.getValue())
                     .collect(Collectors.joining(", "));
        
        ApiResponse<Object> response = ApiResponse.error(errorMessage, errors.toString());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle constraint validation errors
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
            ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        ApiResponse<Object> response = ApiResponse.error("Validation failed: " + errorMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        ApiResponse<Object> response = ApiResponse.error("Invalid argument: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle method argument type mismatch (e.g., invalid path variables)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String errorMessage = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
        
        ApiResponse<Object> response = ApiResponse.error(errorMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle date/time parsing errors
     */
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ApiResponse<Object>> handleDateTimeParse(
            DateTimeParseException ex) {
        String errorMessage = "Invalid date/time format. Please use format: yyyy-MM-dd HH:mm:ss";
        ApiResponse<Object> response = ApiResponse.error(errorMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        ApiResponse<Object> response = ApiResponse.error("An error occurred: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle general Exception - catch all
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        ApiResponse<Object> response = ApiResponse.error("An unexpected error occurred: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 