# 🎓 Interview Microservice ABC

> Hệ thống phỏng vấn trực tuyến hoàn chỉnh với kiến trúc microservices, ELO ranking và AI grading

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

---

## 📋 MỤC LỤC

- [Giới thiệu](#-giới-thiệu)
- [Kiến trúc](#️-kiến-trúc-hệ-thống)
- [Cài đặt](#-cài-đặt-3-phút)
- [API Endpoints](#-api-endpoints)
- [Authentication](#-authentication)
- [Testing](#-testing)
- [Documentation](#-documentation)

---

## 🎯 GIỚI THIỆU

### Tính năng chính

✅ **Authentication & Authorization** - JWT-based với role management (USER, RECRUITER, ADMIN)  
✅ **User Management** - Profile, ELO ranking system (NEWBIE → MASTER)  
✅ **Question Bank** - Quản lý câu hỏi theo fields, topics, levels  
✅ **Exam System** - Technical & Behavioral exams với auto-grading  
✅ **Career Matching** - Gợi ý career path dựa trên kỹ năng  
✅ **News & Recruitment** - Tin tức và cơ hội việc làm  
✅ **NLP Service** - AI grading và similarity detection (Python FastAPI)

### Tech Stack

**Backend:** Spring Boot 3.x, Spring Cloud (Gateway, Eureka, Config), Spring Security, JPA  
**Database:** PostgreSQL 15 (6 databases)  
**Container:** Docker & Docker Compose  
**NLP:** FastAPI, spaCy, scikit-learn

---

## 🏗️ KIẾN TRÚC HỆ THỐNG

### Microservices Architecture

```
┌──────────────┐
│ API Gateway  │  :8080
└──────┬───────┘
       │
   ┌───┴────────────────────────┐
   │                            │
┌──▼──────┐  ┌──────────┐  ┌───▼──────┐
│  Auth   │  │   User   │  │ Question │
│  :8081  │  │  :8082   │  │  :8085   │
│ authdb  │  │ userdb   │  │questiondb│
└─────────┘  └──────────┘  └──────────┘

┌──────────┐  ┌──────────┐  ┌─────────┐
│   Exam   │  │  Career  │  │  News   │
│  :8086   │  │  :8084   │  │  :8087  │
│ examdb   │  │ careerdb │  │ newsdb  │
└──────────┘  └──────────┘  └─────────┘

  ┌────────┐   ┌────────┐   ┌────────┐
  │ Eureka │   │ Config │   │  NLP   │
  │  :8761 │   │  :8888 │   │ :5000  │
  └────────┘   └────────┘   └────────┘
```

### ⚡ Phân tách trách nhiệm

**🔐 Auth Service** - Authentication ONLY
- Register, Login, JWT token generation
- Gọi User Service để tạo user data

**👤 User Service** - User Management ONLY  
- CRUD users, ELO system, Role management
- Nhận request từ Auth Service qua `/internal/create`

**📦 Other Services** - Business Logic
- Question, Exam, News, Career
- Mỗi service có database riêng

---

## 🚀 CÀI ĐẶT (3 PHÚT)

### 1. Start Services

```bash
docker-compose up -d
```

### 2. Import Data (160+ records)

```powershell
.\run-init-with-data.ps1
# Chọn: 1 → yes
```

### 3. Verify

```powershell
.\quick-test.ps1
```

**✅ Xong! Hệ thống sẵn sàng**

---

## 📊 API ENDPOINTS

### Tổng quan: **78 endpoints**

| Service | Endpoints | Port | Database |
|---------|-----------|------|----------|
| Auth | 5 | 8081 | authdb |
| User | 9 | 8082 | userdb |
| Career | 5 | 8084 | careerdb |
| Question | 21 | 8085 | questiondb |
| Exam | 23 | 8086 | examdb |
| News | 15 | 8087 | newsdb |

### 🔐 Auth Service

```
POST /auth/register      Register new user
POST /auth/login         Login & get JWT
POST /auth/refresh       Refresh token
GET  /auth/verify        Verify token
```

### 👤 User Service

# Interview Microservices — ABC (Git-friendly README)

Ngắn gọn, dễ dùng README để bắt đầu với repository này. Bao gồm: mục đích, cách build & chạy, cách publish Docker images và những lệnh hữu ích cho developer.

---

## Tổng quan

Hệ thống là một bộ microservices Spring Boot (Java 17) + PostgreSQL với 1 NLP service (Python/FastAPI). Mục tiêu: hệ thống phỏng vấn trực tuyến có quản lý users, question bank, exam flow, NLP grading và tính năng ELO.

Các service chính (thư mục cùng tên):
- `gateway-service`, `discovery-service`, `config-service`
- `auth-service`, `user-service`, `question-service`, `exam-service`, `career-service`, `news-service`
- `nlp-service` (Python FastAPI)

---

## Quick start (local, Docker Compose)

Prerequisites:
- Docker Desktop (or Docker engine) running
- Docker Compose
- (Optional) Java 17 and Maven if you want to build JARs locally

1) Start all services (containers):

```powershell
docker-compose up -d
```

2) Import sample data (PowerShell helper):

```powershell
.\database-import\quick-import-data.ps1
```

3) Check services are up (example):

```powershell
docker-compose ps
curl http://localhost:8761  # Eureka UI
```

---

## Build (per-service) and helper scripts

To build all Java services (use Maven wrappers included):

```powershell
.\build-all-services.ps1 -SkipTests
```

Build a single service (example):

```powershell
.\build-service.ps1 -Service exam-service -SkipTests
```

Scripts provided in repo:
- `build-all-services.ps1` — builds every service via its `mvnw`
- `build-service.ps1` — build a single service
- `rebuild-services.ps1` — convenience wrapper used by CI/local runs
- `test-exam-flow.ps1` — end-to-end script exercising exam creation/submission

---

## Docker images and publishing

If you want to publish images to Docker Hub, use the helper `push-images.ps1`.

Example (build artifacts then push images):

```powershell
# Make sure you are logged in
docker login

# Build JARs then build & push images
.\push-images.ps1 -HubUser <yourHubUser> -Tag v1.0 -Build -SkipTests
```

Images are pushed as `<hubUser>/<service>:<tag>`. Consumers can pull individual images or update `docker-compose.yml` to reference the published images.

Notes:
- Ensure `target/*.jar` exist before image build (the `-Build` flag runs the build script).
- For private images, grant access on Docker Hub or use a private registry.

---

## Developer workflow & common tasks

- Run one service locally (for debugging):

```powershell
```powershell
# Check ports
netstat -ano | findstr "8080 8081 8082"
```

- Run tests for a service:

```powershell

# Stop all
```

- Rebuild and restart a single container after code changes:

```powershell
docker-compose down
```

---
```

---

## Troubleshooting

- Docker build shows buildkit progress lines (like `#0 building with "desktop-linux"`) — these are normal. The script captures exit codes; if you see build failures, inspect the full output with:

```powershell
docker build ./discovery-service -t temp/discovery:local
```

- If `push-images.ps1` fails to push, check `docker login` and network connectivity.
- If a service fails on startup, check the log of the container:

```powershell
docker-compose logs -f exam-service
```

---

## Contributing & notes for maintainers

- Config files: `config-repo/` contains YAML used by the Spring Cloud Config server in containerized environments.
- Service ports and routes are configured in `gateway-service` and `config-repo/api-gateway.yml`.
- When changing auth keys/secrets, update both `auth-service` and `gateway-service` config.

If you'd like, I can also:
- Add a short `CONTRIBUTING.md` with PR checklist and commit message format.
- Create a minimal quick-start GitHub Actions workflow to build & publish images on tags.

---

If you want this README translated to English or expanded with diagrams and commands for CI, tell me which format (English/Markdown + badges + diagrams) and I'll update it.


## ✅ SYSTEM STATUS

**Architecture:** Clean, no duplication ✅  
**Services:** 9 microservices ✅  
**Databases:** 6 PostgreSQL databases ✅  
**Endpoints:** 78 APIs ✅  
**Security:** JWT + BCrypt ✅  
**Documentation:** Complete ✅  
**Testing:** Scripts + Swagger ✅  

**Status: PRODUCTION READY** 🚀

---

## 📞 SUPPORT

### Documentation
- [Architecture](ARCHITECTURE-CLARIFICATION.md) - System design explained
- [API Testing](POSTMAN-IMPORT-INSTRUCTIONS.md) - Postman guide
- [Database](HUONG-DAN-IMPORT-DU-LIEU.md) - Data import guide
- [API Specs](API-SPECIFICATION.md) - Complete endpoint documentation

### Quick Links
- Swagger UIs: http://localhost:8081/swagger-ui.html (and 8082, 8085, 8086, 8087)
- Eureka Dashboard: http://localhost:8761
- Gateway Health: http://localhost:8080/actuator/health

---

## 🎓 PROJECT STRUCTURE

```
Interview Microservice ABC/
├── auth-service/           Authentication service
├── user-service/           User management service
├── career-service/         Career preference service
├── question-service/       Question bank service
├── exam-service/           Exam management service
├── news-service/           News & recruitment service
├── gateway-service/        API Gateway
├── discovery-service/      Eureka server
├── config-service/         Config server
├── nlp-service/            NLP service (Python)
├── docker-compose.yml      Docker orchestration
├── init-with-data.sql      Database initialization
└── README.md               This file
```

---

## 🚀 QUICK START CHECKLIST

- [ ] Docker Desktop running
- [ ] Run `docker-compose up -d`
- [ ] Run `.\run-init-with-data.ps1` (Option 1)
- [ ] Run `.\quick-test.ps1` to verify
- [ ] Open Swagger UI: http://localhost:8081/swagger-ui.html
- [ ] Test login with `user@example.com / password123`
- [ ] Import Postman collection from Swagger
- [ ] Start developing! 🎉

---

**Last Updated:** 2025-10-09  
**Version:** 3.0 - Clean & Complete  
**License:** MIT  
**Author:** ABC Company
