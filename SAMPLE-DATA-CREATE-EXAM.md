# Dữ Liệu Mẫu - API Tạo Exam với Random Questions

## Endpoint Mới
```
POST /exams/with-random-questions
```

## Mô Tả
Tạo exam và thêm câu hỏi ngẫu nhiên trong **một lần** gọi API duy nhất.

### Đặc điểm:
- **Status**: `DRAFT` (chưa publish, dùng cho tự luyện tập)
- **Type**: `PRACTICE` (bài tập tự học)
- **Permission**: `USER`, `ADMIN`, hoặc `RECRUITER` đều được phép
- **Tính năng**: Tự động tìm và shuffle câu hỏi phù hợp với tiêu chí

---

## Dữ Liệu Có Sẵn Trong Database

### Fields (Lĩnh vực)
```
- Lập trình viên
- Business Analyst
- Tester
- DevOps
- Data Science
```

### Topics (Chủ đề) cho "Lập trình viên"
```
- ReactJS
- Spring Boot
- VueJS
```

### Topics cho "Tester"
```
- Automated Testing
```

### Levels (Cấp độ)
```
- Fresher
- Junior
- Middle
- Senior
```

### Question Types (Loại câu hỏi)
```
- Multiple Choice
- Open Ended
```

---

## Phân Bố Câu Hỏi Hiện Tại

| Field | Topic | Level | Type | Số lượng |
|-------|-------|-------|------|----------|
| Lập trình viên | ReactJS | Junior | Multiple Choice | 2 |
| Lập trình viên | Spring Boot | Middle | Open Ended | 1 |
| Lập trình viên | ReactJS | Junior | Open Ended | 1 |
| Tester | Automated Testing | Fresher | Multiple Choice | 1 |
| Lập trình viên | VueJS | Fresher | Multiple Choice | 1 |

---

## Mẫu Request JSON

### 1. ReactJS Junior - Multiple Choice (2 câu)
```json
{
  "title": "Bài luyện tập ReactJS cơ bản",
  "position": "Front-end Developer",
  "duration": 60,
  "language": "vi",
  "field": "Lập trình viên",
  "topics": ["ReactJS"],
  "level": "Junior",
  "questionType": "Multiple Choice",
  "numberOfQuestions": 2
}
```

**Response mẫu**:
```json
{
  "examId": 10,
  "title": "Bài luyện tập ReactJS cơ bản",
  "status": "DRAFT",
  "duration": 60,
  "questionCount": 2,
  "questionIds": [1, 3]
}
```

---

### 2. Spring Boot Middle - Open Ended (1 câu)
```json
{
  "title": "Spring Boot Advanced Topics",
  "position": "Back-end Developer",
  "duration": 90,
  "language": "vi",
  "field": "Lập trình viên",
  "topics": ["Spring Boot"],
  "level": "Middle",
  "questionType": "Open Ended",
  "numberOfQuestions": 1
}
```

---

### 3. Automated Testing - Multiple Choice (1 câu)
```json
{
  "title": "Kiểm thử tự động cơ bản",
  "position": "QA Engineer",
  "duration": 45,
  "language": "vi",
  "field": "Tester",
  "topics": ["Automated Testing"],
  "level": "Fresher",
  "questionType": "Multiple Choice",
  "numberOfQuestions": 1
}
```

---

### 4. VueJS Fresher - Multiple Choice (1 câu)
```json
{
  "title": "VueJS Basics for Beginners",
  "position": "Front-end Developer",
  "duration": 30,
  "language": "vi",
  "field": "Lập trình viên",
  "topics": ["VueJS"],
  "level": "Fresher",
  "questionType": "Multiple Choice",
  "numberOfQuestions": 1
}
```

---

### 5. Nhiều Topics (hệ thống chọn ngẫu nhiên)
```json
{
  "title": "Full Stack Developer Practice",
  "position": "Full Stack Developer",
  "duration": 120,
  "language": "vi",
  "field": "Lập trình viên",
  "topics": ["ReactJS", "Spring Boot", "VueJS"],
  "level": "Junior",
  "questionType": "Multiple Choice",
  "numberOfQuestions": 3
}
```
> **Lưu ý**: Hệ thống sẽ tìm câu hỏi có **BẤT KỲ** topic nào trong danh sách

---

## Ví Dụ Curl

### Test với ReactJS
```bash
curl -X POST http://localhost:8080/exams/with-random-questions \
  -H "Content-Type: application/json; charset=utf-8" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title": "ReactJS Practice Exam",
    "position": "Developer",
    "duration": 60,
    "language": "vi",
    "field": "Lập trình viên",
    "topics": ["ReactJS"],
    "level": "Junior",
    "questionType": "Multiple Choice",
    "numberOfQuestions": 2
  }'
```

---

## Ví Dụ PowerShell

### Cách 1: Gọi trực tiếp exam-service (port 8086)
```powershell
# Không cần token khi gọi trực tiếp
$headers = @{
    'Content-Type' = 'application/json; charset=utf-8'
    'X-User-Id' = '1'
}

$body = @{
    title = 'ReactJS Practice'
    position = 'Developer'
    duration = 60
    language = 'vi'
    field = 'Lập trình viên'
    topics = @('ReactJS')
    level = 'Junior'
    questionType = 'Multiple Choice'
    numberOfQuestions = 2
} | ConvertTo-Json

$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($body)

$response = Invoke-RestMethod `
    -Uri 'http://localhost:8086/exams/with-random-questions' `
    -Method POST `
    -Headers $headers `
    -Body $bodyBytes `
    -ContentType 'application/json; charset=utf-8'

$response | ConvertTo-Json
```

### Cách 2: Qua API Gateway (port 8080)
```powershell
# Bước 1: Login để lấy token
$loginBody = '{"email":"admin@example.com","password":"admin123"}'
$loginResponse = Invoke-RestMethod `
    -Uri 'http://localhost:8080/auth/login' `
    -Method POST `
    -Headers @{'Content-Type'='application/json'} `
    -Body $loginBody

$token = $loginResponse.access_token

# Bước 2: Tạo exam
$headers = @{
    'Content-Type' = 'application/json; charset=utf-8'
    'Authorization' = "Bearer $token"
}

$body = @{
    title = 'ReactJS Practice via Gateway'
    position = 'Developer'
    duration = 60
    language = 'vi'
    field = 'Lập trình viên'
    topics = @('ReactJS')
    level = 'Junior'
    questionType = 'Multiple Choice'
    numberOfQuestions = 2
} | ConvertTo-Json

$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($body)

$response = Invoke-RestMethod `
    -Uri 'http://localhost:8080/exams/with-random-questions' `
    -Method POST `
    -Headers $headers `
    -Body $bodyBytes `
    -ContentType 'application/json; charset=utf-8'

Write-Host "Exam ID: $($response.examId)"
Write-Host "Status: $($response.status)"
Write-Host "Questions: $($response.questionCount)"
```

---

## Validation Rules

| Field | Required | Type | Constraints |
|-------|----------|------|-------------|
| title | ✅ | String | Max 200 ký tự |
| position | ❌ | String | - |
| duration | ✅ | Integer | Min: 1 phút |
| language | ✅ | String | Ví dụ: 'vi', 'en' |
| field | ❌ | String | Phải khớp với data trong DB |
| topics | ❌ | Array<String> | - |
| level | ❌ | String | - |
| questionType | ❌ | String | - |
| numberOfQuestions | ✅ | Integer | Min: 1, Max: 100 |

---

## Error Cases

### Không tìm thấy câu hỏi
**Request**:
```json
{
  "title": "Invalid Test",
  "duration": 30,
  "language": "en",
  "field": "Non-existent Field",
  "topics": ["Invalid Topic"],
  "level": "Senior",
  "questionType": "Multiple Choice",
  "numberOfQuestions": 10
}
```

**Response** (400 Bad Request):
```json
{
  "type": "https://errors.abc.com/RUNTIME_ERROR",
  "title": "Runtime Error",
  "status": 400,
  "detail": "Failed to create exam with random questions: No questions found matching the criteria",
  "errorCode": "RUNTIME_ERROR"
}
```

---

### Thiếu trường bắt buộc
**Request**:
```json
{
  "title": "Test",
  "duration": 60
}
```

**Response** (400 Bad Request):
```json
{
  "type": "https://errors.abc.com/VALIDATION_ERROR",
  "title": "Validation Error",
  "detail": "language must not be blank"
}
```

---

## So Sánh: Trước vs Sau

### ❌ Cách Cũ (2 bước)
```javascript
// Bước 1: Tạo exam
POST /exams
{
  "title": "My Exam",
  "duration": 60,
  "language": "vi"
}
// Response: { examId: 1 }

// Bước 2: Thêm câu hỏi ngẫu nhiên
POST /exams/questions/random
{
  "examId": 1,
  "field": "Lập trình viên",
  "topics": ["ReactJS"],
  "level": "Junior",
  "questionType": "Multiple Choice",
  "numberOfQuestions": 5
}
```

### ✅ Cách Mới (1 bước)
```javascript
POST /exams/with-random-questions
{
  "title": "My Exam",
  "duration": 60,
  "language": "vi",
  "field": "Lập trình viên",
  "topics": ["ReactJS"],
  "level": "Junior",
  "questionType": "Multiple Choice",
  "numberOfQuestions": 5
}
```

---

## Lợi Ích

1. **Giảm số lần gọi API**: Từ 2 request → 1 request
2. **Đơn giản hơn**: Không cần quản lý examId giữa 2 bước
3. **An toàn hơn**: Tránh trường hợp tạo exam nhưng quên thêm câu hỏi
4. **Phù hợp cho USER**: Người dùng thường có thể tự tạo bài luyện tập
5. **DRAFT status**: Không ảnh hưởng đến exam chính thức

---

## Testing Script

Chạy script test tự động:
```powershell
.\test-create-exam-with-questions.ps1
```

Hoặc xem file JSON với tất cả mẫu dữ liệu:
```powershell
Get-Content .\sample-data-create-exam-with-questions.json | ConvertFrom-Json
```
