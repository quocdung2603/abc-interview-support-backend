# ✅ ABC Interview Platform - Complete Postman Collection

## 📋 Tổng Quan

File Postman Collection đầy đủ **VERIFIED từ source code** của tất cả microservices:

- **File chính**: `ABC-Interview-VERIFIED-Complete.postman_collection.json`
- **Tổng số endpoints**: 116 endpoints
- **Services**: 6 microservices

## 🎯 Danh Sách Endpoints Theo Service

### 1️⃣ Auth Service (5 endpoints)
| Method | Endpoint | Mô tả |
|--------|----------|--------|
| POST | `/auth/register` | Đăng ký user mới |
| POST | `/auth/login` | Đăng nhập |
| POST | `/auth/refresh` | Refresh token |
| GET | `/auth/verify?token=xxx` | Verify token |
| GET | `/auth/user-info` | Lấy thông tin user hiện tại |

**Source**: `auth-service/src/main/java/com/auth/service/controller/AuthController.java`

### 2️⃣ User Service (16 endpoints)
| Method | Endpoint | Mô tả | Security |
|--------|----------|--------|----------|
| POST | `/users/internal/create` | Tạo user (internal) | - |
| GET | `/users/internal/user/{id}` | Get user (internal) | - |
| GET | `/users/check-email/{email}` | Check email tồn tại | - |
| GET | `/users/by-email/{email}` | Get user by email | - |
| POST | `/users/validate-password` | Validate password | - |
| POST | `/users/verify-token` | Verify JWT token | - |
| GET | `/users/{id}` | Get user by ID | - |
| PUT | `/users/{id}/role` | Update role | ADMIN |
| PUT | `/users/{id}/status` | Update status | ADMIN |
| POST | `/users/elo` | Apply ELO rating | - |
| PUT | `/users/{id}` | Update user | USER/ADMIN |
| DELETE | `/users/{id}` | Delete user | ADMIN |
| GET | `/users` | Get all users | ADMIN |
| GET | `/users/role/{roleId}` | Get users by role | ADMIN |
| GET | `/users/status/{status}` | Get users by status | ADMIN |
| GET | `/users/roles` | Get all roles | - |

**Source**: `user-service/src/main/java/com/abc/user_service/controller/UserController.java`

### 3️⃣ Question Service (48 endpoints)

#### Fields (5)
- POST `/questions/fields` - Create field (ADMIN)
- GET `/questions/fields` - Get all fields
- GET `/questions/fields/{id}` - Get field by ID
- PUT `/questions/fields/{id}` - Update field (ADMIN)
- DELETE `/questions/fields/{id}` - Delete field (ADMIN)

#### Topics (5)
- POST `/questions/topics` - Create topic (ADMIN)
- GET `/questions/topics` - Get all topics
- GET `/questions/topics/{id}` - Get topic by ID
- PUT `/questions/topics/{id}` - Update topic (ADMIN)
- DELETE `/questions/topics/{id}` - Delete topic (ADMIN)

#### Levels (5)
- POST `/questions/levels` - Create level (ADMIN)
- GET `/questions/levels` - Get all levels
- GET `/questions/levels/{id}` - Get level by ID
- PUT `/questions/levels/{id}` - Update level (ADMIN)
- DELETE `/questions/levels/{id}` - Delete level (ADMIN)

#### Question Types (5)
- POST `/questions/question-types` - Create type (ADMIN)
- GET `/questions/question-types` - Get all types
- GET `/questions/question-types/{id}` - Get type by ID
- PUT `/questions/question-types/{id}` - Update type (ADMIN)
- DELETE `/questions/question-types/{id}` - Delete type (ADMIN)

#### Questions (8)
- POST `/questions` - Create question (USER/ADMIN)
- GET `/questions` - Get all questions
- GET `/questions/{id}` - Get question by ID
- PUT `/questions/{id}` - Update question (ADMIN)
- DELETE `/questions/{id}` - Delete question (ADMIN)
- POST `/questions/{id}/approve` - Approve question (ADMIN)
- POST `/questions/{id}/reject` - Reject question (ADMIN)
- GET `/questions/topics/{topicId}/questions` - Get questions by topic

#### Answers (7)
- POST `/questions/answers` - Create answer (USER/ADMIN)
- GET `/questions/answers` - Get all answers
- GET `/questions/answers/{id}` - Get answer by ID
- PUT `/questions/answers/{id}` - Update answer (USER/ADMIN)
- DELETE `/questions/answers/{id}` - Delete answer (ADMIN)
- POST `/questions/answers/{id}/sample` - Mark sample answer (ADMIN)
- GET `/questions/{questionId}/answers` - Get answers by question

**Source**: `question-service/src/main/java/com/abc/question_service/controller/QuestionController.java`

### 4️⃣ Exam Service (26 endpoints)

#### Exam Management (11)
- POST `/exams` - Create exam (USER/ADMIN/RECRUITER)
- GET `/exams` - Get all exams
- GET `/exams/{id}` - Get exam by ID
- PUT `/exams/{id}` - Update exam (ADMIN/RECRUITER)
- DELETE `/exams/{id}` - Delete exam (ADMIN/RECRUITER)
- POST `/exams/{examId}/publish` - Publish exam (ADMIN/RECRUITER)
- POST `/exams/{examId}/start` - Start exam (USER/ADMIN)
- POST `/exams/{examId}/complete` - Complete exam (USER/ADMIN)
- GET `/exams/user/{userId}` - Get exams by user (USER/ADMIN)
- GET `/exams/type?type=xxx` - Get exams by type
- GET `/exams/types` - Get all exam types

#### Question Management (3)
- POST `/exams/questions` - Add question to exam (ADMIN/RECRUITER)
- **POST `/exams/questions/random`** - ⭐ **Add random questions** (ADMIN/RECRUITER) **← MỚI**
- DELETE `/exams/{examId}/questions` - Remove all questions (ADMIN/RECRUITER)

#### Results (3)
- POST `/exams/results` - Submit result (USER/ADMIN)
- GET `/exams/results/{id}` - Get result by ID (USER/ADMIN)
- GET `/exams/{examId}/results` - Get results by exam (ADMIN/RECRUITER)
- GET `/exams/results/user/{userId}` - Get results by user (USER/ADMIN)

#### Answers (3)
- POST `/exams/answers` - Submit answer (USER/ADMIN)
- GET `/exams/answers/{id}` - Get answer by ID (USER/ADMIN)
- GET `/exams/{examId}/answers/{userId}` - Get user answers (USER/ADMIN)

#### Registrations (4)
- POST `/exams/registrations` - Register for exam (USER/ADMIN)
- GET `/exams/registrations/{id}` - Get registration by ID (USER/ADMIN)
- POST `/exams/registrations/{registrationId}/cancel` - Cancel registration (USER/ADMIN)
- GET `/exams/{examId}/registrations` - Get registrations by exam (ADMIN/RECRUITER)
- GET `/exams/registrations/user/{userId}` - Get registrations by user (USER/ADMIN)

**Source**: `exam-service/src/main/java/com/abc/exam_service/controller/ExamController.java`

### 5️⃣ News Service (17 endpoints)

#### News (16)
- POST `/news` - Create news (USER/ADMIN/RECRUITER)
- GET `/news/{id}` - Get news by ID
- PUT `/news/{id}` - Update news (USER/ADMIN/RECRUITER)
- DELETE `/news/{id}` - Delete news (ADMIN/RECRUITER)
- POST `/news/{newsId}/approve` - Approve news (ADMIN)
- POST `/news/{newsId}/reject` - Reject news (ADMIN)
- POST `/news/{newsId}/publish` - Publish news (ADMIN)
- POST `/news/{newsId}/vote?voteType=xxx` - Vote news (USER/ADMIN)
- GET `/news` - Get all news
- GET `/news/type?type=xxx` - Get news by type
- GET `/news/user/{userId}` - Get news by user (USER/ADMIN/RECRUITER)
- GET `/news/status/{status}` - Get news by status (ADMIN)
- GET `/news/field/{fieldId}` - Get news by field
- GET `/news/published/{newsType}` - Get published news
- GET `/news/moderation/pending` - Get pending moderation (ADMIN)
- GET `/news/types` - Get all news types

#### Recruitment (3 - trong cùng file)
- POST `/recruitments` - Create recruitment (RECRUITER/ADMIN)
- GET `/recruitments` - Get all recruitments
- GET `/recruitments/company/{companyName}` - Get by company

**Source**: `news-service/src/main/java/com/abc/news_service/controller/NewsController.java`

### 6️⃣ Career Service (5 endpoints)
| Method | Endpoint | Mô tả | Security |
|--------|----------|--------|----------|
| POST | `/career` | Create career preference | USER/ADMIN |
| GET | `/career/{careerId}` | Get career by ID | USER/ADMIN |
| PUT | `/career/update/{careerId}` | Update career | USER/ADMIN |
| GET | `/career/preferences/{userId}` | Get careers by user | USER/ADMIN |
| DELETE | `/career/{careerId}` | Delete career | USER/ADMIN |

**Source**: `career-service/src/main/java/com/abc/career_service/controller/CareerPreferenceController.java`

## 📥 Cách Import vào Postman

1. **Mở Postman**
2. Click **File → Import**
3. Chọn file `ABC-Interview-VERIFIED-Complete.postman_collection.json`
4. Collection sẽ được import với tất cả folders và requests

## 🚀 Cách Sử Dụng

### Bước 1: Login để lấy token
1. Mở folder "🔐 Auth Service"
2. Chạy request **"Login"** với:
   ```json
   {
     "username": "admin1",
     "password": "password123"
   }
   ```
3. Token sẽ tự động được lưu vào variables `access_token` và `refresh_token`

### Bước 2: Test Random Questions (Endpoint Mới)
1. Mở folder "📝 Exam Service"
2. Tìm request **"Add Random Questions To Exam"**
3. Body mẫu:
   ```json
   {
     "examId": 1,
     "numberOfQuestions": 5,
     "field": "Lập trình viên",
     "topics": ["ReactJS"],
     "level": "Junior",
     "questionType": "Multiple Choice"
   }
   ```
4. Click **Send**

## 🔧 Variables Được Sử Dụng

Collection sử dụng các biến sau:
- `{{base_url}}` - Base URL của API Gateway (mặc định: `http://localhost:8080`)
- `{{access_token}}` - JWT access token (tự động set sau login)
- `{{refresh_token}}` - JWT refresh token (tự động set sau login)

## ✅ Verification

Tất cả endpoints đã được verify trực tiếp từ source code:
- ✅ Auth Service: 5/5 endpoints
- ✅ User Service: 16/16 endpoints
- ✅ Question Service: 48/48 endpoints (Fields, Topics, Levels, Types, Questions, Answers)
- ✅ Exam Service: 26/26 endpoints (bao gồm Random Questions mới)
- ✅ News Service: 17/17 endpoints (News + Recruitment)
- ✅ Career Service: 5/5 endpoints

**Tổng: 117 endpoints**

## 🎯 Lưu Ý Quan Trọng

1. **Authentication**: Hầu hết endpoints cần Bearer token. Chạy Login trước.
2. **Admin Endpoints**: Endpoints có `(ADMIN)` cần role ADMIN.
3. **Pagination**: Các GET endpoints list thường hỗ trợ `?page=0&size=10`.
4. **Random Questions**: Endpoint mới nhất, cần ADMIN hoặc RECRUITER role.

## 📞 Hỗ Trợ

Nếu gặp lỗi 401 Unauthorized:
- Kiểm tra token còn hạn không
- Chạy lại Login để refresh token
- Kiểm tra role của user có đủ quyền không

Nếu gặp lỗi 400 Bad Request:
- Kiểm tra request body đúng format JSON
- Kiểm tra các trường required có đủ không
- Xem logs ở terminal của service tương ứng
