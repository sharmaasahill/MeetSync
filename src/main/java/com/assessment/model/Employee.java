package com.assessment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Employee entity representing a calendar owner
 * Each employee owns their calendar and can participate in meetings
 */
@Entity
@Table(name = "employees")
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Employee name is required")
    @Size(min = 2, max = 100, message = "Employee name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;
    
    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    
    @Column(length = 100)
    private String department;
    
    @Column(length = 100)
    private String designation;
    
    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Meeting> organizedMeetings = new ArrayList<>();
    
    // Constructors
    public Employee() {}
    
    public Employee(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    public Employee(String name, String email, String department, String designation) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.designation = designation;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getDesignation() {
        return designation;
    }
    
    public void setDesignation(String designation) {
        this.designation = designation;
    }
    
    public List<Meeting> getOrganizedMeetings() {
        return organizedMeetings;
    }
    
    public void setOrganizedMeetings(List<Meeting> organizedMeetings) {
        this.organizedMeetings = organizedMeetings;
    }
    
    // Helper methods
    public void addOrganizedMeeting(Meeting meeting) {
        organizedMeetings.add(meeting);
        meeting.setOrganizer(this);
    }
    
    public void removeOrganizedMeeting(Meeting meeting) {
        organizedMeetings.remove(meeting);
        meeting.setOrganizer(null);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id) && 
               Objects.equals(email, employee.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
    
    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", designation='" + designation + '\'' +
                '}';
    }
} 