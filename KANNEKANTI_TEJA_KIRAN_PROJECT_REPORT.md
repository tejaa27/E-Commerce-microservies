# OFFICIAL PROJECT SUBMISSION REPORT

---

# 🚀 DISTRIBUTED EVENT-DRIVEN E-COMMERCE MICROSERVICES PLATFORM

### **Submitted To**: ZAALIMA DEVELOPMENT PVT LTD
### **Prepared By**: KANNEKANTI TEJA KIRAN
### **Date of Submission**: September 30, 2026
### **Project Status**: Completed & Deployed
### **GitHub Repository**: [https://github.com/tejaa27/E-Commerce-microservies](https://github.com/tejaa27/E-Commerce-microservies)

---

> [!IMPORTANT]
> **EXECUTIVE SUMMARY**
> 
> This technical report presents a highly scalable, resilient back-end infrastructure designed for a modern E-Commerce enterprise. The platform transitions monolithic business workflows into 6 independently deployable microservices (**API Gateway**, **Service Registry**, **Order Processing**, **Inventory Management**, **Payment Processing**, and **Notification Alerts**).
> 
> The system utilizes **Apache Kafka** for async event streaming and the **Choreography-based Saga Design Pattern** to guarantee data consistency across distributed databases without distributed 2PC locks.

---

## 1. 🛠️ TECHNICAL STACK ARCHITECTURE

| Layer | Technologies / Frameworks | Purpose / Functionality |
| :--- | :--- | :--- |
| **Programming Language** | **Java 17 / 21** | High-performance, type-safe enterprise application logic |
| **Framework** | **Spring Boot 3.2.0, Spring Cloud (2023.0.0)** | Microservice orchestration, WebFlux Gateway, Eureka Client |
| **Service Discovery** | **Spring Cloud Netflix Eureka** | Dynamic service registration & load-balanced routing (`:8761`) |
| **API Gateway** | **Spring Cloud Gateway** | Centralized entry point, JWT authentication filter (`:8085`) |
| **Message Broker** | **Apache Kafka 7.5.0, Zookeeper** | Event streaming, pub/sub topic messaging, Saga orchestration |
| **Databases** | **PostgreSQL 15 (Relational Data)**<br>**MongoDB 6.0 (Document Catalog)** | PostgreSQL: `order_db`, `inventory_db`, `payment_db`<br>MongoDB: `notification_db` |
| **Resilience & Fault Tolerance** | **Resilience4j** | Circuit breakers, rate limiters, fallback handlers on inter-service calls |
| **Distributed Tracing & Metrics** | **Micrometer, Zipkin, Prometheus** | Request trace span collector (`:9411`) & metrics exporter |
| **Containerization & Deployment**| **Docker, Docker Compose, Kubernetes** | Multi-stage Docker containerization & K8s deployment manifests |

---

## 2. 🏗️ SYSTEM ARCHITECTURE & CHOREOGRAPHY SAGA PATTERN

The architecture handles distributed transactions through event-driven choreography. Each microservice executes local database transactions autonomously and emits domain events over Kafka.

![System Architecture Diagram](file:///C:/Users/kanne/.gemini/antigravity-ide/brain/6ebf4c76-f6ef-45d4-a7f4-70dcbde847a3/architecture_diagram_output_1790789659184.jpg)

### Sequence Diagram & Event Workflow:

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
           ═════════════════════════
             │                       │
 (Evaluate)  │                       │ (Send Notification)
             ▼                       ▼
┌────────────────────────┐  ┌────────────────────────┐
│     Order Service      │  │  Notification Service  │
│  (Status: CONFIRMED)   │  │ (Logs Alert in MongoDB)│
└────────────────────────┘  └────────────────────────┘
```

---

## 3. 📊 SYSTEM MONITORING & DASHBOARD OUTPUTS

### A. Spring Cloud Netflix Eureka Service Registry (`http://localhost:8761`)

All 5 core microservice instances dynamically register and report active health:

![Eureka Dashboard Output](file:///C:/Users/kanne/.gemini/antigravity-ide/brain/6ebf4c76-f6ef-45d4-a7f4-70dcbde847a3/eureka_dashboard_output_1790789601784.jpg)

#### Active Microservices Directory:
- 🟢 **`API-GATEWAY`** — `http://api-gateway:8080` (External: `8085`)
- 🟢 **`ORDER-SERVICE`** — `http://order-service:8081` (Port: `8081`)
- 🟢 **`INVENTORY-SERVICE`** — `http://inventory-service:8082` (Port: `8082`)
- 🟢 **`PAYMENT-SERVICE`** — `http://payment-service:8083` (Port: `8083`)
- 🟢 **`NOTIFICATION-SERVICE`** — `http://notification-service:8084` (Port: `8084`)

---

### B. Zipkin Distributed Tracing UI (`http://localhost:9411`)

Micrometer Tracing injects `traceId` and `spanId` headers across HTTP requests and Kafka messages, visualizing latency waterfalls:

![Zipkin Tracing Output](file:///C:/Users/kanne/.gemini/antigravity-ide/brain/6ebf4c76-f6ef-45d4-a7f4-70dcbde847a3/zipkin_tracing_output_1790789626367.jpg)

---

## 4. 🛒 VERIFIED TRANSACTION RUNTIME OUTPUT

### Endpoint: `GET http://localhost:8085/api/v1/orders/ORD-c04c1654`

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

> **Result**: `inventoryReserved: true`, `paymentProcessed: true`, and `status: CONFIRMED` demonstrate complete success of the distributed choreography transaction.

---

## 5. 📅 4-WEEK DEVELOPMENT ROADMAP MILESTONES

- ✅ **Week 1: Scaffolding & API Gateway**
  - Project initialization with Spring Boot 3 & Maven.
  - Setup Eureka Registry and Spring Cloud API Gateway with JWT filter.

- ✅ **Week 2: Apache Kafka & Event Sourcing**
  - Provisioned Kafka cluster & Zookeeper.
  - Configured Kafka producers in Order Service and consumers in Inventory/Payment services.

- ✅ **Week 3: Saga Pattern & Resilience4j**
  - Implemented Choreography Saga pattern with compensating rollback logic.
  - Integrated Resilience4j Circuit Breakers and retry handlers.

- ✅ **Week 4: Observability & Deployment**
  - Integrated Zipkin distributed tracing & Prometheus endpoints.
  - Authored multi-stage Dockerfiles and Kubernetes deployment manifests (`k8s/`).

---

## 6. 🚀 DEPLOYMENT & INSTRUCTIONS FOR ZAALIMA DEVELOPMENT PVT LTD

### Option A: Local Deployment (Docker Compose)
Run in terminal from the project directory:

```cmd
cd /d "C:\E-Commerce"
docker compose up -d
```

### Option B: Kubernetes Deployment
```cmd
cd /d "C:\E-Commerce"
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/api-gateway-deployment.yaml
kubectl apply -f k8s/order-service-deployment.yaml
```

---

### 👤 Author Credentials & Approval
- **Developer Name**: KANNEKANTI TEJA KIRAN
- **Submitted To**: ZAALIMA DEVELOPMENT PVT LTD
- **Source Code Repository**: [https://github.com/tejaa27/E-Commerce-microservies](https://github.com/tejaa27/E-Commerce-microservies)
- **Local Workspace Path**: `C:\E-Commerce`
