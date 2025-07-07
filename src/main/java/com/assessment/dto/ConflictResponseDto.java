package com.assessment.dto;

import com.assessment.model.Meeting;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for conflict detection responses
 */
public class ConflictResponseDto {
    
    private boolean hasConflicts;
    private String message;
    private Map<String, List<Meeting>> conflictsByEmployee;
    
    // Constructors
    public ConflictResponseDto() {}
    
    public ConflictResponseDto(boolean hasConflicts, String message) {
        this.hasConflicts = hasConflicts;
        this.message = message;
    }
    
    public ConflictResponseDto(boolean hasConflicts, String message, 
                               Map<String, List<Meeting>> conflictsByEmployee) {
        this.hasConflicts = hasConflicts;
        this.message = message;
        this.conflictsByEmployee = conflictsByEmployee;
    }
    
    // Getters and Setters
    public boolean isHasConflicts() {
        return hasConflicts;
    }
    
    public void setHasConflicts(boolean hasConflicts) {
        this.hasConflicts = hasConflicts;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Map<String, List<Meeting>> getConflictsByEmployee() {
        return conflictsByEmployee;
    }
    
    public void setConflictsByEmployee(Map<String, List<Meeting>> conflictsByEmployee) {
        this.conflictsByEmployee = conflictsByEmployee;
    }
    
    @Override
    public String toString() {
        return "ConflictResponseDto{" +
                "hasConflicts=" + hasConflicts +
                ", message='" + message + '\'' +
                ", conflictsByEmployee=" + conflictsByEmployee +
                '}';
    }
} 