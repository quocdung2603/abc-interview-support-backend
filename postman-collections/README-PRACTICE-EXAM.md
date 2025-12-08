# PRACTICE Exam Type - Postman Collection

## Overview
This Postman collection tests the new PRACTICE exam type feature.

## Features Tested
1. ✅ Auto-publish PRACTICE exams (status = PUBLISHED)
2. ✅ VIRTUAL exams remain DRAFT
3. ✅ Submit answers without registration (PRACTICE only)
4. ✅ Submit results without registration (PRACTICE only)
5. ✅ Registration required for VIRTUAL exams

## How to Use

### 1. Import Collection
- Open Postman
- Import `PRACTICE-Exam-Testing.postman_collection.json`

### 2. Setup Environment
Create variables:
- `base_url`: http://localhost:8080
- `access_token`: (will be set by login request)

### 3. Run Tests
Execute requests in order:
1. Login (gets token)
2. Create PRACTICE exam (auto-published)
3. Create VIRTUAL exam (stays DRAFT)
4. Submit answer to PRACTICE (no registration)
5. Submit result to PRACTICE (no registration)
6. Try submit to VIRTUAL (should fail)

## Expected Results
- PRACTICE exam: status = "PUBLISHED"
- VIRTUAL exam: status = "DRAFT"
- Answer/Result submission works for PRACTICE
- Answer/Result submission fails for VIRTUAL without registration

## Variables
Collection variables are auto-set:
- `practice_exam_id`: ID of created PRACTICE exam
- `virtual_exam_id`: ID of created VIRTUAL exam
