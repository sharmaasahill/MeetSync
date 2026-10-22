package com.assessment.service;

import com.assessment.dto.FreeSlotRequestDto;
import com.assessment.dto.TimeSlot;
import com.assessment.model.Employee;
import com.assessment.model.Meeting;
import com.assessment.model.MeetingStatus;
import com.assessment.repository.MeetingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CalendarService
 * Tests free slot finding algorithm and calendar operations
 */
@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {
    
    @Mock
    private MeetingRepository meetingRepository;
    
    @Mock
    private EmployeeService employeeService;
    
    @InjectMocks
    private CalendarService calendarService;
    
    private Employee employee1;
    private Employee employee2;
    private FreeSlotRequestDto freeSlotRequest;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    @BeforeEach
    void setUp() {
        employee1 = new Employee("Rahul Mehta", "rahul@company.com", "Engineering", "Developer");
        employee1.setId(1L);
        
        employee2 = new Employee("Priya Patel", "priya@company.com", "Marketing", "Manager");
        employee2.setId(2L);
        
        // Set up a test day from 9 AM to 6 PM
        startDate = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        endDate = startDate.withHour(18).withMinute(0);
        
        freeSlotRequest = new FreeSlotRequestDto();
        freeSlotRequest.setEmployee1Email(employee1.getEmail());
        freeSlotRequest.setEmployee2Email(employee2.getEmail());
        freeSlotRequest.setStartDate(startDate);
        freeSlotRequest.setEndDate(endDate);
        freeSlotRequest.setMeetingDurationMinutes(30);
    }
    
    @Test
    void findFreeSlots_NoMeetings_ReturnsAllBusinessHours() {
        // Given
        when(employeeService.getEmployeeByEmail(employee1.getEmail())).thenReturn(employee1);
        when(employeeService.getEmployeeByEmail(employee2.getEmail())).thenReturn(employee2);
        when(meetingRepository.findMeetingsForParticipantInDateRange(anyString(), any(), any()))
                .thenReturn(Arrays.asList());
        
        // When
        List<TimeSlot> result = calendarService.findFreeSlots(freeSlotRequest);
        
        // Then
        assertNotNull(result);
        assertTrue(result.size() > 0);
        
        // Verify first slot starts at business hours
        TimeSlot firstSlot = result.get(0);
        assertEquals(9, firstSlot.getStartTime().getHour());
        assertEquals(0, firstSlot.getStartTime().getMinute());
        
        // Verify each slot is 30 minutes
        for (TimeSlot slot : result) {
            assertEquals(30, slot.getDurationInMinutes());
        }
        
        verify(employeeService).getEmployeeByEmail(employee1.getEmail());
        verify(employeeService).getEmployeeByEmail(employee2.getEmail());
        verify(meetingRepository, times(2)).findMeetingsForParticipantInDateRange(anyString(), any(), any());
    }
    
    @Test
    void findFreeSlots_WithMeetings_ReturnsAvailableSlots() {
        // Given
        Meeting meeting1 = createMeeting(1L, "Meeting 1", 
                                        startDate.withHour(10), 
                                        startDate.withHour(11));
        
        Meeting meeting2 = createMeeting(2L, "Meeting 2", 
                                        startDate.withHour(14), 
                                        startDate.withHour(15));
        
        when(employeeService.getEmployeeByEmail(employee1.getEmail())).thenReturn(employee1);
        when(employeeService.getEmployeeByEmail(employee2.getEmail())).thenReturn(employee2);
        
        // Employee 1 has meeting1, Employee 2 has meeting2
        when(meetingRepository.findMeetingsForParticipantInDateRange(
                eq(employee1.getEmail()), any(), any()))
                .thenReturn(Arrays.asList(meeting1));
        
        when(meetingRepository.findMeetingsForParticipantInDateRange(
                eq(employee2.getEmail()), any(), any()))
                .thenReturn(Arrays.asList(meeting2));
        
        // When
        List<TimeSlot> result = calendarService.findFreeSlots(freeSlotRequest);
        
        // Then
        assertNotNull(result);
        assertTrue(result.size() > 0);
        
        // Verify no slots conflict with meetings
        for (TimeSlot slot : result) {
            assertFalse(slot.overlaps(new TimeSlot(meeting1.getStartTime(), meeting1.getEndTime())));
            assertFalse(slot.overlaps(new TimeSlot(meeting2.getStartTime(), meeting2.getEndTime())));
        }
    }
    
    @Test
    void findFreeSlots_EmployeeNotFound_ThrowsException() {
        // Given
        when(employeeService.getEmployeeByEmail(employee1.getEmail()))
                .thenThrow(new RuntimeException("Employee not found"));
        
        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> calendarService.findFreeSlots(freeSlotRequest)
        );
        
        assertTrue(exception.getMessage().contains("Employee not found"));
        verify(employeeService).getEmployeeByEmail(employee1.getEmail());
    }
    
    @Test
    void findFreeSlots_SameEmployee_ThrowsException() {
        // Given
        freeSlotRequest.setEmployee2Email(employee1.getEmail()); // Same as employee1
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calendarService.findFreeSlots(freeSlotRequest)
        );
        
        assertTrue(exception.getMessage().contains("Cannot search for slots between the same employee"));
    }
    
    @Test
    void findFreeSlots_InvalidTimeRange_ThrowsException() {
        // Given
        freeSlotRequest.setStartDate(endDate);
        freeSlotRequest.setEndDate(startDate);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calendarService.findFreeSlots(freeSlotRequest)
        );
        
        assertTrue(exception.getMessage().contains("Start date must be before end date"));
    }
    
    @Test
    void findFreeSlots_PastTime_ThrowsException() {
        // Given
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        freeSlotRequest.setStartDate(pastTime);
        freeSlotRequest.setEndDate(pastTime.plusHours(2));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calendarService.findFreeSlots(freeSlotRequest)
        );
        
        assertTrue(exception.getMessage().contains("Cannot search for slots in the past"));
    }
    
    @Test
    void findFreeSlots_InvalidDuration_ThrowsException() {
        // Given
        freeSlotRequest.setMeetingDurationMinutes(5); // Less than minimum 15 minutes
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calendarService.findFreeSlots(freeSlotRequest)
        );
        
        assertTrue(exception.getMessage().contains("Meeting duration must be at least 15 minutes"));
    }
    
    @Test
    void findFreeSlots_TooLongDuration_ThrowsException() {
        // Given
        freeSlotRequest.setMeetingDurationMinutes(10 * 60); // More than 8 hours
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calendarService.findFreeSlots(freeSlotRequest)
        );
        
        assertTrue(exception.getMessage().contains("Meeting duration cannot exceed 8 hours"));
    }
    
    @Test
    void findFreeSlotsForEmployee_Success() {
        // Given
        String email = employee1.getEmail();
        int duration = 60; // 1 hour
        
        when(employeeService.getEmployeeByEmail(email)).thenReturn(employee1);
        when(meetingRepository.findMeetingsForParticipantInDateRange(email, startDate, endDate))
                .thenReturn(Arrays.asList());
        
        // When
        List<TimeSlot> result = calendarService.findFreeSlotsForEmployee(email, startDate, endDate, duration);
        
        // Then
        assertNotNull(result);
        assertTrue(result.size() > 0);
        
        // Verify each slot is the requested duration
        for (TimeSlot slot : result) {
            assertEquals(duration, slot.getDurationInMinutes());
        }
        
        verify(employeeService).getEmployeeByEmail(email);
        verify(meetingRepository).findMeetingsForParticipantInDateRange(email, startDate, endDate);
    }
    
    @Test
    void findFreeSlotsForGroup_Success() {
        // Given
        List<String> participants = Arrays.asList(employee1.getEmail(), employee2.getEmail());
        int duration = 45; // 45 minutes
        
        when(employeeService.getEmployeeByEmail(employee1.getEmail())).thenReturn(employee1);
        when(employeeService.getEmployeeByEmail(employee2.getEmail())).thenReturn(employee2);
        when(meetingRepository.findMeetingsForParticipantsInTimeRange(participants, startDate, endDate))
                .thenReturn(Arrays.asList());
        
        // When
        List<TimeSlot> result = calendarService.findFreeSlotsForGroup(participants, startDate, endDate, duration);
        
        // Then
        assertNotNull(result);
        assertTrue(result.size() > 0);
        
        // Verify each slot is the requested duration
        for (TimeSlot slot : result) {
            assertEquals(duration, slot.getDurationInMinutes());
        }
        
        verify(employeeService).getEmployeeByEmail(employee1.getEmail());
        verify(employeeService).getEmployeeByEmail(employee2.getEmail());
        verify(meetingRepository).findMeetingsForParticipantsInTimeRange(participants, startDate, endDate);
    }
    
    @Test
    void isTimeSlotAvailable_NoConflicts_ReturnsTrue() {
        // Given
        List<String> participants = Arrays.asList(employee1.getEmail());
        LocalDateTime slotStart = startDate.withHour(10);
        LocalDateTime slotEnd = slotStart.plusHours(1);
        
        when(meetingRepository.findMeetingsForParticipantsInTimeRange(participants, slotStart, slotEnd))
                .thenReturn(Arrays.asList());
        
        // When
        boolean result = calendarService.isTimeSlotAvailable(participants, slotStart, slotEnd);
        
        // Then
        assertTrue(result);
        verify(meetingRepository).findMeetingsForParticipantsInTimeRange(participants, slotStart, slotEnd);
    }
    
    @Test
    void isTimeSlotAvailable_WithConflicts_ReturnsFalse() {
        // Given
        List<String> participants = Arrays.asList(employee1.getEmail());
        LocalDateTime slotStart = startDate.withHour(10);
        LocalDateTime slotEnd = slotStart.plusHours(1);
        
        Meeting conflictingMeeting = createMeeting(1L, "Conflicting", slotStart, slotEnd);
        
        when(meetingRepository.findMeetingsForParticipantsInTimeRange(participants, slotStart, slotEnd))
                .thenReturn(Arrays.asList(conflictingMeeting));
        
        // When
        boolean result = calendarService.isTimeSlotAvailable(participants, slotStart, slotEnd);
        
        // Then
        assertFalse(result);
        verify(meetingRepository).findMeetingsForParticipantsInTimeRange(participants, slotStart, slotEnd);
    }
    
    @Test
    void getNextAvailableSlot_Success() {
        // Given
        List<String> participants = Arrays.asList(employee1.getEmail());
        LocalDateTime afterTime = startDate.withHour(10);
        int duration = 30;
        int maxDays = 5;
        
        when(employeeService.getEmployeeByEmail(employee1.getEmail())).thenReturn(employee1);
        when(meetingRepository.findMeetingsForParticipantsInTimeRange(any(), any(), any()))
                .thenReturn(Arrays.asList());
        
        // When
        TimeSlot result = calendarService.getNextAvailableSlot(participants, afterTime, duration, maxDays);
        
        // Then
        assertNotNull(result);
        assertEquals(duration, result.getDurationInMinutes());
        assertTrue(result.getStartTime().isAfter(afterTime) || result.getStartTime().isEqual(afterTime));
        
        verify(employeeService).getEmployeeByEmail(employee1.getEmail());
    }
    
    @Test
    void getNextAvailableSlot_NoSlotsAvailable_ReturnsNull() {
        // Given
        List<String> participants = Arrays.asList(employee1.getEmail());
        LocalDateTime afterTime = startDate.withHour(10);
        int duration = 30;
        int maxDays = 0; // No days to search
        
        when(employeeService.getEmployeeByEmail(employee1.getEmail())).thenReturn(employee1);
        when(meetingRepository.findMeetingsForParticipantsInTimeRange(any(), any(), any()))
                .thenReturn(Arrays.asList());
        
        // When
        TimeSlot result = calendarService.getNextAvailableSlot(participants, afterTime, duration, maxDays);
        
        // Then
        assertNull(result);
    }
    
    // Helper method to create test meetings
    private Meeting createMeeting(Long id, String title, LocalDateTime start, LocalDateTime end) {
        Meeting meeting = new Meeting();
        meeting.setId(id);
        meeting.setTitle(title);
        meeting.setStartTime(start);
        meeting.setEndTime(end);
        meeting.setStatus(MeetingStatus.SCHEDULED);
        meeting.setOrganizer(employee1);
        return meeting;
    }
} 