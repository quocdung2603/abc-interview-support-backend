# Exam Service - Numeric IDs Fix

## Vấn đề (Problem)

API `/exams/with-random-questions` gặp lỗi encoding tiếng Việt khi lưu và truy vấn dữ liệu:
- **Trước**: Lưu field, topics, level dạng **text strings** → Lỗi encoding "Lập trình viên" → "L???p tr??nh vi??n"
- **Hậu quả**: Truy vấn text bị sai, không tìm được dữ liệu

## Giải pháp (Solution)

Chuyển sang sử dụng **numeric IDs** thay vì text strings:
- ✅ Lưu `fieldId`, `topicId`, `levelId` (Long) thay vì text
- ✅ Truy vấn bằng IDs → Không phụ thuộc encoding
- ✅ Response trả về cả IDs và names

## Thay đổi (Changes)

### 1. Database Schema
```sql
-- Added columns to exams table
ALTER TABLE exams ADD COLUMN field_id BIGINT;
ALTER TABLE exams ADD COLUMN topic_id BIGINT;
ALTER TABLE exams ADD COLUMN level_id BIGINT;
```

### 2. DTOs Updated

#### ExamRequest.java
```java
@NotNull(message = "Field ID is required")
private Long fieldId;

@NotNull(message = "Topic ID is required")
private Long topicId;

@NotNull(message = "Level ID is required")
private Long levelId;
```

#### CreateExamWithQuestionsRequest.java
```java
private Long fieldId;           // Changed from String field
private List<Long> topicIds;    // Changed from List<String> topics
private Long levelId;           // Changed from String level
private Long questionTypeId;
```

#### QuestionDTO.java (Response)
```java
// Numeric IDs (preferred - encoding independent)
private Long fieldId;
private List<Long> topicIds;
private Long levelId;
private Long questionTypeId;

// Text names (for display - may have encoding issues)
private String field;
private List<String> topics;
private String level;
private String questionType;
```

### 3. Service Layer

#### ExamService.createExamWithRandomQuestions()
```java
// Store numeric IDs to avoid Unicode encoding issues
exam.setFieldId(req.getFieldId());
exam.setTopicId(req.getTopicIds() != null && !req.getTopicIds().isEmpty() 
    ? req.getTopicIds().get(0) : null);
exam.setLevelId(req.getLevelId());
```

#### ExamService.createExam() - Added Validation
```java
// Validate that referenced IDs exist
if (req.getFieldId() != null && !questionServiceClient.fieldExists(req.getFieldId())) {
    throw new RuntimeException("Field not found with id: " + req.getFieldId());
}
// Similar validation for topicId and levelId
```

### 4. Question Service

#### New Endpoint: GET /questions/search
```
Query Parameters:
- fieldId (Long)
- topicIds (List<Long>)
- levelId (Long)
- questionTypeId (Long)
- limit (Integer)
```

## API Usage

### Before (❌ Lỗi encoding)
```json
POST /exams/with-random-questions
{
  "title": "Test Exam",
  "field": "Lập trình viên",      // ❌ Text string
  "topics": ["Spring Boot"],       // ❌ Text strings
  "level": "Junior"                // ❌ Text string
}
```

### After (✅ Không lỗi encoding)
```json
POST /exams/with-random-questions
{
  "title": "Test Exam",
  "fieldId": 1,                    // ✅ Numeric ID
  "topicIds": [1, 2, 3],          // ✅ Numeric IDs
  "levelId": 2,                    // ✅ Numeric ID
  "questionTypeId": 1,
  "numberOfQuestions": 5
}
```

### Response Format
```json
{
  "examId": 56,
  "title": "Spring Boot Advanced Topics",
  "status": "DRAFT",
  "duration": 90,
  "questionCount": 1,
  "questionIds": [2],
  "questions": [
    {
      "id": 2,
      "fieldId": 1,                    // ✅ Numeric ID
      "topicIds": [1],                 // ✅ Numeric IDs
      "levelId": 2,                    // ✅ Numeric ID
      "questionTypeId": 1,             // ✅ Numeric ID
      "field": "Lập trình viên",       // ✅ Text hiển thị đúng
      "topics": ["ReactJS"],
      "level": "Junior",
      "questionType": "Open Ended",
      "questionText": "Explain the concept of Virtual DOM in ReactJS",
      "questionAnswer": "Virtual DOM is a JavaScript representation..."
    }
  ]
}
```

## Testing

### Postman Collection
Sử dụng file: `Exam-Numeric-IDs-API.postman_collection.json`

### Test Steps
1. Get available fields: `GET /questions/fields`
2. Get available topics: `GET /questions/topics`
3. Get available levels: `GET /questions/levels`
4. Create exam with numeric IDs: `POST /exams/with-random-questions`
5. Verify response contains both IDs and names without encoding errors

## Benefits

✅ **Encoding Independent**: Không phụ thuộc vào character encoding
✅ **Performance**: Truy vấn bằng numeric IDs nhanh hơn text matching
✅ **Data Integrity**: Foreign key constraints đảm bảo tính toàn vẹn
✅ **Backward Compatible**: Vẫn giữ text fields cho display
✅ **Vietnamese Support**: Hiển thị tiếng Việt chính xác trong response

## Migration

Database migration tự động chạy khi khởi động service:
- File: `exam-service/src/main/resources/db/migration/V2__add_exam_metadata_ids.sql`
- Thêm columns: `field_id`, `topic_id`, `level_id`
- Tạo indexes cho performance
- Nullable để backward compatible

## Notes

- Old API endpoints vẫn hoạt động (backward compatible)
- Recommend sử dụng numeric IDs cho tất cả API mới
- Text fields được giữ lại chỉ để hiển thị (display only)
