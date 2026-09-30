# Distributed Event-Driven E-Commerce Microservices Platform

A scalable, resilient, event-driven back-end microservices platform built with Java 17+, Spring Boot 3, Spring Cloud, Apache Kafka, PostgreSQL, MongoDB, Resilience4j, Docker, and Kubernetes.

---

## 🚀 Key Architectural Features

- **Decoupled Architecture**: 4 Independent domain services (`Order`, `Inventory`, `Payment`, `Notification`) + `API Gateway` & `Eureka Service Registry`.
- **Event-Driven Choreography Saga Pattern**: Distributed transaction management across services without distributed locks or 2PC.
- **Resilience & Fault Tolerance**: Resilience4j Circuit Breakers, Rate Limiters, and Fallback handlers.
- **Polyglot Persistence**: PostgreSQL for ACID relational data (Orders, Inventory, Payments) and MongoDB for non-relational notification logs.
- **Security**: Centralized JWT Authentication filter at the API Gateway.
- **Containerization & Orchestration**: Full `docker-compose` local development stack and production-ready Kubernetes deployment manifests.

---

## 🛠 Tech Stack

- **Language**: Java 17 / 21
- **Framework**: Spring Boot 3.2+, Spring Cloud (Gateway, Eureka Server, OpenFeign)
- **Messaging / Event Streaming**: Apache Kafka
- **Databases**: PostgreSQL, MongoDB
- **Resilience**: Resilience4j
- **Tracing & Metrics**: Micrometer, Zipkin, Prometheus
- **Container & Orchestration**: Docker, Docker Compose, Kubernetes

---

## 📁 Repository Structure

```
distributed-ecommerce-microservices/
├── docker-compose.yml             # Complete local infrastructure stack (Kafka, Postgres, Mongo, Zipkin)
├── ARCHITECTURE.md                # Detailed Saga & Event-Driven Architecture documentation
├── api-gateway/                   # Spring Cloud Gateway with JWT Auth Filter & Eureka Client
├── service-registry/              # Spring Cloud Netflix Eureka Server
├── order-service/                 # Order creation, Saga initiator, Kafka Producer & Consumer
├── inventory-service/             # Inventory reservation & stock management, Kafka Producer & Consumer
├── payment-service/               # Payment processing & refund compensating transaction logic
├── notification-service/          # Event listener for customer alerts (Email/SMS simulation)
└── k8s/                           # Kubernetes Deployment, Service, and ConfigMap manifests
```

---

## ⚡ Quick Start (Local Development)

### 1. Start Infrastructure Services (Kafka, Postgres, Mongo, Zipkin)
```bash
docker-compose up -d
```

### 2. Service Ports Overview
| Service | Port | Description |
| :--- | :--- | :--- |
| **API Gateway** | `8080` | Entry point for all client requests (JWT Secured) |
| **Eureka Registry** | `8761` | Service Discovery Dashboard |
| **Order Service** | `8081` | Order Management & Saga Initiator |
| **Inventory Service** | `8082` | Stock Reservation & Management |
| **Payment Service** | `8083` | Payment Processing & Compensating Refunds |
| **Notification Service**| `8084` | Event-driven Notification Service |
| **Kafka Broker** | `9092` | Event Broker |
| **PostgreSQL** | `5432` | Relational Databases (`order_db`, `inventory_db`, `payment_db`) |
| **MongoDB** | `27017` | Document Database (`notification_db`) |
| **Zipkin Tracing** | `9411` | Distributed Tracing UI |

---

## 🔄 Event-Driven Choreography Saga Flow

```
[ Client ] -> HTTP POST /api/v1/orders
               |
               v
        [ API Gateway ]
               |
               v
        [ Order Service ] -- (Creates Order PENDING)
               |
               +---> Kafka Topic: `order-created-events`
                           |
            +--------------+--------------+
            |                             |
            v                             v
  [ Inventory Service ]          [ Payment Service ]
    (Reserves Stock)               (Charges Payment)
            |                             |
    Success: StockReserved        Success: PaymentProcessed
    Failure: StockFailed          Failure: PaymentFailed
            |                             |
            +-------------->+<------------+
                            |
                            v
                   [ Order Service ]
             (Evaluates Saga State)
          - Both OK  -> Order CONFIRMED
          - Any FAIL -> Order CANCELLED + Trigger Compensating Actions
                            |
                            v
                 [ Notification Service ]
               (Sends Email/SMS Alert)
```

---

## 📄 License
MIT License.
