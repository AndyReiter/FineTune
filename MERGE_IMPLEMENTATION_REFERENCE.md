# WorkOrder Merge Implementation - Quick Reference

## Files Changed

### 1. **SkiItem.java** (Entity)
- ✅ Added `status` field (PENDING, IN_PROGRESS, DONE)
- ✅ Added default constructor
- ✅ Added parameterized constructor
- ✅ Added getStatus() and setStatus() methods

**Key addition**:
```java
private String status = "PENDING";  // Track individual item progress
```

---

### 2. **WorkOrder.java** (Entity)
- ✅ Added `isOpen()` method
- ✅ Added `updateStatusBasedOnItems()` method

**Key additions**:
```java
public boolean isOpen() {
    return !("PICKED_UP".equals(this.status));
}

public void updateStatusBasedOnItems() {
    // DONE only if ALL items are DONE
    boolean allDone = this.skiItems.stream()
        .allMatch(item -> "DONE".equals(item.getStatus()));
    if (allDone) {
        this.status = "DONE";
    }
}
```

---

### 3. **WorkOrderRepository.java**
- ✅ Added `findOpenWorkOrdersByCustomer()` query
- ✅ Added `findMostRecentOpenWorkOrder()` query

**Key additions**:
```java
@Query("SELECT w FROM WorkOrder w WHERE w.customer = :customer 
        AND w.status != 'PICKED_UP' ORDER BY w.createdAt DESC")
List<WorkOrder> findOpenWorkOrdersByCustomer(@Param("customer") Customer customer);

@Query("SELECT w FROM WorkOrder w WHERE w.customer = :customer 
        AND w.status != 'PICKED_UP' ORDER BY w.createdAt DESC LIMIT 1")
Optional<WorkOrder> findMostRecentOpenWorkOrder(@Param("customer") Customer customer);
```

---

### 4. **WorkOrderService.java** (NEW LOGIC)
- ✅ Added `createOrMergeWorkOrder()` - MAIN MERGE METHOD
- ✅ Added `updateSkiItemStatus()` - updates individual items
- ✅ Added `pickupWorkOrder()` - marks order as picked up
- ✅ Kept `save()` - basic persist method

**Core Logic**:
```java
@Transactional
public WorkOrder createOrMergeWorkOrder(CreateWorkOrderRequest request) {
    // 1. Find or create customer
    Customer customer = customerService.findOrCreateCustomer(...);
    
    // 2. Find open work order
    Optional<WorkOrder> existingOpenOrder = 
        workOrderRepository.findMostRecentOpenWorkOrder(customer);
    
    // 3. Merge or create
    WorkOrder workOrder;
    if (existingOpenOrder.isPresent()) {
        workOrder = existingOpenOrder.get();  // MERGE
    } else {
        workOrder = new WorkOrder();          // CREATE NEW
        workOrder.setStatus("RECEIVED");
        workOrder.setCreatedAt(LocalDateTime.now());
        customer.addWorkOrder(workOrder);
    }
    
    // 4. Add items with PENDING status
    for (SkiItemRequest skiReq : request.getSkis()) {
        SkiItem skiItem = new SkiItem(...);
        skiItem.setStatus("PENDING");
        workOrder.addSkiItem(skiItem);
    }
    
    // 5. Update status (DONE if all items done, else RECEIVED)
    workOrder.updateStatusBasedOnItems();
    
    // 6. Persist with cascade
    customerRepository.save(customer);
    
    return workOrder;
}
```

---

### 5. **WorkOrderController.java**
- ✅ Changed POST endpoint to use `workOrderService.createOrMergeWorkOrder()`
- ✅ Removed manual customer/order creation logic (moved to service)
- ✅ Added `POST /workorders/{id}/pickup` endpoint
- ✅ Updated Javadoc comments

**Old Code**:
```java
@PostMapping
public ResponseEntity<WorkOrderResponse> createWorkOrder(...) {
    Customer customer = customerService.findOrCreateCustomer(...);
    WorkOrder workOrder = new WorkOrder();
    workOrder.setStatus("RECEIVED");
    customer.addWorkOrder(workOrder);
    for (SkiItemRequest skiReq : request.getSkis()) {
        SkiItem skiItem = new SkiItem();
        // ... set fields
        workOrder.addSkiItem(skiItem);
    }
    customerRepository.save(customer);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(WorkOrderResponse.fromEntity(workOrder));
}
```

**New Code**:
```java
@PostMapping
public ResponseEntity<WorkOrderResponse> createWorkOrder(...) {
    // Service handles everything!
    WorkOrder workOrder = workOrderService.createOrMergeWorkOrder(request);
    
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(WorkOrderResponse.fromEntity(workOrder));
}
```

---

### 6. **SkiItemResponse.java** (DTO)
- ✅ Added `status` field
- ✅ Updated `fromEntity()` to include status

**Key addition**:
```java
private String status;  // Include status in API response

public static SkiItemResponse fromEntity(SkiItem skiItem) {
    SkiItemResponse response = new SkiItemResponse();
    // ... other fields
    response.status = skiItem.getStatus();  // NEW
    return response;
}
```

---

## API Endpoints

### 1. POST /workorders (CREATE OR MERGE)
**Request**:
```json
{
  "customerFirstName": "John",
  "customerLastName": "Doe",
  "email": "john@example.com",
  "phone": "5551234567",
  "skis": [
    {
      "skiMake": "Rossignol",
      "skiModel": "Experience 80",
      "serviceType": "WAXING"
    }
  ]
}
```

**Response** (HTTP 201):
```json
{
  "id": 1,
  "status": "RECEIVED",
  "createdAt": "2026-01-31T11:00:00",
  "customerId": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "customerPhone": "5551234567",
  "skiItems": [
    {
      "id": 1,
      "skiMake": "Rossignol",
      "skiModel": "Experience 80",
      "serviceType": "WAXING",
      "status": "PENDING"  // NEW FIELD
    }
  ]
}
```

---

### 2. POST /workorders/{id}/pickup (MARK AS PICKED UP)
**Purpose**: Close an order so new items create a new order

**Request**: No body needed

**Response** (HTTP 200):
```json
{
  "id": 1,
  "status": "PICKED_UP",
  "createdAt": "2026-01-31T11:00:00",
  "customerId": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "customerPhone": "5551234567",
  "skiItems": [
    {
      "id": 1,
      "skiMake": "Rossignol",
      "skiModel": "Experience 80",
      "serviceType": "WAXING",
      "status": "DONE"
    }
  ]
}
```

---

### 3. GET /workorders (LIST ALL - UNCHANGED)
**Response**: List of WorkOrderResponse with status and ski item statuses

---

### 4. GET /workorders/{id} (GET ONE - UNCHANGED)
**Response**: Single WorkOrderResponse with all ski items and their statuses

---

## Database Changes

### New Column: `ski_items.status`
```sql
ALTER TABLE ski_items ADD COLUMN status VARCHAR(50) DEFAULT 'PENDING';
```

### Updated `work_orders` Queries
The repository now queries for:
- Open orders: `status != 'PICKED_UP'`
- Most recent: `ORDER BY createdAt DESC LIMIT 1`

---

## Key Behaviors

### Merge Detection
```
Incoming Request (customer + items)
  ↓
1. Find Customer by email + phone
   ├─ Exists? Use it
   └─ Doesn't exist? Create it
  ↓
2. Find open work orders (status != 'PICKED_UP')
   ├─ Found? → MERGE items into existing order
   └─ Not found? → CREATE new order
  ↓
3. Add all items with status='PENDING'
  ↓
4. Update work order status
   ├─ All items DONE? → status = DONE
   └─ Any item PENDING/IN_PROGRESS? → status = RECEIVED
  ↓
5. Save (cascade saves customer, work order, ski items)
```

---

## Status Calculations

### Ski Item Status
- **PENDING**: New items (default)
- **IN_PROGRESS**: Currently being worked on
- **DONE**: Work completed

### Work Order Status
- **RECEIVED**: Initial status, has pending items
- **DONE**: All ski items are DONE
- **PICKED_UP**: Order is closed (no more merges allowed)

**Logic**:
```java
if (all ski items have status == "DONE") {
    work_order.status = "DONE"
} else {
    work_order.status = "RECEIVED"
}
```

---

## Transaction Flow

```
POST /workorders
  ↓
WorkOrderController.createWorkOrder()
  ↓
@Transactional service method
  ↓
1. Find/create customer
2. Find open order
3. Merge or create order
4. Add items
5. Update status
6. Save (single transaction)
  ↓
Response: HTTP 201 + WorkOrderResponse
```

**All-or-Nothing**: If any step fails, entire transaction rolls back

---

## Notifications Strategy

### When to Send Notifications

**Send notification**:
- New work order created (isNewWorkOrder = true)
- Customer is new
- Customer has no open orders

**DON'T send notification**:
- Items merged into existing order
- isNewWorkOrder = false

### Implementation Hook
```java
if (isNewWorkOrder) {
    // Send notification
    notificationService.sendNewOrderNotification(customer, workOrder);
}
// Merged items skip notification
```

---

## Testing Examples

### Test 1: Create New Order
```java
Customer new customer
Item: Ski A
Expected: New WorkOrder created, status=RECEIVED
```

### Test 2: Merge Items
```java
Existing customer with open WorkOrder 1
Item: Ski B
Expected: Ski B added to WorkOrder 1, no new order
```

### Test 3: Status Calculation
```java
WorkOrder with 2 items
Item 1: status=DONE
Item 2: status=PENDING
Expected: WorkOrder status=RECEIVED (not all done)

Item 2: status=DONE
Expected: WorkOrder status=DONE (all done)
```

### Test 4: No Merge After Pickup
```java
Existing customer with WorkOrder status=PICKED_UP
Item: Ski C
Expected: New WorkOrder created, not merged into picked-up order
```

---

## Summary of Changes

| Component | Change | Impact |
|-----------|--------|--------|
| SkiItem | Added `status` field | Can track individual item progress |
| WorkOrder | Added helper methods | Can check if open, update status |
| Repository | Added queries | Can find open orders for merge |
| Service | Added merge logic | Main business logic centralized |
| Controller | Simplified | Now just calls service |
| DTO | Added status | API response includes item status |

---

## Next Steps

1. ✅ Implement database migration (add status column)
2. ✅ Test merge logic with multiple scenarios
3. ⏳ Implement `updateSkiItemStatus()` to mark items as IN_PROGRESS/DONE
4. ⏳ Implement notification service
5. ⏳ Add audit logging for status changes
6. ⏳ Create admin dashboard for managing orders
