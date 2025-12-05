# Design Document

## Overview

This design implements consistent default sorting by ID in ascending order for all GET ALL endpoints in the Question Service. Currently, only the Field and Topic endpoints have sorting logic, while Level, QuestionType, Question, and Answer endpoints return results in unpredictable order. This design will standardize the sorting behavior across all six entity types to improve data navigation and user experience.

## Architecture

The Question Service follows a layered architecture:

1. **Controller Layer**: REST endpoints (QuestionController) that handle HTTP requests
2. **Service Layer**: Business logic (QuestionService) that orchestrates operations
3. **Repository Layer**: Data access (JpaRepository implementations) that interact with the database
4. **Entity Layer**: JPA entities representing database tables

The sorting logic will be implemented in the Service Layer, specifically in the `getAllXxx()` methods that accept a `Pageable` parameter.

## Components and Interfaces

### 1. QuestionService - getAllXxx Methods

Each `getAllXxx()` method currently accepts a `Pageable` parameter and returns a `Page<XxxResponse>`. The implementation pattern will be:

**Current State (Field and Topic - already implemented):**
```java
public Page<FieldResponse> getAllFields(Pageable pageable) {
    if (pageable.getSort().isUnsorted()) {
        pageable = PageRequest.of(
            pageable.getPageNumber(), 
            pageable.getPageSize(), 
            Sort.by("id").ascending()
        );
    }
    return fieldRepository.findAll(pageable).map(mappers::toResponse);
}
```

**Current State (Level, QuestionType, Question, Answer - needs implementation):**
```java
public Page<LevelResponse> getAllLevels(Pageable pageable) { 
    return levelRepository.findAll(pageable).map(mappers::toResponse); 
}
```

**Target State (all methods):**
```java
public Page<XxxResponse> getAllXxx(Pageable pageable) {
    // Apply default sort by ID ascending if no sort specified
    if (pageable.getSort().isUnsorted()) {
        pageable = PageRequest.of(
            pageable.getPageNumber(), 
            pageable.getPageSize(), 
            Sort.by("id").ascending()
        );
    }
    return xxxRepository.findAll(pageable).map(mappers::toResponse);
}
```

### 2. Repository Layer

No changes required. All repositories extend `JpaRepository<Entity, Long>` which provides the `findAll(Pageable)` method that supports sorting.

**Existing Repositories:**
- `FieldRepository` - already used with sorting
- `TopicRepository` - already used with sorting
- `LevelRepository` - needs sorting in service layer
- `QuestionTypeRepository` - needs sorting in service layer
- `QuestionRepository` - needs sorting in service layer
- `AnswerRepository` - needs sorting in service layer

### 3. Controller Layer

No changes required. Controllers already pass `Pageable` parameters to service methods:

```java
@GetMapping
public ResponseEntity<Page<XxxResponse>> getAllXxx(Pageable pageable) {
    return ResponseEntity.ok(questionService.getAllXxx(pageable));
}
```

## Data Models

No changes to entity models required. All entities have an `id` field of type `Long` which serves as the primary key:

```java
@Entity
public class Field {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // other fields...
}
```

The same pattern applies to: Topic, Level, QuestionType, Question, Answer.

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Default sorting applies when no sort specified

*For any* GET ALL request without explicit sort parameters, the returned list should be sorted by ID in ascending order (each element's ID should be less than or equal to the next element's ID).

**Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 1.6**

### Property 2: Explicit sorting is preserved

*For any* GET ALL request with explicit sort parameters, the returned list should be sorted according to the specified parameters, not by default ID sorting.

**Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6**

### Property 3: Sorting preserves all elements

*For any* GET ALL request, the sorted result should contain exactly the same elements as an unsorted query (no elements added or removed by sorting).

**Validates: Requirements 3.1, 3.2**

### Property 4: ID ordering is consistent

*For any* two consecutive GET ALL requests without data modifications, the order of elements in the results should be identical.

**Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 1.6**

## Error Handling

### Invalid Sort Parameters

- **Invalid field name**: When a client specifies a sort field that doesn't exist on the entity, Spring Data will throw an exception. This is handled by the global exception handler.
- **Null pageable**: When pageable is null, Spring will provide a default unpaged Pageable. Our code will add sorting to it.

### Performance Considerations

- **Large datasets**: Sorting by ID is efficient because ID columns are indexed as primary keys
- **Database load**: Sorting is performed at the database level using SQL ORDER BY, not in application memory
- **Response time**: ID-based sorting should have minimal performance impact compared to unsorted queries

## Testing Strategy

### Unit Testing

Unit tests will verify specific examples and edge cases:

1. **Empty result set**: Test that sorting works correctly when no entities exist
2. **Single element**: Test that a list with one element is returned correctly
3. **Multiple elements**: Test that multiple elements are sorted in ascending ID order
4. **Explicit sort parameter**: Test that explicit sort parameters override default sorting
5. **Pagination with sorting**: Test that sorting works correctly across multiple pages

### Property-Based Testing

Property-based tests will verify universal properties across all inputs using **jqwik** (Java property-based testing library):

1. **Property 1 - Default sorting applies**: Generate random entity lists, call getAllXxx without sort, verify ascending ID order
2. **Property 2 - Explicit sorting preserved**: Generate random entity lists, call getAllXxx with explicit sort, verify specified order
3. **Property 3 - Sorting preserves elements**: Generate random entity lists, compare sorted vs unsorted results, verify same elements
4. **Property 4 - ID ordering consistency**: Call getAllXxx twice without modifications, verify identical order

Each property-based test will run a minimum of 100 iterations to ensure comprehensive coverage across the input space.

### Integration Testing

Integration tests will verify the end-to-end flow:

1. **GET /fields**: Verify fields are returned sorted by ID
2. **GET /topics**: Verify topics are returned sorted by ID
3. **GET /levels**: Verify levels are returned sorted by ID
4. **GET /question-types**: Verify question types are returned sorted by ID
5. **GET /questions**: Verify questions are returned sorted by ID
6. **GET /answers**: Verify answers are returned sorted by ID
7. **Pagination**: Verify sorting works correctly with page=0, page=1, etc.
8. **Custom sort**: Verify that ?sort=name,desc overrides default ID sorting

## Implementation Notes

### Consistency Pattern

The implementation follows the existing pattern already used in `getAllFields()` and `getAllTopics()`:

1. Check if the incoming `Pageable` has no sort specified using `pageable.getSort().isUnsorted()`
2. If unsorted, create a new `PageRequest` with the same page number and size, but add `Sort.by("id").ascending()`
3. Pass the modified `Pageable` to the repository

### Methods to Update

The following methods in `QuestionService` need to be updated:

1. ✅ `getAllFields()` - already implemented
2. ✅ `getAllTopics()` - already implemented
3. ❌ `getAllLevels()` - needs implementation
4. ❌ `getAllQuestionTypes()` - needs implementation
5. ❌ `getAllQuestions()` - needs implementation
6. ❌ `getAllAnswers()` - needs implementation

### Import Requirements

The following imports are needed in `QuestionService`:

```java
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
```

These imports are already present since `getAllFields()` and `getAllTopics()` use them.

## Dependencies

- **Spring Data JPA**: Provides `Pageable`, `PageRequest`, and `Sort` classes
- **JpaRepository**: Provides `findAll(Pageable)` method with sorting support
- **jqwik**: For property-based testing
- **JUnit 5**: For unit and integration testing
- **Spring Boot Test**: For integration testing with test database

## Performance Impact

### Database Level

- **Index usage**: Sorting by ID leverages the primary key index, making it very efficient
- **Query plan**: The database will use an index scan rather than a full table scan
- **Memory**: No additional memory required in application; sorting happens in database

### Application Level

- **CPU**: Minimal CPU overhead to check if sort is unsorted and create new PageRequest
- **Memory**: No additional memory; same Page object is returned
- **Response time**: Expected impact < 1ms for the sorting logic check

### Benchmark Expectations

For a table with 10,000 records:
- Unsorted query: ~50ms
- Sorted by ID query: ~52ms (negligible difference due to index)
- Sorted by non-indexed field: ~150ms (for comparison)

The ID sorting should have minimal performance impact because primary keys are always indexed.
