# DTO Reference Guide

## Overview
DTOs (Data Transfer Objects) are used to control JSON serialization and prevent circular references in REST API responses.

---

## WorkOrderResponse DTO

### Purpose
Serializes WorkOrder entities for API responses with customer summary and ski items.

### Fields
```
id              (Long)                   - WorkOrder ID
status          (String)                 - e.g., "RECEIVED", "IN_PROGRESS"
createdAt       (LocalDateTime)          - When work order was created
customerId      (Long)                   - Customer ID
customerName    (String)                 - Full customer name (firstName + lastName)
customerEmail   (String)                 - Customer email
customerPhone   (String)                 - Customer phone
skiItems        (List<SkiItemResponse>)  - Associated ski items
```

### Usage
```java
// Convert entity to DTO
WorkOrderResponse response = WorkOrderResponse.fromEntity(workOrder);

// Use in controller
@GetMapping("/{id}")
public ResponseEntity<WorkOrderResponse> getWorkOrderById(@PathVariable Long id) {
    return workOrderRepository.findById(id)
        .map(workOrder -> ResponseEntity.ok(WorkOrderResponse.fromEntity(workOrder)))
        .orElse(ResponseEntity.notFound().build());
}
```

### JSON Example
```json
{
  "id": 1,
  "status": "RECEIVED",
  "createdAt": "2026-01-31T10:30:00",
  "customerId": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "customerPhone": "5551234567",
  "skiItems": [
    {
      "id": 1,
      "skiMake": "Rossignol",
      "skiModel": "Experience 80",
      "serviceType": "WAXING"
    },
    {
      "id": 2,
      "skiMake": "Atomic",
      "skiModel": "Vantage",
      "serviceType": "TUNING"
    }
  ]
}
```

---

## SkiItemResponse DTO

### Purpose
Serializes SkiItem entities without the WorkOrder reference (prevents circular references).

### Fields
```
id              (Long)      - SkiItem ID
skiMake         (String)    - e.g., "Rossignol", "Atomic", "Salomon"
skiModel        (String)    - e.g., "Experience 80", "Vantage"
serviceType     (String)    - e.g., "WAXING", "TUNING", "REPAIR"
```

### Usage
```java
// Convert entity to DTO
SkiItemResponse response = SkiItemResponse.fromEntity(skiItem);

// Automatically used by WorkOrderResponse
skiItems = workOrder.getSkiItems().stream()
    .map(SkiItemResponse::fromEntity)
    .collect(Collectors.toList());
```

### JSON Example
```json
{
  "id": 1,
  "skiMake": "Rossignol",
  "skiModel": "Experience 80",
  "serviceType": "WAXING"
}
```

---

## CustomerResponse DTO

### Purpose
Serializes Customer entities with work order summaries (prevents deep nesting of ski items).

### Main Fields
```
id              (Long)                           - Customer ID
firstName       (String)                         - Customer first name
lastName        (String)                         - Customer last name
email           (String)                         - Customer email
phone           (String)                         - Customer phone
workOrders      (List<WorkOrderSummary>)        - Lightweight work order summaries
```

### WorkOrderSummary Inner Class Fields
```
id              (Long)      - WorkOrder ID
status          (String)    - WorkOrder status
createdAt       (String)    - WorkOrder creation date (as string)
skiItemCount    (Integer)   - Number of ski items in this work order
```

### Usage
```java
// Convert entity to DTO
CustomerResponse response = CustomerResponse.fromEntity(customer);

// Use in controller
@GetMapping
public List<CustomerResponse> getAllCustomers() {
    return customerRepository.findAll().stream()
        .map(CustomerResponse::fromEntity)
        .collect(Collectors.toList());
}
```

### JSON Example
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": "5551234567",
  "workOrders": [
    {
      "id": 1,
      "status": "RECEIVED",
      "createdAt": "2026-01-31T10:30:00",
      "skiItemCount": 2
    },
    {
      "id": 2,
      "status": "IN_PROGRESS",
      "createdAt": "2026-01-30T14:15:00",
      "skiItemCount": 1
    }
  ]
}
```

---

## Why DTOs? (The Problem They Solve)

### Without DTOs (Raw Entities)
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": "5551234567",
  "workOrders": [
    {
      "id": 1,
      "status": "RECEIVED",
      "createdAt": "2026-01-31T10:30:00",
      "customer": {
        "id": 1,
        "firstName": "John",
        "lastName": "Doe",
        "email": "john@example.com",
        "phone": "5551234567",
        "workOrders": [
          {
            "id": 1,
            "status": "RECEIVED",
            "createdAt": "2026-01-31T10:30:00",
            "customer": {
              "id": 1,
              ... INFINITE RECURSION ...
            }
          }
        ]
      },
      "skiItems": [
        {
          "id": 1,
          "skiMake": "Rossignol",
          "skiModel": "Experience 80",
          "serviceType": "WAXING",
          "workOrder": {
            "id": 1,
            "status": "RECEIVED",
            ... INFINITE RECURSION ...
          }
        }
      ]
    }
  ]
}
```

### With DTOs (Clean Response)
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": "5551234567",
  "workOrders": [
    {
      "id": 1,
      "status": "RECEIVED",
      "createdAt": "2026-01-31T10:30:00",
      "skiItemCount": 1
    }
  ]
}
```

**Benefits:**
- ✅ No circular references
- ✅ Smaller JSON payloads
- ✅ Control over what data is exposed
- ✅ Decouples API contract from database schema
- ✅ Easier to maintain and version APIs

---

## Mapping Pattern

### Single Entity to DTO
```java
WorkOrder workOrder = workOrderRepository.findById(1).orElse(null);
WorkOrderResponse response = WorkOrderResponse.fromEntity(workOrder);
```

### List of Entities to DTOs
```java
List<WorkOrder> workOrders = workOrderRepository.findAll();
List<WorkOrderResponse> responses = workOrders.stream()
    .map(WorkOrderResponse::fromEntity)
    .collect(Collectors.toList());
```

### In Controller Methods
```java
@GetMapping
public List<WorkOrderResponse> getAllWorkOrders() {
    return workOrderRepository.findAll().stream()
        .map(WorkOrderResponse::fromEntity)
        .collect(Collectors.toList());
}

@GetMapping("/{id}")
public ResponseEntity<WorkOrderResponse> getWorkOrderById(@PathVariable Long id) {
    return workOrderRepository.findById(id)
        .map(workOrder -> ResponseEntity.ok(WorkOrderResponse.fromEntity(workOrder)))
        .orElse(ResponseEntity.notFound().build());
}
```

---

## API Endpoints Using DTOs

| Endpoint | Returns | DTO |
|----------|---------|-----|
| GET /workorders | List | `List<WorkOrderResponse>` |
| GET /workorders/{id} | Single | `WorkOrderResponse` |
| POST /workorders | Single | `WorkOrderResponse` (HTTP 201) |
| DELETE /workorders/{id} | None | HTTP 200/404 |
| GET /customers | List | `List<CustomerResponse>` |
| GET /customers/{id} | Single | `CustomerResponse` |
| GET /customers/{id}/workorders | List | `List<WorkOrderResponse>` |
| GET /customers/search?email=... | Single | `CustomerResponse` |

---

## Best Practices

1. **Always use factory methods**: `WorkOrderResponse.fromEntity(workOrder)`
2. **Map in controller, not service**: Services return entities, controllers return DTOs
3. **Use streams for collections**: `.map(DTO::fromEntity).collect(Collectors.toList())`
4. **Keep DTOs separate**: Don't mix entity and DTO logic
5. **Update DTOs, not entities**: If API contract changes, only update DTOs
6. **Use inheritance for common fields**: Consider creating a base DTO class
7. **Add Javadoc**: Document what each DTO field represents

---

## Future Enhancements

### If you need more detailed responses:
- Create separate DTO for GET /customers/{id}/workorders (with full ski items)
- Extend WorkOrderResponse with additional fields as needed

### If responses become too large:
- Use pagination for list endpoints
- Add query parameters to control level of detail
- Create multiple DTO versions (summary vs. detailed)

### Versioning:
- Name DTOs by version if API evolves: `WorkOrderResponseV1`, `WorkOrderResponseV2`
- Maintain backward compatibility
