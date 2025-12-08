# Hướng dẫn Test PRACTICE Exam Type

## ✅ Service đã được deploy

Service exam-service đã được rebuild và restart với code mới:
- Container ID mới đã được tạo
- Service đang chạy trên port 8086
- Đã register với Eureka Discovery

## 🧪 Cách test

### Option 1: Qua Gateway (Recommended)

**Endpoint:** `http://localhost:8080/api/exams`

**1. Tạo PRACTICE Exam**

```bash
POST http://localhost:8080/api/exams
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "userId": 3,
  "examType": "PRACTICE",
  "title": "Java Backend Developer Practice Test",
  "position": "Backend Developer",
  "fieldId": 1,
  "topicIds": [1, 2, 3],
  "levelId": 2,
  "questionTypeIds": [1, 2],
  "questionCount": 20,
  "duration": 60,
  "language": "vi"
}
```

**Expected Response:**
```json
{
  "id": 134,
  "userId": 3,
  "examType": "PRACTICE",
  "title": "Java Backend Developer Practice Test",
  "status": "PUBLISHED",  // ← Phải là PUBLISHED, không phải DRAFT!
  "createdAt": "2025-12-08T13:30:00",
  ...
}
```

**2. Verify trong logs:**

```powershell
docker logs interview-exam-service --tail 50 | Select-String -Pattern "Auto-publishing"
```

Bạn sẽ thấy:
```
INFO ... Auto-publishing PRACTICE exam: Java Backend Developer Practice Test
```

**3. Submit Answer (không cần registration)**

```bash
POST http://localhost:8080/api/exams/answers
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "examId": 134,
  "userId": 3,
  "questionId": 1,
  "answerContent": "My answer here"
}
```

**Expected:** ✅ Success (không báo lỗi registration required)

**4. Submit Result (không cần registration)**

```bash
POST http://localhost:8080/api/exams/results
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "examId": 134,
  "userId": 3,
  "score": 85.5,
  "passStatus": true,
  "feedback": "Good job!"
}
```

**Expected:** ✅ Success (không báo lỗi registration required)

### Option 2: Direct to Service (cho testing)

**Endpoint:** `http://localhost:8086/exams`

Tương tự như trên nhưng dùng port 8086 thay vì 8080.

## 🔍 Verify Implementation

### 1. Check Status Field

**PRACTICE exam:**
```json
{
  "examType": "PRACTICE",
  "status": "PUBLISHED"  // ✅ Correct
}
```

**VIRTUAL/RECRUITER exam:**
```json
{
  "examType": "VIRTUAL",
  "status": "DRAFT"  // ✅ Correct (unchanged)
}
```

### 2. Check Logs

Sau khi tạo PRACTICE exam, check logs:

```powershell
# Xem log auto-publish
docker logs interview-exam-service | Select-String -Pattern "Auto-publishing"

# Xem tất cả logs gần đây
docker logs interview-exam-service --tail 100
```

### 3. Test Registration Validation

**Test A: PRACTICE exam - không cần registration**
1. Tạo PRACTICE exam (id=134)
2. Submit answer trực tiếp → ✅ Should work
3. Submit result trực tiếp → ✅ Should work

**Test B: VIRTUAL exam - cần registration**
1. Tạo VIRTUAL exam (id=135)
2. Submit answer trực tiếp → ❌ Should fail với error "User must register for this exam before submitting"
3. Register cho exam
4. Submit answer → ✅ Should work now

## 🐛 Troubleshooting

### Issue: Status vẫn là "DRAFT"

**Nguyên nhân:** Service chưa được restart với code mới

**Giải pháp:**
```powershell
# Stop và remove container cũ
docker-compose stop exam-service
docker-compose rm -f exam-service

# Start lại với image mới
docker-compose up -d exam-service

# Đợi 20 giây để service khởi động
timeout /t 20 /nobreak

# Verify service đã start
docker logs interview-exam-service --tail 30
```

### Issue: Không thấy log "Auto-publishing"

**Nguyên nhân:** Chưa có exam nào được tạo sau khi restart

**Giải pháp:** Tạo một PRACTICE exam mới qua API

### Issue: Submit answer vẫn báo lỗi registration

**Nguyên nhân:** 
1. Service chưa được restart
2. Exam type không phải "PRACTICE" (kiểm tra lại examType trong response)

**Giải pháp:**
1. Restart service như hướng dẫn trên
2. Verify examType trong response khi tạo exam

## 📊 Test Results Expected

| Test Case | Expected Result |
|-----------|----------------|
| Create PRACTICE exam | status = "PUBLISHED" |
| Create VIRTUAL exam | status = "DRAFT" |
| Submit answer to PRACTICE | ✅ Success (no registration) |
| Submit answer to VIRTUAL | ❌ Error (registration required) |
| Submit result to PRACTICE | ✅ Success (no registration) |
| Submit result to VIRTUAL | ❌ Error (registration required) |
| Log message | "Auto-publishing PRACTICE exam: ..." |

## 🎯 Quick Test Script

Nếu bạn muốn test nhanh, dùng Postman collection hoặc curl:

```bash
# 1. Tạo PRACTICE exam
curl -X POST http://localhost:8080/api/exams \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 3,
    "examType": "PRACTICE",
    "title": "Quick Test",
    "position": "Developer",
    "fieldId": 1,
    "topicIds": [1],
    "levelId": 1,
    "questionTypeIds": [1],
    "questionCount": 5,
    "duration": 30,
    "language": "en"
  }'

# 2. Check logs
docker logs interview-exam-service | grep "Auto-publishing"
```

## ✅ Success Criteria

Tính năng hoạt động đúng khi:
1. ✅ PRACTICE exam có status="PUBLISHED" ngay khi tạo
2. ✅ VIRTUAL/RECRUITER exam vẫn có status="DRAFT" (backward compatible)
3. ✅ Có log "Auto-publishing PRACTICE exam: ..." trong logs
4. ✅ Submit answer/result cho PRACTICE exam không cần registration
5. ✅ Submit answer/result cho VIRTUAL/RECRUITER exam vẫn cần registration

---

**Note:** Service đã được deploy thành công. Bạn chỉ cần test qua API để verify tính năng hoạt động đúng!
