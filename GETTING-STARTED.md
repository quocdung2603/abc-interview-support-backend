# 🚀 Getting Started - Interview Microservice System

> **Hướng dẫn setup và chạy project từ đầu sau khi clone từ Git**

---

## 📋 Mục lục

1. [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
2. [Clone Repository](#clone-repository)
3. [Cài đặt Dependencies](#cài-đặt-dependencies)
4. [Cấu hình Database](#cấu-hình-database)
5. [Build Services](#build-services)
6. [Chạy Services](#chạy-services)
7. [Import Sample Data](#import-sample-data)
8. [Kiểm tra Services](#kiểm-tra-services)
9. [Troubleshooting](#troubleshooting)
10. [Development Workflow](#development-workflow)

---

## Yêu cầu hệ thống

### 1. Software cần cài đặt

#### ✅ Required (Bắt buộc)

- **Java JDK 17+** (Spring Boot 3.x yêu cầu)
  ```powershell
  # Kiểm tra version
  java -version
  # Nên là: openjdk version "17.0.x" hoặc cao hơn
  ```

- **Maven 3.8+** (Build tool cho Java)
  ```powershell
  # Kiểm tra version
  mvn -version
  ```

- **Docker Desktop** (Chạy PostgreSQL, Redis, và các services)
  ```powershell
  # Kiểm tra version
  docker --version
  docker-compose --version
  ```

- **Git**
  ```powershell
  git --version
  ```

#### 🔧 Optional (Tùy chọn)

- **Python 3.9+** (Nếu muốn chạy NLP service)
  ```powershell
  python --version
  ```

- **Node.js 18+** (Nếu có frontend)
  ```powershell
  node --version
  npm --version
  ```

- **IntelliJ IDEA / VS Code** (IDE khuyên dùng)

- **Postman** (Test API)

---

### 2. Hardware tối thiểu

- **RAM:** 8GB (khuyến nghị 16GB)
- **CPU:** 4 cores
- **Disk:** 10GB trống
- **OS:** Windows 10/11, macOS, hoặc Linux

---

## Clone Repository

```powershell
# Clone từ GitHub
git clone https://github.com/quocdung2603/abc-interview-support-backend.git

# Di chuyển vào thư mục project
cd abc-interview-support-backend

# Kiểm tra branch hiện tại
git branch
# Nên thấy: * feature/microservice-completed

# Nếu không đúng branch, checkout:
git checkout feature/microservice-completed
```

---

## Cài đặt Dependencies

### 1. Kiểm tra cấu trúc project

```
abc-interview-support-backend/
├── auth-service/          # Authentication service (Port 8081)
├── user-service/          # User management (Port 8082)
├── career-service/        # Career preferences (Port 8084)
├── question-service/      # Question bank (Port 8085)
├── exam-service/          # Exam management (Port 8086)
├── news-service/          # News & recruitment (Port 8087)
├── gateway-service/       # API Gateway (Port 8080)
├── discovery-service/     # Eureka Server (Port 8761)
├── config-service/        # Config Server (Port 8888)
├── nlp-service/           # NLP AI service (Port 5000)
├── config-repo/           # Configuration files
├── database-import/       # Sample data scripts
├── docker-compose.yml     # Docker orchestration
└── README.md
```

### 2. Download Maven dependencies

**Option 1: Build tất cả services (Recommended)**

```powershell
# Chạy script tự động build tất cả
.\rebuild-services.ps1
```

**Option 2: Build từng service thủ công**

```powershell
# Discovery Service (Chạy đầu tiên)
cd discovery-service
.\mvnw.cmd clean install -DskipTests
cd ..

# Config Service (Chạy thứ 2)
cd config-service
.\mvnw.cmd clean install -DskipTests
cd ..

# Gateway Service
cd gateway-service
.\mvnw.cmd clean install -DskipTests
cd ..

# Auth Service
cd auth-service
.\mvnw.cmd clean install -DskipTests
cd ..

# User Service
cd user-service
.\mvnw.cmd clean install -DskipTests
cd ..

# Question Service
cd question-service
.\mvnw.cmd clean install -DskipTests
cd ..

# Exam Service
cd exam-service
.\mvnw.cmd clean install -DskipTests
cd ..

# News Service
cd news-service
.\mvnw.cmd clean install -DskipTests
cd ..

# Career Service
cd career-service
.\mvnw.cmd clean install -DskipTests
cd ..
```

⏱️ **Thời gian:** ~10-15 phút cho lần đầu (download dependencies)

---

## Cấu hình Database

### Option 1: Sử dụng Docker (Recommended)

Docker Compose đã config sẵn PostgreSQL, không cần setup thêm.

```powershell
# Kiểm tra docker-compose.yml có config postgres
cat docker-compose.yml | Select-String -Pattern "postgres"
```

### Option 2: PostgreSQL Local (Manual)

Nếu muốn dùng PostgreSQL local thay vì Docker:

1. **Cài PostgreSQL 15+**
   - Download: https://www.postgresql.org/download/

2. **Tạo databases**
   ```sql
   CREATE DATABASE authdb;
   CREATE DATABASE userdb;
   CREATE DATABASE questiondb;
   CREATE DATABASE examdb;
   CREATE DATABASE newsdb;
   CREATE DATABASE careerdb;
   ```

3. **Update connection strings**
   
   Sửa file `config-repo/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/{dbname}
       username: postgres
       password: your_password
   ```

---

## Build Services

### 1. Build tất cả services (Recommended)

```powershell
# Script tự động build tất cả services
.\build-all-services.ps1
```

**Build với options:**

```powershell
# Clean build (xóa target/ trước khi build)
.\build-all-services.ps1 -Clean

# Build nhanh (skip tests)
.\build-all-services.ps1 -SkipTests

# Build với chi tiết log
.\build-all-services.ps1 -Verbose

# Tổng hợp options
.\build-all-services.ps1 -Clean -SkipTests -Verbose
```

### 2. Build từng service riêng lẻ

```powershell
# Build một service cụ thể
.\build-service.ps1 auth-service

# Build service với options
.\build-service.ps1 exam-service -Clean -SkipTests

# Danh sách services có thể build:
# discovery-service, config-service, gateway-service
# auth-service, user-service, question-service
# exam-service, career-service, news-service
```

### 3. Build thủ công (Manual)

**Nếu không dùng script, có thể build từng service:**

```powershell
# Vào thư mục service và build
cd auth-service
.\mvnw.cmd clean package -DskipTests
cd ..

cd exam-service  
.\mvnw.cmd clean package -DskipTests
cd ..

# ... tương tự cho các service khác
```

### 4. Kiểm tra build thành công

```powershell
# Kiểm tra JAR files đã được tạo
Get-ChildItem -Path . -Recurse -Filter "*.jar" | Where-Object { $_.Directory.Name -eq "target" } | Select-Object Name, Directory

# Hoặc dùng script check
.\build-all-services.ps1 | Select-String "SUCCESS|FAILED"
```

✅ **Kết quả:** Mỗi service sẽ có file `.jar` trong thư mục `target/`  
🚀 **Thời gian:** 2-5 phút tùy máy và số services  
📊 **Hiển thị:** Bảng tóm tắt kết quả build từng service

---

## Chạy Services

### Option 1: Docker Compose (Recommended - All in One)

**Chạy tất cả services cùng lúc:**

```powershell
# Chạy tất cả services
docker-compose up -d

# Kiểm tra services đang chạy
docker-compose ps

# Xem logs của tất cả services
docker-compose logs -f

# Xem logs của 1 service cụ thể
docker-compose logs -f auth-service
```

**Dừng services:**

```powershell
# Dừng tất cả
docker-compose down

# Dừng và xóa volumes (reset database)
docker-compose down -v
```

---

### Option 2: Chạy từng Service (Development Mode)

**Thứ tự chạy quan trọng:**

#### 1️⃣ Infrastructure Services (Chạy đầu tiên)

```powershell
# Terminal 1: PostgreSQL (nếu dùng Docker)
docker run --name postgres-dev -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:15

# Terminal 2: Redis (nếu cần rate limiting)
docker run --name redis-dev -p 6379:6379 -d redis:7-alpine
```

#### 2️⃣ Discovery Service (Port 8761)

```powershell
# Terminal 3
cd discovery-service
.\mvnw.cmd spring-boot:run
```

Đợi thấy: `Eureka Server is running at http://localhost:8761`

#### 3️⃣ Config Service (Port 8888)

```powershell
# Terminal 4
cd config-service
.\mvnw.cmd spring-boot:run
```

Đợi thấy: `Config Server is running`

#### 4️⃣ Gateway Service (Port 8080)

```powershell
# Terminal 5
cd gateway-service
.\mvnw.cmd spring-boot:run
```

Đợi thấy: `Gateway started on port 8080`

#### 5️⃣ Business Services (Parallel - bất kỳ thứ tự)

```powershell
# Terminal 6: Auth Service (Port 8081)
cd auth-service
.\mvnw.cmd spring-boot:run

# Terminal 7: User Service (Port 8082)
cd user-service
.\mvnw.cmd spring-boot:run

# Terminal 8: Question Service (Port 8085)
cd question-service
.\mvnw.cmd spring-boot:run

# Terminal 9: Exam Service (Port 8086)
cd exam-service
.\mvnw.cmd spring-boot:run

# Terminal 10: News Service (Port 8087)
cd news-service
.\mvnw.cmd spring-boot:run

# Terminal 11: Career Service (Port 8084)
cd career-service
.\mvnw.cmd spring-boot:run
```

#### 6️⃣ NLP Service (Optional - Port 5000)

```powershell
# Terminal 12
cd nlp-service
pip install -r requirements.txt
uvicorn app.main:app --reload --port 5000
```

⏱️ **Startup time:** ~2-3 phút cho tất cả services

---

## Import Sample Data

### 1. Đợi services sẵn sàng

```powershell
# Kiểm tra health của tất cả services
$services = @(
    @{name="Gateway"; url="http://localhost:8080/actuator/health"},
    @{name="Auth"; url="http://localhost:8081/actuator/health"},
    @{name="User"; url="http://localhost:8082/actuator/health"}
)

foreach ($s in $services) {
    try {
        $resp = Invoke-RestMethod -Uri $s.url -Method Get -TimeoutSec 5
        Write-Host "[OK] $($s.name): $($resp.status)" -ForegroundColor Green
    } catch {
        Write-Host "[FAIL] $($s.name): Not ready" -ForegroundColor Red
    }
}
```

### 2. Import data

**Option 1: Sử dụng script tự động**

```powershell
cd database-import
.\quick-import-data.ps1
```

**Option 2: Import thủ công (PostgreSQL client)**

```powershell
# Nếu dùng Docker PostgreSQL
docker exec -i postgres-container psql -U postgres -d authdb < database-import/authdb-sample-data.sql
docker exec -i postgres-container psql -U postgres -d userdb < database-import/userdb-sample-data.sql
docker exec -i postgres-container psql -U postgres -d questiondb < database-import/questiondb-sample-data.sql
docker exec -i postgres-container psql -U postgres -d examdb < database-import/examdb-sample-data.sql
docker exec -i postgres-container psql -U postgres -d newsdb < database-import/newsdb-sample-data.sql
docker exec -i postgres-container psql -U postgres -d careerdb < database-import/careerdb-sample-data.sql
```

**Option 3: Sử dụng root SQL script**

```powershell
# Chạy script chính
psql -U postgres -f init-with-data.sql
```

### 3. Verify data

```powershell
# Kiểm tra số lượng users
curl http://localhost:8080/users?page=0&size=5 -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Kiểm tra Services

### 1. Eureka Dashboard (Service Discovery)

```
URL: http://localhost:8761
```

✅ Kiểm tra tất cả services đã đăng ký:
- AUTH-SERVICE
- USER-SERVICE
- QUESTION-SERVICE
- EXAM-SERVICE
- NEWS-SERVICE
- CAREER-SERVICE
- GATEWAY-SERVICE

### 2. Swagger UI (API Documentation)

```
Gateway:        http://localhost:8080/swagger-ui.html (có thể không có)
Auth Service:   http://localhost:8081/swagger-ui.html
User Service:   http://localhost:8082/swagger-ui.html
Question:       http://localhost:8085/swagger-ui.html
Exam:           http://localhost:8086/swagger-ui.html
News:           http://localhost:8087/swagger-ui.html
Career:         http://localhost:8084/swagger-ui.html
NLP:            http://localhost:5000/docs
```

**Swagger Portal (All in One):**
```
File: swagger-ui.html (mở bằng browser)
```

### 3. Health Checks

```powershell
# Gateway
curl http://localhost:8080/actuator/health

# Auth Service
curl http://localhost:8081/actuator/health

# User Service
curl http://localhost:8082/actuator/health
```

### 4. Test Authentication

```powershell
# Register user
curl -X POST http://localhost:8080/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "roleName": "USER",
    "fullName": "Test User"
  }'

# Login
curl -X POST http://localhost:8080/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'

# Response sẽ có accessToken
```

### 5. Test với Postman

```
File: postman-collections/ABC-Interview-ALL-Endpoints.postman_collection.json
```

**Import vào Postman:**
1. Mở Postman
2. Import → File → Chọn file collection
3. Chạy các request test

---

## Troubleshooting

### ❌ Lỗi: "Port already in use"

**Nguyên nhân:** Port đã bị chiếm bởi process khác

**Giải pháp:**

```powershell
# Tìm process đang dùng port 8080
netstat -ano | findstr :8080

# Kill process theo PID
taskkill /PID <PID_NUMBER> /F

# Hoặc thay đổi port trong application.yml
```

---

### ❌ Lỗi: "Connection refused to database"

**Nguyên nhân:** PostgreSQL chưa chạy hoặc wrong credentials

**Giải pháp:**

```powershell
# Kiểm tra PostgreSQL đang chạy
docker ps | findstr postgres

# Restart PostgreSQL
docker restart postgres-container

# Kiểm tra connection string trong config-repo/application.yml
```

---

### ❌ Lỗi: "Service not registered with Eureka"

**Nguyên nhân:** Discovery Service chưa sẵn sàng

**Giải pháp:**

1. Đảm bảo Discovery Service chạy đầu tiên
2. Đợi 30-60 giây để service register
3. Restart service cần register
4. Kiểm tra Eureka Dashboard: http://localhost:8761

---

### ❌ Lỗi: "ClassNotFoundException" hoặc "NoSuchMethodError"

**Nguyên nhân:** Dependency conflict hoặc Java version sai

**Giải pháp:**

```powershell
# Kiểm tra Java version (phải >= 17)
java -version

# Clean và rebuild
cd service-name
.\mvnw.cmd clean install -U

# Clear Maven cache (nếu cần)
Remove-Item -Recurse -Force ~/.m2/repository
```

---

### ❌ Lỗi: "401 Unauthorized" khi gọi API

**Nguyên nhân:** Missing hoặc invalid JWT token

**Giải pháp:**

```powershell
# 1. Login để lấy token
$response = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"admin@example.com","password":"password123"}'

$token = $response.accessToken

# 2. Sử dụng token
Invoke-RestMethod -Uri "http://localhost:8080/users" `
  -Headers @{Authorization="Bearer $token"}
```

---

### ❌ Lỗi: "OutOfMemoryError"

**Nguyên nhân:** Không đủ RAM

**Giải pháp:**

```powershell
# Tăng heap size cho Maven
$env:MAVEN_OPTS="-Xmx2048m -Xms512m"

# Chạy ít services hơn cùng lúc
# Hoặc tăng RAM cho máy
```

---

### ❌ Docker Compose không khởi động được

**Giải pháp:**

```powershell
# Kiểm tra Docker đang chạy
docker info

# Xem logs chi tiết
docker-compose logs

# Clean và restart
docker-compose down -v
docker-compose up -d --build

# Kiểm tra disk space
docker system df
docker system prune -a  # Dọn dẹp nếu cần
```

---

## Development Workflow

### 1. Daily Development

```powershell
# Pull latest changes
git pull origin feature/microservice-completed

# Rebuild changed services only
cd user-service
.\mvnw.cmd clean package -DskipTests
docker-compose restart user-service

# Check logs
docker-compose logs -f user-service
```

### 2. Tạo branch mới

```powershell
# Tạo feature branch
git checkout -b feature/my-new-feature

# Làm việc và commit
git add .
git commit -m "feat: add new feature"

# Push lên remote
git push origin feature/my-new-feature
```

### 3. Testing

```powershell
# Run tests cho 1 service
cd user-service
.\mvnw.cmd test

# Run specific test class
.\mvnw.cmd test -Dtest=UserServiceTest

# Run tests với coverage
.\mvnw.cmd test jacoco:report
```

### 4. Debug với IDE

**IntelliJ IDEA:**
1. Open project as Maven project
2. Set JDK 17+
3. Run/Debug configuration → Spring Boot
4. Set main class: `Application.java`
5. Set active profile: `dev` hoặc `local`

**VS Code:**
1. Install Java Extension Pack
2. Open folder
3. F5 để debug
4. Chọn "Spring Boot" configuration

---

## 🔗 Links hữu ích

### Documentation
- **API Specification:** `FRONTEND-API-SPECIFICATION.md`
- **Architecture:** `README.md`
- **Swagger Portal:** `swagger-ui.html` (open in browser)

### Dashboards
- **Eureka:** http://localhost:8761
- **Gateway:** http://localhost:8080
- **Swagger UI:** http://localhost:8081/swagger-ui.html (Auth)

### Testing
- **Postman Collection:** `postman-collections/ABC-Interview-ALL-Endpoints.postman_collection.json`
- **Sample Data:** `database-import/*.sql`

---

## 📧 Support

**Gặp vấn đề?**
1. Kiểm tra [Troubleshooting](#troubleshooting) section
2. Xem logs: `docker-compose logs -f service-name`
3. Check Eureka Dashboard: http://localhost:8761
4. Kiểm tra health endpoint: `http://localhost:{port}/actuator/health`

---

## ✅ Checklist sau khi setup

- [ ] Java 17+ installed và `java -version` works
- [ ] Maven installed và `mvn -version` works
- [ ] Docker Desktop running
- [ ] All services built successfully (`.jar` files in `target/`)
- [ ] Docker Compose started: `docker-compose ps` shows all services
- [ ] Eureka Dashboard accessible: http://localhost:8761
- [ ] All services registered in Eureka
- [ ] Sample data imported successfully
- [ ] Login works: `POST /auth/login` returns token
- [ ] API calls work với JWT token
- [ ] Swagger UI accessible
- [ ] Postman collection imported and working

---

**🎉 Chúc mừng! Bạn đã setup xong Interview Microservice System!**

Next steps:
1. Đọc `FRONTEND-API-SPECIFICATION.md` để hiểu APIs
2. Import Postman collection và test
3. Bắt đầu develop features mới

---

**Document Version:** 1.0.0  
**Last Updated:** October 22, 2025
