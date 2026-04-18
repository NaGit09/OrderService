# 🛒 Order Service - Furniro Backend

Welcome to the **Order Service**, a robust and scalable microservice designed to handle the core checkout flow of the **Furniro** e-commerce platform. This service manages shopping carts, processes orders, and coordinates with other services via event-driven messaging.

---

## 🚀 Overview

The Order Service is built with a modern, high-performance architecture to ensure seamless transaction handling. It serves as the backbone for:
- 🛒 **Cart Management**: Real-time shopping cart operations storage.
- 📦 **Order Lifecycle**: From creation to fulfillment tracking.
- 💳 **Payment Orchestration**: Initial hooks for secure payment processing.
- 📬 **Event-Driven Messaging**: Leveraging Kafka for reliable inter-service communication.

---

## 🛠 Tech Stack

- **Core**: Java 17, Spring Boot 4.0.5 (Project Version)
- **Database**: MySQL (relational storage for orders & user data)
- **Messaging**: Apache Kafka (asynchronous events)
- **Caching & Sessions**: Redis (high-speed cart & session management)
- **Documentation**: Springdoc OpenAPI / Swagger UI
- **AI Integration**: Spring AI (BOM 2.0.0-M4)
- **Utilities**: Lombok, Dotenv, Hibernate JPA

---

## 📦 Project Structure

```text
src/main/java/com/furniro/OrderService/
├── config/      # Application & Security configurations
├── controller/  # REST API Resources
├── database/    # Entities & JPA Repositories
├── dto/         # Data Transfer Objects
├── exception/   # Global Exception Handling
├── service/     # Business Logic (Cart, Order, Payment)
└── utils/       # Shared Utilities
```

---

## ⚙️ Environment Configuration

The service uses a `.env` file for configuration. Ensure the following variables are defined before running:

```env
SERVER_PORT=8082

# DATABASE
DATABASE_URL=jdbc:mysql://localhost:3306/FURNIRO_ORDER
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password

# KAFKA
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_CONSUMER_GROUP_ID=order-service-group

# REDIS
REDIS_HOST=localhost
REDIS_PORT=6379
```

---

## 🚀 Getting Started

### Prerequisites
- **JDK 17+**
- **Maven**
- **Docker** (Recommended for infra dependencies)

### Local Development

1. **Clone the repository**:
   ```bash
   git clone https://github.com/NaGit09/Order-Service.git
   cd Order-Service
   ```

2. **Set up Infrastructure**:
   Ensure MySQL, Kafka, and Redis are running. You can use a `docker-compose.yml` if available or run them locally.

3. **Install Dependencies**:
   ```bash
   ./mvnw clean install
   ```

4. **Run the Service**:
   ```bash
   ./mvnw spring-boot:run
   ```

---

## 📖 API Documentation

The project includes integrated Swagger documentation. Once the server is running, explore the API endpoints at:
📌 [Swagger UI](http://localhost:8082/swagger-ui/index.html)

---

## 🐳 Docker Deployment

To build and run the service via Docker:

```bash
docker build -t order-service .
docker run -p 8082:8082 order-service
```

---

## 🤝 Contributing

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

*Part of the Furniro E-Commerce Ecosystem.*
