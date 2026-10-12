# MeetSync - Simple Testing Guide

## What You Need:
- 2 Terminal Windows

---

## TERMINAL 1 (Application Running)

### Command:
```bash
cd /e/Projects/MeetSync
./mvnw spring-boot:run
```

### What you'll see:
```
[INFO] Scanning for projects...
[INFO] Building meeting-calendar-assistant 1.0.0
...
Started MeetingCalendarAssistantApplication in 5.234 seconds
✓ Created 5 employees
✓ Created 3 meetings
✓ API available at: http://localhost:8080/api
```

**KEEP THIS TERMINAL OPEN AND RUNNING!**

---

## TERMINAL 2 (Testing)

Open a NEW terminal window and run these commands:

### Test 1: View Employees
```bash
curl http://localhost:8080/api/employees
```

**Expected Output:**
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@company.com",
    "department": "Engineering",
    "designation": "Senior Developer"
  },
  {
    "id": 2,
    "name": "Jane Smith",
    "email": "jane.smith@company.com",
    "department": "Engineering",
    "designation": "Team Lead"
  },
  ...
]
```

✅ If you see JSON data with employees → **IT'S WORKING!**

---

### Test 2: View Meetings
```bash
curl http://localhost:8080/api/meetings
```

**Expected Output:**
```json
[
  {
    "id": 1,
    "title": "Team Standup",
    "startTime": "2025-07-15T10:00:00",
    "endTime": "2025-07-15T11:00:00",
    ...
  },
  ...
]
```

✅ If you see JSON data with meetings → **IT'S WORKING!**

---

### Test 3: Create New Employee
```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"name":"My Test User","email":"mytest@company.com","department":"Testing","designation":"Tester"}'
```

**Expected Output:**
```json
{
  "id": 6,
  "name": "My Test User",
  "email": "mytest@company.com",
  "department": "Testing",
  "designation": "Tester"
}
```

✅ If you see the new employee with an ID → **IT'S WORKING!**

---

### Test 4: Verify New Employee Was Created
```bash
curl http://localhost:8080/api/employees
```

**Expected:** You should now see 6 employees (5 original + 1 new)

---

### Test 5: Book a Meeting
```bash
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My Test Meeting",
    "description": "Testing the system",
    "startTime": "2026-01-15 14:00:00",
    "endTime": "2026-01-15 15:00:00",
    "organizerEmail": "john.doe@company.com",
    "participantEmails": ["jane.smith@company.com"],
    "location": "Room 101"
  }'
```

**Expected Output:**
```json
{
  "id": 4,
  "title": "My Test Meeting",
  "status": "SCHEDULED",
  ...
}
```

✅ If you see the meeting created → **IT'S WORKING!**

---

### Test 6: Find Free Time Slots
```bash
curl -X POST http://localhost:8080/api/calendar/free-slots \
  -H "Content-Type: application/json" \
  -d '{
    "employee1Email": "john.doe@company.com",
    "employee2Email": "bob.wilson@company.com",
    "startDate": "2026-01-20 09:00:00",
    "endDate": "2026-01-20 18:00:00",
    "meetingDurationMinutes": 60
  }'
```

**Expected Output:**
```json
{
  "success": true,
  "message": "Found X free slots",
  "data": [
    {
      "startTime": "2026-01-20T09:00:00",
      "endTime": "2026-01-20T10:00:00"
    },
    ...
  ]
}
```

✅ If you see available time slots → **IT'S WORKING!**

---

## How to Stop

In **TERMINAL 1** (where app is running), press:
```
Ctrl + C
```

---

## Summary

### The Flow:
1. Start app in Terminal 1 → It runs and waits for requests
2. Send requests from Terminal 2 → App processes and responds
3. View responses → Confirm it's working

### The Concept:
- **Terminal 1** = Server (like a store that's open)
- **Terminal 2** = Customer (sending requests to the store)
- **curl commands** = Questions/requests to the server
- **JSON responses** = Answers from the server

---

## Browser Alternative

Instead of curl, you can also open in your browser:

- http://localhost:8080/api/employees
- http://localhost:8080/api/meetings

You'll see the JSON data directly in your browser!
