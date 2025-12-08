# Exam Registration Policy Update

## 📋 Thay đổi

Đã cập nhật logic registration cho các loại exam:

### Trước đây:
- ✅ PRACTICE: Không cần registration
- ❌ VIRTUAL: Cần registration
- ❌ RECRUITER: Cần registration

### Bây giờ:
- ✅ PRACTICE: Không cần registration
- ✅ VIRTUAL: Không cần registration
- ❌ RECRUITER: Cần registration

## 🔧 Các thay đổi code

### 1. ExamService.requiresRegistration()

**Trước:**
```java
private boolean requiresRegistration(String examType) {
    return !"PRACTICE".equalsIgnoreCase(examType);
}
```

**Sau:**
```java
private boolean requiresRegistration(String examType) {
    // Only RECRUITER exams require registration
    return "RECRUITER".equalsIgnoreCase(examType);
}
```

### 2. GradingService.validateRegistration()

**Trước:**
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

**Sau:**
```java
private void validateRegistration(Exam exam, Long userId) {
    // Skip registration check for PRACTICE and VIRTUAL exams
    // Only RECRUITER exams require registration
    if (!"RECRUITER".equalsIgnoreCase(exam.getExamType())) {
        log.info("Skipping registration validation for {} exam: {}", exam.getExamType(), exam.getId());
        return;
    }
    
    boolean isRegistered = examRegistrationRepository
            .existsByExamIdAndUserIdAndRegistrationStatus(exam.getId(), userId, "REGISTERED");
    
    if (!isRegistered) {
        throw new InvalidRequestException("User is not registered for this exam");
    }
}
```

## 📊 Ma trận Registration Requirements

| Exam Type | Registration Required | Submit Answer | Submit Result | Submit Exam |
|-----------|----------------------|---------------|---------------|-------------|
| PRACTICE  | ❌ No | ✅ Yes | ✅ Yes | ✅ Yes |
| VIRTUAL   | ❌ No | ✅ Yes | ✅ Yes | ✅ Yes |
| RECRUITER | ✅ Yes | ⚠️ Need registration | ⚠️ Need registration | ⚠️ Need registration |

## 🎯 Use Cases

### PRACTICE Exam
- **Mục đích**: Tự luyện tập
- **Registration**: Không cần
- **Status khi tạo**: PUBLISHED
- **Ai có thể làm**: Bất kỳ user nào

### VIRTUAL Exam  
- **Mục đích**: Thi thử, đánh giá năng lực
- **Registration**: Không cần
- **Status khi tạo**: DRAFT → Admin publish
- **Ai có thể làm**: Bất kỳ user nào (sau khi exam được published)

### RECRUITER Exam
- **Mục đích**: Tuyển dụng chính thức
- **Registration**: Bắt buộc
- **Status khi tạo**: DRAFT → Admin publish
- **Ai có thể làm**: Chỉ user đã đăng ký

## 🧪 Testing

### Test PRACTICE Exam (không cần registration)
```bash
# 1. Create PRACTICE exam
POST /api/exams
{
  "examType": "PRACTICE",
  ...
}

# 2. Submit directly (no registration)
POST /api/exams/{examId}/submit
{
  "userId": 3,
  "answers": [...]
}
# Expected: ✅ Success
```

### Test VIRTUAL Exam (không cần registration)
```bash
# 1. Create VIRTUAL exam
POST /api/exams
{
  "examType": "VIRTUAL",
  ...
}

# 2. Admin publish exam
POST /api/exams/{examId}/publish

# 3. Submit directly (no registration)
POST /api/exams/{examId}/submit
{
  "userId": 3,
  "answers": [...]
}
# Expected: ✅ Success
```

### Test RECRUITER Exam (cần registration)
```bash
# 1. Create RECRUITER exam
POST /api/exams
{
  "examType": "RECRUITER",
  ...
}

# 2. Admin publish exam
POST /api/exams/{examId}/publish

# 3. Try to submit without registration
POST /api/exams/{examId}/submit
{
  "userId": 3,
  "answers": [...]
}
# Expected: ❌ Error "User is not registered for this exam"

# 4. Register first
POST /api/exams/registrations
{
  "examId": 123,
  "userId": 3
}

# 5. Submit after registration
POST /api/exams/{examId}/submit
{
  "userId": 3,
  "answers": [...]
}
# Expected: ✅ Success
```

## 📁 Files Modified

1. `exam-service/src/main/java/com/abc/exam_service/service/ExamService.java`
   - Updated `requiresRegistration()` method

2. `exam-service/src/main/java/com/abc/exam_service/service/GradingService.java`
   - Updated `validateRegistration()` method

## 🚀 Deployment

Service đã được rebuild và restart:
```bash
docker-compose build exam-service
docker-compose stop exam-service
docker-compose rm -f exam-service
docker-compose up -d exam-service
```

Status: ✅ Running

## ✅ Verification

Để verify thay đổi hoạt động:

```bash
# Check logs
docker logs interview-exam-service | grep "Skipping registration validation"
```

Bạn sẽ thấy:
```
INFO ... Skipping registration validation for PRACTICE exam: 135
INFO ... Skipping registration validation for VIRTUAL exam: 136
```

Nhưng KHÔNG thấy cho RECRUITER exams.

## 🎉 Summary

- ✅ PRACTICE exams: Không cần registration (như trước)
- ✅ VIRTUAL exams: Không cần registration (MỚI)
- ✅ RECRUITER exams: Vẫn cần registration (không đổi)

User giờ có thể làm cả PRACTICE và VIRTUAL exams mà không cần đăng ký!
