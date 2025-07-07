package com.assessment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for meeting booking requests
 */
public class MeetingRequestDto {
    
    @NotBlank(message = "Meeting title is required")
    @Size(min = 3, max = 200, message = "Meeting title must be between 3 and 200 characters")
    private String title;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    @NotNull(message = "Organizer email is required")
    @Email(message = "Valid organizer email is required")
    private String organizerEmail;
    
    private List<@Email String> participantEmails;
    
    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private String location;
    
    // Constructors
    public MeetingRequestDto() {}
    
    public MeetingRequestDto(String title, LocalDateTime startTime, LocalDateTime endTime, String organizerEmail) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.organizerEmail = organizerEmail;
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    
    public String getOrganizerEmail() {
        return organizerEmail;
    }
    
    public void setOrganizerEmail(String organizerEmail) {
        this.organizerEmail = organizerEmail;
    }
    
    public List<String> getParticipantEmails() {
        return participantEmails;
    }
    
    public void setParticipantEmails(List<String> participantEmails) {
        this.participantEmails = participantEmails;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    @Override
    public String toString() {
        return "MeetingRequestDto{" +
                "title='" + title + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", organizerEmail='" + organizerEmail + '\'' +
                ", participantEmails=" + participantEmails +
                '}';
    }
} 