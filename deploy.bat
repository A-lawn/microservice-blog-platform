@echo off
REM Blog Platform Deployment Script for Windows

echo ==========================================
echo Deploying Microservice Blog Platform
echo ==========================================

REM Check if Docker Compose is installed
where docker-compose >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Docker Compose is not installed. Please install Docker Compose first.
    exit /b 1
)

REM Create necessary directories
echo [INFO] Creating necessary directories...
if not exist "nacos\logs" mkdir nacos\logs
if not exist "nacos\data" mkdir nacos\data
if not exist "logs\user-service" mkdir logs\user-service
if not exist "logs\article-service" mkdir logs\article-service
if not exist "logs\comment-service" mkdir logs\comment-service
if not exist "logs\gateway" mkdir logs\gateway

REM Deploy infrastructure services first
echo [INFO] Starting infrastructure services...
docker-compose up -d nacos-mysql redis elasticsearch rocketmq-namesrv

REM Wait for infrastructure to be ready
echo [INFO] Waiting for infrastructure to initialize...
timeout /t 10 /nobreak >nul

REM Start Nacos
echo [INFO] Starting Nacos...
docker-compose up -d nacos

REM Wait for Nacos to be ready
echo [INFO] Waiting for Nacos to be ready...
timeout /t 30 /nobreak >nul

REM Start databases
echo [INFO] Starting application databases...
docker-compose up -d user-mysql article-mysql comment-mysql

REM Wait for databases
echo [INFO] Waiting for databases to be ready...
timeout /t 20 /nobreak >nul

REM Start message queue broker
echo [INFO] Starting RocketMQ broker...
docker-compose up -d rocketmq-broker

REM Start distributed transaction coordinator
echo [INFO] Starting Seata server...
docker-compose up -d seata-server

REM Start monitoring services
echo [INFO] Starting monitoring services...
docker-compose up -d prometheus grafana loki promtail jaeger

REM Wait for infrastructure to stabilize
echo [INFO] Waiting for infrastructure to stabilize...
timeout /t 15 /nobreak >nul

REM Start business services
echo [INFO] Starting business services...
docker-compose up -d user-service article-service comment-service

REM Wait for business services
echo [INFO] Waiting for business services to be ready...
timeout /t 30 /nobreak >nul

REM Start API Gateway last
echo [INFO] Starting API Gateway...
docker-compose up -d gateway

REM Wait for gateway
echo [INFO] Waiting for gateway to be ready...
timeout /t 15 /nobreak >nul

echo ==========================================
echo Deployment Completed Successfully!
echo ==========================================

echo.
echo [INFO] Service URLs:
echo   - API Gateway: http://localhost:8080
echo   - User Service: http://localhost:8081
echo   - Article Service: http://localhost:8082
echo   - Comment Service: http://localhost:8083
echo   - Nacos Console: http://localhost:8848/nacos (nacos/nacos)
echo.
echo [INFO] Monitoring URLs:
echo   - Prometheus: http://localhost:9090
echo   - Grafana: http://localhost:3000 (admin/admin123)
echo   - Jaeger: http://localhost:16686
echo.
echo [INFO] Database Connections:
echo   - User DB: localhost:3306/user_db
echo   - Article DB: localhost:3308/article_db
echo   - Comment DB: localhost:3309/comment_db
echo   - Redis: localhost:6379
echo   - Elasticsearch: http://localhost:9200
echo.

echo [INFO] To check service status: docker-compose ps
echo [INFO] To view logs: docker-compose logs -f [service-name]
echo [INFO] To stop all services: docker-compose down

echo ==========================================
echo Platform is ready for use!
echo ==========================================