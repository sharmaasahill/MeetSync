package com.assessment.service;

import com.assessment.model.Employee;
import com.assessment.repository.EmployeeRepository;
import com.assessment.exception.ResourceNotFoundException;
import com.assessment.exception.DuplicateResourceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Employee business logic
 * Implements employee management operations with proper validation and error handling
 */
@Service
@Transactional
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    
    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    /**
     * Create a new employee
     * @param employee the employee to create
     * @return the created employee
     * @throws DuplicateResourceException if email already exists
     */
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new DuplicateResourceException("Employee with email " + employee.getEmail() + " already exists");
        }
        
        return employeeRepository.save(employee);
    }
    
    /**
     * Get employee by ID
     * @param id the employee ID
     * @return the employee
     * @throws ResourceNotFoundException if employee not found
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }
    
    /**
     * Get employee by email
     * @param email the employee email
     * @return the employee
     * @throws ResourceNotFoundException if employee not found
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));
    }
    
    /**
     * Get all employees
     * @return list of all employees
     */
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    /**
     * Update an existing employee
     * @param id the employee ID
     * @param updatedEmployee the updated employee data
     * @return the updated employee
     * @throws ResourceNotFoundException if employee not found
     * @throws DuplicateResourceException if email already exists for another employee
     */
    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        Employee existingEmployee = getEmployeeById(id);
        
        // Check if email is being changed and if it already exists
        if (!existingEmployee.getEmail().equals(updatedEmployee.getEmail())) {
            if (employeeRepository.existsByEmail(updatedEmployee.getEmail())) {
                throw new DuplicateResourceException("Employee with email " + updatedEmployee.getEmail() + " already exists");
            }
        }
        
        // Update fields
        existingEmployee.setName(updatedEmployee.getName());
        existingEmployee.setEmail(updatedEmployee.getEmail());
        existingEmployee.setDepartment(updatedEmployee.getDepartment());
        existingEmployee.setDesignation(updatedEmployee.getDesignation());
        
        return employeeRepository.save(existingEmployee);
    }
    
    /**
     * Delete an employee
     * @param id the employee ID
     * @throws ResourceNotFoundException if employee not found
     */
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employeeRepository.delete(employee);
    }
    
    /**
     * Find employees by department
     * @param department the department name
     * @return list of employees in the department
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }
    
    /**
     * Find employees by designation
     * @param designation the designation
     * @return list of employees with the designation
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDesignation(String designation) {
        return employeeRepository.findByDesignation(designation);
    }
    
    /**
     * Search employees by name (case-insensitive partial match)
     * @param name the name to search for
     * @return list of matching employees
     */
    @Transactional(readOnly = true)
    public List<Employee> searchEmployeesByName(String name) {
        return employeeRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Find employees by department and designation
     * @param department the department name
     * @param designation the designation
     * @return list of employees matching both criteria
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartmentAndDesignation(String department, String designation) {
        return employeeRepository.findByDepartmentAndDesignation(department, designation);
    }
    
    /**
     * Check if an employee exists with the given email
     * @param email the email to check
     * @return true if employee exists
     */
    @Transactional(readOnly = true)
    public boolean employeeExistsByEmail(String email) {
        return employeeRepository.existsByEmail(email);
    }
    
    /**
     * Find employee by email (returns Optional)
     * @param email the email to search for
     * @return Optional containing the employee if found
     */
    @Transactional(readOnly = true)
    public Optional<Employee> findEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }
    
    /**
     * Get employees with meetings in a specific time range
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of employees with meetings in the time range
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesWithMeetingsInTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return employeeRepository.findEmployeesWithMeetingsInTimeRange(startTime, endTime);
    }
    
    /**
     * Validate employee data
     * @param employee the employee to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateEmployee(Employee employee) {
        if (employee.getName() == null || employee.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name is required");
        }
        
        if (employee.getEmail() == null || employee.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee email is required");
        }
        
        // Additional email format validation could be added here
        if (!employee.getEmail().contains("@")) {
            throw new IllegalArgumentException("Valid email format is required");
        }
    }
} 