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

Summary
I've completed a comprehensive review and enhancement of your Spring Boot WorkOrder management system. Here's what was delivered:

✨ Files Created (9 total)
Documentation (6 files):

00_START_HERE.md - Quick overview and status
README_WORKORDER_SYSTEM.md - Complete system guide (500+ lines)
QUICK_START_GUIDE.md - API examples and testing (300+ lines)
WORKORDER_WORKFLOW_VERIFICATION.md - Detailed verification (500+ lines)
DTO_REFERENCE_GUIDE.md - DTO usage guide (350+ lines)
ARCHITECTURE_GUIDE.md - System architecture with diagrams (400+ lines)
Data Transfer Objects (3 files):

WorkOrderResponse.java - DTO for work order responses
SkiItemResponse.java - DTO for ski item responses
CustomerResponse.java - DTO for customer responses (with WorkOrderSummary inner class)
🔧 Files Enhanced (4 total)
Customer.java - Added ArrayList initialization and null checks in addWorkOrder()
WorkOrder.java - Added getSkiItems() and setSkiItems() getters/setters
WorkOrderController.java - Now returns DTOs with HTTP 201 CREATED and comprehensive Javadoc
CustomerController.java - Now returns DTOs with improved endpoints and Javadoc
✅ Key Improvements
Issue	Fix
JSON circular references	✅ DTOs prevent infinite recursion
NullPointerException risk	✅ ArrayList initialization + null checks
Missing getters	✅ Added getSkiItems/setSkiItems
Wrong HTTP status	✅ POST now returns 201 CREATED
Raw entity responses	✅ All endpoints return DTOs
No API examples	✅ Curl examples provided
🎯 Verified Workflows
✅ Creating work orders for new customers
✅ Finding existing customers by email OR phone
✅ Linking customers to work orders correctly
✅ Cascading saves (Customer → WorkOrders → SkiItems)
✅ Cascading deletes with orphan removal
✅ Clean JSON responses without circular references
📚 Start with these files:
00_START_HERE.md - Quick overview
QUICK_START_GUIDE.md - API usage examples
README_WORKORDER_SYSTEM.md - Complete guide