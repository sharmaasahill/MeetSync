package com.assessment.service;

import com.assessment.dto.FreeSlotRequestDto;
import com.assessment.dto.TimeSlot;
import com.assessment.model.Meeting;
import com.assessment.repository.MeetingRepository;
import com.assessment.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for Calendar operations
 * Implements free slot finding algorithm and calendar management operations
 */
@Service
@Transactional(readOnly = true)
public class CalendarService {
    
    private final MeetingRepository meetingRepository;
    private final EmployeeService employeeService;
    
    // Business hours configuration
    private static final LocalTime BUSINESS_START = LocalTime.of(9, 0);  // 9:00 AM
    private static final LocalTime BUSINESS_END = LocalTime.of(18, 0);   // 6:00 PM
    private static final int MIN_SLOT_DURATION_MINUTES = 15; // Minimum meeting duration
    
    @Autowired
    public CalendarService(MeetingRepository meetingRepository, EmployeeService employeeService) {
        this.meetingRepository = meetingRepository;
        this.employeeService = employeeService;
    }
    
    /**
     * Find free time slots between two employees for a meeting of specified duration
     * @param request the free slot request containing employee emails, date range, and duration
     * @return list of available time slots
     * @throws ResourceNotFoundException if employees not found
     */
    public List<TimeSlot> findFreeSlots(FreeSlotRequestDto request) {
        // Validate employees exist
        employeeService.getEmployeeByEmail(request.getEmployee1Email());
        employeeService.getEmployeeByEmail(request.getEmployee2Email());
        
        // Validate request
        validateFreeSlotRequest(request);
        
        // Get all meetings for both employees in the date range
        List<Meeting> employee1Meetings = meetingRepository.findMeetingsForParticipantInDateRange(
                request.getEmployee1Email(), request.getStartDate(), request.getEndDate());
        
        List<Meeting> employee2Meetings = meetingRepository.findMeetingsForParticipantInDateRange(
                request.getEmployee2Email(), request.getStartDate(), request.getEndDate());
        
        // Combine and sort all meetings
        List<Meeting> allMeetings = new ArrayList<>();
        allMeetings.addAll(employee1Meetings);
        allMeetings.addAll(employee2Meetings);
        
        // Remove duplicates and sort by start time
        List<Meeting> sortedMeetings = allMeetings.stream()
                .distinct()
                .sorted(Comparator.comparing(Meeting::getStartTime))
                .collect(Collectors.toList());
        
        // Find free slots
        return findAvailableSlots(sortedMeetings, request.getStartDate(), 
                                request.getEndDate(), request.getMeetingDurationMinutes());
    }
    
    /**
     * Find free slots for a single employee
     * @param employeeEmail the employee's email
     * @param startDate the start date for searching
     * @param endDate the end date for searching
     * @param durationMinutes the required meeting duration in minutes
     * @return list of available time slots
     */
    public List<TimeSlot> findFreeSlotsForEmployee(String employeeEmail, LocalDateTime startDate, 
                                                  LocalDateTime endDate, int durationMinutes) {
        // Validate employee exists
        employeeService.getEmployeeByEmail(employeeEmail);
        
        // Get all meetings for the employee in the date range
        List<Meeting> meetings = meetingRepository.findMeetingsForParticipantInDateRange(
                employeeEmail, startDate, endDate);
        
        return findAvailableSlots(meetings, startDate, endDate, durationMinutes);
    }
    
    /**
     * Find free slots for multiple employees (group meeting)
     * @param participantEmails list of participant emails
     * @param startDate the start date for searching
     * @param endDate the end date for searching
     * @param durationMinutes the required meeting duration in minutes
     * @return list of available time slots for all participants
     */
    public List<TimeSlot> findFreeSlotsForGroup(List<String> participantEmails, LocalDateTime startDate, 
                                               LocalDateTime endDate, int durationMinutes) {
        // Validate all employees exist
        for (String email : participantEmails) {
            employeeService.getEmployeeByEmail(email);
        }
        
        // Get all meetings for all participants
        List<Meeting> allMeetings = meetingRepository.findMeetingsForParticipantsInTimeRange(
                participantEmails, startDate, endDate);
        
        return findAvailableSlots(allMeetings, startDate, endDate, durationMinutes);
    }
    
    /**
     * Check if a specific time slot is available for given participants
     * @param participantEmails list of participant emails
     * @param startTime the start time of the slot
     * @param endTime the end time of the slot
     * @return true if the slot is available for all participants
     */
    public boolean isTimeSlotAvailable(List<String> participantEmails, LocalDateTime startTime, LocalDateTime endTime) {
        List<Meeting> conflictingMeetings = meetingRepository.findMeetingsForParticipantsInTimeRange(
                participantEmails, startTime, endTime);
        
        return conflictingMeetings.isEmpty();
    }
    
    /**
     * Get the next available slot for participants after a given time
     * @param participantEmails list of participant emails
     * @param afterTime the time after which to search
     * @param durationMinutes the required duration in minutes
     * @param maxDaysToSearch maximum days to search ahead
     * @return the next available time slot or null if none found
     */
    public TimeSlot getNextAvailableSlot(List<String> participantEmails, LocalDateTime afterTime, 
                                        int durationMinutes, int maxDaysToSearch) {
        LocalDateTime searchEnd = afterTime.plusDays(maxDaysToSearch);
        
        List<TimeSlot> freeSlots = findFreeSlotsForGroup(participantEmails, afterTime, searchEnd, durationMinutes);
        
        return freeSlots.isEmpty() ? null : freeSlots.get(0);
    }
    
    // Private helper methods
    
    /**
     * Core algorithm to find available time slots
     * @param meetings sorted list of meetings
     * @param startDate search start date
     * @param endDate search end date
     * @param durationMinutes required duration in minutes
     * @return list of available time slots
     */
    private List<TimeSlot> findAvailableSlots(List<Meeting> meetings, LocalDateTime startDate, 
                                            LocalDateTime endDate, int durationMinutes) {
        List<TimeSlot> freeSlots = new ArrayList<>();
        
        // Generate business hour slots for each day
        LocalDateTime currentDay = startDate.toLocalDate().atStartOfDay();
        
        while (!currentDay.isAfter(endDate)) {
            LocalDateTime dayStart = currentDay.with(BUSINESS_START);
            LocalDateTime dayEnd = currentDay.with(BUSINESS_END);
            
            // Adjust for search boundaries
            if (dayStart.isBefore(startDate)) {
                dayStart = startDate;
            }
            if (dayEnd.isAfter(endDate)) {
                dayEnd = endDate;
            }
            
            // Find free slots within this day
            List<TimeSlot> daySlots = findFreeSlotsInDay(meetings, dayStart, dayEnd, durationMinutes);
            freeSlots.addAll(daySlots);
            
            currentDay = currentDay.plusDays(1);
        }
        
        return freeSlots;
    }
    
    /**
     * Find free slots within a single day
     * @param meetings sorted list of meetings
     * @param dayStart start of the day (adjusted for business hours)
     * @param dayEnd end of the day (adjusted for business hours)
     * @param durationMinutes required duration in minutes
     * @return list of available time slots for the day
     */
    private List<TimeSlot> findFreeSlotsInDay(List<Meeting> meetings, LocalDateTime dayStart, 
                                            LocalDateTime dayEnd, int durationMinutes) {
        List<TimeSlot> daySlots = new ArrayList<>();
        
        // Filter meetings that overlap with this day's time range and sort them
        List<Meeting> dayMeetings = meetings.stream()
                .filter(m -> !m.getStatus().name().equals("CANCELLED"))
                .filter(m -> {
                    // Meeting overlaps with day if it starts before day ends and ends after day starts
                    return m.getStartTime().isBefore(dayEnd) && m.getEndTime().isAfter(dayStart);
                })
                .sorted(Comparator.comparing(Meeting::getStartTime))
                .collect(Collectors.toList());
        
        LocalDateTime currentTime = dayStart;
        
        for (Meeting meeting : dayMeetings) {
            LocalDateTime meetingStart = meeting.getStartTime();
            LocalDateTime meetingEnd = meeting.getEndTime();
            
            // Adjust meeting times to day boundaries
            if (meetingStart.isBefore(dayStart)) {
                meetingStart = dayStart;
            }
            if (meetingEnd.isAfter(dayEnd)) {
                meetingEnd = dayEnd;
            }
            
            // Check if there's a gap before this meeting
            if (currentTime.isBefore(meetingStart)) {
                List<TimeSlot> slotsBeforeMeeting = createSlotsInRange(currentTime, meetingStart, durationMinutes);
                daySlots.addAll(slotsBeforeMeeting);
            }
            
            // Move current time to after this meeting
            if (meetingEnd.isAfter(currentTime)) {
                currentTime = meetingEnd;
            }
        }
        
        // Check for time after the last meeting
        if (currentTime.isBefore(dayEnd)) {
            List<TimeSlot> slotsAfterLastMeeting = createSlotsInRange(currentTime, dayEnd, durationMinutes);
            daySlots.addAll(slotsAfterLastMeeting);
        }
        
        return daySlots;
    }
    
    /**
     * Create time slots within a given range
     * @param start start time
     * @param end end time
     * @param durationMinutes slot duration in minutes
     * @return list of time slots that fit in the range
     */
    private List<TimeSlot> createSlotsInRange(LocalDateTime start, LocalDateTime end, int durationMinutes) {
        List<TimeSlot> slots = new ArrayList<>();
        
        LocalDateTime slotStart = start;
        
        while (!slotStart.plusMinutes(durationMinutes).isAfter(end)) {
            LocalDateTime slotEnd = slotStart.plusMinutes(durationMinutes);
            slots.add(new TimeSlot(slotStart, slotEnd));
            
            // Move to next slot (minimum gap of 15 minutes between slots)
            slotStart = slotStart.plusMinutes(MIN_SLOT_DURATION_MINUTES);
        }
        
        return slots;
    }
    
    /**
     * Validate free slot request
     * @param request the request to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateFreeSlotRequest(FreeSlotRequestDto request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        
        if (request.getStartDate().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new IllegalArgumentException("Cannot search for slots in the past");
        }
        
        if (request.getMeetingDurationMinutes() < MIN_SLOT_DURATION_MINUTES) {
            throw new IllegalArgumentException("Meeting duration must be at least " + MIN_SLOT_DURATION_MINUTES + " minutes");
        }
        
        if (request.getMeetingDurationMinutes() > 8 * 60) { // Max 8 hours
            throw new IllegalArgumentException("Meeting duration cannot exceed 8 hours");
        }
        
        if (request.getEmployee1Email().equals(request.getEmployee2Email())) {
            throw new IllegalArgumentException("Cannot search for slots between the same employee");
        }
    }
} 