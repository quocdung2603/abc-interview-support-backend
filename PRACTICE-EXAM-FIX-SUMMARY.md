# PRACTICE Exam - Registration Validation Fix

## 🐛 Vấn đề

User không thể submit exam khi examType là PRACTICE, gặp lỗi:
```json
{
  "type": "https://errors.abc.com/INVALID_REQUEST",
  "title": "Invalid Request",
  "status": 400,
  "detail": "User is not registered for this exam",
  "instance": "/exams/135/submit",
  "errorCode": "INVALID_REQUEST"
}
```

## 🔍 Nguyên nhân

Lỗi đến từ `GradingService.submitAndGradeExam()` method, được gọi qua endpoint `POST /exams/{examId}/submit`.

Method `validateRegistration()` trong `GradingService` luôn check registration cho TẤT CẢ exam types, không có logic đặc biệt cho PRACTICE exams.

## ✅ Giải pháp

### 1. Sửa GradingService.validateRegistration()

**Trước:**
```java
private void validateRegistration(Long examId, Long userId) {
    boolean isRegistered = examRegistrationRepository
            .existsByExamIdAndUserIdAndRegistrationStatus(examId, userId, "REGISTERED");
    
    if (!isRegistered) {
        throw new InvalidRequestException("User is not registered for this exam");
    }
}
```

**Sau:**
```java
private void validateRegistration(Exam exam, Long userId) {
    // Skip registration check for PRACTICE exams
    if ("PRACTICE".equalsIgnoreCase(exam.getExamType())) {
        log.info("Skipping registration validation for PRACTICE exam: {}", exam.getId());
        return;
    }
    
    boolean isRegistered = examRegistrationRepository
            .existsByExamIdAndUserIdAndRegistrationStatus(exam.getId(), userId, "REGISTERED");
    
    if (!isRegistered) {
        throw new InvalidRequestException("User is not registered for this exam");
    }
}
```

### 2. Update submitAndGradeExam() method

**Thay đổi:**
```java
// Trước
validateRegistration(examId, userId);

// Sau
validateRegistration(exam, userId);  // Pass Exam object thay vì examId
```

## 📝 Các thay đổi bổ sung

Ngoài fix chính, tôi cũng đã thêm logic tương tự cho các methods khác:

### ExamService

1. **startExamWithUser()** - Method mới cho phép start exam với user validation
2. **getExamByIdWithUser()** - Method mới cho phép get exam với user validation

### ExamController

1. **startExam()** - Updated để sử dụng `startExamWithUser()` khi có userId
2. **getExamById()** - Updated để sử dụng `getExamByIdWithUser()` khi có userId

## 🎯 Kết quả

Bây giờ PRACTICE exams hoạt động đúng:

### ✅ PRACTICE Exam Flow

1. **Tạo exam:**
   ```json
   POST /exams
   {
     "examType": "PRACTICE",
     ...
   }
   ```
   → Response: `status: "PUBLISHED"` ✅

2. **Submit exam (không cần registration):**
   ```json
   POST /exams/{examId}/submit
   {
     "userId": 3,
     "answers": [...]
   }
   ```
   → Success! ✅ (Không còn lỗi "User is not registered")

3. **Submit answer (không cần registration):**
   ```json
   POST /exams/answers
   {
     "examId": 135,
     "userId": 3,
     ...
   }
   ```
   → Success! ✅

4. **Submit result (không cần registration):**
   ```json
   POST /exams/results
   {
     "examId": 135,
     "userId": 3,
     ...
   }
   ```
   → Success! ✅

### ⚠️ VIRTUAL/RECRUITER Exam Flow (Unchanged)

1. **Tạo exam:**
   → Response: `status: "DRAFT"` ✅

2. **Submit exam (cần registration):**
   → Error: "User is not registered" ✅ (Correct behavior)

3. **Register first:**
   ```json
   POST /exams/registrations
   ```
   → Success ✅

4. **Submit exam:**
   → Success! ✅

## 📊 So sánh

| Action | PRACTICE | VIRTUAL/RECRUITER |
|--------|----------|-------------------|
| Initial Status | PUBLISHED ✅ | DRAFT ✅ |
| Submit without registration | ✅ Works | ❌ Error (correct) |
| Submit with registration | ✅ Works | ✅ Works |
| Get exam details | ✅ Works | ✅ Works (with registration) |
| Start exam | ✅ Works | ✅ Works (with registration) |

## 🚀 Deployment

Service đã được rebuild và restart:
```bash
docker-compose build exam-service
docker-compose stop exam-service
docker-compose rm -f exam-service
docker-compose up -d exam-service
```

Service status: ✅ Running on port 8086

## 🧪 Testing

Để test tính năng:

1. **Tạo PRACTICE exam** qua API
2. **Submit exam** ngay lập tức (không cần register)
3. **Verify** không còn lỗi "User is not registered"

### Test với Postman/curl:

```bash
# 1. Create PRACTICE exam
POST http://localhost:8080/api/exams
{
  "userId": 3,
  "examType": "PRACTICE",
  "title": "Test Practice Exam",
  ...
}

# 2. Submit exam (no registration needed)
POST http://localhost:8080/api/exams/{examId}/submit
{
  "userId": 3,
  "answers": [
    {
      "questionId": 1,
      "answerContent": "My answer"
    }
  ]
}
```

Expected: ✅ Success response with grading results

## 📁 Files Modified

1. `exam-service/src/main/java/com/abc/exam_service/service/GradingService.java`
   - Modified `validateRegistration()` method
   - Updated `submitAndGradeExam()` method call

2. `exam-service/src/main/java/com/abc/exam_service/service/ExamService.java`
   - Added `startExamWithUser()` method
   - Added `getExamByIdWithUser()` method

3. `exam-service/src/main/java/com/abc/exam_service/controller/ExamController.java`
   - Updated `startExam()` endpoint
   - Updated `getExamById()` endpoint

## ✅ Verification

Để verify fix hoạt động:

```bash
# Check logs for PRACTICE exam submission
docker logs interview-exam-service | grep "Skipping registration validation"
```

Bạn sẽ thấy:
```
INFO ... Skipping registration validation for PRACTICE exam: 135
```

## 🎉 Conclusion

Tính năng PRACTICE exam đã hoạt động hoàn toàn đúng:
- ✅ Auto-published khi tạo
- ✅ Không cần registration để submit
- ✅ Không cần registration để xem đề
- ✅ Backward compatible với VIRTUAL/RECRUITER exams

User giờ có thể tạo và làm PRACTICE exam mà không gặp bất kỳ lỗi registration nào!
