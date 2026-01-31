# WorkOrder Management Architecture

## Data Model (Entity Relationships)

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CUSTOMER ENTITY                              │
├──────────────────────────────────────────────────────────────────────┤
│ PK: id (Long)                                                        │
│ Fields:                                                              │
│   - firstName: String                                               │
│   - lastName: String                                                │
│   - email: String                                                   │
│   - phone: String                                                   │
│                                                                      │
│ Relationships:                                                       │
│   - workOrders: List<WorkOrder> (OneToMany, cascade=ALL)           │
│     └─ Initialized with: new ArrayList<>()                          │
│     └─ Safe add: customer.addWorkOrder(workOrder)                  │
└──────────────────────────────────────────────────────────────────────┘
                              ↓ 1:N
                         (cascade: ALL)
                              ↓
┌──────────────────────────────────────────────────────────────────────┐
│                       WORKORDER ENTITY                                │
├──────────────────────────────────────────────────────────────────────┤
│ PK: id (Long)                                                        │
│ FK: customer_id (references Customer.id)                            │
│ Fields:                                                              │
│   - status: String (e.g., "RECEIVED", "IN_PROGRESS")               │
│   - createdAt: LocalDateTime                                        │
│                                                                      │
│ Relationships:                                                       │
│   - customer: Customer (ManyToOne, @JsonIgnore)                    │
│   - skiItems: List<SkiItem> (OneToMany, cascade=ALL)               │
│     └─ Initialized with: new ArrayList<>()                          │
│     └─ Safe add: workOrder.addSkiItem(skiItem)                     │
└──────────────────────────────────────────────────────────────────────┘
                              ↓ 1:N
                         (cascade: ALL)
                              ↓
┌──────────────────────────────────────────────────────────────────────┐
│                        SKIITEM ENTITY                                 │
├──────────────────────────────────────────────────────────────────────┤
│ PK: id (Long)                                                        │
│ FK: work_order_id (references WorkOrder.id)                         │
│ Fields:                                                              │
│   - skiMake: String (e.g., "Rossignol")                            │
│   - skiModel: String (e.g., "Experience 80")                       │
│   - serviceType: String (e.g., "WAXING")                           │
│                                                                      │
│ Relationships:                                                       │
│   - workOrder: WorkOrder (ManyToOne, @JsonBackReference)           │
└──────────────────────────────────────────────────────────────────────┘
```

---

## API Request/Response Flow

### Scenario: Create a Work Order for a New Customer

```
CLIENT REQUEST
    ↓
┌─────────────────────────────────────────────────────────────┐
│ POST /workorders                                            │
│ {                                                           │
│   "customerFirstName": "John",                             │
│   "customerLastName": "Doe",                               │
│   "email": "john@example.com",                             │
│   "phone": "5551234567",                                   │
│   "skis": [                                                │
│     {                                                      │
│       "skiMake": "Rossignol",                             │
│       "skiModel": "Experience 80",                         │
│       "serviceType": "WAXING"                             │
│     }                                                      │
│   ]                                                        │
│ }                                                          │
└─────────────────────────────────────────────────────────────┘
    ↓
┌──────────────────────────────────────────────────────────────────┐
│ WorkOrderController.createWorkOrder()                            │
├──────────────────────────────────────────────────────────────────┤
│ 1. @Valid annotation validates CreateWorkOrderRequest           │
│    - email format check                                         │
│    - phone format check (10 digits)                            │
│    - at least 1 ski item required                              │
│                                                                 │
│ 2. Call CustomerService.findOrCreateCustomer()               │
│    ├─ Query: findByEmailOrPhone(email, phone)                │
│    ├─ Result: Customer found or created                     │
│    └─ Update firstName, lastName                            │
│    └─ Save to database                                      │
│                                                                 │
│ 3. Create WorkOrder entity                                    │
│    ├─ status = "RECEIVED"                                   │
│    ├─ createdAt = LocalDateTime.now()                      │
│    └─ customer = null (to be set)                          │
│                                                                 │
│ 4. Link Customer ↔ WorkOrder                                 │
│    └─ customer.addWorkOrder(workOrder)                     │
│       ├─ Null check                                         │
│       ├─ Duplicate check                                    │
│       └─ Bidirectional link                                 │
│                                                                 │
│ 5. For each SkiItem in request:                             │
│    ├─ Create SkiItem entity                                │
│    ├─ Set skiMake, skiModel, serviceType                  │
│    └─ workOrder.addSkiItem(skiItem)                       │
│       └─ Bidirectional link                                │
│                                                                 │
│ 6. Cascade Save                                             │
│    └─ customerRepository.save(customer)                    │
│       ├─ Save Customer                                     │
│       ├─ Cascade: Save all WorkOrders                      │
│       └─ Cascade: Save all SkiItems                        │
│                                                                 │
│ 7. Map to DTO and return                                    │
│    └─ WorkOrderResponse.fromEntity(workOrder)              │
└──────────────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────────────┐
│ HTTP 201 CREATED                                                │
│ {                                                               │
│   "id": 1,                                                      │
│   "status": "RECEIVED",                                         │
│   "createdAt": "2026-01-31T10:30:00",                          │
│   "customerId": 1,                                              │
│   "customerName": "John Doe",                                   │
│   "customerEmail": "john@example.com",                          │
│   "customerPhone": "5551234567",                                │
│   "skiItems": [                                                 │
│     {                                                           │
│       "id": 1,                                                  │
│       "skiMake": "Rossignol",                                   │
│       "skiModel": "Experience 80",                              │
│       "serviceType": "WAXING"                                   │
│     }                                                           │
│   ]                                                             │
│ }                                                               │
└─────────────────────────────────────────────────────────────────┘
    ↓
CLIENT RESPONSE
```

---

## Database Schema (SQL)

```sql
-- Customers Table
CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    UNIQUE KEY uk_email_phone (email, phone)
);

-- Work Orders Table
CREATE TABLE work_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    status VARCHAR(50),
    created_at TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Ski Items Table
CREATE TABLE ski_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_order_id BIGINT NOT NULL,
    ski_make VARCHAR(255),
    ski_model VARCHAR(255),
    service_type VARCHAR(255),
    FOREIGN KEY (work_order_id) REFERENCES work_orders(id) ON DELETE CASCADE
);
```

---

## Layer Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                       REST API LAYER                            │
├────────────────────────────────────────────────────────────────┤
│ • WorkOrderController (REST endpoints)                         │
│ • CustomerController (REST endpoints)                          │
│                                                                 │
│ Responsibilities:                                              │
│   - Receive HTTP requests                                      │
│   - Validate input                                             │
│   - Call services                                              │
│   - Map entities to DTOs                                       │
│   - Return HTTP responses                                      │
└────────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                              │
├────────────────────────────────────────────────────────────────┤
│ • CustomerService (business logic)                             │
│ • WorkOrderService (if needed)                                 │
│                                                                 │
│ Responsibilities:                                              │
│   - Business logic                                             │
│   - findOrCreateCustomer logic                                 │
│   - Complex workflows                                          │
│   - Return entities to controllers                             │
└────────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────────┐
│                   REPOSITORY LAYER (DATA ACCESS)               │
├────────────────────────────────────────────────────────────────┤
│ • CustomerRepository (Spring Data JPA)                         │
│ • WorkOrderRepository (Spring Data JPA)                        │
│ • SkiItemRepository (if needed)                                │
│                                                                 │
│ Responsibilities:                                              │
│   - Database queries                                           │
│   - Cascade operations                                         │
│   - Return entities                                            │
└────────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────────┐
│                      ENTITY LAYER                               │
├────────────────────────────────────────────────────────────────┤
│ • Customer (JPA Entity)                                        │
│ • WorkOrder (JPA Entity)                                       │
│ • SkiItem (JPA Entity)                                         │
│                                                                 │
│ Responsibilities:                                              │
│   - Define database structure                                  │
│   - Relationship mapping                                       │
│   - Cascade configuration                                      │
└────────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                               │
├────────────────────────────────────────────────────────────────┤
│ • MySQL/PostgreSQL/H2                                          │
│   - Customers table                                            │
│   - Work Orders table                                          │
│   - Ski Items table                                            │
└────────────────────────────────────────────────────────────────┘
```

---

## DTO Mapping Pattern

```
┌──────────────────┐
│  Entities Layer  │
├──────────────────┤
│ WorkOrder        │
│ ├─ id: 1         │
│ ├─ customer:     │
│ │  ├─ id: 1      │
│ │  ├─ firstName  │
│ │  ├─ lastName   │
│ │  ├─ email      │
│ │  ├─ phone      │
│ │  └─ workOrders │
│ │     └─ [...]   │
│ ├─ status        │
│ ├─ createdAt     │
│ └─ skiItems      │
│    ├─ id: 1      │
│    ├─ skiMake    │
│    ├─ skiModel   │
│    ├─ serviceType│
│    └─ workOrder  │
│       └─ [...]   │
└──────────────────┘
         ↓
   [Mapper/DTO]
         ↓
┌──────────────────────────┐
│  DTOs Layer (JSON Safe)  │
├──────────────────────────┤
│ WorkOrderResponse        │
│ ├─ id: 1                 │
│ ├─ customerId: 1         │
│ ├─ customerName: "John"  │
│ ├─ customerEmail         │
│ ├─ customerPhone         │
│ ├─ status                │
│ ├─ createdAt             │
│ └─ skiItems              │
│    └─ SkiItemResponse[]  │
│       ├─ id: 1           │
│       ├─ skiMake         │
│       ├─ skiModel        │
│       └─ serviceType     │
│                          │
│ NO CIRCULAR REFERENCES! ✅
└──────────────────────────┘
```

---

## Cascade Operations Example

```
DELETE Customer (id=1)
    ↓
┌─────────────────────────────────────────┐
│ CASCADE DELETE: CascadeType.ALL          │
│ orphanRemoval = true                    │
└─────────────────────────────────────────┘
    ↓
DELETE all WorkOrders where customer_id=1
    ├─ WorkOrder 1 (DELETED)
    │  └─ CASCADE: Delete all SkiItems where work_order_id=1
    │     ├─ SkiItem 1 (DELETED)
    │     ├─ SkiItem 2 (DELETED)
    │     └─ SkiItem 3 (DELETED)
    │
    ├─ WorkOrder 2 (DELETED)
    │  └─ CASCADE: Delete all SkiItems where work_order_id=2
    │     ├─ SkiItem 4 (DELETED)
    │     └─ SkiItem 5 (DELETED)
    │
    └─ WorkOrder 3 (DELETED)
       └─ CASCADE: Delete all SkiItems where work_order_id=3
          └─ SkiItem 6 (DELETED)

Result: Customer + 3 WorkOrders + 6 SkiItems (9 total) DELETED
All in single transaction ✅
```

---

## Error Handling Flow

```
POST /workorders (invalid request)
    ↓
┌────────────────────────────────────┐
│ Validation Layer (@Valid)          │
├────────────────────────────────────┤
│ ✗ Invalid email format?            │
│   → HTTP 400 Bad Request           │
│                                    │
│ ✗ Phone not 10 digits?            │
│   → HTTP 400 Bad Request           │
│                                    │
│ ✗ No ski items provided?           │
│   → HTTP 400 Bad Request           │
└────────────────────────────────────┘
    ↓
┌────────────────────────────────────┐
│ Business Logic Layer               │
├────────────────────────────────────┤
│ ✗ WorkOrder is null?               │
│   → IllegalArgumentException       │
│   → HTTP 500 Internal Error        │
│                                    │
│ ✗ Customer not found in DB?        │
│   → Create new customer            │
│   → Continue normally              │
└────────────────────────────────────┘
    ↓
Success → HTTP 201 CREATED + WorkOrderResponse
```

---

## Concurrency & Safety

```
Two simultaneous requests for same email/phone
    ↓
Request 1                          Request 2
├─ Query: findByEmailOrPhone      ├─ Query: findByEmailOrPhone
│  (result: not found)             │  (result: not found)
│                                  │
├─ Create Customer                 ├─ Create Customer
│  (firstName="John")              │  (firstName="Jon")
│                                  │
├─ Save (DB unique constraint)     ├─ Save (DB unique constraint)
│  ✓ Success                       │  ✗ UNIQUE violation
│                                  │
└─ Return WorkOrder               └─ Error or retry
```

**Solutions:**
1. Add database UNIQUE constraint (already implemented)
2. Use pessimistic locking if needed
3. Handle SQLIntegrityConstraintViolationException
4. Retry logic in service layer

---

## Performance Considerations

### LazyLoading vs. EAGER Loading

```
Current Configuration:
WorkOrder.skiItems = FetchType.EAGER

When you load a WorkOrder:
├─ Load WorkOrder entity
├─ Immediately load all SkiItems (no N+1 problem)
└─ Ready for REST response

Trade-off:
✅ Good: No lazy loading issues
✅ Good: REST APIs usually need complete data
❌ Bad: Loads ski items even if not needed
```

### Query Optimization
```
Current: GET /customers/1/workorders
├─ Load Customer
├─ Load all WorkOrders for Customer
├─ Load all SkiItems for each WorkOrder
└─ Acceptable for small to medium data

If you have 1000+ customers with 100+ workorders each:
├─ Consider: Pagination
├─ Consider: Lazy loading for workOrders
├─ Consider: Custom queries
└─ Create: DTO projections
```

---

## Summary

✅ **Entities**: Properly configured with cascades and bidirectional relationships
✅ **Services**: Business logic for customer management
✅ **Controllers**: REST endpoints returning clean DTOs
✅ **DTOs**: Prevent JSON recursion and control API contracts
✅ **Database**: Proper foreign keys and constraints
✅ **Validation**: Input validation at request level
✅ **Error Handling**: Safe cascade operations
✅ **Security**: No infinite recursion in JSON responses
