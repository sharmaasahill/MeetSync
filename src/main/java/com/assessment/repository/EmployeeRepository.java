package com.assessment.repository;

import com.assessment.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Employee entity operations
 * Provides CRUD operations and custom queries for employee management
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    /**
     * Find employee by email address
     * @param email the email address to search for
     * @return Optional containing the employee if found
     */
    Optional<Employee> findByEmail(String email);
    
    /**
     * Find employees by department
     * @param department the department name
     * @return list of employees in the department
     */
    List<Employee> findByDepartment(String department);
    
    /**
     * Find employees by designation
     * @param designation the designation/position
     * @return list of employees with the designation
     */
    List<Employee> findByDesignation(String designation);
    
    /**
     * Find employees whose names contain the search term (case-insensitive)
     * @param name the name pattern to search for
     * @return list of matching employees
     */
    List<Employee> findByNameContainingIgnoreCase(String name);
    
    /**
     * Check if an employee exists with the given email
     * @param email the email to check
     * @return true if employee exists with the email
     */
    boolean existsByEmail(String email);
    
    /**
     * Find employees by department and designation
     * @param department the department name
     * @param designation the designation
     * @return list of employees matching both criteria
     */
    List<Employee> findByDepartmentAndDesignation(String department, String designation);
    
    /**
     * Custom query to find employees with at least one meeting in the given time range
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of employees with meetings in the time range
     */
    @Query("SELECT DISTINCT e FROM Employee e " +
           "JOIN e.organizedMeetings m " +
           "WHERE m.startTime <= :endTime AND m.endTime >= :startTime")
    List<Employee> findEmployeesWithMeetingsInTimeRange(
            @Param("startTime") java.time.LocalDateTime startTime,
            @Param("endTime") java.time.LocalDateTime endTime);
    
    /**
     * Custom query to find employees who are participants in meetings
     * @param participantEmail the email of the participant
     * @return list of employees who have meetings where the given email is a participant
     */
    @Query("SELECT DISTINCT e FROM Employee e " +
           "JOIN e.organizedMeetings m " +
           "WHERE :participantEmail MEMBER OF m.participantEmails")
    List<Employee> findEmployeesWithParticipant(@Param("participantEmail") String participantEmail);
} 