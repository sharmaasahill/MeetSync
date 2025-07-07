package com.assessment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for checking meeting conflicts among participants
 */
public class ConflictCheckRequestDto {
    
    @NotEmpty(message = "At least one participant email is required")
    private List<@Email String> participantEmails;
    
    @NotNull(message = "Meeting start time is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @NotNull(message = "Meeting end time is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    // Optional: exclude a specific meeting from conflict check (useful for updates)
    private Long excludeMeetingId;
    
    // Constructors
    public ConflictCheckRequestDto() {}
    
    public ConflictCheckRequestDto(List<String> participantEmails, 
                                   LocalDateTime startTime, 
                                   LocalDateTime endTime) {
        this.participantEmails = participantEmails;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public ConflictCheckRequestDto(List<String> participantEmails, 
                                   LocalDateTime startTime, 
                                   LocalDateTime endTime,
                                   Long excludeMeetingId) {
        this.participantEmails = participantEmails;
        this.startTime = startTime;
        this.endTime = endTime;
        this.excludeMeetingId = excludeMeetingId;
    }
    
    // Getters and Setters
    public List<String> getParticipantEmails() {
        return participantEmails;
    }
    
    public void setParticipantEmails(List<String> participantEmails) {
        this.participantEmails = participantEmails;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Long getExcludeMeetingId() {
        return excludeMeetingId;
    }
    
    public void setExcludeMeetingId(Long excludeMeetingId) {
        this.excludeMeetingId = excludeMeetingId;
    }
    
    @Override
    public String toString() {
        return "ConflictCheckRequestDto{" +
                "participantEmails=" + participantEmails +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", excludeMeetingId=" + excludeMeetingId +
                '}';
    }
} 