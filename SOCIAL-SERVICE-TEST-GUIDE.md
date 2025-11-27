# Social Service - Hướng Dẫn Test Realtime Comment Sorting

## 📋 Tổng Quan

File này hướng dẫn test chức năng sắp xếp comment theo vote realtime trong social-service, bao gồm:
- **Post chưa locked**: Comments sắp xếp theo thời gian tạo (oldest first)
- **Post đã locked**: Comments sắp xếp theo số vote (highest first)

## 🚀 Chuẩn Bị

### 1. Chạy PostgreSQL
```powershell
docker-compose up -d postgres
```

### 2. Chạy Social Service
```powershell
cd social-service
java -jar target/social-service-0.0.1-SNAPSHOT.jar
```

### 3. Import Postman Collection
- Mở Postman
- Import file: `Social-Service-Realtime-Sorting-Test.postman_collection.json`

## 📝 Test Scenarios

### TEST 1: Unlocked Post (Sắp Xếp Theo Thời Gian)

**Kịch bản:**
1. Tạo post KHÔNG có lockTime
2. Tạo 3 comments theo thứ tự: A → B → C
3. Vote cho comments:
   - Comment A: 5 votes
   - Comment B: 10 votes (nhiều nhất)
   - Comment C: 2 votes (ít nhất)
4. Lấy danh sách comments

**Kết quả mong đợi:**
```
Order: A → B → C (theo thời gian tạo, KHÔNG theo votes)
- Comment A (5 votes) - created FIRST
- Comment B (10 votes) - created SECOND  
- Comment C (2 votes) - created THIRD
```

**Giải thích:** Vì post chưa locked, comments được sắp xếp theo `createdAt ASC` (thời gian tạo tăng dần), bất kể số vote.

---

### TEST 2: Locked Post (Sắp Xếp Theo Votes)

**Kịch bản:**
1. Tạo post với lockTime trong quá khứ (đã locked)
2. Thử tạo comment → **FAIL** (post đã locked)
3. Unlock post tạm thời (set lockTime = future)
4. Tạo 3 comments theo thứ tự: X → Y → Z
5. Vote cho comments:
   - Comment X: 3 votes (ít nhất)
   - Comment Y: 15 votes (nhiều nhất)
   - Comment Z: 7 votes (trung bình)
6. Lock lại post (set lockTime = past)
7. Lấy danh sách comments

**Kết quả mong đợi:**
```
Order: Y → Z → X (theo votes, KHÔNG theo thời gian)
- Comment Y (15 votes) - created SECOND but HIGHEST votes
- Comment Z (7 votes) - created THIRD
- Comment X (3 votes) - created FIRST but LOWEST votes
```

**Giải thích:** Vì post đã locked, comments được sắp xếp theo `voteCount DESC, createdAt ASC` (vote giảm dần, nếu bằng nhau thì theo thời gian).

## 🎯 Các Bước Test Trong Postman

### Test 1: Unlocked Post
1. Chạy folder "TEST 1: Unlocked Post (Sort by Time)"
2. Chạy lần lượt các steps:
   - Step 1: Create Unlocked Post
   - Step 2-4: Create Comments A, B, C
   - Step 5-9: Vote on Comment A (5 votes)
   - Step 10-19: Vote on Comment B (10 votes)
   - Step 20-21: Vote on Comment C (2 votes)
   - **Step 22: Get Comments** ← Kiểm tra kết quả

### Test 2: Locked Post
1. Chạy folder "TEST 2: Locked Post (Sort by Votes)"
2. Chạy lần lượt các steps:
   - Step 1: Create Locked Post
   - Step 2: Try Create Comment (should FAIL)
   - Step 3: Unlock Post First
   - Step 4-6: Create Comments X, Y, Z
   - Step 7-9: Vote on Comment X (3 votes)
   - Step 10-24: Vote on Comment Y (15 votes)
   - Step 25-31: Vote on Comment Z (7 votes)
   - Step 32: Lock the Post
   - **Step 33: Get Comments** ← Kiểm tra kết quả

## ✅ Xác Nhận Kết Quả

### Unlocked Post Response (Step 22):
```json
[
  {
    "id": 1,
    "content": "Comment A - Created FIRST (will get 5 votes)",
    "voteCount": 5,
    "createdAt": "2025-11-26T..."
  },
  {
    "id": 2,
    "content": "Comment B - Created SECOND (will get 10 votes)",
    "voteCount": 10,
    "createdAt": "2025-11-26T..."
  },
  {
    "id": 3,
    "content": "Comment C - Created THIRD (will get 2 votes)",
    "voteCount": 2,
    "createdAt": "2025-11-26T..."
  }
]
```

### Locked Post Response (Step 33):
```json
[
  {
    "id": 5,
    "content": "Comment Y - Created SECOND (will get 15 votes)",
    "voteCount": 15,
    "createdAt": "2025-11-26T..."
  },
  {
    "id": 6,
    "content": "Comment Z - Created THIRD (will get 7 votes)",
    "voteCount": 7,
    "createdAt": "2025-11-26T..."
  },
  {
    "id": 4,
    "content": "Comment X - Created FIRST (will get 3 votes)",
    "voteCount": 3,
    "createdAt": "2025-11-26T..."
  }
]
```

## 🔍 Kiểm Tra Realtime

Để test realtime sorting, bạn có thể:

1. **Thêm vote mới** cho bất kỳ comment nào
2. **Gọi lại GET comments** endpoint
3. **Quan sát** thứ tự thay đổi (nếu post đã locked)

Ví dụ:
```
# Thêm 20 votes cho Comment X
POST http://localhost:8090/comments/4/vote
Body: {"userId": 100}, {"userId": 101}, ... {"userId": 119}

# Lấy lại comments
GET http://localhost:8090/comments/post/2

# Kết quả: Comment X sẽ lên đầu vì có nhiều votes nhất
```

## 📊 Logic Sắp Xếp

### Code Implementation (CommentService.java):
```java
public List<CommentResponse> getCommentsByPostId(Long postId) {
    Post post = postService.getPostEntityById(postId);
    
    List<Comment> comments;
    if (postService.isLocked(post)) {
        // LOCKED: Sort by votes DESC, then time ASC
        comments = commentRepository.findByPostIdOrderByVoteCountDescCreatedAtAsc(postId);
    } else {
        // UNLOCKED: Sort by time ASC only
        comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }
    
    return commentMapper.toResponseList(comments);
}
```

## 🎓 Kết Luận

- ✅ **Unlocked Post**: Comments luôn theo thứ tự thời gian (oldest first)
- ✅ **Locked Post**: Comments tự động sắp xếp theo votes (highest first)
- ✅ **Realtime**: Mỗi lần vote, thứ tự tự động cập nhật khi gọi GET
- ✅ **Duplicate Prevention**: Mỗi user chỉ vote 1 lần cho 1 comment

## 🐛 Troubleshooting

**Lỗi: "Post is locked, no new comments allowed"**
- Giải pháp: Set lockTime = future hoặc null để unlock post

**Lỗi: "User has already voted on this comment"**
- Giải pháp: Dùng userId khác để vote

**Comments không đúng thứ tự:**
- Kiểm tra lockTime của post
- Kiểm tra voteCount của từng comment
- Xem logs trong console của social-service
