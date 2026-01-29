# FineTune
Production-ready Spring Boot template project.


Act as a senior Spring Boot engineer.
Follow clean architecture principles.
Generate minimal, readable, production-quality code.
Explain assumptions when needed.


# AI Prompts

A. Entity + Repository
- Context:
- Spring Boot 3
- Java 17
- PostgreSQL
- JPA (Hibernate)
- No Lombok

Task:
Create a JPA entity called Order with fields:
- id (UUID, primary key)
- customerId (UUID)
- status (ENUM)
- totalAmount (BigDecimal)
- createdAt (Instant)

Include:
- Proper JPA annotations
- Enum mapping
- Equals and hashCode best practices


B. Service Layer
Context:
- Layered architecture
- Constructor injection only
- Business logic belongs in service
- Repository already exists

Task:
Create OrderService with methods:
- createOrder(CreateOrderRequest dto)
- getOrderById(UUID id)
- getOrdersForCustomer(UUID customerId)

Include:
- Validation
- Meaningful exceptions
- Transaction boundaries where appropriate

C. Controller (REST API)
Context:
- REST API
- Spring Boot 3
- DTO-based
- Proper HTTP status codes

Task:
Create OrderController with endpoints:
- POST /orders
- GET /orders/{id}
- GET /customers/{customerId}/orders

Include:
- @RestController
- @RequestMapping
- @Valid
- ResponseEntity usage

D. Unit Tests
Context:
- JUnit 5
- Mockito
- Service layer testing

Task:
Write unit tests for OrderService.
Cover:
- Successful creation
- Invalid input
- Order not found scenario


E. Global Error Handling
Task:
Create a @ControllerAdvice that handles:
- EntityNotFoundException
- IllegalArgumentException
- Validation errors

Return consistent JSON error responses.

