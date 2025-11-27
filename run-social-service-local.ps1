# Run Social Service Locally
$env:POSTGRES_HOST = "localhost"
$env:POSTGRES_PORT = "5432"
$env:POSTGRES_USER = "postgres"
$env:POSTGRES_PASSWORD = "123456"
$env:SOCIAL_DB = "socialdb"
$env:SOCIAL_SERVICE_PORT = "8090"
$env:JWT_SECRET = "UCIafMmHwgsJKIgg4xVAL/eOvR3ZXD/ZnYE9AfMaMQg="
$env:EUREKA_DEFAULT_ZONE = "http://localhost:8761/eureka/"

Write-Host "Starting Social Service..." -ForegroundColor Green
Write-Host "Database: localhost:5432/socialdb" -ForegroundColor Yellow
Write-Host "Port: 8090" -ForegroundColor Yellow

cd social-service
java -jar target/social-service-0.0.1-SNAPSHOT.jar
