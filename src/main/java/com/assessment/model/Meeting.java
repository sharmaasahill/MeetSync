package com.assessment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Meeting entity representing a calendar meeting
 * Contains meeting details, timing, organizer, and participants
 */
@Entity
@Table(name = "meetings")
public class Meeting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Meeting title is required")
    @Size(min = 3, max = 200, message = "Meeting title must be between 3 and 200 characters")
    @Column(nullable = false, length = 200)
    private String title;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(length = 1000)
    private String description;
    
    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime endTime;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    @JsonBackReference
    private Employee organizer;
    
    @ElementCollection
    @CollectionTable(
        name = "meeting_participants",
        joinColumns = @JoinColumn(name = "meeting_id")
    )
    @Column(name = "participant_email")
    private List<String> participantEmails = new ArrayList<>();
    
    @Column(length = 100)
    private String location;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus status = MeetingStatus.SCHEDULED;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    // Constructors
    public Meeting() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Meeting(String title, LocalDateTime startTime, LocalDateTime endTime, Employee organizer) {
        this();
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.organizer = organizer;
        validateMeetingTime();
    }
    
    public Meeting(String title, String description, LocalDateTime startTime, 
                   LocalDateTime endTime, Employee organizer, String location) {
        this(title, startTime, endTime, organizer);
        this.description = description;
        this.location = location;
    }
    
    // Validation method
    private void validateMeetingTime() {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        validateMeetingTime();
    }
    
    @PrePersist
    protected void onCreate() {
        validateMeetingTime();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
        validateMeetingTime();
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        validateMeetingTime();
    }
    
    /**
     * Update both start and end times simultaneously to avoid validation issues
     * @param startTime the new start time
     * @param endTime the new end time
     */
    public void updateMeetingTimes(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        validateMeetingTime();
    }
    
    public Employee getOrganizer() {
        return organizer;
    }
    
    public void setOrganizer(Employee organizer) {
        this.organizer = organizer;
    }
    
    public List<String> getParticipantEmails() {
        return participantEmails;
    }
    
    public void setParticipantEmails(List<String> participantEmails) {
        this.participantEmails = participantEmails != null ? participantEmails : new ArrayList<>();
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public MeetingStatus getStatus() {
        return status;
    }
    
    public void setStatus(MeetingStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Helper methods
    public void addParticipant(String email) {
        if (email != null && !participantEmails.contains(email)) {
            participantEmails.add(email);
        }
    }
    
    public void removeParticipant(String email) {
        participantEmails.remove(email);
    }
    
    public boolean hasConflictWith(Meeting other) {
        if (other == null) return false;
        
        return !(this.endTime.isBefore(other.startTime) || 
                 this.startTime.isAfter(other.endTime));
    }
    
    public long getDurationInMinutes() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meeting meeting = (Meeting) o;
        return Objects.equals(id, meeting.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Meeting{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", organizer=" + (organizer != null ? organizer.getName() : "null") +
                ", status=" + status +
                '}';
    }
} 