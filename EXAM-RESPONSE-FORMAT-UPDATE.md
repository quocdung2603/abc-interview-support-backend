# Exam Service - Cập nhật Response Format

## Mục tiêu
Cập nhật response format của exam service để match với yêu cầu:
- Thêm `questionTypeIds` (array) thay vì chỉ có `questionTypes`
- Đảm bảo `topicIds` là array trong exam
- Questions trả về `topicId` (singular) từ question-service

## Cấu trúc Response mới

### ExamResponse
```json
{
    "id": 47,
    "userId": 1,
    "examType": "VIRTUAL",
    "title": "Java Backend Developer Interview",
    "position": "Senior Backend Developer",
    "fieldId": 2,
    "levelId": 2,
    "topicIds": [1, 2, 3],
    "questionTypeIds": [1, 2],
    "questionCount": 20,
    "duration": 60,
    "status": "DRAFT",
    "language": "vi",
    "createdAt": "2025-11-26T00:37:16.612293",
    "createdBy": 1,
    "questions": [
        {
            "id": 3,
            "fieldId": 1,
            "topicId": 1,
            "levelId": 1,
            "questionTypeId": 1,
            "questionText": "What is VueJS and how does it differ from ReactJS?",
            "questionAnswer": "VueJS is a progressive JavaScript framework..."
        }
    ]
}
```

## Thay đổi đã thực hiện

### 1. ExamRequest.java
**Thêm:**
```java
private List<Long> questionTypeIds; // Multiple question type IDs
```

### 2. ExamResponse.java
**Thêm:**
```java
private List<Long> questionTypeIds; // Multiple question type IDs
```

### 3. QuestionDTO.java
**Thay đổi:**
```java
// Trước:
private List<Long> topicIds;

// Sau:
private Long topicId; // Single topic ID from question-service
```

**Lý do:** Question-service trả về `topicId` (singular), không phải `topicIds` (plural)

### 4. Exam.java (Entity)
**Thêm:**
```java
private String questionTypeIds; // JSON array of question type IDs
```

### 5. Mappers.java
**Thêm mapping:**
```java
@Mapping(target = "questionTypeIds", expression = "java(convertListToString(req.getQuestionTypeIds()))")
public abstract Exam toEntity(ExamRequest req);

@Mapping(target = "questionTypeIds", expression = "java(convertStringToList(entity.getQuestionTypeIds()))")
public abstract ExamResponse toResponse(Exam entity);
```

### 6. QuestionServiceClient.java
**Đơn giản hóa mapping:**
```java
// Map topicId from question-service (single value)
if (map.get("topicId") != null) {
    dto.setTopicId(((Number) map.get("topicId")).longValue());
}
```

### 7. Database Migration
**File:** `V4__add_question_type_ids_column.sql`
```sql
ALTER TABLE exams ADD COLUMN IF NOT EXISTS question_type_ids TEXT;
CREATE INDEX IF NOT EXISTS idx_exams_question_type_ids ON exams(question_type_ids);
```

## So sánh trước và sau

### Trước
```json
{
    "topicIds": [1, 2, 3],
    "questionTypes": [1, 2],  // Deprecated format
    "questions": [
        {
            "topicIds": [1]  // Array (không đúng với question-service)
        }
    ]
}
```

### Sau
```json
{
    "topicIds": [1, 2, 3],
    "questionTypeIds": [1, 2],  // New format
    "questions": [
        {
            "topicId": 1  // Single value (đúng với question-service)
        }
    ]
}
```

## Backward Compatibility
Vẫn giữ các field cũ để tương thích ngược:
- `topics` (deprecated)
- `questionTypes` (deprecated)

Các field mới được ưu tiên sử dụng:
- `topicIds` (preferred)
- `questionTypeIds` (preferred)

## Kết quả
- ✅ Thêm `questionTypeIds` vào request/response
- ✅ Questions trả về `topicId` (singular) đúng với question-service
- ✅ Database migration thành công
- ✅ Build và deploy thành công
- ✅ Service khởi động bình thường
- ✅ Backward compatibility được duy trì
