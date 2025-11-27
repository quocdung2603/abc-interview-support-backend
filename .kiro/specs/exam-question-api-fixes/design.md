# Design Document

## Overview

This design addresses the removal of deprecated fields (`topics`, `questionTypes`) from the Exam API response format. The system currently maintains both new fields (`topicIds`, `questionTypeIds`) and deprecated fields for backward compatibility. This design will clean up the codebase by removing the deprecated fields while ensuring existing data continues to work through migration logic in the mapper layer.

## Architecture

The Exam Service follows a layered architecture:

1. **Controller Layer**: REST endpoints that receive requests and return responses
2. **Service Layer**: Business logic for exam management (ExamService)
3. **Mapper Layer**: Data transformation between entities and DTOs (Mappers)
4. **Repository Layer**: Database access (ExamRepository)
5. **Entity Layer**: JPA entities representing database tables (Exam)
6. **DTO Layer**: Data Transfer Objects for API contracts (ExamRequest, ExamResponse)

The changes will primarily affect the Entity, DTO, and Mapper layers.

## Components and Interfaces

### 1. ExamResponse DTO

**Current State:**
```java
public class ExamResponse {
    private List<Long> topicIds;
    private List<Long> questionTypeIds;
    private List<Long> topics;        // Deprecated
    private List<Long> questionTypes; // Deprecated
}
```

**Target State:**
```java
public class ExamResponse {
    private List<Long> topicIds;
    private List<Long> questionTypeIds;
    // Removed: topics, questionTypes
}
```

### 2. Exam Entity

**Current State:**
```java
@Entity
public class Exam {
    private String topicIds;        // JSON array
    private String questionTypeIds; // JSON array
    private String topics;          // Deprecated JSON array
    private String questionTypes;   // Deprecated JSON array
}
```

**Target State:**
```java
@Entity
public class Exam {
    private String topicIds;        // JSON array
    private String questionTypeIds; // JSON array
    // Removed: topics, questionTypes
}
```

### 3. ExamRequest DTO

**Current State:**
```java
public class ExamRequest {
    private List<Long> topicIds;
    private List<Long> questionTypeIds;
    private List<Long> topics;        // Deprecated
    private List<Long> questionTypes; // Deprecated
}
```

**Target State:**
```java
public class ExamRequest {
    private List<Long> topicIds;
    private List<Long> questionTypeIds;
    // Removed: topics, questionTypes
}
```

### 4. Mappers

**Current State:**
The Mappers class contains fallback logic that reads from deprecated fields if new fields are empty:

```java
protected List<Long> getTopicIds(Exam entity) {
    List<Long> topicIds = convertStringToList(entity.getTopicIds());
    if (topicIds.isEmpty()) {
        topicIds = convertStringToList(entity.getTopics()); // Fallback
    }
    return topicIds;
}
```

**Target State:**
Remove fallback logic and deprecated field mappings. The mapper will only work with the new fields:

```java
@Mapping(target = "topicIds", expression = "java(convertStringToList(entity.getTopicIds()))")
@Mapping(target = "questionTypeIds", expression = "java(convertStringToList(entity.getQuestionTypeIds()))")
public abstract ExamResponse toResponse(Exam entity);
```

## Data Models

### Exam Entity Database Schema

**Current Schema:**
```sql
CREATE TABLE exams (
    id BIGSERIAL PRIMARY KEY,
    field_id BIGINT,
    topic_ids TEXT,        -- JSON array: "[1,2,3]"
    level_id BIGINT,
    question_type_ids TEXT, -- JSON array: "[1,2]"
    topics TEXT,           -- Deprecated
    question_types TEXT,   -- Deprecated
    ...
);
```

**Target Schema:**
```sql
CREATE TABLE exams (
    id BIGSERIAL PRIMARY KEY,
    field_id BIGINT,
    topic_ids TEXT,        -- JSON array: "[1,2,3]"
    level_id BIGINT,
    question_type_ids TEXT, -- JSON array: "[1,2]"
    -- Removed: topics, question_types
    ...
);
```

**Migration Strategy:**
- No database migration is required if old data already has values in `topic_ids` and `question_type_ids`
- If old data exists only in `topics` and `question_types`, a one-time data migration script should copy values before removing columns
- The application code will stop reading/writing deprecated fields

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

After reviewing the acceptance criteria, several properties are redundant or can be combined. Properties 1.1 and 1.2 can be combined into a single property about field presence. Properties 1.3 and 1.4 can be combined into a property about field absence. Property 1.5 is redundant as it's covered by the combination of presence and absence properties.

### Property 1: Response contains required fields

*For any* exam entity with topicIds and questionTypeIds, when mapped to ExamResponse, the response should contain both topicIds and questionTypeIds fields with the correct values.

**Validates: Requirements 1.1, 1.2**

### Property 2: Response excludes deprecated fields

*For any* exam entity, when serialized to JSON via ExamResponse, the resulting JSON string should not contain the keys "topics" or "questionTypes".

**Validates: Requirements 1.3, 1.4**

### Property 3: Request deserialization accepts new fields

*For any* valid JSON request containing topicIds and questionTypeIds fields, deserializing to ExamRequest should successfully populate those fields with the correct values.

**Validates: Requirements 2.5**

### Property 4: Entity to response mapping preserves data

*For any* exam entity with populated topicIds and questionTypeIds, mapping to ExamResponse and back should preserve the list contents and order.

**Validates: Requirements 3.1**

### Property 5: New exam creation uses correct fields

*For any* ExamRequest with topicIds and questionTypeIds, creating an exam entity should populate only the topicIds and questionTypeIds fields in the entity (not the deprecated fields).

**Validates: Requirements 3.3**

## Error Handling

### Validation Errors

- **Empty topicIds**: When an ExamRequest contains an empty or null topicIds list, the service should reject the request with a validation error
- **Empty questionTypeIds**: When an ExamRequest contains an empty or null questionTypeIds list, the service should reject the request with a validation error
- **Invalid JSON in entity**: When an Exam entity contains malformed JSON in topicIds or questionTypeIds fields, the mapper should return an empty list rather than throwing an exception

### Migration Errors

- **Missing data**: If an exam entity has neither topicIds nor the deprecated topics field populated, the response should return an empty list for topicIds
- **Database constraint violations**: If removing deprecated columns causes foreign key or constraint issues, the migration script should handle them gracefully

## Testing Strategy

### Unit Testing

Unit tests will verify specific examples and edge cases:

1. **Empty lists**: Test that empty topicIds/questionTypeIds lists are handled correctly
2. **Null values**: Test that null values in entity fields map to empty lists in responses
3. **Malformed JSON**: Test that invalid JSON in entity fields doesn't crash the mapper
4. **Single item lists**: Test that lists with one element serialize/deserialize correctly
5. **Large lists**: Test that lists with many elements (e.g., 50+ items) work correctly

### Property-Based Testing

Property-based tests will verify universal properties across all inputs using **jqwik** (Java property-based testing library):

1. **Property 1 - Response contains required fields**: Generate random exam entities with various topicIds and questionTypeIds, map to response, verify fields exist and match
2. **Property 2 - Response excludes deprecated fields**: Generate random exam entities, serialize to JSON, verify "topics" and "questionTypes" keys are absent
3. **Property 3 - Request deserialization**: Generate random valid JSON with topicIds/questionTypeIds, deserialize, verify correct population
4. **Property 4 - Round-trip mapping**: Generate random exam entities, map to response and verify data preservation
5. **Property 5 - New exam creation**: Generate random ExamRequests, create entities, verify only new fields are populated

Each property-based test will run a minimum of 100 iterations to ensure comprehensive coverage across the input space.

### Integration Testing

Integration tests will verify the end-to-end flow:

1. **Create exam via API**: POST request with topicIds/questionTypeIds, verify response format
2. **Retrieve exam via API**: GET request, verify response contains only new fields
3. **Update exam via API**: PUT request with new fields, verify update succeeds
4. **List exams via API**: GET request for multiple exams, verify all responses use new format

## Implementation Notes

### Removal Order

To minimize risk, remove deprecated fields in this order:

1. **Phase 1**: Remove from ExamResponse DTO (affects API output)
2. **Phase 2**: Remove from ExamRequest DTO (affects API input)
3. **Phase 3**: Remove from Mappers (remove fallback logic)
4. **Phase 4**: Remove from Exam entity (affects database)
5. **Phase 5**: Database migration to drop columns (if needed)

### Backward Compatibility

Since we're removing deprecated fields, this is a **breaking change** for API consumers who still use the old field names. Consider:

- **API versioning**: If the API is versioned, this change should be in a new major version
- **Deprecation notice**: If not already done, announce deprecation before removal
- **Migration period**: Give consumers time to migrate to new field names

### Database Considerations

- The database columns `topics` and `question_types` can remain temporarily even after code removal
- A separate database migration can drop these columns after confirming no data loss
- Ensure all existing data has been migrated from old columns to new columns before dropping

## Dependencies

- **Jackson**: For JSON serialization/deserialization
- **MapStruct**: For entity-DTO mapping
- **Spring Data JPA**: For database persistence
- **jqwik**: For property-based testing
- **JUnit 5**: For unit and integration testing
