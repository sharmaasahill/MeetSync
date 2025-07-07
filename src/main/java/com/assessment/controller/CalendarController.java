package com.assessment.controller;

import com.assessment.dto.ApiResponse;
import com.assessment.dto.FreeSlotRequestDto;
import com.assessment.dto.TimeSlot;
import com.assessment.service.CalendarService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for Calendar operations
 * Provides endpoints for finding free slots between employee calendars
 */
@RestController
@RequestMapping("/calendar")
@CrossOrigin(origins = "*")
public class CalendarController {
    
    private final CalendarService calendarService;
    
    @Autowired
    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }
    
    /**
     * Find free slots between two employees
     * POST /api/calendar/free-slots
     */
    @PostMapping("/free-slots")
    public ResponseEntity<ApiResponse<List<TimeSlot>>> findFreeSlots(
            @Valid @RequestBody FreeSlotRequestDto request) {
        List<TimeSlot> freeSlots = calendarService.findFreeSlots(request);
        String message = String.format("Found %d free slots between %s and %s", 
                                      freeSlots.size(), 
                                      request.getEmployee1Email(), 
                                      request.getEmployee2Email());
        ApiResponse<List<TimeSlot>> response = ApiResponse.success(message, freeSlots);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Find free slots for a single employee
     * GET /api/calendar/free-slots/{email}?start={start}&end={end}&duration={duration}
     */
    @GetMapping("/free-slots/{email}")
    public ResponseEntity<ApiResponse<List<TimeSlot>>> findFreeSlotsForEmployee(
            @PathVariable String email,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(defaultValue = "30") int duration) {
        
        List<TimeSlot> freeSlots = calendarService.findFreeSlotsForEmployee(email, start, end, duration);
        String message = String.format("Found %d free slots for %s", freeSlots.size(), email);
        ApiResponse<List<TimeSlot>> response = ApiResponse.success(message, freeSlots);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Find free slots for multiple employees (group meeting)
     * POST /api/calendar/free-slots/group
     */
    @PostMapping("/free-slots/group")
    public ResponseEntity<ApiResponse<List<TimeSlot>>> findFreeSlotsForGroup(
            @RequestParam List<String> participants,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(defaultValue = "30") int duration) {
        
        List<TimeSlot> freeSlots = calendarService.findFreeSlotsForGroup(participants, start, end, duration);
        String message = String.format("Found %d free slots for %d participants", freeSlots.size(), participants.size());
        ApiResponse<List<TimeSlot>> response = ApiResponse.success(message, freeSlots);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if a specific time slot is available for participants
     * POST /api/calendar/check-availability
     */
    @PostMapping("/check-availability")
    public ResponseEntity<ApiResponse<Boolean>> checkTimeSlotAvailability(
            @RequestParam List<String> participants,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        
        boolean isAvailable = calendarService.isTimeSlotAvailable(participants, start, end);
        String message = isAvailable ? "Time slot is available" : "Time slot is not available";
        ApiResponse<Boolean> response = ApiResponse.success(message, isAvailable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get the next available slot for participants
     * GET /api/calendar/next-available-slot
     */
    @GetMapping("/next-available-slot")
    public ResponseEntity<ApiResponse<TimeSlot>> getNextAvailableSlot(
            @RequestParam List<String> participants,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime after,
            @RequestParam(defaultValue = "30") int duration,
            @RequestParam(defaultValue = "7") int maxDays) {
        
        TimeSlot nextSlot = calendarService.getNextAvailableSlot(participants, after, duration, maxDays);
        String message = nextSlot != null ? "Next available slot found" : "No available slot found in the next " + maxDays + " days";
        ApiResponse<TimeSlot> response = ApiResponse.success(message, nextSlot);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Find optimal meeting time between two employees (convenience endpoint)
     * GET /api/calendar/optimal-time?employee1={email1}&employee2={email2}&duration={duration}&maxDays={days}
     */
    @GetMapping("/optimal-time")
    public ResponseEntity<ApiResponse<TimeSlot>> findOptimalMeetingTime(
            @RequestParam String employee1,
            @RequestParam String employee2,
            @RequestParam(defaultValue = "30") int duration,
            @RequestParam(defaultValue = "7") int maxDays) {
        
        List<String> participants = List.of(employee1, employee2);
        LocalDateTime now = LocalDateTime.now();
        
        TimeSlot optimalSlot = calendarService.getNextAvailableSlot(participants, now, duration, maxDays);
        String message = optimalSlot != null ? 
                "Optimal meeting time found" : 
                "No available meeting time found in the next " + maxDays + " days";
        
        ApiResponse<TimeSlot> response = ApiResponse.success(message, optimalSlot);
        return ResponseEntity.ok(response);
    }
} 