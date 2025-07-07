package com.assessment.service;

import com.assessment.dto.ConflictCheckRequestDto;
import com.assessment.dto.ConflictResponseDto;
import com.assessment.dto.MeetingRequestDto;
import com.assessment.model.Employee;
import com.assessment.model.Meeting;
import com.assessment.model.MeetingStatus;
import com.assessment.repository.MeetingRepository;
import com.assessment.exception.ResourceNotFoundException;
import com.assessment.exception.BusinessLogicException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for Meeting business logic
 * Implements meeting management operations including booking, conflict detection, and scheduling
 */
@Service
@Transactional
public class MeetingService {
    
    private final MeetingRepository meetingRepository;
    private final EmployeeService employeeService;
    
    @Autowired
    public MeetingService(MeetingRepository meetingRepository, EmployeeService employeeService) {
        this.meetingRepository = meetingRepository;
        this.employeeService = employeeService;
    }
    
    /**
     * Book a new meeting
     * @param meetingRequest the meeting request details
     * @return the created meeting
     * @throws ResourceNotFoundException if organizer not found
     * @throws BusinessLogicException if there are scheduling conflicts
     */
    public Meeting bookMeeting(MeetingRequestDto meetingRequest) {
        // Validate meeting request
        validateMeetingRequest(meetingRequest);
        
        // Get organizer
        Employee organizer = employeeService.getEmployeeByEmail(meetingRequest.getOrganizerEmail());
        
        // Create meeting
        Meeting meeting = new Meeting();
        meeting.setTitle(meetingRequest.getTitle());
        meeting.setDescription(meetingRequest.getDescription());
        meeting.setStartTime(meetingRequest.getStartTime());
        meeting.setEndTime(meetingRequest.getEndTime());
        meeting.setOrganizer(organizer);
        meeting.setLocation(meetingRequest.getLocation());
        meeting.setParticipantEmails(meetingRequest.getParticipantEmails() != null ? 
                                   meetingRequest.getParticipantEmails() : new ArrayList<>());
        
        // Check for conflicts
        ConflictResponseDto conflicts = checkConflictsForMeeting(meeting);
        if (conflicts.isHasConflicts()) {
            throw new BusinessLogicException("Cannot book meeting due to conflicts: " + conflicts.getMessage());
        }
        
        return meetingRepository.save(meeting);
    }
    
    /**
     * Get meeting by ID
     * @param id the meeting ID
     * @return the meeting
     * @throws ResourceNotFoundException if meeting not found
     */
    @Transactional(readOnly = true)
    public Meeting getMeetingById(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + id));
    }
    
    /**
     * Get all meetings
     * @return list of all meetings
     */
    @Transactional(readOnly = true)
    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAll();
    }
    
    /**
     * Get meetings by organizer email
     * @param organizerEmail the organizer's email
     * @return list of meetings organized by the employee
     */
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsByOrganizerEmail(String organizerEmail) {
        return meetingRepository.findByOrganizerEmail(organizerEmail);
    }
    
    /**
     * Get meetings in a time range
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of meetings in the time range
     */
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsInTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return meetingRepository.findMeetingsInTimeRange(startTime, endTime);
    }
    
    /**
     * Update an existing meeting
     * @param id the meeting ID
     * @param meetingRequest the updated meeting data
     * @return the updated meeting
     * @throws ResourceNotFoundException if meeting not found
     * @throws BusinessLogicException if there are scheduling conflicts
     */
    public Meeting updateMeeting(Long id, MeetingRequestDto meetingRequest) {
        Meeting existingMeeting = getMeetingById(id);
        
        // Validate meeting request
        validateMeetingRequest(meetingRequest);
        
        // Update fields
        existingMeeting.setTitle(meetingRequest.getTitle());
        existingMeeting.setDescription(meetingRequest.getDescription());
        existingMeeting.setLocation(meetingRequest.getLocation());
        existingMeeting.setParticipantEmails(meetingRequest.getParticipantEmails() != null ? 
                                           meetingRequest.getParticipantEmails() : new ArrayList<>());
        
        // Update times together to avoid validation issues
        updateMeetingTimes(existingMeeting, meetingRequest.getStartTime(), meetingRequest.getEndTime());
        
        // Check for conflicts (excluding this meeting)
        ConflictCheckRequestDto conflictRequest = new ConflictCheckRequestDto();
        conflictRequest.setParticipantEmails(getAllParticipants(existingMeeting));
        conflictRequest.setStartTime(meetingRequest.getStartTime());
        conflictRequest.setEndTime(meetingRequest.getEndTime());
        conflictRequest.setExcludeMeetingId(id);
        
        ConflictResponseDto conflicts = checkConflicts(conflictRequest);
        if (conflicts.isHasConflicts()) {
            throw new BusinessLogicException("Cannot update meeting due to conflicts: " + conflicts.getMessage());
        }
        
        return meetingRepository.save(existingMeeting);
    }
    
    /**
     * Cancel a meeting
     * @param id the meeting ID
     * @throws ResourceNotFoundException if meeting not found
     */
    public Meeting cancelMeeting(Long id) {
        Meeting meeting = getMeetingById(id);
        meeting.setStatus(MeetingStatus.CANCELLED);
        return meetingRepository.save(meeting);
    }
    
    /**
     * Delete a meeting
     * @param id the meeting ID
     * @throws ResourceNotFoundException if meeting not found
     */
    public void deleteMeeting(Long id) {
        Meeting meeting = getMeetingById(id);
        meetingRepository.delete(meeting);
    }
    
    /**
     * Check for conflicts for all participants of a meeting request
     * @param conflictRequest the conflict check request
     * @return conflict response with details
     */
    @Transactional(readOnly = true)
    public ConflictResponseDto checkConflicts(ConflictCheckRequestDto conflictRequest) {
        Map<String, List<Meeting>> conflictsByEmployee = new HashMap<>();
        boolean hasConflicts = false;
        
        for (String participantEmail : conflictRequest.getParticipantEmails()) {
            List<Meeting> conflicts;
            
            if (conflictRequest.getExcludeMeetingId() != null) {
                conflicts = meetingRepository.findConflictingMeetingsForParticipantExcludingMeeting(
                        participantEmail,
                        conflictRequest.getStartTime(),
                        conflictRequest.getEndTime(),
                        conflictRequest.getExcludeMeetingId()
                );
            } else {
                conflicts = meetingRepository.findConflictingMeetingsForParticipant(
                        participantEmail,
                        conflictRequest.getStartTime(),
                        conflictRequest.getEndTime()
                );
            }
            
            if (!conflicts.isEmpty()) {
                hasConflicts = true;
                conflictsByEmployee.put(participantEmail, conflicts);
            }
        }
        
        String message = hasConflicts ? 
                "Conflicts found for " + conflictsByEmployee.size() + " participant(s)" :
                "No conflicts found";
        
        return new ConflictResponseDto(hasConflicts, message, conflictsByEmployee);
    }
    
    /**
     * Get upcoming meetings for an employee
     * @param email the employee's email
     * @return list of upcoming meetings
     */
    @Transactional(readOnly = true)
    public List<Meeting> getUpcomingMeetingsForEmployee(String email) {
        return meetingRepository.findUpcomingMeetingsForOrganizer(email, LocalDateTime.now());
    }
    
    /**
     * Get meetings for a participant in a date range
     * @param email the participant's email
     * @param startDate the start date
     * @param endDate the end date
     * @return list of meetings for the participant
     */
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsForParticipantInDateRange(String email, LocalDateTime startDate, LocalDateTime endDate) {
        return meetingRepository.findMeetingsForParticipantInDateRange(email, startDate, endDate);
    }
    
    // Private helper methods
    
    /**
     * Check conflicts for a specific meeting
     * @param meeting the meeting to check
     * @return conflict response
     */
    private ConflictResponseDto checkConflictsForMeeting(Meeting meeting) {
        List<String> allParticipants = getAllParticipants(meeting);
        
        ConflictCheckRequestDto conflictRequest = new ConflictCheckRequestDto();
        conflictRequest.setParticipantEmails(allParticipants);
        conflictRequest.setStartTime(meeting.getStartTime());
        conflictRequest.setEndTime(meeting.getEndTime());
        
        return checkConflicts(conflictRequest);
    }
    
    /**
     * Get all participants including organizer
     * @param meeting the meeting
     * @return list of all participant emails
     */
    private List<String> getAllParticipants(Meeting meeting) {
        List<String> allParticipants = new ArrayList<>();
        allParticipants.add(meeting.getOrganizer().getEmail());
        
        if (meeting.getParticipantEmails() != null) {
            allParticipants.addAll(meeting.getParticipantEmails());
        }
        
        return allParticipants.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * Update meeting times safely to avoid validation issues
     * @param meeting the meeting to update
     * @param startTime the new start time
     * @param endTime the new end time
     */
    private void updateMeetingTimes(Meeting meeting, LocalDateTime startTime, LocalDateTime endTime) {
        meeting.updateMeetingTimes(startTime, endTime);
    }

    /**
     * Validate meeting request
     * @param meetingRequest the meeting request to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateMeetingRequest(MeetingRequestDto meetingRequest) {
        if (meetingRequest.getStartTime().isAfter(meetingRequest.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        
        if (meetingRequest.getStartTime().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new IllegalArgumentException("Cannot schedule meetings in the past");
        }
        
        // Validate organizer exists
        if (!employeeService.employeeExistsByEmail(meetingRequest.getOrganizerEmail())) {
            throw new ResourceNotFoundException("Organizer not found with email: " + meetingRequest.getOrganizerEmail());
        }
        
        // Validate participant emails exist (optional validation)
        if (meetingRequest.getParticipantEmails() != null) {
            for (String email : meetingRequest.getParticipantEmails()) {
                if (!employeeService.employeeExistsByEmail(email)) {
                    throw new ResourceNotFoundException("Participant not found with email: " + email);
                }
            }
        }
    }
} 