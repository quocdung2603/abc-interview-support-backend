# 🔧 Postman Collection Fixes & Testing Guide

**File:** `ABC-Interview-ALL-Endpoints.postman_collection.json`  
**Last Updated:** October 22, 2025  
**Status:** ✅ Fixed and Ready for Testing

---

## 📋 Summary of Fixed Issues

### 1. ❌ **User Service - Apply ELO Score** (FIXED ✅)

**Endpoint:** `POST /users/elo`

**Problem Found:**
```json
// ❌ WRONG - Old request body
{
  "userId": 1,
  "eloChange": 100
}
```

**Fixed To:**
```json
// ✅ CORRECT - Matches EloApplyRequest DTO
{
  "userId": 1,
  "action": "WIN",
  "points": 50,
  "description": "Won exam with high score"
}
```

**DTO Source:** `user-service/src/main/java/com/abc/user_service/dto/request/EloApplyRequest.java`

**Required Fields:**
- `userId` (Long, @NotNull)
- `action` (String, @NotBlank) - e.g., "WIN", "LOSE", "DRAW"
- `points` (Integer, @NotNull)
- `description` (String, optional)

---

### 2. ❌ **Career Service - Create/Update Career Preference** (FIXED ✅)

**Endpoint:** `POST /career` and `PUT /career/update/{careerId}`

**Problem Found:**
```json
// ❌ WRONG - Old request body with unnecessary fields
{
  "userId": 1,
  "fieldId": 1,
  "levelId": 2,
  "desiredPosition": "Senior Backend Developer",
  "desiredSalary": "2000 USD",
  "desiredLocation": "Hanoi or Remote"
}
```

**Fixed To:**
```json
// ✅ CORRECT - Matches CareerPreferenceRequest DTO
{
  "userId": 1,
  "fieldId": 1,
  "topicId": 1
}
```

**DTO Source:** `career-service/src/main/java/com/abc/career_service/dto/request/CareerPreferenceRequest.java`

**Required Fields:**
- `userId` (Long, @NotNull)
- `fieldId` (Long, @NotNull)
- `topicId` (Long, @NotNull)

**Note:** Career preferences link users to specific topics within fields, not to job positions or salaries.

---

## ✅ Verified Correct Endpoints

These endpoints were checked and confirmed to match source code:

### Auth Service (5 endpoints) ✅
- ✅ `POST /auth/register` - RegisterRequest accepts both `roleName` and `roleId`
- ✅ `POST /auth/login` - LoginRequest: `{email, password}`
- ✅ `POST /auth/refresh` - RefreshRequest: `{refreshToken}`
- ✅ `GET /auth/verify?token={token}`
- ✅ `GET /auth/user-info` - Bearer token in header

### User Service (10 endpoints) ✅
- ✅ `GET /users/{id}` - Get user by ID
- ✅ `GET /users?page=0&size=20` - Get all users (paginated, admin)
- ✅ `GET /users/role/{roleId}` - Filter by role
- ✅ `GET /users/status/{status}` - Filter by status (ACTIVE, INACTIVE, BANNED)
- ✅ `GET /users/roles` - Get all roles (public)
- ✅ `PUT /users/{id}` - Update user: `{email, fullName, roleId}`
- ✅ `PUT /users/{id}/role` - Change role: `{roleId}`
- ✅ `PUT /users/{id}/status` - Change status: `{status}`
- ✅ `POST /users/elo` - Apply ELO: `{userId, action, points, description}` (FIXED)
- ✅ `DELETE /users/{id}` - Delete user (admin)

### Question Service (30+ endpoints) ✅
All Fields, Topics, Levels, Question Types, Questions, and Answers endpoints verified.

**Sample Question Create Request:**
```json
{
  "userId": 1,
  "topicId": 1,
  "fieldId": 1,
  "levelId": 1,
  "questionTypeId": 1,
  "content": "What is dependency injection?",
  "answer": "DI is a design pattern...",
  "language": "Vietnamese"
}
```

### Exam Service (25 endpoints) ✅
All exam CRUD, workflow (publish/start/complete), questions, results, answers, and registrations verified.

**Sample Exam Create Request:**
```json
{
  "userId": 1,
  "examType": "VIRTUAL",
  "title": "Java Backend Test",
  "position": "Backend Developer",
  "topics": [1, 2, 3],
  "questionTypes": [1, 2],
  "questionCount": 20,
  "duration": 60,
  "language": "Vietnamese"
}
```

### News Service (16 endpoints) ✅
All news CRUD, moderation (approve/reject/publish), voting, and filtering endpoints verified.

**Sample News Create Request:**
```json
{
  "userId": 1,
  "title": "Breaking Tech News",
  "content": "Detailed content...",
  "fieldId": 1,
  "newsType": "NEWS"
}
```

**Sample Recruitment Create Request:**
```json
{
  "userId": 1,
  "title": "Senior Java Developer",
  "content": "Job description...",
  "fieldId": 1,
  "newsType": "RECRUITMENT",
  "companyName": "Tech Company Ltd",
  "location": "Hanoi, Vietnam",
  "salary": "1500-2500 USD",
  "experience": "3+ years",
  "position": "Senior Backend Developer"
}
```

### Career Service (5 endpoints) ✅
- ✅ `POST /career` - Create preference: `{userId, fieldId, topicId}` (FIXED)
- ✅ `PUT /career/update/{careerId}` - Update: `{userId, fieldId, topicId}` (FIXED)
- ✅ `GET /career/{careerId}` - Get by ID
- ✅ `GET /career/preferences/{userId}` - Get user preferences (paginated)
- ✅ `DELETE /career/{careerId}` - Delete preference

---

## 🧪 Testing Instructions

### 1. Import Collection to Postman

```powershell
# Open Postman
# File → Import → Choose file
# Select: postman-collections/ABC-Interview-ALL-Endpoints.postman_collection.json
```

### 2. Setup Environment Variables

Create a new Postman Environment with these variables:

| Variable | Initial Value | Current Value |
|----------|---------------|---------------|
| `base_url` | `http://localhost:8080` | `http://localhost:8080` |
| `access_token` | (empty) | (auto-set after login) |
| `refresh_token` | (empty) | (auto-set after login) |
| `verify_token` | (empty) | (auto-set after register) |

### 3. Start All Services

```powershell
# Option 1: Docker Compose (Recommended)
docker-compose up -d

# Option 2: Individual services (see GETTING-STARTED.md)
# Discovery → Config → Gateway → Business Services
```

### 4. Test Workflow

#### Step 1: Register New User
```
POST /auth/register
Body: {
  "email": "testuser@example.com",
  "password": "test123",
  "fullName": "Test User",
  "roleName": "USER"
}
```
✅ Response: `{accessToken, refreshToken, verifyToken}`  
✅ Tokens auto-saved to environment variables

#### Step 2: Verify Account (Optional)
```
GET /auth/verify?token={{verify_token}}
```

#### Step 3: Login
```
POST /auth/login
Body: {
  "email": "testuser@example.com",
  "password": "test123"
}
```
✅ Response: `{accessToken, refreshToken}`  
✅ Tokens auto-saved

#### Step 4: Test Protected Endpoints

**Get User Info:**
```
GET /auth/user-info
Headers: Authorization: Bearer {{access_token}}
```

**Create Question:**
```
POST /questions
Headers: 
  Authorization: Bearer {{access_token}}
  Content-Type: application/json
Body: {
  "userId": 1,
  "topicId": 1,
  "fieldId": 1,
  "levelId": 1,
  "questionTypeId": 1,
  "content": "Test question",
  "answer": "Test answer",
  "language": "Vietnamese"
}
```

**Apply ELO Score (FIXED):**
```
POST /users/elo
Headers: 
  Authorization: Bearer {{access_token}}
  Content-Type: application/json
Body: {
  "userId": 1,
  "action": "WIN",
  "points": 50,
  "description": "Won exam"
}
```

**Create Career Preference (FIXED):**
```
POST /career
Headers: 
  Authorization: Bearer {{access_token}}
  Content-Type: application/json
Body: {
  "userId": 1,
  "fieldId": 1,
  "topicId": 1
}
```

#### Step 5: Test Pagination
```
GET /users?page=0&size=10
GET /questions?page=0&size=20
GET /exams?page=0&size=10
GET /news?page=0&size=20
```

#### Step 6: Test Admin Endpoints (Need ADMIN role)

**Update User Role:**
```
PUT /users/1/role
Body: { "roleId": 2 }
```

**Approve Question:**
```
POST /questions/1/approve?adminId=1
```

**Approve News:**
```
POST /news/1/approve?adminId=1
```

---

## 🔍 Common Test Scenarios

### Scenario 1: Complete Exam Flow
```
1. POST /exams - Create exam
2. POST /exams/{examId}/publish?userId=1 - Publish
3. POST /exams/registrations - Register user
4. POST /exams/{examId}/start - Start exam
5. POST /exams/answers - Submit answers
6. POST /exams/results - Submit result
7. POST /exams/{examId}/complete - Complete exam
8. GET /exams/{examId}/results - View results
```

### Scenario 2: News Moderation Flow
```
1. POST /news - Create news (status: PENDING)
2. POST /news/{newsId}/approve?adminId=1 - Approve (ADMIN)
3. POST /news/{newsId}/publish - Publish (ADMIN)
4. GET /news/published/NEWS - View published
5. POST /news/{newsId}/vote?voteType=UPVOTE - User votes
```

### Scenario 3: Question Contribution Flow
```
1. POST /questions - User creates question (status: PENDING)
2. POST /questions/{id}/approve?adminId=1 - Admin approves
3. GET /questions/topics/1/questions - View by topic
4. POST /questions/answers - User adds answer
5. POST /questions/answers/{id}/sample?isSample=true - Mark as sample
```

---

## ❌ Error Handling Tests

### Test Invalid Data
```json
// Missing required field
POST /users/elo
{
  "userId": 1,
  "points": 50
  // Missing "action" → 400 Bad Request
}

// Invalid enum
POST /exams
{
  "examType": "INVALID_TYPE"  
  // Should be VIRTUAL or RECRUITER → 400
}

// Non-existent ID
GET /users/999999
// → 404 Not Found
```

### Test Authentication
```
// No token
GET /users
// → 401 Unauthorized

// Expired token
Authorization: Bearer expired_token_here
// → 401 Unauthorized

// Wrong role
PUT /users/1/role (USER tries to change role)
// → 403 Forbidden (need ADMIN)
```

---

## 📊 Expected Response Formats

### Success Response (200/201)
```json
{
  "id": 1,
  "email": "user@example.com",
  "fullName": "User Name",
  "createdAt": "2025-10-22T10:00:00",
  ...
}
```

### Paginated Response
```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 100,
  "totalPages": 5,
  "last": false,
  "first": true
}
```

### Error Response (4xx/5xx)
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Action is required",
  "errorCode": "VALIDATION_ERROR",
  "traceId": "abc123..."
}
```

---

## 🎯 Checklist After Testing

- [ ] All Auth endpoints work (register, login, refresh, verify)
- [ ] User CRUD operations work
- [ ] ELO score application works with new format
- [ ] Career preferences create/update with correct fields
- [ ] Question service: Fields, Topics, Levels, Types, Questions, Answers
- [ ] Exam lifecycle: create → publish → start → answers → results → complete
- [ ] News moderation: create → approve → publish → vote
- [ ] Recruitment posts create correctly
- [ ] Pagination works on all list endpoints
- [ ] Authentication required for protected endpoints
- [ ] Role-based access control (USER/ADMIN/RECRUITER)
- [ ] Error responses follow RFC 7807 format
- [ ] Token refresh works when access token expires

---

## 🐛 Known Issues (None Currently)

All identified issues have been fixed:
- ✅ ELO Score request body corrected
- ✅ Career Preference fields simplified to match DTO

---

## 📞 Support

**If you encounter issues:**

1. Check service logs:
   ```powershell
   docker-compose logs -f service-name
   ```

2. Verify service health:
   ```powershell
   curl http://localhost:8080/actuator/health
   curl http://localhost:8081/actuator/health  # Auth
   curl http://localhost:8082/actuator/health  # User
   ```

3. Check Eureka Dashboard:
   ```
   http://localhost:8761
   ```
   All services should be registered.

4. Review API specification:
   ```
   See: FRONTEND-API-SPECIFICATION.md
   ```

---

**Last Verified:** October 22, 2025  
**Collection Version:** 3.0 (Fixed)  
**Total Endpoints:** 100+  
**Status:** ✅ Production Ready
