# 📅 Meeting Calendar Assistant

A sophisticated **Spring Boot REST API** for enterprise meeting management, featuring intelligent calendar coordination, conflict detection, and free slot discovery.

## 🎯 Project Overview

This application demonstrates advanced **Spring Boot development** with complex business logic implementation for calendar management. Built as a comprehensive solution for enterprise meeting coordination challenges.

### ✨ Key Features

- **📝 Meeting Management**: Complete CRUD operations for meeting booking and management
- **👥 Employee Management**: Comprehensive employee registry with department/designation tracking
- **🔍 Smart Conflict Detection**: Automatic detection of scheduling conflicts across participants
- **⏰ Free Slot Discovery**: Intelligent algorithm to find available time slots between multiple employees
- **🕐 Business Hours Support**: Configurable working hours (9 AM - 6 PM by default)
- **👨‍👩‍👧‍👦 Multi-participant Meetings**: Support for group meetings with conflict resolution
- **🛡️ Input Validation**: Comprehensive validation with proper error handling
- **🗄️ In-Memory Database**: H2 database with sample data for immediate testing

## 🏗️ Technical Architecture

### **Technology Stack**
- **Backend**: Java 17, Spring Boot 3.2.0
- **Framework**: Spring Web, Spring Data JPA
- **Database**: H2 In-Memory Database
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Build Tool**: Maven
- **Documentation**: OpenAPI/Swagger ready

### **Design Patterns**
- **Repository Pattern**: Clean data access abstraction
- **Service Layer Pattern**: Business logic separation
- **DTO Pattern**: Data transfer between layers
- **Builder Pattern**: Complex object construction
- **Strategy Pattern**: Flexible conflict detection algorithms

### **Package Structure**
```
src/main/java/com/assessment/
├── controller/          # REST endpoints
├── service/            # Business logic
├── repository/         # Data access layer
├── model/              # JPA entities
├── dto/                # Data transfer objects
├── config/             # Configuration classes
└── exception/          # Custom exception handling
```

## 🚀 Quick Start

### Prerequisites
- **Java 17+** installed
- **Maven 3.6+** installed
- **Git** for cloning

### Installation & Setup

```bash
# Clone the repository
git clone <repository-url>
cd meeting-calendar-assistant

# Build and run the application
mvn clean compile
mvn spring-boot:run
```

### Access Points
- **API Base URL**: `http://localhost:8080/api`
- **H2 Console**: `http://localhost:8080/api/h2-console`
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: `password`

## 📋 API Documentation

### **Employee Management**
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/employees` | Get all employees |
| GET | `/api/employees/{id}` | Get employee by ID |
| GET | `/api/employees/email/{email}` | Get employee by email |
| POST | `/api/employees` | Create new employee |
| PUT | `/api/employees/{id}` | Update employee |
| DELETE | `/api/employees/{id}` | Delete employee |
| GET | `/api/employees/department/{dept}` | Get employees by department |

### **Meeting Management**
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/meetings` | Get all meetings |
| GET | `/api/meetings/{id}` | Get meeting by ID |
| POST | `/api/meetings` | Book new meeting |
| PUT | `/api/meetings/{id}` | Update meeting |
| DELETE | `/api/meetings/{id}` | Delete meeting |
| POST | `/api/meetings/conflicts` | Check for conflicts |
| GET | `/api/meetings/upcoming/{email}` | Get upcoming meetings |

### **Calendar Services**
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/calendar/free-slots` | Find free slots between employees |
| GET | `/api/calendar/free-slots/{email}` | Find free slots for employee |
| POST | `/api/calendar/check-availability` | Check time slot availability |
| GET | `/api/calendar/next-available-slot` | Get next available slot |
| GET | `/api/calendar/optimal-time` | Find optimal meeting time |

## 🧪 Testing

### **Test Coverage**
- **Unit Tests**: Service layer business logic
- **Integration Tests**: End-to-end API testing
- **Repository Tests**: Data access validation
- **Test Profiles**: Isolated test environment

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report
```

### **Postman Collection**
Import `MeetingCalendar-API-Collection.json` for comprehensive API testing with pre-configured requests.

## 💼 Sample Usage

### **1. Create Employees**
```json
POST /api/employees
{
  "name": "John Doe",
  "email": "john.doe@company.com",
  "department": "Engineering",
  "designation": "Senior Developer"
}
```

### **2. Book a Meeting**
```json
POST /api/meetings
{
  "title": "Team Planning",
  "description": "Sprint planning session",
  "startTime": "2025-07-15 10:00:00",
  "endTime": "2025-07-15 11:00:00",
  "organizerEmail": "john.doe@company.com",
  "participantEmails": ["jane.smith@company.com"],
  "location": "Conference Room A"
}
```

### **3. Find Free Slots**
```json
POST /api/calendar/free-slots
{
  "employee1Email": "john.doe@company.com",
  "employee2Email": "jane.smith@company.com",
  "startDate": "2025-07-15 09:00:00",
  "endDate": "2025-07-15 18:00:00",
  "meetingDurationMinutes": 60
}
```

### **4. Check Conflicts**
```json
POST /api/meetings/conflicts
{
  "participantEmails": ["john.doe@company.com", "jane.smith@company.com"],
  "startTime": "2025-07-15 10:30:00",
  "endTime": "2025-07-15 11:30:00"
}
```

## 🎨 Key Algorithmic Features

### **Conflict Detection Algorithm**
- Validates meeting overlaps across all participants
- Considers business hours constraints
- Handles edge cases for adjacent meetings
- Supports multi-participant validation

### **Free Slot Discovery**
- Intelligent slot calculation between employee calendars
- Configurable meeting duration requirements
- Business hours filtering
- Optimal time recommendation engine

## 🛠️ Development Features

### **Error Handling**
- Global exception handler with standardized responses
- Input validation with meaningful error messages
- HTTP status code compliance
- Detailed logging for debugging

### **Data Validation**
- Bean validation with custom annotations
- Business rule validation
- Email format validation
- Date/time constraint validation

### **Sample Data**
The application includes pre-loaded sample data:
- 5 employees across different departments
- 3 sample meetings for immediate testing
- Realistic business scenarios

## 📈 Performance Considerations

- **In-Memory Database**: Fast startup and testing
- **Lazy Loading**: Optimized JPA relationships
- **Query Optimization**: Efficient database queries
- **Caching Strategy**: Ready for production caching implementation

## 🔧 Configuration

### **Application Properties**
- **Server Port**: 8080
- **Context Path**: `/api`
- **Database**: H2 in-memory
- **Logging**: DEBUG level for development

### **Business Rules**
- **Working Hours**: 9:00 AM - 6:00 PM
- **Minimum Meeting Duration**: 15 minutes
- **Maximum Meeting Duration**: 8 hours
- **Timezone**: UTC

## 🤝 Contributing

This project demonstrates best practices for:
- Clean code architecture
- Comprehensive testing
- API design principles
- Spring Boot development
- Enterprise application patterns

## 📄 License

This project is developed for assessment purposes and showcases enterprise-level Spring Boot application development capabilities.

---

## 🎯 Assessment Highlights

This project demonstrates proficiency in:
- **Spring Boot Ecosystem**: Web, Data JPA, Validation, Testing
- **RESTful API Design**: HTTP compliance, proper status codes, resource modeling
- **Complex Business Logic**: Calendar algorithms, conflict detection, optimization
- **Testing Strategy**: Unit, integration, and API testing
- **Clean Architecture**: Separation of concerns, design patterns
- **Error Handling**: Comprehensive exception management
- **Database Integration**: JPA/Hibernate with relationship modeling
- **Documentation**: Professional API and code documentation 