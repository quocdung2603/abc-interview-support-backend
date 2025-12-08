# Requirements Document

## Introduction

Hệ thống exam-service hiện tại hỗ trợ hai loại exam: VIRTUAL và RECRUITER. Cả hai loại này đều yêu cầu quy trình đăng ký (registration) và phê duyệt từ admin trước khi user có thể làm bài. Tính năng mới này sẽ bổ sung thêm loại exam thứ ba là PRACTICE - cho phép user tự tạo và làm bài tập luyện tập mà không cần đăng ký hay phê duyệt.

## Glossary

- **Exam Service**: Dịch vụ quản lý các kỳ thi và bài kiểm tra trong hệ thống
- **PRACTICE Exam**: Loại exam do user tự tạo để luyện tập, không yêu cầu đăng ký hay phê duyệt
- **VIRTUAL Exam**: Loại exam ảo yêu cầu đăng ký và phê duyệt
- **RECRUITER Exam**: Loại exam tuyển dụng yêu cầu đăng ký và phê duyệt
- **Exam Status**: Trạng thái của exam (DRAFT, PUBLISHED, ONGOING, COMPLETED, CANCELLED)
- **Registration**: Quy trình đăng ký tham gia exam
- **User Answer**: Câu trả lời của user cho một câu hỏi trong exam
- **Result**: Kết quả tổng hợp của user sau khi hoàn thành exam

## Requirements

### Requirement 1

**User Story:** Là một user, tôi muốn tạo exam loại PRACTICE để tự luyện tập mà không cần chờ phê duyệt, để tôi có thể học tập linh hoạt theo nhu cầu của mình.

#### Acceptance Criteria

1. WHEN a user creates an exam with examType "PRACTICE", THE Exam Service SHALL set the status field to "PUBLISHED" automatically
2. WHEN a user creates an exam with examType "PRACTICE", THE Exam Service SHALL store all exam metadata including title, position, fieldId, topicIds, levelId, questionTypeIds, questionCount, duration, and language
3. WHEN a user creates an exam with examType "PRACTICE", THE Exam Service SHALL set the createdBy field to the user's ID
4. WHEN a user creates an exam with examType "PRACTICE", THE Exam Service SHALL set the createdAt field to the current timestamp
5. WHEN a user creates an exam with examType "VIRTUAL" or "RECRUITER", THE Exam Service SHALL set the status field to "DRAFT" as before

### Requirement 2

**User Story:** Là một user, tôi muốn submit câu trả lời cho PRACTICE exam mà không cần đăng ký trước, để tôi có thể bắt đầu luyện tập ngay lập tức.

#### Acceptance Criteria

1. WHEN a user submits an answer for a PRACTICE exam, THE Exam Service SHALL accept and store the answer without checking for registration
2. WHEN a user submits an answer for a VIRTUAL or RECRUITER exam, THE Exam Service SHALL verify that the user has a valid registration before accepting the answer
3. WHEN a user submits an answer for any exam type, THE Exam Service SHALL store the examId, userId, questionId, userAnswer, and createdAt timestamp
4. WHEN a user submits an answer for a non-existent exam, THE Exam Service SHALL return an error message

### Requirement 3

**User Story:** Là một user, tôi muốn submit kết quả cho PRACTICE exam mà không cần đăng ký trước, để tôi có thể xem điểm số và đánh giá năng lực của mình.

#### Acceptance Criteria

1. WHEN a user submits a result for a PRACTICE exam, THE Exam Service SHALL accept and store the result without checking for registration
2. WHEN a user submits a result for a VIRTUAL or RECRUITER exam, THE Exam Service SHALL verify that the user has a valid registration before accepting the result
3. WHEN a user submits a result for any exam type, THE Exam Service SHALL store the examId, userId, score, totalQuestions, correctAnswers, and completedAt timestamp
4. WHEN a user submits a result for a non-existent exam, THE Exam Service SHALL return an error message

### Requirement 4

**User Story:** Là một developer, tôi muốn hệ thống phân biệt rõ ràng giữa các loại exam, để logic nghiệp vụ được xử lý chính xác cho từng loại.

#### Acceptance Criteria

1. WHEN the system processes an exam operation, THE Exam Service SHALL identify the exam type from the examType field
2. WHEN the examType is "PRACTICE", THE Exam Service SHALL skip registration validation for submit operations
3. WHEN the examType is "VIRTUAL" or "RECRUITER", THE Exam Service SHALL enforce registration validation for submit operations
4. WHEN the examType is invalid or null, THE Exam Service SHALL return a validation error

### Requirement 5

**User Story:** Là một user, tôi muốn tạo PRACTICE exam với câu hỏi ngẫu nhiên, để tôi có thể luyện tập với các bộ đề đa dạng.

#### Acceptance Criteria

1. WHEN a user creates a PRACTICE exam with random questions, THE Exam Service SHALL fetch questions from Question Service based on fieldId, topicIds, levelId, and questionTypeId
2. WHEN a user creates a PRACTICE exam with random questions, THE Exam Service SHALL shuffle the fetched questions and select the requested number
3. WHEN a user creates a PRACTICE exam with random questions, THE Exam Service SHALL set the exam status to "PUBLISHED" immediately
4. WHEN a user creates a PRACTICE exam with random questions and no matching questions are found, THE Exam Service SHALL return an error message
