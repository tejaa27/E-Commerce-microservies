# 🚀 Distributed Event-Driven E-Commerce Microservices Platform

[![Java 17](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot 3.2.0](https://img.shields.io/badge/Spring_Boot-3.2.0-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Cloud 2023.0.0](https://img.shields.io/badge/Spring_Cloud-2023.0.0-blue?logo=spring)](https://spring.io/projects/spring-cloud)
[![Apache Kafka 7.5.0](https://img.shields.io/badge/Apache_Kafka-7.5.0-black?logo=apachekafka)](https://kafka.apache.org/)
[![PostgreSQL 15](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)](https://www.postgresql.org/)
[![MongoDB 6.0](https://img.shields.io/badge/MongoDB-6.0-green?logo=mongodb)](https://www.mongodb.com/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-blue?logo=docker)](https://www.docker.com/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-Orchestrated-blue?logo=kubernetes)](https://kubernetes.io/)

A highly scalable, resilient, event-driven back-end infrastructure built for high-traffic modern e-commerce platforms. The system breaks down a monolithic architecture into **6 independently deployable microservices** (`Order`, `Inventory`, `Payment`, `Notification`, `API Gateway`, and `Eureka Registry`) communicating asynchronously via **Apache Kafka** and the **Choreography-based Saga Design Pattern**.

---

## 🏗️ System Architecture

The platform uses asynchronous event sourcing to guarantee data consistency across distributed relational (**PostgreSQL**) and document (**MongoDB**) datastores without distributed locks or 2PC protocols.

![System Architecture Diagram](assets/architecture_diagram.jpg)

### 🔄 Event-Driven Choreography Saga Flow:

```
[ Client Request ] ➔ HTTP POST /api/v1/orders
                         │
                         ▼
               ┌──────────────────┐
               │   API Gateway    │ (Port 8085 - JWT Auth Filter)
               └────────┬─────────┘
                        │
                        ▼
               ┌──────────────────┐
               │  Order Service   │ (Status: PENDING)
               └────────┬─────────┘
                        │ Publish OrderCreatedEvent
                        ▼
           ═════════════════════════════
                 Apache Kafka Broker
           ═════════════════════════════
             │                       │
 (Consume)   │                       │ (Consume)
             ▼                       ▼
┌────────────────────────┐  ┌────────────────────────┐
│   Inventory Service    │  │    Payment Service     │
│   (Reserves Stock)     │  │   (Charges Payment)    │
└───────────┬────────────┘  └───────────┬────────────┘
            │                           │
            └─────────────┬─────────────┘
                          │ Publish Result Events
                          ▼
           ═════════════════════════════
                 Apache Kafka Broker
           ═════════════════════════════
             │                       │
 (Evaluate)  │                       │ (Send Notification)
             ▼                       ▼
┌────────────────────────┐  ┌────────────────────────┐
│     Order Service      │  │  Notification Service  │
│  (Status: CONFIRMED)   │  │ (Logs Alert in MongoDB)│
└────────────────────────┘  └────────────────────────┘
```

---

## 📊 System Monitoring & Runtime Outputs

### 1. Spring Cloud Netflix Eureka Service Registry (`http://localhost:8761`)
Dynamic discovery and load balancing dashboard showing all registered microservices marked **`UP`**:

![Eureka Service Registry Output](assets/eureka_dashboard.jpg)

### 2. Zipkin Distributed Tracing UI (`http://localhost:9411`)
Distributed trace span waterfall tracking Kafka message propagation latencies and HTTP request flows:

![Zipkin Tracing Waterfall Output](assets/zipkin_tracing.jpg)

---

## 📁 Microservices Directory Structure

```
distributed-ecommerce-microservices/
├── docker-compose.yml             # Container stack definition (Kafka, Postgres, Mongo, Zipkin + 6 Java services)
├── ARCHITECTURE.md                # Technical Saga architecture documentation
├── README.md                      # Primary repository documentation
├── init-db.sh                     # PostgreSQL database creation script (order_db, inventory_db, payment_db)
├── assets/                        # Architecture diagrams & dashboard output screenshots
├── api-gateway/                   # Spring Cloud Gateway (JWT authentication filter) + Dockerfile
├── service-registry/              # Netflix Eureka Server dashboard + Dockerfile
├── order-service/                 # Order management & Saga initiator + Dockerfile
├── inventory-service/             # Stock reservation & rollback compensating transactions + Dockerfile
├── payment-service/               # Payment processing & refund logic + Dockerfile
├── notification-service/          # MongoDB async notification logger + Dockerfile
└── k8s/                           # Kubernetes Deployments, Services & ConfigMaps
```

---

## 📅 4-Week Development Roadmap Breakdown

### 🔹 Week 1: Scaffolding, Discovery & API Gateway
- **Service Discovery**: Provisioned Netflix Eureka Server in `service-registry`.
- **Core Microservices**: Scaffolded Spring Boot 3 projects with Spring Data JPA & PostgreSQL.
- **Gateway Security**: Implemented centralized JWT authentication filter in `api-gateway`.

### 🔹 Week 2: Apache Kafka & Event Sourcing
- **Messaging Infrastructure**: Configured Kafka broker & Zookeeper cluster.
- **Event Schemas**: Defined JSON event schemas (`OrderCreatedEvent`, `InventoryReservedEvent`, `PaymentProcessedEvent`).
- **Pub/Sub Integration**: Implemented Kafka producers in `order-service` and consumers in `inventory-service` & `payment-service`.

### 🔹 Week 3: Choreography Saga Pattern & Resilience
- **Saga Pattern**: Implemented distributed saga state evaluation in `order-service`.
- **Compensating Actions**: Integrated automatic rollbacks (`INVENTORY_RELEASE`, `PAYMENT_REFUND`).
- **Resilience4j**: Configured Circuit Breakers (`slidingWindowSize = 10`, `failureRateThreshold = 50%`) and retry mechanisms.

### 🔹 Week 4: Observability, Containerization & Kubernetes Deployment
- **Distributed Tracing**: Integrated Micrometer Tracing & Zipkin collector (`http://zipkin:9411/api/v2/spans`).
- **Multi-Stage Containerization**: Authored optimized multi-stage `Dockerfile`s for all microservices.
- **Kubernetes Manifests**: Developed Kubernetes deployment manifests in `k8s/` for production orchestration.

---

## 🛒 API Endpoint Specifications & Verified Output

### 1. Place a New Order (`POST /api/v1/orders`)

#### Request Body:
```json
{
  "customerId": "cust_new_1",
  "productId": "prod_laptop_1",
  "quantity": 1,
  "totalAmount": 499.99
}
```

### 2. Verified Order Response (`GET /api/v1/orders/{orderNumber}`)

#### Response Output (Gateway Routed `http://localhost:8085/api/v1/orders/ORD-c04c1654`):
```json
{
  "id": 24,
  "orderNumber": "ORD-c04c1654",
  "customerId": "cust_new_1",
  "productId": "prod_laptop_1",
  "quantity": 1,
  "totalAmount": 499.99,
  "status": "CONFIRMED",
  "inventoryReserved": true,
  "paymentProcessed": true,
  "createdAt": "2026-09-30T22:53:01Z"
}
```

> **Saga State Result**: `inventoryReserved: true`, `paymentProcessed: true`, and `status: CONFIRMED` demonstrate full distributed transaction completion across microservices!

---

## ⚡ Quick Start Guide (Local Deployment)

### 1. Clone the Repository
```bash
git clone https://github.com/tejaa27/E-Commerce-microservies.git
cd E-Commerce-microservies
```

### 2. Launch Full Stack with Docker Compose
Ensure **Docker Desktop** is running, then execute:

```bash
docker compose up --build -d
```

### 3. Service Ports Overview
| Service Name | Host Access Port | Internal Port | Description / URL |
| :--- | :---: | :---: | :--- |
| **API Gateway** | `8085` | `8080` | Entry Point: [http://localhost:8085/api/v1/orders](http://localhost:8085/api/v1/orders) |
| **Eureka Registry** | `8761` | `8761` | Dashboard: [http://localhost:8761](http://localhost:8761) |
| **Zipkin Tracing** | `9411` | `9411` | Dashboard: [http://localhost:9411](http://localhost:9411) |
| **Order Service** | `8081` | `8081` | Order Management & Saga Initiator |
| **Inventory Service**| `8082` | `8082` | Stock Reservation & Inventory DB |
| **Payment Service** | `8083` | `8083` | Payment Processing & Refund Compensating Tx |
| **Notification Service**| `8084` | `8084` | MongoDB Customer Alert Logger |
| **Kafka Broker** | `9092` | `9092` | Event Broker (`localhost:9092` / `localhost:29092`) |
| **PostgreSQL** | `5432` | `5432` | Relational Databases (`order_db`, `inventory_db`, `payment_db`) |
| **MongoDB** | `27017` | `27017` | Document Database (`notification_db`) |

---

## ☸️ Kubernetes Deployment

Deploy to Minikube or Cloud Kubernetes cluster using the included manifests:

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/api-gateway-deployment.yaml
kubectl apply -f k8s/order-service-deployment.yaml
```

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for details.
