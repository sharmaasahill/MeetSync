package com.assessment.service;

import com.assessment.exception.DuplicateResourceException;
import com.assessment.exception.ResourceNotFoundException;
import com.assessment.model.Employee;
import com.assessment.repository.EmployeeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeService
 * Tests all business logic scenarios including edge cases and error conditions
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    
    @Mock
    private EmployeeRepository employeeRepository;
    
    @InjectMocks
    private EmployeeService employeeService;
    
    private Employee testEmployee;
    private Employee existingEmployee;
    
    @BeforeEach
    void setUp() {
        testEmployee = new Employee("Rahul Mehta", "rahul.mehta@company.com", "Engineering", "Developer");
        testEmployee.setId(1L);
        
        existingEmployee = new Employee("Priya Patel", "priya.patel@company.com", "HR", "Manager");
        existingEmployee.setId(2L);
    }
    
    @Test
    void createEmployee_Success() {
        // Given
        when(employeeRepository.existsByEmail(testEmployee.getEmail())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        
        // When
        Employee result = employeeService.createEmployee(testEmployee);
        
        // Then
        assertNotNull(result);
        assertEquals(testEmployee.getName(), result.getName());
        assertEquals(testEmployee.getEmail(), result.getEmail());
        
        verify(employeeRepository).existsByEmail(testEmployee.getEmail());
        verify(employeeRepository).save(testEmployee);
    }
    
    @Test
    void createEmployee_DuplicateEmail_ThrowsException() {
        // Given
        when(employeeRepository.existsByEmail(testEmployee.getEmail())).thenReturn(true);
        
        // When & Then
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> employeeService.createEmployee(testEmployee)
        );
        
        assertTrue(exception.getMessage().contains(testEmployee.getEmail()));
        verify(employeeRepository).existsByEmail(testEmployee.getEmail());
        verify(employeeRepository, never()).save(any());
    }
    
    @Test
    void getEmployeeById_Success() {
        // Given
        Long employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        
        // When
        Employee result = employeeService.getEmployeeById(employeeId);
        
        // Then
        assertNotNull(result);
        assertEquals(testEmployee.getId(), result.getId());
        assertEquals(testEmployee.getName(), result.getName());
        
        verify(employeeRepository).findById(employeeId);
    }
    
    @Test
    void getEmployeeById_NotFound_ThrowsException() {
        // Given
        Long employeeId = 999L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.getEmployeeById(employeeId)
        );
        
        assertTrue(exception.getMessage().contains(employeeId.toString()));
        verify(employeeRepository).findById(employeeId);
    }
    
    @Test
    void getEmployeeByEmail_Success() {
        // Given
        String email = testEmployee.getEmail();
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(testEmployee));
        
        // When
        Employee result = employeeService.getEmployeeByEmail(email);
        
        // Then
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        
        verify(employeeRepository).findByEmail(email);
    }
    
    @Test
    void getEmployeeByEmail_NotFound_ThrowsException() {
        // Given
        String email = "nonexistent@company.com";
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.getEmployeeByEmail(email)
        );
        
        assertTrue(exception.getMessage().contains(email));
        verify(employeeRepository).findByEmail(email);
    }
    
    @Test
    void getAllEmployees_Success() {
        // Given
        List<Employee> employees = Arrays.asList(testEmployee, existingEmployee);
        when(employeeRepository.findAll()).thenReturn(employees);
        
        // When
        List<Employee> result = employeeService.getAllEmployees();
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testEmployee));
        assertTrue(result.contains(existingEmployee));
        
        verify(employeeRepository).findAll();
    }
    
    @Test
    void updateEmployee_Success() {
        // Given
        Long employeeId = 1L;
        Employee updatedData = new Employee("Rahul Updated", "rahul.updated@company.com", "Engineering", "Senior Developer");
        
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.existsByEmail(updatedData.getEmail())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        
        // When
        Employee result = employeeService.updateEmployee(employeeId, updatedData);
        
        // Then
        assertNotNull(result);
        
        verify(employeeRepository).findById(employeeId);
        verify(employeeRepository).existsByEmail(updatedData.getEmail());
        verify(employeeRepository).save(testEmployee);
    }
    
    @Test
    void updateEmployee_SameEmail_Success() {
        // Given
        Long employeeId = 1L;
        Employee updatedData = new Employee("Rahul Updated", testEmployee.getEmail(), "Engineering", "Senior Developer");
        
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        
        // When
        Employee result = employeeService.updateEmployee(employeeId, updatedData);
        
        // Then
        assertNotNull(result);
        
        verify(employeeRepository).findById(employeeId);
        verify(employeeRepository, never()).existsByEmail(any());
        verify(employeeRepository).save(testEmployee);
    }
    
    @Test
    void updateEmployee_DuplicateEmail_ThrowsException() {
        // Given
        Long employeeId = 1L;
        Employee updatedData = new Employee("Rahul Updated", "existing@company.com", "Engineering", "Senior Developer");
        
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.existsByEmail(updatedData.getEmail())).thenReturn(true);
        
        // When & Then
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> employeeService.updateEmployee(employeeId, updatedData)
        );
        
        assertTrue(exception.getMessage().contains(updatedData.getEmail()));
        verify(employeeRepository).findById(employeeId);
        verify(employeeRepository).existsByEmail(updatedData.getEmail());
        verify(employeeRepository, never()).save(any());
    }
    
    @Test
    void deleteEmployee_Success() {
        // Given
        Long employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(testEmployee));
        
        // When
        employeeService.deleteEmployee(employeeId);
        
        // Then
        verify(employeeRepository).findById(employeeId);
        verify(employeeRepository).delete(testEmployee);
    }
    
    @Test
    void deleteEmployee_NotFound_ThrowsException() {
        // Given
        Long employeeId = 999L;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.deleteEmployee(employeeId)
        );
        
        assertTrue(exception.getMessage().contains(employeeId.toString()));
        verify(employeeRepository).findById(employeeId);
        verify(employeeRepository, never()).delete(any());
    }
    
    @Test
    void getEmployeesByDepartment_Success() {
        // Given
        String department = "Engineering";
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByDepartment(department)).thenReturn(employees);
        
        // When
        List<Employee> result = employeeService.getEmployeesByDepartment(department);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEmployee, result.get(0));
        
        verify(employeeRepository).findByDepartment(department);
    }
    
    @Test
    void searchEmployeesByName_Success() {
        // Given
        String namePattern = "Rahul";
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findByNameContainingIgnoreCase(namePattern)).thenReturn(employees);
        
        // When
        List<Employee> result = employeeService.searchEmployeesByName(namePattern);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEmployee, result.get(0));
        
        verify(employeeRepository).findByNameContainingIgnoreCase(namePattern);
    }
    
    @Test
    void employeeExistsByEmail_True() {
        // Given
        String email = testEmployee.getEmail();
        when(employeeRepository.existsByEmail(email)).thenReturn(true);
        
        // When
        boolean result = employeeService.employeeExistsByEmail(email);
        
        // Then
        assertTrue(result);
        verify(employeeRepository).existsByEmail(email);
    }
    
    @Test
    void employeeExistsByEmail_False() {
        // Given
        String email = "nonexistent@company.com";
        when(employeeRepository.existsByEmail(email)).thenReturn(false);
        
        // When
        boolean result = employeeService.employeeExistsByEmail(email);
        
        // Then
        assertFalse(result);
        verify(employeeRepository).existsByEmail(email);
    }
} 