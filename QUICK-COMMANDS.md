# ============================================================
# Quick Commands Reference
# ============================================================

## 🚀 Quick Start

# 1. Build all services (skip tests)
.\quick-build.ps1

# 2. Run all services
.\quick-run.ps1

# 3. Import sample data
cd database-import
.\quick-import-data.ps1
cd ..

# 4. Test all APIs
.\test-all-109-apis.ps1

# 5. Stop services
.\quick-stop.ps1

## 📦 Individual Commands

# Build specific service
cd auth-service
.\mvnw.cmd clean package -DskipTests

# View logs
docker-compose logs -f gateway-service
docker-compose logs -f auth-service

# Restart service
docker-compose restart gateway-service

# Check service status
docker-compose ps

# Access PostgreSQL
docker exec -it postgres psql -U postgres -d userdb

# Access Redis
docker exec -it redis redis-cli

## 🔧 Troubleshooting

# Clean rebuild
docker-compose down -v
.\quick-build.ps1
.\quick-run.ps1

# View all logs
docker-compose logs --tail=100

# Remove all containers and volumes
docker-compose down -v --remove-orphans

## 🌐 Service URLs

Gateway:           http://localhost:8080
Eureka:            http://localhost:8761
Config Server:     http://localhost:8888
Auth Service:      http://localhost:8081
User Service:      http://localhost:8082
Career Service:    http://localhost:8084
Question Service:  http://localhost:8085
Exam Service:      http://localhost:8086
News Service:      http://localhost:8087
NLP Service:       http://localhost:5000
PostgreSQL:        localhost:5432
Redis:             localhost:6379

## 🔑 Test Credentials

admin@example.com / admin123 (ADMIN)
recruiter@example.com / admin123 (RECRUITER)
user@example.com / admin123 (USER)

## 📊 Key Files

Interview-Microservice-ABC-Postman-Collection.json  - Postman collection (109 APIs)
test-all-109-apis.ps1                               - Comprehensive test script
API-TESTING-GUIDE.md                                - Complete documentation
docker-compose.yml                                  - Docker configuration
