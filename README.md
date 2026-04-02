# 🍔 FOODIE - Cloud-Native Distributed Food Ordering Platform
![Java](https://img.shields.io/badge/Java_21-ED8B00?style=flat&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.2-6DB33F?style=flat&logo=springboot&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=flat&logo=apachekafka)
![AWS](https://img.shields.io/badge/AWS_EKS-FF9900?style=flat&logo=amazonaws)
![Terraform](https://img.shields.io/badge/Terraform-623CE4?style=flat&logo=terraform)

> A production-grade microservices system built to demonstrate **Event-Driven Architecture**, **Infrastructure as Code**, and **High-Availability** patterns on AWS.

---

## 🏗️ System Architecture
This project implements a distributed system using the **Saga Pattern (Orchestration)** to manage transactions across disparate services without tight coupling.

* **Bounded Contexts:** Order, Payment, Inventory, Delivery, Auth, Notification.
* **Event Bus:** Apache Kafka for asynchronous inter-service communication.
* **Data Strategy:** Polyglot persistence (MySQL for ACID, MongoDB for Catalog, Redis for Caching).

### 🔄 The Event-Driven Workflow
1.  **Order Created:** `OrderService` publishes event → **Kafka**.
2.  **Payment Processing:** `PaymentService` consumes event → Deducts balance (Idempotent) → Publishes `PaymentConfirmed`.
3.  **Delivery Allocation:** `DeliveryService` allocates rider → Updates real-time status via **WebSockets**.
4.  **Compensation:** If Payment fails, a `RollbackEvent` triggers compensating transactions in `OrderService`.

---

## 🛠️ Tech Stack

| Category | Technologies |
| :--- | :--- |
| **Core Backend** | Java 21, Spring Boot 3.2, Spring Cloud, OpenFeign |
| **Messaging** | Apache Kafka (Idempotent Consumers, Retry Topics, DLQ) |
| **Databases** | MySQL 8, MongoDB, Redis (Cache-Aside) |
| **Infrastructure** | AWS EKS, Terraform, Docker, Helm Charts |
| **CI/CD** | GitHub Actions, Jenkins |
| **Observability** | Prometheus, Grafana, OpenTelemetry, ELK Stack, Jaegar |
| **Resilience** | Resilience4j (Circuit Breakers, Bulkheads, Rate Limiters) |

---

## 🚀 Infrastructure & DevOps

The platform is designed to be "Operationally Ready" with full automation.

### 1. Infrastructure as Code (Terraform)
Infrastructure is provisioned using Terraform modules found in the `[infra/terraform]` directory.
* **EKS Cluster:** Managed Kubernetes control plane with auto-scaling worker nodes.
* **Networking:** VPC, Public/Private Subnets, NAT Gateways, and Security Groups.
* **State Management:** S3 Backend with DynamoDB locking.

### 2. Kubernetes Deployment (Helm)
Services are packaged as Helm charts in `[infra/helm]`.
* **Rolling Updates:** Configured `maxUnavailable` and `maxSurge` for zero-downtime.
* **Probes:** Liveness (Deadlocks) and Readiness (Dependency checks) probes configured for Spring Boot Actuator.
* **Resources:** Requests/Limits tuned based on JVM memory profiling.

### 3. CI/CD Pipeline
Automated pipelines defined in `[.github/workflows]`:
* **CI:** Unit Tests (JUnit) → Maven Build → Docker Image Build → Push to ECR.
* **CD:** Updates Helm release on EKS upon merge to `main`.

---

## 📊 Observability & Reliability

### Distributed Tracing
Integrated **OpenTelemetry** agents to trace requests across the mesh.
* *Trace ID propagation from API Gateway → Order → Kafka → Payment.*

### Metrics (Grafana)
Custom dashboards monitoring key SLIs:
* **Kafka Consumer Lag:** Monitoring backpressure.
* **P95 Latency:** Alerting if API latency exceeds 200ms.
* **Circuit Breaker State:** Tracking `OPEN` states in Resilience4j.

### Resilience Patterns
* **Bulkhead Pattern:** Isolated thread pools for Payment Gateway calls to prevent cascading failures.
* **Dead Letter Queues:** Poison messages (malformed JSON) are automatically moved to DLQ for manual inspection.

---

## 🏃‍♂️ Getting Started (Local)

You can spin up the entire stack locally using Docker Compose.

```bash
# 1. Clone the repo
git clone [https://github.com/chandasaiprakash/foodie.git](https://github.com/chandasaiprakash/foodie.git)

# 2. Start Infrastructure (Kafka, Zookeeper, MySQL, Redis, Mongo, Zipkin)
docker-compose up -d infra

# 3. Build & Start Microservices
mvn clean package -DskipTests
docker-compose up -d services
