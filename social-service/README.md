# Social Service

Social Service quản lý posts, comments và voting system cho nền tảng social.

## Features

- ✅ CRUD operations cho Posts
- ✅ Comment system với voting
- ✅ Auto-sorting comments (by votes nếu post locked, by time nếu unlocked)
- ✅ Duplicate vote prevention
- ✅ Cascade delete (xóa post → xóa comments → xóa votes)
- ✅ Pagination support
- ✅ Input validation
- ✅ Error handling
- ✅ Swagger documentation

## Tech Stack

- Spring Boot 3.5.5
- Spring Data JPA
- PostgreSQL
- MapStruct
- Lombok
- Eureka Client
- Swagger/OpenAPI

## Database Schema

### Posts Table
```sql
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    lock_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Comments Table
```sql
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    vote_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Comment Votes Table
```sql
CREATE TABLE comment_votes (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    voted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(comment_id, user_id)
);
```

## API Endpoints

### Posts

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/posts` | Create a new post |
| GET | `/posts` | Get all posts (paginated) |
| GET | `/posts/{id}` | Get post by ID |
| PUT | `/posts/{id}` | Update post |
| DELETE | `/posts/{id}` | Delete post (admin only) |
| PUT | `/posts/{id}/lock` | Set lock time (admin only) |

### Comments

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/comments` | Create a new comment |
| GET | `/comments/post/{postId}` | Get comments for a post (auto-sorted) |
| GET | `/comments/{id}` | Get comment by ID |
| DELETE | `/comments/{id}` | Delete comment (admin only) |
| POST | `/comments/{id}/vote` | Vote on a comment |

## Running the Service

### With Docker Compose

```bash
docker-compose up -d social-service
```

### Standalone

```bash
cd social-service
./mvnw clean package
java -jar target/social-service-0.0.1-SNAPSHOT.jar
```

## Configuration

Environment variables:

```yaml
SOCIAL_SERVICE_PORT: 8090
POSTGRES_HOST: postgres
POSTGRES_PORT: 5432
SOCIAL_DB: socialdb
POSTGRES_USER: postgres
POSTGRES_PASSWORD: 123456
JWT_SECRET: your-secret-key
EUREKA_DEFAULT_ZONE: http://discovery-service:8761/eureka/
```

## Testing

### Swagger UI
Access at: http://localhost:8090/swagger-ui.html

### Postman Collection
Import `Social-Service-API.postman_collection.json`

### Sample Requests

**Create Post:**
```json
POST http://localhost:8090/posts
{
  "userId": 1,
  "title": "My First Post",
  "content": "This is the content of my first post.",
  "lockTime": null
}
```

**Create Comment:**
```json
POST http://localhost:8090/comments
{
  "postId": 1,
  "userId": 2,
  "content": "Great post! Thanks for sharing."
}
```

**Vote on Comment:**
```json
POST http://localhost:8090/comments/1/vote
{
  "userId": 3
}
```

## Business Logic

### Comment Sorting

Comments are automatically sorted based on post lock status:

- **Locked Post** (lockTime < now): Sort by `vote_count DESC, created_at ASC`
- **Unlocked Post**: Sort by `created_at ASC`

### Voting Rules

- Each user can vote only once per comment
- Duplicate votes return 409 Conflict error
- Vote count is denormalized in comments table for performance

### Cascade Deletion

- Delete Post → Deletes all Comments → Deletes all Votes
- Delete Comment → Deletes all Votes
- Handled by database foreign key constraints with ON DELETE CASCADE

## Error Handling

| Status Code | Error | Description |
|-------------|-------|-------------|
| 400 | Bad Request | Invalid input or post is locked |
| 404 | Not Found | Post or comment not found |
| 409 | Conflict | Duplicate vote attempt |
| 500 | Internal Server Error | Server error |

## Sample Data

Run import script to load sample data:

```bash
./import-data.ps1
```

This creates:
- 10 sample posts
- 30+ sample comments
- 50+ sample votes
- 2 locked posts for testing

## Development

### Build
```bash
./mvnw clean package
```

### Run Tests
```bash
./mvnw test
```

### Generate MapStruct Mappers
```bash
./mvnw compile
```

## Architecture

```
Controller Layer (REST API)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Database (PostgreSQL)
```

## Dependencies

- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter Validation
- Spring Cloud Starter Netflix Eureka Client
- PostgreSQL Driver
- Lombok
- MapStruct
- Springdoc OpenAPI
- jqwik (for property-based testing)
