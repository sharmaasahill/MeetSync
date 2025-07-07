package com.assessment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Objects;

/**
 * Represents a time slot that can be used for scheduling meetings
 * Contains start and end time information
 */
public class TimeSlot {
    
    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    // Constructors
    public TimeSlot() {}
    
    public TimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        validateTimeSlot();
    }
    
    private void validateTimeSlot() {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }
    
    // Getters and Setters
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        validateTimeSlot();
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        validateTimeSlot();
    }
    
    // Utility methods
    public long getDurationInMinutes() {
        if (startTime != null && endTime != null) {
            return Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }
    
    public boolean canAccommodate(int requiredDurationMinutes) {
        return getDurationInMinutes() >= requiredDurationMinutes;
    }
    
    public boolean overlaps(TimeSlot other) {
        if (other == null) return false;
        
        // Two time slots overlap if one starts before the other ends
        // and vice versa. Adjacent slots (ending exactly when other starts) don't overlap.
        return !(this.endTime.isBefore(other.startTime) || this.endTime.equals(other.startTime) || 
                 this.startTime.isAfter(other.endTime) || this.startTime.equals(other.endTime));
    }
    
    public boolean contains(LocalDateTime time) {
        return time != null && 
               !time.isBefore(startTime) && 
               !time.isAfter(endTime);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equals(startTime, timeSlot.startTime) &&
               Objects.equals(endTime, timeSlot.endTime);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime);
    }
    
    @Override
    public String toString() {
        return "TimeSlot{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", duration=" + getDurationInMinutes() + " minutes" +
                '}';
    }
} 