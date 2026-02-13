# Microservice Blog Platform

A cloud-native blog platform built with Spring Boot, Spring Cloud, and modern microservices architecture. This project demonstrates best practices in Domain-Driven Design (DDD), CQRS, distributed transactions, and comprehensive observability.

## 🏗️ Architecture Overview

### Core Services
- **User Service** (Port 8081): User management, authentication, and authorization
- **Article Service** (Port 8082): Article creation, publishing, and search
- **Comment Service** (Port 8083): Comment management and hierarchical replies
- **API Gateway** (Port 8080): Unified entry point with routing and security

### Infrastructure Components
- **Nacos**: Service discovery and configuration management
- **MySQL**: Primary data storage (separate databases per service)
- **Redis**: Caching and session management
- **Elasticsearch**: Full-text search and read models (CQRS)
- **RocketMQ**: Asynchronous messaging and event-driven architecture
- **Seata**: Distributed transaction management

### Observability Stack
- **Prometheus**: Metrics collection and monitoring
- **Grafana**: Visualization and dashboards
- **Loki**: Centralized logging
- **Jaeger**: Distributed tracing

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- 4GB+ RAM recommended

### 1. Clone and Build
```bash
git clone <repository-url>
cd microservice-blog-platform

# Build all services
chmod +x build.sh
./build.sh
```

### 2. Deploy Infrastructure
```bash
# Deploy all services
chmod +x deploy.sh
./deploy.sh
```

### 3. Access Services
- **API Gateway**: http://localhost:8080
- **Nacos Console**: http://localhost:8848/nacos (nacos/nacos)
- **Grafana**: http://localhost:3001 (admin/admin123)
- **Prometheus**: http://localhost:9090
- **Jaeger**: http://localhost:16686

## 📁 Project Structure

```
microservice-blog-platform/
├── blog-common/              # Shared utilities and components
├── user-service/             # User management microservice
├── article-service/          # Article management microservice
├── comment-service/          # Comment management microservice
├── gateway/                  # API Gateway
├── sql/                      # Database schemas
├── monitoring/               # Monitoring configurations
├── docker-compose.yml        # Container orchestration
├── build.sh                  # Build script
└── deploy.sh                 # Deployment script
```

## 🔧 Configuration

### Environment Profiles
- **default**: Local development
- **docker**: Container deployment

### Key Configuration Files
- `docker-compose.yml`: Service orchestration
- `monitoring/prometheus.yml`: Metrics collection
- `monitoring/loki.yml`: Log aggregation
- `sql/*.sql`: Database schemas

## 🧪 Testing Strategy

The project implements comprehensive testing with:

### Unit Tests
- JUnit 5 for standard unit testing
- Mockito for mocking dependencies
- TestContainers for integration testing

### Property-Based Testing
- jqwik library for property-based testing
- Validates correctness properties across all inputs
- Minimum 100 iterations per property test

### Test Execution
```bash
# Run all tests
mvn test

# Run tests for specific service
cd user-service && mvn test

# Run with coverage
mvn test jacoco:report
```

## 📊 Monitoring and Observability

### Metrics (Prometheus + Grafana)
- **System Metrics**: CPU, memory, disk usage
- **Application Metrics**: Request rates, response times, error rates
- **Business Metrics**: User registrations, article publications, comments

### Logging (Loki + Promtail)
- Centralized log aggregation
- Structured logging with correlation IDs
- Log levels: ERROR, WARN, INFO, DEBUG

### Tracing (Jaeger)
- Distributed request tracing
- Service dependency mapping
- Performance bottleneck identification

## 🔐 Security Features

- JWT-based authentication
- Role-based access control (RBAC)
- API rate limiting
- Input validation and sanitization
- SQL injection prevention

## 🗄️ Database Design

### User Service Database
- Users, user statistics, roles, sessions

### Article Service Database  
- Articles, categories, tags, statistics, likes

### Comment Service Database
- Comments, hierarchical replies, likes, reports, notifications

## 🚦 API Endpoints

### User Service
```
POST /api/users/register     # User registration
POST /api/users/login        # User authentication
GET  /api/users/{id}         # Get user profile
PUT  /api/users/{id}/profile # Update user profile
```

### Article Service
```
POST /api/articles           # Create article
PUT  /api/articles/{id}/publish # Publish article
GET  /api/articles           # List articles (paginated)
GET  /api/articles/{id}      # Get article details
```

### Comment Service
```
POST /api/comments           # Create comment
POST /api/comments/{id}/reply # Reply to comment
GET  /api/comments/article/{id} # Get article comments
```

## 🔄 Distributed Transactions

### Saga Pattern Implementation
- **Article Publishing**: Update article status → Update user statistics → Send notifications
- **Comment Creation**: Create comment → Update article statistics → Send notifications

### Compensation Mechanisms
- Automatic rollback on failure
- Audit logging for transaction states
- Manual intervention capabilities

## 📈 Performance Optimization

### Caching Strategy
- Redis for session management
- Application-level caching for hot data
- Cache warming for popular content

### Database Optimization
- Proper indexing strategy
- Read/write separation
- Connection pooling

### JVM Tuning
- G1 garbage collector
- Optimized heap sizes for containers
- JVM monitoring and profiling

## 🐳 Container Deployment

### Resource Requirements
- **Minimum**: 2 CPU cores, 4GB RAM, 20GB storage
- **Recommended**: 4 CPU cores, 8GB RAM, 50GB storage

### Scaling Considerations
- Horizontal scaling for business services
- Load balancing with Spring Cloud Gateway
- Database connection pool management

## 🛠️ Development Guidelines

### Code Quality
- Follow Spring Boot best practices
- Implement proper error handling
- Use meaningful logging
- Write comprehensive tests

### Domain-Driven Design
- Clear bounded contexts
- Rich domain models
- Event-driven communication
- CQRS for read/write separation

## 📚 Documentation

- [API Documentation](docs/api.md)
- [Architecture Guide](docs/architecture.md)
- [Deployment Guide](docs/deployment.md)
- [Troubleshooting](docs/troubleshooting.md)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Implement changes with tests
4. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For issues and questions:
- Create an issue in the repository
- Check the troubleshooting guide
- Review the documentation

---

**Built with ❤️ using Spring Boot, Spring Cloud, and modern microservices patterns**