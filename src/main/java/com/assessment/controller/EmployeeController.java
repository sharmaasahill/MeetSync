package com.assessment.controller;

import com.assessment.dto.ApiResponse;
import com.assessment.model.Employee;
import com.assessment.service.EmployeeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

/**
 * REST Controller for Employee management operations
 * Provides CRUD endpoints for employee operations
 */
@RestController
@RequestMapping("/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    
    /**
     * Create a new employee
     * POST /api/employees
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Employee>> createEmployee(@Valid @RequestBody Employee employee) {
        Employee createdEmployee = employeeService.createEmployee(employee);
        ApiResponse<Employee> response = ApiResponse.success("Employee created successfully", createdEmployee);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Get employee by ID
     * GET /api/employees/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Employee>> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        ApiResponse<Employee> response = ApiResponse.success(employee);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get employee by email
     * GET /api/employees/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<Employee>> getEmployeeByEmail(@PathVariable String email) {
        Employee employee = employeeService.getEmployeeByEmail(email);
        ApiResponse<Employee> response = ApiResponse.success(employee);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all employees
     * GET /api/employees
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Employee>>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        ApiResponse<List<Employee>> response = ApiResponse.success("Retrieved all employees", employees);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update an employee
     * PUT /api/employees/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Employee>> updateEmployee(@PathVariable Long id, 
                                                               @Valid @RequestBody Employee employee) {
        Employee updatedEmployee = employeeService.updateEmployee(id, employee);
        ApiResponse<Employee> response = ApiResponse.success("Employee updated successfully", updatedEmployee);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete an employee
     * DELETE /api/employees/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        ApiResponse<Void> response = ApiResponse.success("Employee deleted successfully", null);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Search employees by name
     * GET /api/employees/search?name={name}
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Employee>>> searchEmployeesByName(@RequestParam String name) {
        List<Employee> employees = employeeService.searchEmployeesByName(name);
        ApiResponse<List<Employee>> response = ApiResponse.success("Search results for: " + name, employees);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get employees by department
     * GET /api/employees/department/{department}
     */
    @GetMapping("/department/{department}")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesByDepartment(@PathVariable String department) {
        List<Employee> employees = employeeService.getEmployeesByDepartment(department);
        ApiResponse<List<Employee>> response = ApiResponse.success("Employees in " + department + " department", employees);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get employees by designation
     * GET /api/employees/designation/{designation}
     */
    @GetMapping("/designation/{designation}")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesByDesignation(@PathVariable String designation) {
        List<Employee> employees = employeeService.getEmployeesByDesignation(designation);
        ApiResponse<List<Employee>> response = ApiResponse.success("Employees with " + designation + " designation", employees);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get employees by department and designation
     * GET /api/employees/filter?department={dept}&designation={desg}
     */
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesByDepartmentAndDesignation(
            @RequestParam String department, 
            @RequestParam String designation) {
        List<Employee> employees = employeeService.getEmployeesByDepartmentAndDesignation(department, designation);
        ApiResponse<List<Employee>> response = ApiResponse.success("Filtered employees", employees);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if employee exists by email
     * GET /api/employees/exists/{email}
     */
    @GetMapping("/exists/{email}")
    public ResponseEntity<ApiResponse<Boolean>> checkEmployeeExists(@PathVariable String email) {
        boolean exists = employeeService.employeeExistsByEmail(email);
        ApiResponse<Boolean> response = ApiResponse.success("Employee existence check", exists);
        return ResponseEntity.ok(response);
    }
} 