package com.assessment.model;

/**
 * Enumeration representing the status of a meeting
 */
public enum MeetingStatus {
    SCHEDULED("Scheduled"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    POSTPONED("Postponed");
    
    private final String displayName;
    
    MeetingStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
} 