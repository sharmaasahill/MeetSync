package com.assessment.config;

import com.assessment.model.Employee;
import com.assessment.model.Meeting;
import com.assessment.model.MeetingStatus;
import com.assessment.repository.EmployeeRepository;
import com.assessment.repository.MeetingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Data initializer to populate the database with sample data
 * This runs on application startup and creates demo employees and meetings
 */
@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {
    
    private final EmployeeRepository employeeRepository;
    private final MeetingRepository meetingRepository;
    
    @Autowired
    public DataInitializer(EmployeeRepository employeeRepository, MeetingRepository meetingRepository) {
        this.employeeRepository = employeeRepository;
        this.meetingRepository = meetingRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        if (employeeRepository.count() == 0) {
            initializeData();
        }
    }
    
    private void initializeData() {
        // Create sample employees
        Employee rahulMehta = new Employee("Rahul Mehta", "rahul.mehta@company.com", "Engineering", "Senior Developer");
        Employee priyaPatel = new Employee("Priya Patel", "priya.patel@company.com", "Marketing", "Product Manager");
        Employee amitVerma = new Employee("Amit Verma", "amit.verma@company.com", "Sales", "Sales Manager");
        Employee snehaIyer = new Employee("Sneha Iyer", "sneha.iyer@company.com", "HR", "HR Specialist");
        Employee vikramReddy = new Employee("Vikram Reddy", "vikram.reddy@company.com", "Engineering", "DevOps Engineer");
        
        // Save employees
        rahulMehta = employeeRepository.save(rahulMehta);
        priyaPatel = employeeRepository.save(priyaPatel);
        amitVerma = employeeRepository.save(amitVerma);
        snehaIyer = employeeRepository.save(snehaIyer);
        vikramReddy = employeeRepository.save(vikramReddy);
        
        // Create sample meetings for demonstration
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        
        // Morning standup
        Meeting standup = new Meeting();
        standup.setTitle("Daily Standup");
        standup.setDescription("Engineering team daily standup meeting");
        standup.setStartTime(tomorrow.withHour(9).withMinute(0).withSecond(0).withNano(0));
        standup.setEndTime(tomorrow.withHour(9).withMinute(30).withSecond(0).withNano(0));
        standup.setOrganizer(rahulMehta);
        standup.setParticipantEmails(Arrays.asList(vikramReddy.getEmail()));
        standup.setLocation("Engineering Conference Room");
        standup.setStatus(MeetingStatus.SCHEDULED);
        
        // Product planning meeting
        Meeting productPlanning = new Meeting();
        productPlanning.setTitle("Product Planning Session");
        productPlanning.setDescription("Monthly product planning and roadmap discussion");
        productPlanning.setStartTime(tomorrow.withHour(14).withMinute(0).withSecond(0).withNano(0));
        productPlanning.setEndTime(tomorrow.withHour(16).withMinute(0).withSecond(0).withNano(0));
        productPlanning.setOrganizer(priyaPatel);
        productPlanning.setParticipantEmails(Arrays.asList(rahulMehta.getEmail(), amitVerma.getEmail()));
        productPlanning.setLocation("Main Conference Room");
        productPlanning.setStatus(MeetingStatus.SCHEDULED);
        
        // HR onboarding session
        Meeting hrOnboarding = new Meeting();
        hrOnboarding.setTitle("New Employee Onboarding");
        hrOnboarding.setDescription("Onboarding session for new team members");
        hrOnboarding.setStartTime(tomorrow.plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0));
        hrOnboarding.setEndTime(tomorrow.plusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(0));
        hrOnboarding.setOrganizer(snehaIyer);
        hrOnboarding.setParticipantEmails(Arrays.asList());
        hrOnboarding.setLocation("HR Training Room");
        hrOnboarding.setStatus(MeetingStatus.SCHEDULED);
        
        // Save meetings
        meetingRepository.save(standup);
        meetingRepository.save(productPlanning);
        meetingRepository.save(hrOnboarding);
        
        System.out.println("Sample data initialized successfully");
        System.out.println("Created " + employeeRepository.count() + " employees");
        System.out.println("Created " + meetingRepository.count() + " meetings");
        System.out.println("API available at: http://localhost:8080/api");
        System.out.println("H2 Console available at: http://localhost:8080/api/h2-console");
    }
}
