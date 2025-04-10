# Currency Backend

Currency Backend is a Spring Boot application that serves as an Exchange Rate Service for Crewmeister. This application fetches, processes, and provides currency exchange rate data through a RESTful API.

## Project Structure

```
currency-backend/
├── .idea                  # IntelliJ IDEA configuration files
├── .mvn                   # Maven wrapper files
├── data                   # Data files
├── docker/                # Docker configuration files
│   ├── grafana            # Grafana configurations
│   ├── postgres           # PostgreSQL init scripts
│   └── prometheus         # Prometheus configurations
├── k8s/                   # Kubernetes configuration files
│   ├── namespaces         # Kubernetes namespace definitions
│   ├── prod               # Production environment configurations
│   └── uat                # UAT environment configurations
├── src/                   # Source code
│   ├── main/
│   │   ├── java/com/crewmeister/currencybackend/
│   │   │   ├── annotation        # Custom annotations
│   │   │   ├── aspect            # AOP aspects
│   │   │   ├── client            # External service clients
│   │   │   ├── config            # Application configurations
│   │   │   ├── controller        # REST API controllers
│   │   │   ├── dto               # Data Transfer Objects
│   │   │   ├── entity            # JPA entities
│   │   │   ├── exception         # Custom exceptions
│   │   │   ├── mapper            # DTO-Entity mappers
│   │   │   ├── repository        # JPA repositories
│   │   │   ├── service           # Business logic services
│   │   │   └── utils             # Helper utility classes
│   │   └── resources            # Configuration files
│   └── test/                    # Test code
│       └── java/com/crewmeister/currencybackend/
│           ├── controller        # Controller tests
│           ├── service.impl      # Service implementation tests
│           └── utils             # Test utilities
├── target                # Build output
├── .gitattributes        # Git attribute configuration
├── .gitignore            # Git ignore configuration
├── docker-compose.yml    # Docker compose configuration
├── Dockerfile            # Application Dockerfile
├── HELP.md               # Help file
├── Jenkinsfile           # Jenkins pipeline definition
└── pom.xml               # Maven project definition
```

## Technologies

- **Java 11**: Main programming language
- **Spring Boot 2.7.18**: Application framework
- **Spring Data JPA**: Database access
- **Spring Cloud OpenFeign**: External API calls
- **PostgreSQL**: Main database
- **Lombok**: Code simplification
- **Resilience4j**: Fault tolerance and service resilience
- **JUnit & Mockito**: Testing framework
- **Jacoco**: Test coverage measurement
- **SonarQube**: Code quality analysis
- **Swagger/OpenAPI**: API documentation
- **Docker & Docker Compose**: Containerization
- **Kubernetes**: Container orchestration
- **Prometheus & Grafana**: Monitoring and metrics visualization
- **Jenkins**: CI/CD pipeline

## Prerequisites

- Java 11 JDK
- Maven 3.6+
- Docker and Docker Compose (for running dependent services)
- IDE (IntelliJ IDEA recommended)

## Setup and Running the Application

### Using Docker Compose for External Services

The application relies on several external services which can be started using Docker Compose:

```bash
# Start all external services (PostgreSQL, SonarQube, Jenkins, Prometheus, Grafana)
docker-compose up -d
```

This will start:
- PostgreSQL database on port 5432
- SonarQube for code quality analysis on port 9000
- Jenkins for CI/CD on port 8081
- Prometheus for metrics collection on port 9090
- Grafana for metrics visualization on port 3000

### Running the Spring Boot Application

There are several ways to run the Spring Boot application:

#### Using IDE (IntelliJ IDEA)

1. Open the project in IntelliJ IDEA
2. Navigate to `CurrencyBackendApplication.java`
3. Right-click and select "Run CurrencyBackendApplication"

#### Using Maven

```bash
# Run using Maven Spring Boot plugin
./mvnw spring-boot:run
```

#### Using Java JAR

```bash
# Build the application
./mvnw clean package

# Run the JAR file
java -jar target/currency-backend-0.0.1-SNAPSHOT.jar
```

### Using Docker (Full Application)

To build and run the entire application including the Spring Boot service in Docker:

```bash
# Build the Docker image
docker build -t currency-backend .

# Run the container
docker run -p 8088:8088 --network currency-network currency-backend
```

## Configuration

The application can be configured through the following properties files:
- `src/main/resources/application.properties` - Main configuration
- `src/main/resources/application-dev.properties` - Development environment configuration
- `src/main/resources/application-prod.properties` - Production environment configuration

## API Documentation

Once the application is running, the API documentation is available at:
- Swagger UI: http://localhost:8088/swagger-ui.html
- OpenAPI Specification: http://localhost:8088/v3/api-docs

## Testing

```bash
# Run tests
./mvnw test

# Run tests with coverage report
./mvnw verify

# Run SonarQube analysis
./mvnw sonar:sonar
```

## Code Quality

The project is configured with SonarQube for code quality analysis. After running the SonarQube analysis, you can view the results at http://localhost:9000.

## Monitoring

The application is configured with Spring Boot Actuator and Prometheus for monitoring. Grafana dashboards are available at http://localhost:3000 after starting the Docker Compose setup.

## CI/CD Pipeline

The project includes a Jenkinsfile for CI/CD pipeline configuration. You can access Jenkins at http://localhost:8081 after starting the Docker Compose setup.

## Kubernetes Deployment

Kubernetes configuration files are available in the `k8s` directory for deploying the application to a Kubernetes cluster:

```bash
# Apply Kubernetes configurations
kubectl apply -f k8s/namespaces/
kubectl apply -f k8s/uat/  # For UAT environment
kubectl apply -f k8s/prod/ # For Production environment
```

## Contributing

Please follow these guidelines when contributing to the project:
1. Write unit tests for new features
2. Maintain code coverage above 70%
3. Follow the existing code style and architecture
4. Update documentation for significant changes

## License

This project is proprietary software owned by Crewmeister.
