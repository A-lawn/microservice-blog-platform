#!/bin/bash

# Blog Platform Deployment Script

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}=========================================="
    echo -e "$1"
    echo -e "==========================================${NC}"
}

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

print_header "Deploying Microservice Blog Platform"

# Create necessary directories
print_status "Creating necessary directories..."
mkdir -p nacos/logs
mkdir -p nacos/data
mkdir -p logs/user-service
mkdir -p logs/article-service
mkdir -p logs/comment-service
mkdir -p logs/gateway

# Set permissions
chmod -R 755 nacos/
chmod -R 755 logs/

# Function to wait for service to be ready
wait_for_service() {
    local service_name=$1
    local port=$2
    local max_attempts=30
    local attempt=1

    print_status "Waiting for $service_name to be ready on port $port..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s http://localhost:$port/actuator/health > /dev/null 2>&1 || \
           curl -f -s http://localhost:$port > /dev/null 2>&1; then
            print_status "$service_name is ready!"
            return 0
        fi
        
        echo -n "."
        sleep 5
        attempt=$((attempt + 1))
    done
    
    print_error "$service_name failed to start within expected time"
    return 1
}

# Function to check database connectivity
check_database() {
    local db_name=$1
    local port=$2
    
    print_status "Checking $db_name database connectivity..."
    
    # Wait for MySQL to be ready
    local attempt=1
    local max_attempts=12
    
    while [ $attempt -le $max_attempts ]; do
        if docker-compose exec -T ${db_name} mysql -uroot -proot123 -e "SELECT 1" > /dev/null 2>&1; then
            print_status "$db_name database is ready!"
            return 0
        fi
        
        echo -n "."
        sleep 5
        attempt=$((attempt + 1))
    done
    
    print_error "$db_name database failed to start"
    return 1
}

# Deploy infrastructure services first
print_status "Starting infrastructure services..."
docker-compose up -d nacos-mysql redis elasticsearch rocketmq-namesrv

# Wait for infrastructure to be ready
sleep 10

# Start Nacos
print_status "Starting Nacos..."
docker-compose up -d nacos

# Wait for Nacos to be ready
wait_for_service "Nacos" 8848

# Start databases
print_status "Starting application databases..."
docker-compose up -d user-mysql article-mysql comment-mysql

# Check database connectivity
check_database "user-mysql" 3306
check_database "article-mysql" 3308
check_database "comment-mysql" 3309

# Start message queue broker
print_status "Starting RocketMQ broker..."
docker-compose up -d rocketmq-broker

# Start distributed transaction coordinator
print_status "Starting Seata server..."
docker-compose up -d seata-server

# Start monitoring services
print_status "Starting monitoring services..."
docker-compose up -d prometheus grafana loki promtail jaeger

# Wait a bit for all infrastructure to stabilize
print_status "Waiting for infrastructure to stabilize..."
sleep 15

# Start business services
print_status "Starting business services..."
docker-compose up -d user-service article-service comment-service

# Wait for business services to be ready
wait_for_service "User Service" 8081
wait_for_service "Article Service" 8082
wait_for_service "Comment Service" 8083

# Start API Gateway last
print_status "Starting API Gateway..."
docker-compose up -d gateway

# Wait for gateway to be ready
wait_for_service "API Gateway" 8080

print_header "Deployment Completed Successfully!"

echo ""
print_status "Service URLs:"
echo "  - API Gateway: http://localhost:8080"
echo "  - User Service: http://localhost:8081"
echo "  - Article Service: http://localhost:8082"
echo "  - Comment Service: http://localhost:8083"
echo "  - Nacos Console: http://localhost:8848/nacos (nacos/nacos)"
echo ""
print_status "Monitoring URLs:"
echo "  - Prometheus: http://localhost:9090"
echo "  - Grafana: http://localhost:3000 (admin/admin123)"
echo "  - Jaeger: http://localhost:16686"
echo ""
print_status "Database Connections:"
echo "  - User DB: localhost:3306/user_db"
echo "  - Article DB: localhost:3308/article_db"
echo "  - Comment DB: localhost:3309/comment_db"
echo "  - Redis: localhost:6379"
echo "  - Elasticsearch: http://localhost:9200"
echo ""

print_status "To check service status: docker-compose ps"
print_status "To view logs: docker-compose logs -f [service-name]"
print_status "To stop all services: docker-compose down"

print_header "Platform is ready for use!"