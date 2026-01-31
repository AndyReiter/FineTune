# WorkOrder Merge Logic - Implementation Guide

## Overview

The system now supports intelligent merging of ski items into existing open work orders. When a customer submits new ski items, the system checks if they already have an open work order and either merges items into it or creates a new one.

---

## Key Changes

### 1. **SkiItem Entity** - New `status` Field
```java
private String status = "PENDING";  // PENDING, IN_PROGRESS, DONE
```

- **Purpose**: Track individual item progress
- **Default**: New items always start as "PENDING"
- **Options**: PENDING, IN_PROGRESS, DONE
- **Usage**: Overall work order status depends on all item statuses

### 2. **WorkOrder Entity** - New Methods

#### `isOpen()` Method
```java
public boolean isOpen() {
    return !("PICKED_UP".equals(this.status));
}
```
- Returns `true` if work order status is NOT "PICKED_UP"
- Used to determine if new items can be merged into this order
- Once picked up, a new order must be created for new items

#### `updateStatusBasedOnItems()` Method
```java
public void updateStatusBasedOnItems() {
    if (this.skiItems.isEmpty()) {
        this.status = "RECEIVED";
        return;
    }

    boolean allDone = this.skiItems.stream()
        .allMatch(item -> "DONE".equals(item.getStatus()));

    if (allDone) {
        this.status = "DONE";
    }
}
```
- Automatically calculates work order status
- **Work order is DONE only when ALL ski items are DONE**
- Otherwise status remains "RECEIVED"
- Call this after any ski item status change

### 3. **WorkOrderRepository** - New Query Methods

#### `findOpenWorkOrdersByCustomer(Customer)`
```java
@Query("SELECT w FROM WorkOrder w WHERE w.customer = :customer AND w.status != 'PICKED_UP' 
        ORDER BY w.createdAt DESC")
List<WorkOrder> findOpenWorkOrdersByCustomer(@Param("customer") Customer customer);
```
- Returns all open work orders for a customer
- Ordered by most recent first

#### `findMostRecentOpenWorkOrder(Customer)`
```java
@Query("SELECT w FROM WorkOrder w WHERE w.customer = :customer AND w.status != 'PICKED_UP' 
        ORDER BY w.createdAt DESC LIMIT 1")
Optional<WorkOrder> findMostRecentOpenWorkOrder(@Param("customer") Customer customer);
```
- Returns the most recent open work order
- Returns empty if none exist
- **Used by the merge logic to determine if items should be merged or new order created**

---

## WorkOrderService - Merge Logic

### `createOrMergeWorkOrder()` Method

This is the core method that implements the merge logic:

```java
@Transactional
public WorkOrder createOrMergeWorkOrder(CreateWorkOrderRequest request) {
    // Step 1: Find or create customer
    Customer customer = customerService.findOrCreateCustomer(...);

    // Step 2: Find the most recent open work order for this customer
    Optional<WorkOrder> existingOpenOrder = 
        workOrderRepository.findMostRecentOpenWorkOrder(customer);

    WorkOrder workOrder;
    boolean isNewWorkOrder = false;

    if (existingOpenOrder.isPresent()) {
        // MERGE: Add new items to existing open work order
        workOrder = existingOpenOrder.get();
        // NO NOTIFICATION sent for merged items
    } else {
        // CREATE: New work order
        workOrder = new WorkOrder();
        workOrder.setStatus("RECEIVED");
        workOrder.setCreatedAt(LocalDateTime.now());
        customer.addWorkOrder(workOrder);
        isNewWorkOrder = true;
        // NOTIFICATION would be sent here
    }

    // Step 3: Add all incoming ski items
    if (request.getSkis() != null && !request.getSkis().isEmpty()) {
        for (SkiItemRequest skiReq : request.getSkis()) {
            SkiItem skiItem = new SkiItem(
                skiReq.getSkiMake(),
                skiReq.getSkiModel(),
                skiReq.getServiceType()
            );
            skiItem.setStatus("PENDING");  // All new items start as PENDING
            workOrder.addSkiItem(skiItem);
        }
    }

    // Step 4: Update work order status based on all items
    workOrder.updateStatusBasedOnItems();

    // Step 5: Persist (cascade saves everything)
    customerRepository.save(customer);

    return workOrder;
}
```

### How the Merge Works

1. **Find or Create Customer**: Uses email + phone (existing behavior)
2. **Check for Open Orders**: Query for any work orders with status != "PICKED_UP"
3. **Decision**:
   - **If found**: Merge path - add new items to existing order
   - **If not found**: Create new order
4. **Add Items**: All new items start with status="PENDING"
5. **Update Status**: Recalculate work order status
   - DONE if ALL items are DONE
   - RECEIVED otherwise
6. **Persist**: Single save with cascades

---

## Database Changes

### New Column in `ski_items` Table
```sql
ALTER TABLE ski_items ADD COLUMN status VARCHAR(50) DEFAULT 'PENDING';
```

- **Tracks individual item progress**
- Default value is "PENDING"
- Can be: PENDING, IN_PROGRESS, DONE

### Updated Queries in `work_orders`
- New indices on (customer_id, status) for faster lookups
- Queries find orders where status != 'PICKED_UP'

---

## API Behavior

### POST /workorders

**Request**:
```json
{
  "customerFirstName": "John",
  "customerLastName": "Doe",
  "email": "john@example.com",
  "phone": "5551234567",
  "skis": [
    {
      "skiMake": "Atomic",
      "skiModel": "Vantage",
      "serviceType": "TUNING"
    }
  ]
}
```

**Scenario 1: New Customer**
- Customer doesn't exist
- Creates new customer
- Creates new work order (status="RECEIVED")
- Adds ski items with status="PENDING"
- **Notification sent** (new customer order)
- Returns HTTP 201 with work order details

**Scenario 2: Existing Customer with Open Order**
- Customer found
- Open work order found (status="RECEIVED")
- **Adds items to existing work order** (MERGE)
- Updates work order status (DONE if all items done, else RECEIVED)
- **NO notification sent** (customer already notified)
- Returns HTTP 201 with merged work order

**Scenario 3: Existing Customer, Order Picked Up**
- Customer found
- No open work orders (all status="PICKED_UP")
- Creates new work order
- Adds ski items
- **Notification sent** (new order for returning customer)
- Returns HTTP 201 with new work order

**Response** (all scenarios):
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
      "skiMake": "Atomic",
      "skiModel": "Vantage",
      "serviceType": "TUNING",
      "status": "PENDING"
    }
  ]
}
```

### GET /workorders/{id}

Returns the work order with all ski items and their individual statuses:
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
      "status": "IN_PROGRESS"
    },
    {
      "id": 2,
      "skiMake": "Atomic",
      "skiModel": "Vantage",
      "serviceType": "TUNING",
      "status": "PENDING"
    }
  ]
}
```

### POST /workorders/{id}/pickup

**Purpose**: Mark a work order as picked up (closed)

Once a work order is picked up:
- No new items can be merged into it
- Next items from the same customer create a NEW work order
- Status becomes "PICKED_UP"

**Request**: No body needed
**Response**: Updated work order with status="PICKED_UP"

---

## Bidirectional Relationships

### Customer → WorkOrder → SkiItem

The merge logic maintains proper bidirectional relationships:

```
Customer (1)
  ├─ workOrders (1:N)
  │   │
  │   └─ WorkOrder
  │       ├─ status (RECEIVED | PICKED_UP | DONE)
  │       └─ skiItems (1:N)
  │           │
  │           └─ SkiItem
  │               └─ status (PENDING | IN_PROGRESS | DONE)
```

**Cascade Behavior**:
- Saving Customer cascades to all WorkOrders
- Saving WorkOrder cascades to all SkiItems
- Deleting Customer cascades to all related WorkOrders and SkiItems
- When SkiItem is removed, it's automatically deleted (orphanRemoval=true)

**Example Flow**:
```
1. Save Customer (new or existing)
   ↓
2. Customer has 1+ WorkOrders (via addWorkOrder)
   ↓
3. WorkOrder has 1+ SkiItems (via addSkiItem)
   ↓
4. customerRepository.save(customer) triggers cascade
   → Saves Customer
   → Saves all linked WorkOrders
   → Saves all linked SkiItems
   → Single transaction!
```

---

## Transaction Management

### @Transactional Annotation

All service methods are `@Transactional`:
```java
@Transactional
public WorkOrder createOrMergeWorkOrder(CreateWorkOrderRequest request) {
    // All operations in single transaction
    // Automatic rollback if exception occurs
}
```

**Benefits**:
- All-or-nothing: If save fails, entire operation rolls back
- Prevents partial saves
- Ensures data consistency
- Hibernate can flush changes efficiently

---

## Notifications (Future Implementation)

### When Notifications Are Sent

**New Work Order**:
- Customer is new OR has no open orders
- Message: "Your work order has been received"
- Items list: All new items

**Merged Items**:
- Customer has open order
- Items are added to existing order
- **NO notification sent** (customer already notified for original order)
- Avoids notification spam

**Pickup Notification** (optional):
- When order is marked as picked up
- Message: "Your order is ready for pickup"

### Implementation Placeholder

In `WorkOrderService.createOrMergeWorkOrder()`:
```java
if (isNewWorkOrder) {
    // Send notification for new order
    notificationService.sendNewOrderNotification(customer, workOrder);
}
// Merged items don't trigger notification
```

---

## Status Transitions

### Ski Item Status Flow
```
┌─────────┐
│ PENDING │  (initial state for all new items)
└────┬────┘
     │ (work starts)
     ↓
┌──────────────┐
│ IN_PROGRESS  │  (item is being worked on)
└────┬─────────┘
     │ (work completes)
     ↓
┌──────┐
│ DONE │  (item is completed)
└──────┘
```

### Work Order Status Flow
```
┌─────────┐
│RECEIVED │  (initial state, has pending items)
└────┬────┘
     │ (all items completed)
     ↓
┌──────┐
│ DONE │  (all items done, ready for pickup)
└────┬─┘
     │ (customer picks up)
     ↓
┌──────────┐
│PICKED_UP │  (closed, no more items can be merged)
└──────────┘
```

---

## Example Scenarios

### Scenario 1: Two Submissions from Same Customer

**Submission 1**:
- Customer: john@example.com, 555-1234
- Items: Ski A (service WAXING)
- Result: Creates Customer, creates WorkOrder 1, adds Ski A
- Notification: Sent (new order)
- Status: RECEIVED (item is PENDING)

**Submission 2** (same day, same customer):
- Customer: john@example.com, 555-1234
- Items: Ski B (service TUNING)
- Result: Finds Customer, finds WorkOrder 1 (status=RECEIVED, is open)
- **MERGES**: Adds Ski B to WorkOrder 1
- Notification: **NOT sent** (merged items)
- Status: RECEIVED (has PENDING items)
- WorkOrder 1 now has: [Ski A (PENDING), Ski B (PENDING)]

**Submission 3** (after order picked up):
- Customer: john@example.com, 555-1234
- Items: Ski C (service TUNING)
- Result: Finds Customer, no open WorkOrders (WorkOrder 1 status=PICKED_UP)
- Creates new WorkOrder 2, adds Ski C
- Notification: Sent (new order for returning customer)
- Status: RECEIVED

---

## Controller Changes

### Old WorkOrderController
- Had merge logic mixed with controller
- Direct database operations
- Hard to test and maintain

### New WorkOrderController
- **Thin controller**, delegates to service
- Calls `workOrderService.createOrMergeWorkOrder(request)`
- Service handles all business logic
- Controller only handles HTTP concerns

```java
@PostMapping
public ResponseEntity<WorkOrderResponse> createWorkOrder(
        @Valid @RequestBody CreateWorkOrderRequest request) {
    
    // Service handles everything
    WorkOrder workOrder = workOrderService.createOrMergeWorkOrder(request);
    
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(WorkOrderResponse.fromEntity(workOrder));
}
```

**Benefits**:
- ✅ Cleaner code
- ✅ Easier to test (can mock service)
- ✅ Separation of concerns
- ✅ Reusable logic

---

## Testing Recommendations

### Unit Tests for WorkOrderService

```java
@Test
public void testCreateNewWorkOrder() {
    // New customer, should create work order
}

@Test
public void testMergeIntoOpenWorkOrder() {
    // Existing customer with open order, should merge
}

@Test
public void testCreateNewOrderWhenPickedUp() {
    // Existing customer but all orders picked up, should create new
}

@Test
public void testWorkOrderStatusCalculation() {
    // All items DONE → order is DONE
    // Any item PENDING → order is RECEIVED
}

@Test
public void testBidirectionalRelationships() {
    // Customer → WorkOrder → SkiItem all linked correctly
}
```

### Integration Tests

```java
@Test
public void testMergeWithCascade() {
    // Create customer
    // Create work order
    // Add ski items
    // Merge more items
    // Save and verify all persisted
}
```

---

## Future Enhancements

1. **SkiItemRepository**: Add queries for items by status
2. **Update SKiItem Status**: Implement `updateSkiItemStatus()` in service
3. **Notifications**: Send emails/SMS for new orders
4. **Reporting**: Query dashboard showing PENDING vs IN_PROGRESS vs DONE
5. **Batch Operations**: Update multiple items at once
6. **Historical Tracking**: Keep audit log of status changes
7. **Auto-Status**: Automatically mark as DONE based on time/business logic

---

## Summary

The merge logic provides:
- ✅ Intelligent combining of items into open orders
- ✅ Individual item status tracking
- ✅ Automatic work order status calculation
- ✅ No duplicate notifications
- ✅ Clean service layer
- ✅ Proper cascade persistence
- ✅ Transaction safety
