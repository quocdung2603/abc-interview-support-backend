# API Endpoint Validation Errors - Fix Guide

## Issues Found

### 1. PUT /users/{id} - Update User
**Error:** Requires `email` and `password` fields  
**Issue:** UserRequest DTO requires @NotBlank email and password, but UPDATE should not require password  
**Current Body:**
```json
{
  "fullName": "Updated Name",
  "address": "Ha Noi"
}
```
**Required Body (due to validation):**
```json
{
  "email": "user@example.com",
  "password": "current_password",
  "fullName": "Updated Name",
  "address": "Ha Noi"
}
```

**Fix Needed:** Create separate `UserUpdateRequest` DTO without password requirement

---

### 2. POST /users/elo - Update ELO
**Error:** Requires `action` and `points` fields  
**Current Body:**
```json
{
  "userId": 3,
  "eloChange": 50
}
```
**Required Body:**
```json
{
  "userId": 3,
  "action": "WIN",
  "points": 50,
  "description": "Optional description"
}
```

**Note:** Field is `action` and `points`, NOT `eloChange`

---

### 3. POST /questions - Create Question
**Error:** Missing required fields: `answer`, `language`, `userId`, `content`  
**Current Body:**
```json
{
  "topicId": 1,
  "fieldId": 1,
  "levelId": 2,
  "questionTypeId": 1,
  "questionContent": "What is Spring Boot?",
  "questionAnswer": "Spring Boot is a framework..."
}
```
**Required Body:**
```json
{
  "userId": 1,
  "topicId": 1,
  "fieldId": 1,
  "levelId": 2,
  "questionTypeId": 1,
  "content": "What is Spring Boot?",
  "answer": "Spring Boot is a framework...",
  "language": "JAVA"
}
```

**Note:** Fields are `content` and `answer`, NOT `questionContent` and `questionAnswer`

---

### 4. POST /questions/answers - Create Answer
**Error:** Missing `userId` field  
**Current Body:**
```json
{
  "questionId": 1,
  "questionTypeId": 1,
  "content": "My answer content",
  "isCorrect": true
}
```
**Required Body:**
```json
{
  "userId": 1,
  "questionId": 1,
  "questionTypeId": 1,
  "content": "My answer content",
  "isCorrect": true
}
```

---

### 5. POST /questions/answers/{id}/sample - Mark as Sample
**Error:** 500 Internal Server Error  
**Current Request:**
```
POST /questions/answers/1/sample
```
**Required Request:**
```
POST /questions/answers/1/sample?isSample=true
```

**Note:** Requires query parameter `isSample=true` or `isSample=false`

---

## Summary of Fixes Needed

### Immediate Fixes (Update Postman Collection & Test Scripts):

1. **PUT /users/{id}**
   - Add `email` and `password` to request body (use existing user's credentials)
   
2. **POST /users/elo**
   - Change `eloChange` → `action` and `points`
   - Use values: `action: "WIN"`, `points: 50`

3. **POST /questions**
   - Change `questionContent` → `content`
   - Change `questionAnswer` → `answer`
   - Add `userId` field
   - Add `language` field (e.g., "JAVA", "PYTHON", "JAVASCRIPT")

4. **POST /questions/answers**
   - Add `userId` field

5. **POST /questions/answers/{id}/sample**
   - Add query parameter `?isSample=true`

### Long-term Fixes (Backend Code):

1. Create `UserUpdateRequest` DTO without password requirement
2. Consider making `email` optional for updates
3. Standardize field naming (content vs questionContent)
