package com.assessment.repository;

import com.assessment.model.Meeting;
import com.assessment.model.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Meeting entity operations
 * Provides CRUD operations and custom queries for meeting management and conflict detection
 */
@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    
    /**
     * Find meetings organized by a specific employee
     * @param organizerId the ID of the organizer
     * @return list of meetings organized by the employee
     */
    List<Meeting> findByOrganizerId(Long organizerId);
    
    /**
     * Find meetings by organizer email
     * @param organizerEmail the email of the organizer
     * @return list of meetings organized by the employee with the given email
     */
    @Query("SELECT m FROM Meeting m WHERE m.organizer.email = :organizerEmail")
    List<Meeting> findByOrganizerEmail(@Param("organizerEmail") String organizerEmail);
    
    /**
     * Find meetings by status
     * @param status the meeting status
     * @return list of meetings with the given status
     */
    List<Meeting> findByStatus(MeetingStatus status);
    
    /**
     * Find meetings in a specific time range
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of meetings that overlap with the time range
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.startTime <= :endTime AND m.endTime >= :startTime " +
           "ORDER BY m.startTime")
    List<Meeting> findMeetingsInTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find meetings organized by an employee in a specific time range
     * @param organizerEmail the organizer's email
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of meetings by the organizer in the time range
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.organizer.email = :organizerEmail " +
           "AND m.startTime <= :endTime AND m.endTime >= :startTime " +
           "ORDER BY m.startTime")
    List<Meeting> findByOrganizerEmailAndTimeRange(
            @Param("organizerEmail") String organizerEmail,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find meetings where a participant has conflicts in a time range
     * @param participantEmail the participant's email
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of conflicting meetings for the participant
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE (:participantEmail MEMBER OF m.participantEmails OR m.organizer.email = :participantEmail) " +
           "AND m.startTime <= :endTime AND m.endTime >= :startTime " +
           "AND m.status != 'CANCELLED' " +
           "ORDER BY m.startTime")
    List<Meeting> findConflictingMeetingsForParticipant(
            @Param("participantEmail") String participantEmail,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find meetings where a participant has conflicts, excluding a specific meeting
     * @param participantEmail the participant's email
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @param excludeMeetingId the meeting ID to exclude from the search
     * @return list of conflicting meetings for the participant
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE (:participantEmail MEMBER OF m.participantEmails OR m.organizer.email = :participantEmail) " +
           "AND m.startTime <= :endTime AND m.endTime >= :startTime " +
           "AND m.status != 'CANCELLED' " +
           "AND m.id != :excludeMeetingId " +
           "ORDER BY m.startTime")
    List<Meeting> findConflictingMeetingsForParticipantExcludingMeeting(
            @Param("participantEmail") String participantEmail,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeMeetingId") Long excludeMeetingId);
    
    /**
     * Find all meetings for multiple participants in a time range
     * @param participantEmails list of participant emails
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of meetings involving any of the participants
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE EXISTS (SELECT 1 FROM Employee e WHERE e.email IN :participantEmails " +
           "AND (e = m.organizer OR e.email MEMBER OF m.participantEmails)) " +
           "AND m.startTime <= :endTime AND m.endTime >= :startTime " +
           "AND m.status != 'CANCELLED' " +
           "ORDER BY m.startTime")
    List<Meeting> findMeetingsForParticipantsInTimeRange(
            @Param("participantEmails") List<String> participantEmails,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find meetings for a participant in a date range
     * @param participantEmail the participant's email
     * @param startDate the start date
     * @param endDate the end date
     * @return list of meetings for the participant in the date range
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE (:participantEmail MEMBER OF m.participantEmails OR m.organizer.email = :participantEmail) " +
           "AND m.startTime >= :startDate AND m.endTime <= :endDate " +
           "AND m.status != 'CANCELLED' " +
           "ORDER BY m.startTime")
    List<Meeting> findMeetingsForParticipantInDateRange(
            @Param("participantEmail") String participantEmail,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find upcoming meetings for an organizer
     * @param organizerEmail the organizer's email
     * @param now the current date/time
     * @return list of upcoming meetings
     */
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.organizer.email = :organizerEmail " +
           "AND m.startTime > :now " +
           "AND m.status = 'SCHEDULED' " +
           "ORDER BY m.startTime")
    List<Meeting> findUpcomingMeetingsForOrganizer(
            @Param("organizerEmail") String organizerEmail,
            @Param("now") LocalDateTime now);
    
    /**
     * Count active meetings for an organizer in a time range
     * @param organizerEmail the organizer's email
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return count of active meetings
     */
    @Query("SELECT COUNT(m) FROM Meeting m " +
           "WHERE m.organizer.email = :organizerEmail " +
           "AND m.startTime <= :endTime AND m.endTime >= :startTime " +
           "AND m.status != 'CANCELLED'")
    long countActiveMeetingsForOrganizerInTimeRange(
            @Param("organizerEmail") String organizerEmail,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
} 