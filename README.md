# 🛒 Order Service - Furniro Backend

Welcome to the **Order Service**, a robust and scalable microservice designed to handle the core checkout flow of the **Furniro** e-commerce platform. This service manages shopping carts, processes orders, and coordinates with other services via event-driven messaging.

---

## 🚀 Overview

The Order Service is built with a modern, high-performance architecture to ensure seamless transaction handling. It serves as the backbone for:
- 🛒 **Cart Management**: Real-time shopping cart operations and storage.
- 📦 **Order Lifecycle**: From creation to fulfillment tracking.
- 💳 **Payment Orchestration**: Integration with secure payment gateways like **PayPal**.
- 📬 **Event-Driven Messaging**: Leveraging **Kafka** for reliable inter-service communication.

---

## 🛠 Tech Stack

- **Core**: Java 17, Spring Boot
- **Database**: MySQL (Order & Payment records)
- **Messaging**: Apache Kafka (Asynchronous events)
- **Caching**: Redis (Cart management)
- **Payments**: PayPal API Integration
- **Documentation**: Springdoc OpenAPI / Swagger UI
- **AI Integration**: Spring AI

---

## 📦 Project Structure

```text
src/main/java/com/furniro/OrderService/
├── config/      # Application, PayPal & Security configurations
├── controller/  # REST API Resources (Order, Cart, PayPal)
├── database/    # Entities & JPA Repositories
├── dto/         # Data Transfer Objects (Requests & Responses)
├── exception/   # Global Exception Handling
├── service/     # Business Logic (Cart, Order, PayPal integration)
└── utils/       # Shared Utilities & Enums
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

# PAYPAL
PAYPAL_CLIENT_ID=your_paypal_client_id
PAYPAL_CLIENT_SECRET=your_paypal_client_secret
PAYPAL_MODE=sandbox
```

---

## 💳 PayPal Integration Flow

The Order Service implements a standard PayPal Checkout flow:
1. **Create Order**: Client calls `/api/v1/orders` with `PAYPAL` method. Backend creates a local order and a PayPal order, returning the approval link.
2. **User Approval**: User approves the payment on the PayPal portal.
3. **Capture Payment**: Client calls `/api/v1/orders/capture-paypal` with the PayPal order ID. Backend captures the funds and updates the order status to `COMPLETED`.

---

## 🚀 Getting Started

### Prerequisites
- **JDK 17+**
- **Maven**
- **Docker** (Recommended for MySQL, Kafka, and Redis)

### Local Development

1. **Clone the repository**:
   ```bash
   git clone https://github.com/NaGit09/Order-Service.git
   cd Order-Service
   ```

2. **Set up Infrastructure**:
   Ensure MySQL, Kafka, and Redis are running.

3. **Install Dependencies**:
   ```bash
   mvn clean install
   ```

4. **Run the Service**:
   ```bash
   mvn spring-boot:run
   ```

---

## 📖 API Documentation

Explore the API endpoints once the server is running:
📌 [Swagger UI](http://localhost:8082/swagger-ui/index.html)

---

## 🤝 Contributing

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

*Part of the Furniro E-Commerce Ecosystem.*
