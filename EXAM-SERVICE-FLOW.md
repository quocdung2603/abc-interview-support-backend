# 📝 EXAM SERVICE - LUỒNG HOẠT ĐỘNG CHI TIẾT

## 📋 MỤC LỤC
- [Tổng quan](#tổng-quan)
- [Kiến trúc Database](#kiến-trúc-database)
- [Luồng hoạt động](#luồng-hoạt-động)
- [API Endpoints](#api-endpoints)
- [Ví dụ thực tế](#ví-dụ-thực-tế)
- [Tích hợp với các Service khác](#tích-hợp-với-các-service-khác)
- [Vấn đề và giải pháp](#vấn-đề-và-giải-pháp)

---

## 🎯 TỔNG QUAN

**Exam Service** quản lý toàn bộ quy trình thi tuyển, từ tạo đề thi, đăng ký, làm bài, đến chấm điểm và xem kết quả.

### Vai trò chính:
- 👨‍💼 **Admin/Recruiter**: Tạo và quản lý bài thi
- 👤 **User (Candidate)**: Đăng ký, làm bài thi và xem kết quả

### Các thành phần:
```
┌─────────────────────────────────────────────────┐
│            EXAM SERVICE                         │
├─────────────────────────────────────────────────┤
│  • Exam Management (Tạo/Sửa/Xóa bài thi)      │
│  • Registration (Đăng ký tham gia)             │
│  • Execution (Làm bài thi)                     │
│  • Grading (Chấm điểm)                         │
│  • Results (Xem kết quả)                       │
└─────────────────────────────────────────────────┘
```

---

## 🗄️ KIẾN TRÚC DATABASE

### 1. Bảng `exams` - Bài thi
```sql
CREATE TABLE exams (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,           -- Người tạo bài thi
    exam_type VARCHAR(50),              -- TECHNICAL, BEHAVIORAL, VIRTUAL, RECRUITER
    title VARCHAR(200) NOT NULL,        -- Tên bài thi
    position VARCHAR(100),              -- Vị trí ứng tuyển
    topics TEXT,                        -- JSON array: [1,2,3] - Topic IDs
    question_types TEXT,                -- JSON array: [1,2] - Question type IDs
    question_count INTEGER,             -- Số lượng câu hỏi
    duration INTEGER,                   -- Thời gian làm bài (phút)
    start_time TIMESTAMP,               -- Thời gian bắt đầu
    end_time TIMESTAMP,                 -- Thời gian kết thúc
    status VARCHAR(50),                 -- DRAFT, PUBLISHED, ONGOING, COMPLETED, CANCELLED
    language VARCHAR(10),               -- en, vi
    created_at TIMESTAMP,
    created_by BIGINT
);
```

**Ví dụ:**
```json
{
  "id": 1,
  "userId": 1,
  "examType": "TECHNICAL",
  "title": "ReactJS Developer Assessment",
  "position": "Frontend Developer",
  "topics": "[1,2,3]",                  // ReactJS, JavaScript, CSS
  "questionTypes": "[1,2]",              // Multiple Choice, Essay
  "questionCount": 20,
  "duration": 60,                        // 60 phút
  "status": "PUBLISHED",
  "language": "en"
}
```

### 2. Bảng `exam_questions` - Câu hỏi trong bài thi
```sql
CREATE TABLE exam_questions (
    id BIGSERIAL PRIMARY KEY,
    exam_id BIGINT REFERENCES exams(id),
    question_id BIGINT NOT NULL,        -- ID từ Question Service
    order_number INTEGER                -- Thứ tự câu hỏi (1, 2, 3...)
);
```

**Mục đích:** Liên kết exam với questions (từ Question Service)

### 3. Bảng `exam_registrations` - Đăng ký thi
```sql
CREATE TABLE exam_registrations (
    id BIGSERIAL PRIMARY KEY,
    exam_id BIGINT REFERENCES exams(id),
    user_id BIGINT NOT NULL,
    registration_status VARCHAR(50),    -- REGISTERED, CANCELLED, COMPLETED
    registered_at TIMESTAMP
);
```

**Ví dụ:**
```json
{
  "id": 1,
  "examId": 1,
  "userId": 3,
  "registrationStatus": "REGISTERED",
  "registeredAt": "2025-11-18T10:30:00"
}
```

### 4. Bảng `user_answers` - Câu trả lời của thí sinh
```sql
CREATE TABLE user_answers (
    id BIGSERIAL PRIMARY KEY,
    exam_id BIGINT REFERENCES exams(id),
    question_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    answer_content TEXT,                -- Nội dung câu trả lời
    is_correct BOOLEAN,                 -- Đúng/Sai (cho Multiple Choice)
    similarity_score DOUBLE PRECISION,  -- Điểm tương đồng (cho Essay, 0.0-1.0)
    created_at TIMESTAMP
);
```

**Ví dụ:**
```json
{
  "id": 1,
  "examId": 1,
  "questionId": 1,
  "userId": 3,
  "answerContent": "ReactJS is a JavaScript library for building user interfaces...",
  "isCorrect": true,
  "similarityScore": 0.95,
  "createdAt": "2025-11-20T14:25:00"
}
```

### 5. Bảng `results` - Kết quả thi
```sql
CREATE TABLE results (
    id BIGSERIAL PRIMARY KEY,
    exam_id BIGINT REFERENCES exams(id),
    user_id BIGINT NOT NULL,
    score DOUBLE PRECISION,             -- Điểm số (0-100)
    pass_status BOOLEAN,                -- Pass/Fail
    feedback TEXT,                      -- Nhận xét
    completed_at TIMESTAMP
);
```

**Ví dụ:**
```json
{
  "id": 1,
  "examId": 1,
  "userId": 3,
  "score": 85.5,
  "passStatus": true,
  "feedback": "Good understanding of ReactJS concepts. Strong performance on component lifecycle.",
  "completedAt": "2025-11-20T15:30:00"
}
```

### Mối quan hệ giữa các bảng:
```
exams (1) ──┬──> (N) exam_questions ──> questionId [Question Service]
            │
            ├──> (N) exam_registrations ──> userId [User Service]
            │
            ├──> (N) user_answers ──┬──> questionId [Question Service]
            │                        └──> userId [User Service]
            │
            └──> (N) results ──> userId [User Service]
```

---

## 🔄 LUỒNG HOẠT ĐỘNG

### **PHASE 1: TẠO BÀI THI** (Admin/Recruiter)

#### **Bước 1: Tạo Exam (DRAFT)**
```http
POST /exams
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
  "userId": 1,
  "examType": "TECHNICAL",
  "title": "ReactJS Developer Assessment",
  "position": "Frontend Developer",
  "topics": [1, 2, 3],                  // ReactJS, JavaScript, CSS
  "questionTypes": [1, 2],               // Multiple Choice, Essay
  "questionCount": 20,
  "duration": 60,                        // 60 phút
  "language": "en"
}
```

**Response:**
```json
{
  "id": 1,
  "status": "DRAFT",
  "title": "ReactJS Developer Assessment",
  "createdAt": "2025-11-20T10:00:00"
}
```

**Trạng thái:** `DRAFT` - Bài thi mới tạo, chưa công khai

---

#### **Bước 2: Thêm câu hỏi vào Exam**
```http
POST /exams/questions
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
  "examId": 1,
  "questionId": 1,                      // ID từ Question Service
  "orderNumber": 1
}
```

**Lặp lại** cho tất cả câu hỏi (ví dụ: 20 lần)

**Kết quả trong DB:**
```
exam_questions:
┌────┬─────────┬─────────────┬──────────────┐
│ id │ exam_id │ question_id │ order_number │
├────┼─────────┼─────────────┼──────────────┤
│ 1  │ 1       │ 1           │ 1            │
│ 2  │ 1       │ 2           │ 2            │
│ 3  │ 1       │ 3           │ 3            │
│... │ ...     │ ...         │ ...          │
│ 20 │ 1       │ 20          │ 20           │
└────┴─────────┴─────────────┴──────────────┘
```

---

#### **Bước 3: Publish Exam**
```http
POST /exams/1/publish?userId=1
Authorization: Bearer {{adminToken}}
```

**Response:**
```json
{
  "id": 1,
  "status": "PUBLISHED",
  "title": "ReactJS Developer Assessment"
}
```

**Trạng thái:** `DRAFT` → `PUBLISHED` (User có thể thấy và đăng ký)

---

### **PHASE 2: ĐĂNG KÝ THI** (User)

#### **Bước 1: Xem danh sách bài thi**
```http
GET /exams?page=0&size=10
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "ReactJS Developer Assessment",
      "position": "Frontend Developer",
      "duration": 60,
      "questionCount": 20,
      "status": "PUBLISHED"
    }
  ],
  "totalElements": 1
}
```

---

#### **Bước 2: Đăng ký tham gia**
```http
POST /exams/registrations
Authorization: Bearer {{userToken}}
Content-Type: application/json

{
  "examId": 1,
  "userId": 3
}
```

**Response:**
```json
{
  "id": 1,
  "examId": 1,
  "userId": 3,
  "registrationStatus": "REGISTERED",
  "registeredAt": "2025-11-20T11:00:00"
}
```

**Validation:**
- ✅ Exam phải có status = `PUBLISHED`
- ✅ User chưa đăng ký exam này trước đó
- ❌ Nếu đã đăng ký → Lỗi: "Already registered for this exam"

---

#### **Bước 3: Hủy đăng ký (nếu cần)**
```http
POST /exams/registrations/1/cancel
Authorization: Bearer {{userToken}}
```

**Response:**
```json
{
  "id": 1,
  "registrationStatus": "CANCELLED"
}
```

---

### **PHASE 3: LÀM BÀI THI** (User)

#### **Bước 1: Bắt đầu làm bài**
```http
POST /exams/1/start
Authorization: Bearer {{userToken}}
```

**Response:**
```json
{
  "id": 1,
  "status": "ONGOING",
  "startTime": "2025-11-20T14:00:00",
  "endTime": "2025-11-20T15:00:00",     // startTime + 60 phút
  "duration": 60
}
```

**Trạng thái:** `PUBLISHED` → `ONGOING`

---

#### **Bước 2: Lấy danh sách câu hỏi**
```
Frontend gọi 2 endpoints:
1. GET /exams/1 → Lấy exam info và exam_questions
2. GET /questions/{id} (Question Service) → Lấy nội dung câu hỏi
```

**Flow:**
```javascript
// 1. Lấy exam và question IDs
const exam = await fetch('/exams/1');
const questionIds = exam.questionIds; // [1, 2, 3, ..., 20]

// 2. Lấy nội dung câu hỏi từ Question Service
const questions = await Promise.all(
  questionIds.map(id => fetch(`/questions/${id}`))
);
```

---

#### **Bước 3: Submit câu trả lời (từng câu)**
```http
POST /exams/answers
Authorization: Bearer {{userToken}}
Content-Type: application/json

{
  "examId": 1,
  "questionId": 1,
  "userId": 3,
  "answerContent": "ReactJS is a JavaScript library for building user interfaces with component-based architecture."
}
```

**Response:**
```json
{
  "id": 1,
  "examId": 1,
  "questionId": 1,
  "userId": 3,
  "answerContent": "ReactJS is a JavaScript library...",
  "isCorrect": null,                    // Chưa chấm
  "similarityScore": null,
  "createdAt": "2025-11-20T14:05:00"
}
```

**Lặp lại** cho 20 câu hỏi → 20 requests

**Kết quả trong DB:**
```
user_answers:
┌────┬─────────┬─────────────┬─────────┬──────────────┬────────────┬──────────────────┐
│ id │ exam_id │ question_id │ user_id │ answer_...   │ is_correct │ similarity_score │
├────┼─────────┼─────────────┼─────────┼──────────────┼────────────┼──────────────────┤
│ 1  │ 1       │ 1           │ 3       │ ReactJS is...│ null       │ null             │
│ 2  │ 1       │ 2           │ 3       │ Virtual DOM..│ null       │ null             │
│... │ ...     │ ...         │ ...     │ ...          │ ...        │ ...              │
│ 20 │ 1       │ 20          │ 3       │ JSX is...    │ null       │ null             │
└────┴─────────┴─────────────┴─────────┴──────────────┴────────────┴──────────────────┘
```

---

#### **Bước 4: Chấm điểm (Auto hoặc Manual)**

**Option 1: Tự động chấm với NLP Service** (cho Essay questions)
```javascript
// Frontend hoặc Backend gọi NLP Service
for (const answer of userAnswers) {
  const result = await fetch('http://nlp-service:5000/evaluate', {
    method: 'POST',
    body: JSON.stringify({
      question: question.content,
      answer: answer.answerContent,
      correctAnswer: question.answer
    })
  });
  
  // Update similarity_score
  await fetch('/exams/answers/' + answer.id, {
    method: 'PATCH',
    body: JSON.stringify({
      similarityScore: result.score,  // 0.0 - 1.0
      isCorrect: result.score >= 0.7
    })
  });
}
```

**Option 2: Manual grading** (Admin/Recruiter chấm tay)
```http
PATCH /exams/answers/1
Authorization: Bearer {{adminToken}}

{
  "isCorrect": true,
  "similarityScore": 0.95
}
```

---

#### **Bước 5: Submit kết quả**
```http
POST /exams/results
Authorization: Bearer {{userToken}}
Content-Type: application/json

{
  "examId": 1,
  "userId": 3,
  "score": 85.5,                        // Tính từ user_answers
  "passStatus": true,                   // score >= 70 → Pass
  "feedback": "Good understanding of ReactJS concepts. Strong performance on component lifecycle and state management."
}
```

**Cách tính score:**
```javascript
// Lấy tất cả câu trả lời
const answers = await fetch('/exams/1/answers/3');

// Tính điểm trung bình
const totalScore = answers.reduce((sum, ans) => {
  return sum + (ans.similarityScore || 0);
}, 0);

const averageScore = (totalScore / answers.length) * 100;
// Ví dụ: (0.95 + 0.92 + 0.88 + ... + 0.85) / 20 * 100 = 85.5
```

**Response:**
```json
{
  "id": 1,
  "examId": 1,
  "userId": 3,
  "score": 85.5,
  "passStatus": true,
  "feedback": "Good understanding...",
  "completedAt": "2025-11-20T15:00:00"
}
```

---

#### **Bước 6: Hoàn thành bài thi**
```http
POST /exams/1/complete
Authorization: Bearer {{userToken}}
```

**Response:**
```json
{
  "id": 1,
  "status": "COMPLETED"
}
```

**Trạng thái:** `ONGOING` → `COMPLETED`

---

### **PHASE 4: XEM KẾT QUẢ**

#### **User xem kết quả của mình**
```http
GET /exams/results/user/3
Authorization: Bearer {{userToken}}
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "examId": 1,
      "examTitle": "ReactJS Developer Assessment",
      "score": 85.5,
      "passStatus": true,
      "feedback": "Good understanding...",
      "completedAt": "2025-11-20T15:00:00"
    }
  ]
}
```

---

#### **User xem chi tiết câu trả lời**
```http
GET /exams/1/answers/3
Authorization: Bearer {{userToken}}
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "questionId": 1,
      "answerContent": "ReactJS is a JavaScript library...",
      "isCorrect": true,
      "similarityScore": 0.95
    },
    {
      "id": 2,
      "questionId": 2,
      "answerContent": "Virtual DOM is...",
      "isCorrect": true,
      "similarityScore": 0.92
    }
  ]
}
```

---

#### **Admin xem tất cả kết quả của exam**
```http
GET /exams/1/results
Authorization: Bearer {{adminToken}}
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "userId": 3,
      "userName": "John Doe",            // Join với User Service
      "score": 85.5,
      "passStatus": true
    },
    {
      "id": 2,
      "userId": 4,
      "userName": "Jane Smith",
      "score": 72.0,
      "passStatus": true
    },
    {
      "id": 3,
      "userId": 5,
      "userName": "Bob Wilson",
      "score": 45.0,
      "passStatus": false
    }
  ]
}
```

---

#### **Admin xem danh sách đăng ký**
```http
GET /exams/1/registrations
Authorization: Bearer {{adminToken}}
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "userId": 3,
      "userName": "John Doe",
      "registrationStatus": "REGISTERED",
      "registeredAt": "2025-11-18T10:00:00"
    },
    {
      "id": 2,
      "userId": 4,
      "userName": "Jane Smith",
      "registrationStatus": "REGISTERED",
      "registeredAt": "2025-11-18T11:00:00"
    }
  ]
}
```

---

## 📡 API ENDPOINTS

### **Exam Management**
| Method | Endpoint | Role | Mô tả |
|--------|----------|------|-------|
| POST | `/exams` | Admin/Recruiter | Tạo bài thi mới |
| GET | `/exams` | All | Lấy danh sách bài thi |
| GET | `/exams/{id}` | All | Lấy chi tiết bài thi |
| PUT | `/exams/{id}` | Admin/Recruiter | Cập nhật bài thi |
| DELETE | `/exams/{id}` | Admin/Recruiter | Xóa bài thi |
| POST | `/exams/{id}/publish` | Admin/Recruiter | Publish bài thi |
| POST | `/exams/{id}/start` | User | Bắt đầu làm bài |
| POST | `/exams/{id}/complete` | User | Hoàn thành bài thi |
| GET | `/exams/user/{userId}` | User/Admin | Lấy bài thi của user |
| GET | `/exams/type?type=TECHNICAL` | All | Lấy bài thi theo loại |

### **Exam Questions**
| Method | Endpoint | Role | Mô tả |
|--------|----------|------|-------|
| POST | `/exams/questions` | Admin/Recruiter | Thêm câu hỏi vào exam |
| DELETE | `/exams/{examId}/questions` | Admin/Recruiter | Xóa tất cả câu hỏi |

### **Registrations**
| Method | Endpoint | Role | Mô tả |
|--------|----------|------|-------|
| POST | `/exams/registrations` | User | Đăng ký thi |
| POST | `/exams/registrations/{id}/cancel` | User | Hủy đăng ký |
| GET | `/exams/{examId}/registrations` | Admin/Recruiter | Xem danh sách đăng ký |
| GET | `/exams/registrations/user/{userId}` | User | Xem đăng ký của user |

### **Answers**
| Method | Endpoint | Role | Mô tả |
|--------|----------|------|-------|
| POST | `/exams/answers` | User | Submit câu trả lời |
| GET | `/exams/{examId}/answers/{userId}` | User/Admin | Xem câu trả lời |
| GET | `/exams/answers/{id}` | User/Admin | Xem 1 câu trả lời |

### **Results**
| Method | Endpoint | Role | Mô tả |
|--------|----------|------|-------|
| POST | `/exams/results` | User | Submit kết quả |
| GET | `/exams/{examId}/results` | Admin/Recruiter | Xem tất cả kết quả |
| GET | `/exams/results/user/{userId}` | User | Xem kết quả của user |
| GET | `/exams/results/{id}` | User/Admin | Xem 1 kết quả |

**Tổng cộng: 25 endpoints**

---

## 💡 VÍ DỤ THỰC TẾ

### **Scenario: Frontend Developer Assessment**

#### **1. Admin tạo bài thi ReactJS**
```bash
# Step 1: Tạo exam
curl -X POST http://localhost:8080/exams \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "examType": "TECHNICAL",
    "title": "ReactJS Developer Assessment",
    "position": "Frontend Developer",
    "topics": [1, 2, 3],
    "questionTypes": [1, 2],
    "questionCount": 20,
    "duration": 60,
    "language": "en"
  }'

# Response: {"id": 1, "status": "DRAFT"}

# Step 2: Thêm 20 câu hỏi
for i in {1..20}; do
  curl -X POST http://localhost:8080/exams/questions \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"examId\": 1, \"questionId\": $i, \"orderNumber\": $i}"
done

# Step 3: Publish
curl -X POST http://localhost:8080/exams/1/publish?userId=1 \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Response: {"id": 1, "status": "PUBLISHED"}
```

---

#### **2. User (John Doe) đăng ký và làm bài**
```bash
# Step 1: Đăng ký
curl -X POST http://localhost:8080/exams/registrations \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"examId": 1, "userId": 3}'

# Response: {"id": 1, "registrationStatus": "REGISTERED"}

# Step 2: Start exam
curl -X POST http://localhost:8080/exams/1/start \
  -H "Authorization: Bearer $USER_TOKEN"

# Response: {"id": 1, "status": "ONGOING", "endTime": "2025-11-20T15:00:00"}

# Step 3: Submit answers (20 câu)
curl -X POST http://localhost:8080/exams/answers \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "examId": 1,
    "questionId": 1,
    "userId": 3,
    "answerContent": "ReactJS is a JavaScript library for building user interfaces..."
  }'

# Lặp lại 19 lần nữa...

# Step 4: Submit result
curl -X POST http://localhost:8080/exams/results \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "examId": 1,
    "userId": 3,
    "score": 85.5,
    "passStatus": true,
    "feedback": "Good understanding of ReactJS"
  }'

# Step 5: Complete exam
curl -X POST http://localhost:8080/exams/1/complete \
  -H "Authorization: Bearer $USER_TOKEN"
```

---

#### **3. User xem kết quả**
```bash
curl -X GET http://localhost:8080/exams/results/user/3 \
  -H "Authorization: Bearer $USER_TOKEN"
```

**Response:**
```json
{
  "content": [
    {
      "examTitle": "ReactJS Developer Assessment",
      "score": 85.5,
      "passStatus": true,
      "completedAt": "2025-11-20T15:00:00"
    }
  ]
}
```

---

## 🔗 TÍCH HỢP VỚI CÁC SERVICE KHÁC

### **1. Question Service**
```
┌─────────────┐              ┌──────────────────┐
│ Exam Service│─────────────>│ Question Service │
└─────────────┘              └──────────────────┘
     Lưu                       Lấy nội dung
  questionId                   câu hỏi
```

**Flow:**
```javascript
// 1. Exam Service chỉ lưu questionId
await examService.addQuestionToExam({
  examId: 1,
  questionId: 42,      // ← Chỉ lưu ID
  orderNumber: 1
});

// 2. Frontend lấy nội dung từ Question Service
const question = await questionService.getQuestion(42);
// → { content: "What is ReactJS?", answer: "A JavaScript library..." }
```

**Vấn đề:** Exam Service không validate questionId có tồn tại không
**Giải pháp:** Nên thêm validation
```java
// ExamService.java
public ExamQuestionResponse addQuestionToExam(ExamQuestionRequest req) {
    // Validate question exists
    Question question = questionServiceClient.getQuestion(req.getQuestionId());
    if (question == null) {
        throw new BusinessException("Question not found: " + req.getQuestionId());
    }
    
    return mappers.toResponse(examQuestionRepository.save(mappers.toEntity(req)));
}
```

---

### **2. User Service**
```
┌─────────────┐              ┌──────────────┐
│ Exam Service│─────────────>│ User Service │
└─────────────┘              └──────────────┘
     Lưu                       Lấy thông tin
    userId                     user (name, email)
```

**Flow:**
```javascript
// 1. Exam Service lưu userId
const result = await examService.submitResult({
  examId: 1,
  userId: 3,       // ← Chỉ lưu ID
  score: 85.5
});

// 2. Frontend merge với User Service
const user = await userService.getUser(3);
// → { id: 3, fullName: "John Doe", email: "john@example.com" }

// 3. Hiển thị kết hợp
{
  userName: user.fullName,    // "John Doe"
  userEmail: user.email,      // "john@example.com"
  score: result.score         // 85.5
}
```

---

### **3. NLP Service (Optional)**
```
┌─────────────┐              ┌─────────────┐
│ Exam Service│─────────────>│ NLP Service │
└─────────────┘              └─────────────┘
  Gửi answer                  Chấm điểm
  cần chấm                    similarity
```

**API của NLP Service:**
```python
# POST http://nlp-service:5000/evaluate
{
  "question": "What is ReactJS?",
  "answer": "ReactJS is a JavaScript library for building user interfaces...",
  "correctAnswer": "ReactJS is a JavaScript library for building UI with component-based architecture."
}

# Response:
{
  "score": 0.95,           # Similarity score (0.0 - 1.0)
  "feedback": "Excellent answer. Captures the key concepts."
}
```

**Integration Flow:**
```java
// ExamService.java
public UserAnswerResponse submitAnswer(UserAnswerRequest req) {
    UserAnswer answer = mappers.toEntity(req);
    
    // 1. Lưu answer trước
    answer = userAnswerRepository.save(answer);
    
    // 2. Gọi NLP Service để chấm điểm (async)
    if (isEssayQuestion(req.getQuestionId())) {
        nlpServiceClient.evaluateAnswer(req)
            .thenAccept(result -> {
                // 3. Update similarity_score
                answer.setSimilarityScore(result.getScore());
                answer.setIsCorrect(result.getScore() >= 0.7);
                userAnswerRepository.save(answer);
            });
    }
    
    return mappers.toResponse(answer);
}
```

---

### **4. Career Service**
```
┌─────────────┐              ┌────────────────┐
│ Exam Service│<─────────────│ Career Service │
└─────────────┘              └────────────────┘
  Tạo exam                     Khi tạo job posting
  cho job
```

**Flow:**
```javascript
// Career Service tạo job posting
const job = await careerService.createJob({
  title: "Senior Frontend Developer",
  position: "Frontend Developer",
  requiredSkills: ["ReactJS", "TypeScript", "Redux"]
});

// Tự động tạo exam cho job này
const exam = await examService.createExam({
  userId: recruiterId,
  examType: "RECRUITER",
  title: `${job.title} - Technical Assessment`,
  position: job.position,
  topics: job.requiredSkills.map(skill => getTopicId(skill)),
  questionCount: 20,
  duration: 60
});

// Link exam với job
await careerService.updateJob(job.id, {
  examId: exam.id
});
```

---

## ⚠️ VẤN ĐỀ VÀ GIẢI PHÁP

### **Problem 1: Không có Timer/Deadline Enforcement**

**Vấn đề hiện tại:**
```java
// User có thể submit answer sau khi hết giờ
// Không có check endTime
public UserAnswerResponse submitAnswer(UserAnswerRequest req) {
    // ❌ Không validate thời gian
    return userAnswerRepository.save(mappers.toEntity(req));
}
```

**Giải pháp:**
```java
public UserAnswerResponse submitAnswer(UserAnswerRequest req) {
    Exam exam = examRepository.findById(req.getExamId()).orElseThrow();
    
    // ✅ Check deadline
    if (LocalDateTime.now().isAfter(exam.getEndTime())) {
        throw new BusinessException("Exam time has expired");
    }
    
    // ✅ Check status
    if (!"ONGOING".equals(exam.getStatus())) {
        throw new BusinessException("Exam is not active");
    }
    
    return userAnswerRepository.save(mappers.toEntity(req));
}
```

---

### **Problem 2: Không có Auto-Grading**

**Vấn đề hiện tại:**
```java
// Admin phải manual submit result
POST /exams/results
{
  "score": 85.5,    // ← Ai tính?
  "passStatus": true
}
```

**Giải pháp: Tự động tính điểm**
```java
public ResultResponse calculateAndSubmitResult(Long examId, Long userId) {
    // 1. Lấy tất cả câu trả lời
    List<UserAnswer> answers = userAnswerRepository
        .findByExamIdAndUserId(examId, userId);
    
    if (answers.isEmpty()) {
        throw new BusinessException("No answers found");
    }
    
    // 2. Tính điểm
    double totalScore = answers.stream()
        .mapToDouble(a -> a.getSimilarityScore() != null ? a.getSimilarityScore() : 0.0)
        .average()
        .orElse(0.0);
    
    double finalScore = totalScore * 100; // Convert to 0-100 scale
    
    // 3. Xác định pass/fail
    boolean passStatus = finalScore >= 70.0;
    
    // 4. Generate feedback
    String feedback = generateFeedback(finalScore, answers);
    
    // 5. Save result
    Result result = new Result();
    result.setExam(examRepository.findById(examId).orElseThrow());
    result.setUserId(userId);
    result.setScore(finalScore);
    result.setPassStatus(passStatus);
    result.setFeedback(feedback);
    result.setCompletedAt(LocalDateTime.now());
    
    return mappers.toResponse(resultRepository.save(result));
}

private String generateFeedback(double score, List<UserAnswer> answers) {
    if (score >= 90) {
        return "Excellent performance! Outstanding understanding of the subject.";
    } else if (score >= 80) {
        return "Good performance! Strong grasp of key concepts.";
    } else if (score >= 70) {
        return "Satisfactory performance. Passed the exam.";
    } else {
        return "Needs improvement. Please review the material and try again.";
    }
}
```

**API mới:**
```http
POST /exams/{examId}/auto-grade/{userId}
Authorization: Bearer {{adminToken}}

# Response:
{
  "score": 85.5,
  "passStatus": true,
  "feedback": "Good performance! Strong grasp of key concepts.",
  "totalAnswers": 20,
  "correctAnswers": 17
}
```

---

### **Problem 3: Không validate Business Rules**

**Vấn đề:**
- User có thể start exam mà chưa register?
- User có thể submit answer cho exam của người khác?
- Admin có thể publish exam không có câu hỏi?

**Giải pháp:**
```java
// 1. Validate registration trước khi start
public ExamResponse startExam(Long examId, Long userId) {
    Exam exam = examRepository.findById(examId).orElseThrow();
    
    // ✅ Check registration
    if (!examRegistrationRepository.existsByExamIdAndUserId(examId, userId)) {
        throw new BusinessException("User not registered for this exam");
    }
    
    // ✅ Check status
    if (!"PUBLISHED".equals(exam.getStatus())) {
        throw new BusinessException("Exam is not available");
    }
    
    exam.setStatus("ONGOING");
    exam.setStartTime(LocalDateTime.now());
    exam.setEndTime(LocalDateTime.now().plusMinutes(exam.getDuration()));
    
    return mappers.toResponse(examRepository.save(exam));
}

// 2. Validate ownership khi submit answer
public UserAnswerResponse submitAnswer(UserAnswerRequest req) {
    // ✅ Check user đang làm exam của mình
    ExamRegistration reg = examRegistrationRepository
        .findByExamIdAndUserId(req.getExamId(), req.getUserId())
        .orElseThrow(() -> new BusinessException("Access denied"));
    
    // ... rest of logic
}

// 3. Validate exam có câu hỏi trước khi publish
public ExamResponse publishExam(Long examId, Long userId) {
    Exam exam = examRepository.findById(examId).orElseThrow();
    
    // ✅ Check có câu hỏi
    long questionCount = examQuestionRepository.countByExamId(examId);
    if (questionCount == 0) {
        throw new BusinessException("Cannot publish exam without questions");
    }
    
    // ✅ Check số lượng câu hỏi đủ
    if (questionCount < exam.getQuestionCount()) {
        throw new BusinessException(
            String.format("Exam requires %d questions but only has %d",
                exam.getQuestionCount(), questionCount)
        );
    }
    
    exam.setStatus("PUBLISHED");
    return mappers.toResponse(examRepository.save(exam));
}
```

---

### **Problem 4: Thiếu Batch Operations**

**Vấn đề:** Submit 20 câu trả lời → 20 HTTP requests (chậm, tốn bandwidth)

**Giải pháp: Batch Submit API**
```java
@PostMapping("/exams/answers/batch")
public List<UserAnswerResponse> submitAnswersBatch(
    @RequestBody BatchAnswerRequest req
) {
    // Validate
    Exam exam = examRepository.findById(req.getExamId()).orElseThrow();
    if (LocalDateTime.now().isAfter(exam.getEndTime())) {
        throw new BusinessException("Exam time expired");
    }
    
    // Save all answers
    List<UserAnswer> answers = req.getAnswers().stream()
        .map(dto -> {
            UserAnswer answer = new UserAnswer();
            answer.setExam(exam);
            answer.setQuestionId(dto.getQuestionId());
            answer.setUserId(req.getUserId());
            answer.setAnswerContent(dto.getAnswerContent());
            answer.setCreatedAt(LocalDateTime.now());
            return answer;
        })
        .collect(Collectors.toList());
    
    List<UserAnswer> saved = userAnswerRepository.saveAll(answers);
    
    return saved.stream()
        .map(mappers::toResponse)
        .collect(Collectors.toList());
}
```

**DTO:**
```java
@Data
public class BatchAnswerRequest {
    private Long examId;
    private Long userId;
    private List<AnswerDto> answers;
}

@Data
public class AnswerDto {
    private Long questionId;
    private String answerContent;
}
```

**Usage:**
```http
POST /exams/answers/batch
Content-Type: application/json

{
  "examId": 1,
  "userId": 3,
  "answers": [
    {
      "questionId": 1,
      "answerContent": "ReactJS is..."
    },
    {
      "questionId": 2,
      "answerContent": "Virtual DOM..."
    },
    ... 18 more ...
  ]
}
```

**Kết quả:** 1 request thay vì 20 requests ✅

---

## 📊 TRẠNG THÁI EXAM (Status Flow)

```
      ┌───────┐
      │ DRAFT │ ← Bài thi mới tạo
      └───┬───┘
          │ publish
          ▼
    ┌───────────┐
    │ PUBLISHED │ ← User có thể xem và đăng ký
    └─────┬─────┘
          │ start
          ▼
     ┌─────────┐
     │ ONGOING │ ← Đang làm bài
     └────┬────┘
          │ complete
          ▼
    ┌───────────┐
    │ COMPLETED │ ← Đã hoàn thành
    └───────────┘
    
    ┌───────────┐
    │ CANCELLED │ ← Có thể cancel từ bất kỳ trạng thái nào
    └───────────┘
```

---

## 🎓 KẾT LUẬN

### **Ưu điểm của thiết kế hiện tại:**
✅ Tách biệt rõ ràng giữa exam và question (loose coupling)  
✅ Support nhiều loại exam (TECHNICAL, BEHAVIORAL, VIRTUAL, RECRUITER)  
✅ Flexible grading (manual hoặc auto với NLP)  
✅ Clear status flow (DRAFT → PUBLISHED → ONGOING → COMPLETED)  

### **Cần cải thiện:**
⚠️ Thêm validation cho business rules  
⚠️ Implement auto-grading  
⚠️ Thêm timer/deadline enforcement  
⚠️ Batch operations cho performance  
⚠️ Tích hợp với Question Service và NLP Service  
⚠️ Thêm notification khi exam sắp hết giờ  

### **Roadmap tiếp theo:**
1. ✅ Fix password hash issues (DONE)
2. 🔄 Implement auto-grading với NLP Service
3. 🔄 Add batch submit answers API
4. 🔄 Add timer/countdown trên frontend
5. 🔄 Add email notification khi có kết quả
6. 🔄 Add analytics dashboard cho Admin

---

**Last Updated:** November 20, 2025  
**Version:** 1.0  
**Author:** GitHub Copilot
