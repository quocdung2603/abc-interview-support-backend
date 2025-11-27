# Social Service - Testing Guide

## ✅ Hệ thống đã hoàn thành và đang chạy!

Social Service đã được triển khai thành công với đầy đủ chức năng Posts, Comments và Voting.

## 🚀 Quick Start

### 1. Chạy hệ thống với Docker Compose

```powershell
# Chạy tất cả services
docker-compose up -d

# Hoặc chỉ chạy social-service
docker-compose up -d postgres discovery-service social-service
```

### 2. Import sample data

```powershell
./import-data.ps1
```

### 3. Kiểm tra service đang chạy

```powershell
# Kiểm tra logs
docker logs interview-social-service

# Test API
curl http://localhost:8090/posts
```

## 📊 Sample Data

Sau khi import, bạn sẽ có:
- **10 posts** (2 posts đã locked)
- **31 comments** 
- **45 votes**

## 🧪 Testing với Postman

### Import Postman Collection

1. Mở Postman
2. Click **Import**
3. Chọn file `Social-Service-API.postman_collection.json`

### Test Scenarios

#### Scenario 1: CRUD Posts

**1.1 Tạo Post mới**
```http
POST http://localhost:8090/posts
Content-Type: application/json

{
  "userId": 1,
  "title": "My New Post",
  "content": "This is my post content",
  "lockTime": null
}
```

**1.2 Lấy tất cả Posts (có pagination)**
```http
GET http://localhost:8090/posts?page=0&size=10
```

**1.3 Lấy Post theo ID**
```http
GET http://localhost:8090/posts/1
```

**1.4 Cập nhật Post**
```http
PUT http://localhost:8090/posts/1
Content-Type: application/json

{
  "userId": 1,
  "title": "Updated Title",
  "content": "Updated content",
  "lockTime": null
}
```

**1.5 Set Lock Time cho Post**
```http
PUT http://localhost:8090/posts/1/lock
Content-Type: application/json

{
  "lockTime": "2025-12-31T23:59:59"
}
```

**1.6 Xóa Post**
```http
DELETE http://localhost:8090/posts/1
```

#### Scenario 2: Comments

**2.1 Tạo Comment**
```http
POST http://localhost:8090/comments
Content-Type: application/json

{
  "postId": 1,
  "userId": 2,
  "content": "This is a great post!"
}
```

**2.2 Lấy Comments của Post**
```http
GET http://localhost:8090/comments/post/1
```

**2.3 Lấy Comment theo ID**
```http
GET http://localhost:8090/comments/1
```

**2.4 Xóa Comment**
```http
DELETE http://localhost:8090/comments/1
```

#### Scenario 3: Voting

**3.1 Vote trên Comment**
```http
POST http://localhost:8090/comments/1/vote
Content-Type: application/json

{
  "userId": 3
}
```

**Response:**
```json
{
  "commentId": 1,
  "voteCount": 6,
  "message": "Vote recorded successfully"
}
```

**3.2 Vote lần 2 (Should Fail - 409 Conflict)**
```http
POST http://localhost:8090/comments/1/vote
Content-Type: application/json

{
  "userId": 3
}
```

**Response:**
```json
{
  "timestamp": "2025-11-26T...",
  "status": 409,
  "error": "Conflict",
  "message": "User has already voted on this comment"
}
```

#### Scenario 4: Locked Post Testing

**4.1 Tạo Post với Lock Time trong quá khứ**
```http
POST http://localhost:8090/posts
Content-Type: application/json

{
  "userId": 1,
  "title": "Locked Post",
  "content": "This post is locked",
  "lockTime": "2020-01-01T00:00:00"
}
```

**4.2 Thử Comment trên Locked Post (Should Fail - 400 Bad Request)**
```http
POST http://localhost:8090/comments
Content-Type: application/json

{
  "postId": 7,
  "userId": 2,
  "content": "This should fail"
}
```

**Response:**
```json
{
  "timestamp": "2025-11-26T...",
  "status": 400,
  "error": "Bad Request",
  "message": "Post is locked, no new comments allowed"
}
```

**4.3 Lấy Comments của Locked Post (Sorted by Votes)**
```http
GET http://localhost:8090/comments/post/4
```

Comments sẽ được sắp xếp theo `voteCount DESC, createdAt ASC`

#### Scenario 5: Comment Sorting

**5.1 Comments của Unlocked Post (Sorted by Time)**
```http
GET http://localhost:8090/comments/post/1
```

Comments sẽ được sắp xếp theo `createdAt ASC`

**5.2 Comments của Locked Post (Sorted by Votes)**
```http
GET http://localhost:8090/comments/post/4
```

Comments sẽ được sắp xếp theo `voteCount DESC, createdAt ASC`

#### Scenario 6: Validation Testing

**6.1 Tạo Post với Title rỗng (Should Fail)**
```http
POST http://localhost:8090/posts
Content-Type: application/json

{
  "userId": 1,
  "title": "",
  "content": "Content here"
}
```

**6.2 Tạo Comment với Content rỗng (Should Fail)**
```http
POST http://localhost:8090/comments
Content-Type: application/json

{
  "postId": 1,
  "userId": 2,
  "content": ""
}
```

**6.3 Tạo Comment quá dài (Should Fail)**
```http
POST http://localhost:8090/comments
Content-Type: application/json

{
  "postId": 1,
  "userId": 2,
  "content": "A very long string with more than 1000 characters..."
}
```

#### Scenario 7: Cascade Delete

**7.1 Tạo Post → Comment → Vote**
```http
# 1. Create Post
POST http://localhost:8090/posts
{
  "userId": 1,
  "title": "Test Post",
  "content": "Test content"
}

# 2. Create Comment (assume post id = 11)
POST http://localhost:8090/comments
{
  "postId": 11,
  "userId": 2,
  "content": "Test comment"
}

# 3. Vote on Comment (assume comment id = 32)
POST http://localhost:8090/comments/32/vote
{
  "userId": 3
}
```

**7.2 Xóa Post → Comments và Votes cũng bị xóa**
```http
DELETE http://localhost:8090/posts/11
```

Verify:
```http
GET http://localhost:8090/comments/post/11
# Should return empty or 404
```

#### Scenario 8: Pagination

**8.1 Lấy Posts với Pagination**
```http
GET http://localhost:8090/posts?page=0&size=5
GET http://localhost:8090/posts?page=1&size=5
```

**8.2 Lấy Comments với Pagination**
```http
GET http://localhost:8090/comments/post/1/paginated?page=0&size=10
```

## 📝 API Documentation

### Swagger UI
Access at: **http://localhost:8090/swagger-ui.html**

### API Endpoints Summary

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/posts` | Create post | ✅ |
| GET | `/posts` | Get all posts (paginated) | ❌ |
| GET | `/posts/{id}` | Get post by ID | ❌ |
| PUT | `/posts/{id}` | Update post | ✅ |
| DELETE | `/posts/{id}` | Delete post | 🔒 Admin |
| PUT | `/posts/{id}/lock` | Set lock time | 🔒 Admin |
| POST | `/comments` | Create comment | ✅ |
| GET | `/comments/post/{postId}` | Get comments (auto-sorted) | ❌ |
| GET | `/comments/{id}` | Get comment by ID | ❌ |
| DELETE | `/comments/{id}` | Delete comment | 🔒 Admin |
| POST | `/comments/{id}/vote` | Vote on comment | ✅ |

## 🎯 Expected Behaviors

### Comment Sorting Logic

1. **Unlocked Post** (lockTime = null OR lockTime > now)
   - Sort by: `created_at ASC`
   - Newest comments appear at bottom

2. **Locked Post** (lockTime <= now)
   - Sort by: `vote_count DESC, created_at ASC`
   - Highest voted comments appear at top
   - Tie-breaking by creation time

### Voting Rules

- ✅ Each user can vote once per comment
- ❌ Duplicate votes return 409 Conflict
- ✅ Vote count is immediately updated
- ✅ Votes are cascade deleted with comments

### Cascade Deletion

```
Delete Post
  ↓
Delete all Comments of that Post
  ↓
Delete all Votes of those Comments
```

## 🔍 Verification Commands

### Check Database

```powershell
# Connect to database
docker exec -it interview-postgres psql -U postgres -d socialdb

# Check posts
SELECT id, title, lock_time FROM posts;

# Check comments with votes
SELECT c.id, c.post_id, c.content, c.vote_count 
FROM comments c 
ORDER BY c.post_id, c.vote_count DESC;

# Check votes
SELECT cv.comment_id, COUNT(*) as vote_count 
FROM comment_votes cv 
GROUP BY cv.comment_id;
```

### Check Service Health

```powershell
# Service logs
docker logs interview-social-service

# Service status
docker ps | grep social-service

# Test endpoint
curl http://localhost:8090/actuator/health
```

## 🐛 Troubleshooting

### Service không khởi động

```powershell
# Check logs
docker logs interview-social-service

# Restart service
docker-compose restart social-service
```

### Database connection error

```powershell
# Check postgres is running
docker ps | grep postgres

# Restart postgres
docker-compose restart postgres
```

### Data không hiển thị

```powershell
# Re-import data
cd database-import
Get-Content socialdb-sample-data.sql -Raw | docker exec -i interview-postgres psql -U postgres -d socialdb
```

## 📊 Performance Testing

### Load Testing với Postman

1. Create Collection Runner
2. Select Social Service collection
3. Set iterations: 100
4. Run and check results

### Expected Response Times

- GET requests: < 100ms
- POST requests: < 200ms
- DELETE requests: < 150ms

## ✅ Test Checklist

- [ ] Create Post successfully
- [ ] Get all Posts with pagination
- [ ] Update Post successfully
- [ ] Set lock time for Post
- [ ] Delete Post successfully
- [ ] Create Comment on unlocked Post
- [ ] Fail to create Comment on locked Post
- [ ] Vote on Comment successfully
- [ ] Fail duplicate vote (409 Conflict)
- [ ] Comments sorted by time (unlocked post)
- [ ] Comments sorted by votes (locked post)
- [ ] Cascade delete: Post → Comments → Votes
- [ ] Validation: Empty title/content rejected
- [ ] Validation: Content > 1000 chars rejected
- [ ] Pagination works correctly

## 🎉 Success Criteria

✅ All API endpoints working
✅ Sample data imported successfully
✅ Comment sorting logic correct
✅ Voting system with duplicate prevention
✅ Cascade deletion working
✅ Validation working
✅ Error handling correct
✅ Swagger documentation accessible

## 📞 Support

Nếu gặp vấn đề, kiểm tra:
1. Docker containers đang chạy: `docker ps`
2. Service logs: `docker logs interview-social-service`
3. Database connection: `docker exec interview-postgres psql -U postgres -d socialdb -c "SELECT 1"`
4. API health: `curl http://localhost:8090/actuator/health`
