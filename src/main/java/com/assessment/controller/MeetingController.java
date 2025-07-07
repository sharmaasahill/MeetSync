package com.assessment.controller;

import com.assessment.dto.ApiResponse;
import com.assessment.dto.ConflictCheckRequestDto;
import com.assessment.dto.ConflictResponseDto;
import com.assessment.dto.MeetingRequestDto;
import com.assessment.model.Meeting;
import com.assessment.service.MeetingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for Meeting management operations
 * Provides endpoints for booking meetings, checking conflicts, and managing meetings
 */
@RestController
@RequestMapping("/meetings")
@CrossOrigin(origins = "*")
public class MeetingController {
    
    private final MeetingService meetingService;
    
    @Autowired
    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }
    
    /**
     * Book a new meeting
     * POST /api/meetings
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Meeting>> bookMeeting(@Valid @RequestBody MeetingRequestDto meetingRequest) {
        Meeting meeting = meetingService.bookMeeting(meetingRequest);
        ApiResponse<Meeting> response = ApiResponse.success("Meeting booked successfully", meeting);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Get meeting by ID
     * GET /api/meetings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Meeting>> getMeetingById(@PathVariable Long id) {
        Meeting meeting = meetingService.getMeetingById(id);
        ApiResponse<Meeting> response = ApiResponse.success(meeting);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all meetings
     * GET /api/meetings
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Meeting>>> getAllMeetings() {
        List<Meeting> meetings = meetingService.getAllMeetings();
        ApiResponse<List<Meeting>> response = ApiResponse.success("Retrieved all meetings", meetings);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get meetings by organizer email
     * GET /api/meetings/organizer/{email}
     */
    @GetMapping("/organizer/{email}")
    public ResponseEntity<ApiResponse<List<Meeting>>> getMeetingsByOrganizerEmail(@PathVariable String email) {
        List<Meeting> meetings = meetingService.getMeetingsByOrganizerEmail(email);
        ApiResponse<List<Meeting>> response = ApiResponse.success("Meetings organized by " + email, meetings);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get meetings in a time range
     * GET /api/meetings/range?start={startTime}&end={endTime}
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<Meeting>>> getMeetingsInTimeRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        List<Meeting> meetings = meetingService.getMeetingsInTimeRange(start, end);
        ApiResponse<List<Meeting>> response = ApiResponse.success("Meetings in time range", meetings);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update an existing meeting
     * PUT /api/meetings/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Meeting>> updateMeeting(@PathVariable Long id, 
                                                             @Valid @RequestBody MeetingRequestDto meetingRequest) {
        Meeting meeting = meetingService.updateMeeting(id, meetingRequest);
        ApiResponse<Meeting> response = ApiResponse.success("Meeting updated successfully", meeting);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel a meeting
     * PATCH /api/meetings/{id}/cancel
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Meeting>> cancelMeeting(@PathVariable Long id) {
        Meeting meeting = meetingService.cancelMeeting(id);
        ApiResponse<Meeting> response = ApiResponse.success("Meeting cancelled successfully", meeting);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a meeting
     * DELETE /api/meetings/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMeeting(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        ApiResponse<Void> response = ApiResponse.success("Meeting deleted successfully", null);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check for meeting conflicts
     * POST /api/meetings/conflicts
     */
    @PostMapping("/conflicts")
    public ResponseEntity<ApiResponse<ConflictResponseDto>> checkConflicts(
            @Valid @RequestBody ConflictCheckRequestDto conflictRequest) {
        ConflictResponseDto conflicts = meetingService.checkConflicts(conflictRequest);
        String message = conflicts.isHasConflicts() ? "Conflicts detected" : "No conflicts found";
        ApiResponse<ConflictResponseDto> response = ApiResponse.success(message, conflicts);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get upcoming meetings for an employee
     * GET /api/meetings/upcoming/{email}
     */
    @GetMapping("/upcoming/{email}")
    public ResponseEntity<ApiResponse<List<Meeting>>> getUpcomingMeetingsForEmployee(@PathVariable String email) {
        List<Meeting> meetings = meetingService.getUpcomingMeetingsForEmployee(email);
        ApiResponse<List<Meeting>> response = ApiResponse.success("Upcoming meetings for " + email, meetings);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get meetings for a participant in a date range
     * GET /api/meetings/participant/{email}?start={startDate}&end={endDate}
     */
    @GetMapping("/participant/{email}")
    public ResponseEntity<ApiResponse<List<Meeting>>> getMeetingsForParticipantInDateRange(
            @PathVariable String email,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        List<Meeting> meetings = meetingService.getMeetingsForParticipantInDateRange(email, start, end);
        ApiResponse<List<Meeting>> response = ApiResponse.success("Meetings for " + email + " in date range", meetings);
        return ResponseEntity.ok(response);
    }
} 