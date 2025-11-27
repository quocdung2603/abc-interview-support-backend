# ✅ Social Service APIs Đã Được Thêm Vào Postman Collection

## 📦 File Đã Cập Nhật

**File:** `ABC-Interview-VERIFIED-Complete.postman_collection.json`

Đã thêm section **💬 Social Service** với các APIs sau:

## 📝 APIs Đã Thêm

### 1. Posts (3 APIs)
- ✅ **Create Post** - `POST {{base_url}}/posts`
- ✅ **Get All Posts** - `GET {{base_url}}/posts?page=0&size=20`
- ✅ **Get Post By ID** - `GET {{base_url}}/posts/1`

### 2. Comments & Voting (3 APIs)
- ✅ **Create Comment** - `POST {{base_url}}/comments`
- ✅ **Get Comments (Auto-sorted)** - `GET {{base_url}}/comments/post/1`
  - Tự động sắp xếp theo votes (nếu locked) hoặc time (nếu unlocked)
- ✅ **Vote on Comment** - `POST {{base_url}}/comments/1/vote`

## 🎯 Cách Sử Dụng

### 1. Import vào Postman
```
File đã có sẵn: ABC-Interview-VERIFIED-Complete.postman_collection.json
Chỉ cần Re-import hoặc Reload trong Postman
```

### 2. Test Flow Cơ Bản

**Bước 1: Tạo Post**
```
POST {{base_url}}/posts
Body:
{
  "userId": 1,
  "title": "My First Post",
  "content": "This is the content",
  "lockTime": null
}
```

**Bước 2: Tạo Comments**
```
POST {{base_url}}/comments
Body:
{
  "postId": 1,
  "userId": 2,
  "content": "Great post!"
}
```

**Bước 3: Vote cho Comment**
```
POST {{base_url}}/comments/1/vote
Body:
{
  "userId": 3
}
```

**Bước 4: Xem Comments (Tự động sắp xếp)**
```
GET {{base_url}}/comments/post/1
```

## 🔍 Tính Năng Đặc Biệt

### Auto-Sorting Comments
API `GET /comments/post/{postId}` tự động sắp xếp comments:

**Nếu Post CHƯA Locked:**
- Sắp xếp theo `createdAt ASC` (thời gian tạo, cũ nhất trước)
- Không quan tâm số votes

**Nếu Post ĐÃ Locked:**
- Sắp xếp theo `voteCount DESC` (votes cao nhất trước)
- Nếu votes bằng nhau, sắp xếp theo thời gian tạo

## 📊 Test Scenarios

### Scenario 1: Unlocked Post
1. Tạo post không có lockTime
2. Tạo 3 comments
3. Vote cho comments (số votes khác nhau)
4. Get comments → **Kết quả: Sắp xếp theo thời gian tạo**

### Scenario 2: Locked Post
1. Tạo post với lockTime trong quá khứ
2. Unlock tạm (set lockTime = future)
3. Tạo 3 comments
4. Vote cho comments (số votes khác nhau)
5. Lock lại (set lockTime = past)
6. Get comments → **Kết quả: Sắp xếp theo votes**

## 🚀 APIs Đầy Đủ Hơn

Nếu cần test đầy đủ hơn, sử dụng các files:
- `Social-Service-API.postman_collection.json` - Tất cả APIs
- `Social-Service-Realtime-Sorting-Test.postman_collection.json` - Test scenarios chi tiết

## 📌 Lưu Ý

- **base_url** mặc định: `http://localhost:8080` (qua Gateway)
- Hoặc trực tiếp: `http://localhost:8090` (Social Service)
- Cần chạy PostgreSQL và Social Service trước khi test
- Mỗi user chỉ vote 1 lần cho 1 comment (duplicate prevention)

## ✅ Tổng Kết

Đã thêm **6 APIs** của Social Service vào file Postman collection chính:
- 3 APIs cho Posts
- 3 APIs cho Comments & Voting

File sẵn sàng để import và test! 🎉
