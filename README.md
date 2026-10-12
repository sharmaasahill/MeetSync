# MeetSync - Meeting Calendar Assistant

A Spring Boot REST API for managing employee calendars, booking meetings, and finding available time slots with automatic conflict detection.

## What Does It Do?

MeetSync helps organizations coordinate meetings by:
- Managing employee profiles and calendars
- Booking meetings with multiple participants
- Detecting scheduling conflicts automatically
- Finding free time slots between employees
- Enforcing business hours (9 AM - 6 PM)

## Quick Start

### Prerequisites
- Java 17 or higher ([Download here](https://adoptium.net/temurin/releases/?version=17))

### Running the Application

```bash
# Clone the repository
git clone https://github.com/sharmaasahill/MeetSync.git
cd MeetSync

# Run the application (no Maven installation needed!)
./mvnw spring-boot:run
```

Wait until you see:
```
Started MeetingCalendarAssistantApplication
✓ Created 5 employees
✓ Created 3 meetings
✓ API available at: http://localhost:8080/api
```

The application is now running with sample data pre-loaded!

### Testing the API

**Using Browser (Easiest):**
- View employees: http://localhost:8080/api/employees
- View meetings: http://localhost:8080/api/meetings
- Database console: http://localhost:8080/api/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: `password`

**Using Command Line:**

Open a new terminal (keep the server running) and try:

```bash
# Get all employees
curl http://localhost:8080/api/employees

# Get all meetings
curl http://localhost:8080/api/meetings

# Create a new employee
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test.user@company.com",
    "department": "IT",
    "designation": "Developer"
  }'

# Book a meeting
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Team Meeting",
    "description": "Weekly sync",
    "startTime": "2026-12-15 10:00:00",
    "endTime": "2026-12-15 11:00:00",
    "organizerEmail": "john.doe@company.com",
    "participantEmails": ["jane.smith@company.com"],
    "location": "Conference Room A"
  }'
```

**Using Postman:**
- Import `MeetingCalendar-API-Collection.json`
- Set `baseUrl` variable to `http://localhost:8080/api`
- Run the pre-configured requests

### Stopping the Application

Press `Ctrl+C` in the terminal where the application is running.

## Features

### Employee Management
- Create, read, update, delete employees
- Search by email or department
- Track department and designation
- Sample data: 5 employees pre-loaded

### Meeting Management
- Book meetings with multiple participants
- Set title, description, location, and time
- Automatic conflict detection
- Meeting status tracking (SCHEDULED, CANCELLED, COMPLETED)
- Sample data: 3 meetings pre-loaded

### Calendar Intelligence
- Find free time slots between employees
- Check availability for specific time ranges
- Detect scheduling conflicts
- Business hours validation (9 AM - 6 PM)
- Get next available meeting slot

## Technical Architecture

### Technology Stack
- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- H2 In-Memory Database
- Maven
- JUnit 5 & Mockito for testing

### Project Structure
```
src/main/java/com/assessment/
├── controller/          # REST API endpoints
├── service/            # Business logic
├── repository/         # Database access
├── model/              # Domain entities
├── dto/                # Data transfer objects
├── config/             # Configuration classes
└── exception/          # Error handling
```

## API Endpoints

### Employee Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/employees` | Get all employees |
| GET | `/api/employees/{id}` | Get employee by ID |
| GET | `/api/employees/email/{email}` | Get employee by email |
| POST | `/api/employees` | Create new employee |
| PUT | `/api/employees/{id}` | Update employee |
| DELETE | `/api/employees/{id}` | Delete employee |

### Meeting Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/meetings` | Get all meetings |
| GET | `/api/meetings/{id}` | Get meeting by ID |
| POST | `/api/meetings` | Book new meeting |
| PUT | `/api/meetings/{id}` | Update meeting |
| DELETE | `/api/meetings/{id}` | Delete meeting |
| POST | `/api/meetings/conflicts` | Check for conflicts |
| GET | `/api/meetings/upcoming/{email}` | Get upcoming meetings |

### Calendar Services
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/calendar/free-slots` | Find free slots between employees |
| GET | `/api/calendar/free-slots/{email}` | Find free slots for employee |

## Sample Data

The application comes pre-loaded with:

**Employees:**
1. John Doe - john.doe@company.com (Engineering, Senior Developer)
2. Jane Smith - jane.smith@company.com (Engineering, Team Lead)
3. Bob Wilson - bob.wilson@company.com (Marketing, Marketing Manager)
4. Alice Johnson - alice.johnson@company.com (HR, HR Manager)
5. Charlie Brown - charlie.brown@company.com (Finance, Financial Analyst)

**Meetings:**
1. Team Standup - July 15, 2025, 10:00-11:00 AM
2. Project Planning - July 15, 2025, 2:00-3:00 PM
3. Budget Review - July 16, 2025, 11:00 AM-12:00 PM

## Testing

Run the test suite:
```bash
./mvnw test
```

The project includes:
- Unit tests for service layer
- Integration tests for API endpoints
- Repository tests for data access

## Configuration

Default settings in `application.yml`:
- Server Port: 8080
- Context Path: `/api`
- Database: H2 in-memory
- Working Hours: 9:00 AM - 6:00 PM

## Development

### Building the Project
```bash
./mvnw clean package
```

### Running Tests
```bash
./mvnw test
```

### Running with Custom Port
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
```

## Troubleshooting

**Port 8080 already in use:**
- Stop other applications using port 8080, or run on a different port

**Connection refused:**
- Ensure the application is running (`./mvnw spring-boot:run`)

**404 Not Found:**
- Remember to include `/api` prefix in URLs
- Example: `http://localhost:8080/api/employees`

**Can't create meeting:**
- Verify employee emails exist in database
- Use correct time format: `YYYY-MM-DD HH:MM:SS`
- Ensure meeting is within business hours (9 AM - 6 PM)

## Project Highlights

This project demonstrates:
- Spring Boot application development
- RESTful API design
- JPA/Hibernate for data persistence
- Complex business logic (calendar algorithms, conflict detection)
- Comprehensive testing strategy
- Clean architecture and separation of concerns
- Professional error handling and validation

## License

This project is developed for demonstration and assessment purposes.
