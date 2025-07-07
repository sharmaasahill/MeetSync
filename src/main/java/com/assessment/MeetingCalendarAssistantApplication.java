package com.assessment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application class for Meeting Calendar Assistant
 * 
 * This application provides REST APIs for:
 * 1. Booking meetings with calendar owners
 * 2. Finding free slots between employee calendars
 * 3. Detecting meeting conflicts for participants
 */
@SpringBootApplication
public class MeetingCalendarAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeetingCalendarAssistantApplication.class, args);
    }
} 