package com.assessment.service;

import com.assessment.dto.ConflictCheckRequestDto;
import com.assessment.dto.ConflictResponseDto;
import com.assessment.dto.MeetingRequestDto;
import com.assessment.exception.BusinessLogicException;
import com.assessment.exception.ResourceNotFoundException;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MeetingService
 * Tests meeting booking, conflict detection, and scheduling business logic
 */
@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {
    
    @Mock
    private MeetingRepository meetingRepository;
    
    @Mock
    private EmployeeService employeeService;
    
    @InjectMocks
    private MeetingService meetingService;
    
    private Employee organizer;
    private Employee participant;
    private MeetingRequestDto meetingRequest;
    private Meeting testMeeting;
    private LocalDateTime futureStart;
    private LocalDateTime futureEnd;
    
    @BeforeEach
    void setUp() {
        organizer = new Employee("John Organizer", "john@company.com", "Engineering", "Manager");
        organizer.setId(1L);
        
        participant = new Employee("Jane Participant", "jane@company.com", "Marketing", "Developer");
        participant.setId(2L);
        
        futureStart = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        futureEnd = futureStart.plusHours(1);
        
        meetingRequest = new MeetingRequestDto();
        meetingRequest.setTitle("Team Meeting");
        meetingRequest.setDescription("Weekly team sync");
        meetingRequest.setStartTime(futureStart);
        meetingRequest.setEndTime(futureEnd);
        meetingRequest.setOrganizerEmail(organizer.getEmail());
        meetingRequest.setParticipantEmails(Arrays.asList(participant.getEmail()));
        meetingRequest.setLocation("Conference Room A");
        
        testMeeting = new Meeting();
        testMeeting.setId(1L);
        testMeeting.setTitle(meetingRequest.getTitle());
        testMeeting.setDescription(meetingRequest.getDescription());
        testMeeting.setStartTime(meetingRequest.getStartTime());
        testMeeting.setEndTime(meetingRequest.getEndTime());
        testMeeting.setOrganizer(organizer);
        testMeeting.setParticipantEmails(meetingRequest.getParticipantEmails());
        testMeeting.setLocation(meetingRequest.getLocation());
        testMeeting.setStatus(MeetingStatus.SCHEDULED);
    }
    
    @Test
    void bookMeeting_Success() {
        // Given
        when(employeeService.getEmployeeByEmail(organizer.getEmail())).thenReturn(organizer);
        when(employeeService.employeeExistsByEmail(organizer.getEmail())).thenReturn(true);
        when(employeeService.employeeExistsByEmail(participant.getEmail())).thenReturn(true);
        when(meetingRepository.findConflictingMeetingsForParticipant(anyString(), any(), any()))
                .thenReturn(Arrays.asList());
        when(meetingRepository.save(any(Meeting.class))).thenReturn(testMeeting);
        
        // When
        Meeting result = meetingService.bookMeeting(meetingRequest);
        
        // Then
        assertNotNull(result);
        assertEquals(testMeeting.getTitle(), result.getTitle());
        assertEquals(testMeeting.getOrganizer().getEmail(), result.getOrganizer().getEmail());
        
        verify(employeeService).getEmployeeByEmail(organizer.getEmail());
        verify(meetingRepository).save(any(Meeting.class));
    }
    
    @Test
    void bookMeeting_OrganizerNotFound_ThrowsException() {
        // Given
        when(employeeService.employeeExistsByEmail(organizer.getEmail())).thenReturn(false);
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> meetingService.bookMeeting(meetingRequest)
        );
        
        assertTrue(exception.getMessage().contains(organizer.getEmail()));
        verify(meetingRepository, never()).save(any());
    }
    
    @Test
    void bookMeeting_ParticipantNotFound_ThrowsException() {
        // Given
        when(employeeService.employeeExistsByEmail(organizer.getEmail())).thenReturn(true);
        when(employeeService.employeeExistsByEmail(participant.getEmail())).thenReturn(false);
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> meetingService.bookMeeting(meetingRequest)
        );
        
        assertTrue(exception.getMessage().contains(participant.getEmail()));
        verify(meetingRepository, never()).save(any());
    }
    
    @Test
    void bookMeeting_StartTimeAfterEndTime_ThrowsException() {
        // Given
        meetingRequest.setStartTime(futureEnd);
        meetingRequest.setEndTime(futureStart);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> meetingService.bookMeeting(meetingRequest)
        );
        
        assertTrue(exception.getMessage().contains("Start time must be before end time"));
        verify(meetingRepository, never()).save(any());
    }
    
    @Test
    void bookMeeting_PastTime_ThrowsException() {
        // Given
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        meetingRequest.setStartTime(pastTime);
        meetingRequest.setEndTime(pastTime.plusHours(1));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> meetingService.bookMeeting(meetingRequest)
        );
        
        assertTrue(exception.getMessage().contains("Cannot schedule meetings in the past"));
        verify(meetingRepository, never()).save(any());
    }
    
    @Test
    void bookMeeting_WithConflicts_ThrowsException() {
        // Given
        Meeting conflictingMeeting = new Meeting();
        conflictingMeeting.setId(2L);
        conflictingMeeting.setTitle("Conflicting Meeting");
        conflictingMeeting.setStartTime(futureStart.minusMinutes(30));
        conflictingMeeting.setEndTime(futureStart.plusMinutes(30));
        
        when(employeeService.getEmployeeByEmail(organizer.getEmail())).thenReturn(organizer);
        when(employeeService.employeeExistsByEmail(organizer.getEmail())).thenReturn(true);
        when(employeeService.employeeExistsByEmail(participant.getEmail())).thenReturn(true);
        when(meetingRepository.findConflictingMeetingsForParticipant(anyString(), any(), any()))
                .thenReturn(Arrays.asList(conflictingMeeting));
        
        // When & Then
        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> meetingService.bookMeeting(meetingRequest)
        );
        
        assertTrue(exception.getMessage().contains("conflicts"));
        verify(meetingRepository, never()).save(any());
    }
    
    @Test
    void getMeetingById_Success() {
        // Given
        Long meetingId = 1L;
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(testMeeting));
        
        // When
        Meeting result = meetingService.getMeetingById(meetingId);
        
        // Then
        assertNotNull(result);
        assertEquals(testMeeting.getId(), result.getId());
        assertEquals(testMeeting.getTitle(), result.getTitle());
        
        verify(meetingRepository).findById(meetingId);
    }
    
    @Test
    void getMeetingById_NotFound_ThrowsException() {
        // Given
        Long meetingId = 999L;
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> meetingService.getMeetingById(meetingId)
        );
        
        assertTrue(exception.getMessage().contains(meetingId.toString()));
        verify(meetingRepository).findById(meetingId);
    }
    
    @Test
    void checkConflicts_NoConflicts() {
        // Given
        ConflictCheckRequestDto conflictRequest = new ConflictCheckRequestDto();
        conflictRequest.setParticipantEmails(Arrays.asList(organizer.getEmail(), participant.getEmail()));
        conflictRequest.setStartTime(futureStart);
        conflictRequest.setEndTime(futureEnd);
        
        when(meetingRepository.findConflictingMeetingsForParticipant(anyString(), any(), any()))
                .thenReturn(Arrays.asList());
        
        // When
        ConflictResponseDto result = meetingService.checkConflicts(conflictRequest);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isHasConflicts());
        assertTrue(result.getMessage().contains("No conflicts found"));
        
        verify(meetingRepository, times(2)).findConflictingMeetingsForParticipant(anyString(), any(), any());
    }
    
    @Test
    void checkConflicts_WithConflicts() {
        // Given
        ConflictCheckRequestDto conflictRequest = new ConflictCheckRequestDto();
        conflictRequest.setParticipantEmails(Arrays.asList(organizer.getEmail()));
        conflictRequest.setStartTime(futureStart);
        conflictRequest.setEndTime(futureEnd);
        
        Meeting conflictingMeeting = new Meeting();
        conflictingMeeting.setId(2L);
        conflictingMeeting.setTitle("Conflicting Meeting");
        
        when(meetingRepository.findConflictingMeetingsForParticipant(
                eq(organizer.getEmail()), any(), any()))
                .thenReturn(Arrays.asList(conflictingMeeting));
        
        // When
        ConflictResponseDto result = meetingService.checkConflicts(conflictRequest);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isHasConflicts());
        assertTrue(result.getMessage().contains("Conflicts found"));
        assertNotNull(result.getConflictsByEmployee());
        assertTrue(result.getConflictsByEmployee().containsKey(organizer.getEmail()));
        
        verify(meetingRepository).findConflictingMeetingsForParticipant(
                eq(organizer.getEmail()), any(), any());
    }
    
    @Test
    void checkConflicts_WithExcludedMeeting() {
        // Given
        ConflictCheckRequestDto conflictRequest = new ConflictCheckRequestDto();
        conflictRequest.setParticipantEmails(Arrays.asList(organizer.getEmail()));
        conflictRequest.setStartTime(futureStart);
        conflictRequest.setEndTime(futureEnd);
        conflictRequest.setExcludeMeetingId(1L);
        
        when(meetingRepository.findConflictingMeetingsForParticipantExcludingMeeting(
                anyString(), any(), any(), any()))
                .thenReturn(Arrays.asList());
        
        // When
        ConflictResponseDto result = meetingService.checkConflicts(conflictRequest);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isHasConflicts());
        
        verify(meetingRepository).findConflictingMeetingsForParticipantExcludingMeeting(
                eq(organizer.getEmail()), any(), any(), eq(1L));
    }
    
    @Test
    void updateMeeting_Success() {
        // Given
        Long meetingId = 1L;
        MeetingRequestDto updateRequest = new MeetingRequestDto();
        updateRequest.setTitle("Updated Meeting");
        updateRequest.setDescription("Updated description");
        updateRequest.setStartTime(futureStart.plusDays(1));
        updateRequest.setEndTime(futureEnd.plusDays(1));
        updateRequest.setOrganizerEmail(organizer.getEmail());
        updateRequest.setParticipantEmails(Arrays.asList(participant.getEmail()));
        
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(testMeeting));
        when(employeeService.employeeExistsByEmail(organizer.getEmail())).thenReturn(true);
        when(employeeService.employeeExistsByEmail(participant.getEmail())).thenReturn(true);
        when(meetingRepository.findConflictingMeetingsForParticipantExcludingMeeting(
                anyString(), any(), any(), any()))
                .thenReturn(Arrays.asList());
        when(meetingRepository.save(any(Meeting.class))).thenReturn(testMeeting);
        
        // When
        Meeting result = meetingService.updateMeeting(meetingId, updateRequest);
        
        // Then
        assertNotNull(result);
        
        verify(meetingRepository).findById(meetingId);
        verify(meetingRepository).save(testMeeting);
    }
    
    @Test
    void cancelMeeting_Success() {
        // Given
        Long meetingId = 1L;
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(testMeeting));
        when(meetingRepository.save(any(Meeting.class))).thenReturn(testMeeting);
        
        // When
        Meeting result = meetingService.cancelMeeting(meetingId);
        
        // Then
        assertNotNull(result);
        
        verify(meetingRepository).findById(meetingId);
        verify(meetingRepository).save(testMeeting);
    }
    
    @Test
    void deleteMeeting_Success() {
        // Given
        Long meetingId = 1L;
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(testMeeting));
        
        // When
        meetingService.deleteMeeting(meetingId);
        
        // Then
        verify(meetingRepository).findById(meetingId);
        verify(meetingRepository).delete(testMeeting);
    }
    
    @Test
    void getMeetingsByOrganizerEmail_Success() {
        // Given
        String organizerEmail = organizer.getEmail();
        List<Meeting> meetings = Arrays.asList(testMeeting);
        when(meetingRepository.findByOrganizerEmail(organizerEmail)).thenReturn(meetings);
        
        // When
        List<Meeting> result = meetingService.getMeetingsByOrganizerEmail(organizerEmail);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMeeting, result.get(0));
        
        verify(meetingRepository).findByOrganizerEmail(organizerEmail);
    }
    
    @Test
    void getUpcomingMeetingsForEmployee_Success() {
        // Given
        String email = organizer.getEmail();
        List<Meeting> upcomingMeetings = Arrays.asList(testMeeting);
        when(meetingRepository.findUpcomingMeetingsForOrganizer(eq(email), any()))
                .thenReturn(upcomingMeetings);
        
        // When
        List<Meeting> result = meetingService.getUpcomingMeetingsForEmployee(email);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMeeting, result.get(0));
        
        verify(meetingRepository).findUpcomingMeetingsForOrganizer(eq(email), any());
    }
} 