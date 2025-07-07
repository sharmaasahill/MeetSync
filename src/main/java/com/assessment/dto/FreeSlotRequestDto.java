package com.assessment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for finding free slots between employee calendars
 */
public class FreeSlotRequestDto {
    
    @NotNull(message = "First employee email is required")
    @Email(message = "Valid first employee email is required")
    private String employee1Email;
    
    @NotNull(message = "Second employee email is required")
    @Email(message = "Valid second employee email is required")
    private String employee2Email;
    
    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;
    
    @Positive(message = "Meeting duration must be positive")
    private int meetingDurationMinutes = 30; // Default 30 minutes
    
    // Constructors
    public FreeSlotRequestDto() {}
    
    public FreeSlotRequestDto(String employee1Email, String employee2Email, 
                              LocalDateTime startDate, LocalDateTime endDate, 
                              int meetingDurationMinutes) {
        this.employee1Email = employee1Email;
        this.employee2Email = employee2Email;
        this.startDate = startDate;
        this.endDate = endDate;
        this.meetingDurationMinutes = meetingDurationMinutes;
    }
    
    // Getters and Setters
    public String getEmployee1Email() {
        return employee1Email;
    }
    
    public void setEmployee1Email(String employee1Email) {
        this.employee1Email = employee1Email;
    }
    
    public String getEmployee2Email() {
        return employee2Email;
    }
    
    public void setEmployee2Email(String employee2Email) {
        this.employee2Email = employee2Email;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public int getMeetingDurationMinutes() {
        return meetingDurationMinutes;
    }
    
    public void setMeetingDurationMinutes(int meetingDurationMinutes) {
        this.meetingDurationMinutes = meetingDurationMinutes;
    }
    
    @Override
    public String toString() {
        return "FreeSlotRequestDto{" +
                "employee1Email='" + employee1Email + '\'' +
                ", employee2Email='" + employee2Email + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", meetingDurationMinutes=" + meetingDurationMinutes +
                '}';
    }
} 