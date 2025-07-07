package com.assessment.integration;

import com.assessment.MeetingCalendarAssistantApplication;
import com.assessment.dto.ApiResponse;
import com.assessment.dto.FreeSlotRequestDto;
import com.assessment.dto.MeetingRequestDto;
import com.assessment.dto.TimeSlot;
import com.assessment.model.Employee;
import com.assessment.model.Meeting;
import com.assessment.repository.EmployeeRepository;
import com.assessment.repository.MeetingRepository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for the Meeting Calendar Assistant application
 * Tests the complete functionality from REST API to database
 */
@SpringBootTest(classes = MeetingCalendarAssistantApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class MeetingCalendarIntegrationTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private MeetingRepository meetingRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private MockMvc mockMvc;
    private Employee employee1;
    private Employee employee2;
    private LocalDateTime futureStart;
    private LocalDateTime futureEnd;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultRequest(get("/").contextPath("/api"))
                .build();
        
        // Clean up database
        meetingRepository.deleteAll();
        employeeRepository.deleteAll();
        
        // Create test employees with unique emails to avoid conflicts
        employee1 = new Employee("Test John", "test.john@example.com", "Engineering", "Developer");
        employee2 = new Employee("Test Jane", "test.jane@example.com", "Marketing", "Manager");
        
        employee1 = employeeRepository.save(employee1);
        employee2 = employeeRepository.save(employee2);
        
        // Set up future times for testing
        futureStart = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        futureEnd = futureStart.plusHours(1);
    }
    
    @Test
    void createEmployee_Success() throws Exception {
        // Given
        Employee newEmployee = new Employee("Test Bob", "test.bob@example.com", "Sales", "Representative");
        
        // When & Then
        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEmployee)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Bob"))
                .andExpect(jsonPath("$.data.email").value("test.bob@example.com"))
                .andExpect(jsonPath("$.data.department").value("Sales"))
                .andExpect(jsonPath("$.data.designation").value("Representative"))
                .andReturn();
        
        // Verify employee was saved to database
        assertTrue(employeeRepository.existsByEmail("test.bob@example.com"));
    }
    
    @Test
    void bookMeeting_Success() throws Exception {
        // Given
        MeetingRequestDto meetingRequest = new MeetingRequestDto();
        meetingRequest.setTitle("Team Meeting");
        meetingRequest.setDescription("Weekly sync");
        meetingRequest.setStartTime(futureStart);
        meetingRequest.setEndTime(futureEnd);
        meetingRequest.setOrganizerEmail(employee1.getEmail());
        meetingRequest.setParticipantEmails(Arrays.asList(employee2.getEmail()));
        meetingRequest.setLocation("Conference Room A");
        
        // When & Then
        MvcResult result = mockMvc.perform(post("/api/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(meetingRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Team Meeting"))
// Note: organizer field is excluded due to @JsonBackReference to avoid circular references
                .andExpect(jsonPath("$.data.location").value("Conference Room A"))
                .andReturn();
        
        // Verify meeting was saved to database
        List<Meeting> meetings = meetingRepository.findByOrganizerEmail(employee1.getEmail());
        assertEquals(1, meetings.size());
        assertEquals("Team Meeting", meetings.get(0).getTitle());
    }
    
    @Test
    void findFreeSlots_Success() throws Exception {
        // Given
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime searchStart = futureStart.withHour(9);
        LocalDateTime searchEnd = futureStart.withHour(18);
        
        FreeSlotRequestDto freeSlotRequest = new FreeSlotRequestDto();
        freeSlotRequest.setEmployee1Email(employee1.getEmail());
        freeSlotRequest.setEmployee2Email(employee2.getEmail());
        freeSlotRequest.setStartDate(searchStart);
        freeSlotRequest.setEndDate(searchEnd);
        freeSlotRequest.setMeetingDurationMinutes(30);
        
        // When & Then
        MvcResult result = mockMvc.perform(post("/api/calendar/free-slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(freeSlotRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        
        // Parse response
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<List<TimeSlot>> response = objectMapper.readValue(
                responseContent, 
                new TypeReference<ApiResponse<List<TimeSlot>>>() {}
        );
        
        // Verify free slots were found
        assertNotNull(response.getData());
        assertTrue(response.getData().size() > 0);
        
        // Verify each slot is 30 minutes
        for (TimeSlot slot : response.getData()) {
            assertEquals(30, slot.getDurationInMinutes());
        }
    }
    
    @Test
    void findFreeSlots_WithExistingMeeting_ReturnsCorrectSlots() throws Exception {
        // Given - Create a meeting that blocks some time
        MeetingRequestDto meetingRequest = new MeetingRequestDto();
        meetingRequest.setTitle("Blocking Meeting");
        meetingRequest.setStartTime(futureStart.withHour(10));
        meetingRequest.setEndTime(futureStart.withHour(11));
        meetingRequest.setOrganizerEmail(employee1.getEmail());
        meetingRequest.setParticipantEmails(Arrays.asList(employee2.getEmail()));
        
        // Book the meeting first
        mockMvc.perform(post("/api/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(meetingRequest)))
                .andExpect(status().isCreated());
        
        // Now search for free slots
        FreeSlotRequestDto freeSlotRequest = new FreeSlotRequestDto();
        freeSlotRequest.setEmployee1Email(employee1.getEmail());
        freeSlotRequest.setEmployee2Email(employee2.getEmail());
        freeSlotRequest.setStartDate(futureStart.withHour(9));
        freeSlotRequest.setEndDate(futureStart.withHour(18));
        freeSlotRequest.setMeetingDurationMinutes(30);
        
        // When & Then
        MvcResult result = mockMvc.perform(post("/api/calendar/free-slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(freeSlotRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        
        // Parse and verify response
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<List<TimeSlot>> response = objectMapper.readValue(
                responseContent, 
                new TypeReference<ApiResponse<List<TimeSlot>>>() {}
        );
        
        // Verify free slots don't conflict with the existing meeting
        assertNotNull(response.getData());
        LocalDateTime meetingStart = futureStart.withHour(10);
        LocalDateTime meetingEnd = futureStart.withHour(11);
        
        for (TimeSlot slot : response.getData()) {
            // Ensure no overlap with the existing meeting
            assertFalse(slot.getStartTime().isBefore(meetingEnd) && slot.getEndTime().isAfter(meetingStart),
                    "Free slot should not overlap with existing meeting");
        }
    }
    
    @Test
    void checkConflicts_WithConflictingMeeting_ReturnsConflicts() throws Exception {
        // Given - Create an existing meeting
        MeetingRequestDto existingMeeting = new MeetingRequestDto();
        existingMeeting.setTitle("Existing Meeting");
        existingMeeting.setStartTime(futureStart.withHour(10));
        existingMeeting.setEndTime(futureStart.withHour(11));
        existingMeeting.setOrganizerEmail(employee1.getEmail());
        
        // Book the existing meeting
        mockMvc.perform(post("/api/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingMeeting)))
                .andExpect(status().isCreated());
        
        // Create a conflicting meeting request
        String conflictRequestJson = """
            {
                "participantEmails": ["%s"],
                "startTime": "%s",
                "endTime": "%s"
            }
            """.formatted(
                employee1.getEmail(),
                futureStart.withHour(10).withMinute(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                futureStart.withHour(11).withMinute(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
        
        // When & Then
        mockMvc.perform(post("/api/meetings/conflicts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conflictRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hasConflicts").value(true))
                .andExpect(jsonPath("$.data.conflictsByEmployee").exists());
    }
    
    @Test
    void getEmployeeByEmail_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/employees/email/{email}", employee1.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value(employee1.getName()))
                .andExpect(jsonPath("$.data.email").value(employee1.getEmail()))
                .andExpect(jsonPath("$.data.department").value(employee1.getDepartment()));
    }
    
    @Test
    void getAllEmployees_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }
    
    @Test
    void endToEndWorkflow_BookMeetingAndFindSlots() throws Exception {
        // Step 1: Create additional employee
        Employee employee3 = new Employee("Test Alice", "test.alice@example.com", "HR", "Specialist");
        employee3 = employeeRepository.save(employee3);
        
        // Step 2: Book a meeting between employee1 and employee2
        MeetingRequestDto meeting1 = new MeetingRequestDto();
        meeting1.setTitle("Morning Standup");
        meeting1.setStartTime(futureStart.withHour(9));
        meeting1.setEndTime(futureStart.withHour(9).plusMinutes(30));
        meeting1.setOrganizerEmail(employee1.getEmail());
        meeting1.setParticipantEmails(Arrays.asList(employee2.getEmail()));
        
        mockMvc.perform(post("/api/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(meeting1)))
                .andExpect(status().isCreated());
        
        // Step 3: Book another meeting for employee2 and employee3
        MeetingRequestDto meeting2 = new MeetingRequestDto();
        meeting2.setTitle("HR Discussion");
        meeting2.setStartTime(futureStart.withHour(14));
        meeting2.setEndTime(futureStart.withHour(15));
        meeting2.setOrganizerEmail(employee2.getEmail());
        meeting2.setParticipantEmails(Arrays.asList(employee3.getEmail()));
        
        mockMvc.perform(post("/api/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(meeting2)))
                .andExpect(status().isCreated());
        
        // Step 4: Find free slots between employee1 and employee3 (should be available)
        FreeSlotRequestDto freeSlotRequest = new FreeSlotRequestDto();
        freeSlotRequest.setEmployee1Email(employee1.getEmail());
        freeSlotRequest.setEmployee2Email(employee3.getEmail());
        freeSlotRequest.setStartDate(futureStart.withHour(9));
        freeSlotRequest.setEndDate(futureStart.withHour(18));
        freeSlotRequest.setMeetingDurationMinutes(60);
        
        MvcResult result = mockMvc.perform(post("/api/calendar/free-slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(freeSlotRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        
        // Verify multiple free slots are available
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<List<TimeSlot>> response = objectMapper.readValue(
                responseContent, 
                new TypeReference<ApiResponse<List<TimeSlot>>>() {}
        );
        
        assertNotNull(response.getData());
        assertTrue(response.getData().size() > 0);
        
        // Step 5: Verify the meetings exist in the database
        List<Meeting> allMeetings = meetingRepository.findAll();
        assertEquals(2, allMeetings.size());
    }
} 