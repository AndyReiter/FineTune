# Pickup Flow - Complete Guide

## Overview

When a work order is marked as "Ready for Pickup":
1. ✅ Work order status changes to PICKED_UP
2. ✅ ALL ski items in that order are marked as PICKED_UP
3. ✅ New orders for the same customer will be created as NEW orders (not merged)
4. ✅ Previously picked-up orders are no longer mergeable

---

## Status Flow Diagram

```
CREATE ORDER
    ↓
Order Status: RECEIVED
All Skis: PENDING (default)
    ↓
WORK ON SKIS
    ↓
Update Skis to: IN_PROGRESS, DONE
Order Status: Auto-calculated (RECEIVED or DONE)
    ↓
ALL SKIS DONE?
    ├─ NO → Progress: "X/Y items done" → Continue updating
    └─ YES → "Ready for Pickup" button appears
    ↓
CLICK "READY FOR PICKUP"
    ↓
Order Status: PICKED_UP
All Skis: PICKED_UP (automatically set)
    ↓
CUSTOMER BRINGS NEW ITEMS
    ↓
System checks: Does customer have OPEN orders?
    ├─ YES (status != PICKED_UP) → MERGE new items
    └─ NO (all PICKED_UP) → CREATE NEW ORDER
```

---

## Ski Item Status Values

### Valid Values
- **PENDING** - New item, work not started (yellow badge)
- **IN_PROGRESS** - Currently being worked on (blue badge)
- **DONE** - Work completed (green badge)
- **PICKED_UP** - Order has been picked up, item is done (cyan badge)

### Status Transitions
```
PENDING → IN_PROGRESS → DONE → [User clicks "Ready for Pickup"] → PICKED_UP
```

### Read-Only Status
- **PICKED_UP**: Once set, dropdown is disabled (can't change)
- Can only be set by clicking "Ready for Pickup" button

---

## Work Order Status Values

### Valid Values
- **RECEIVED** - Has pending/in-progress items (purple badge)
- **DONE** - All items are DONE (green badge - auto-calculated)
- **PICKED_UP** - Order closed and picked up (cyan badge)

### Status Transitions
```
RECEIVED → [as items completed] → DONE → [click button] → PICKED_UP
```

### Auto-Calculation
```
Order Status = (based on ski items)
  
If ANY ski item is PENDING or IN_PROGRESS:
    Status = RECEIVED

If ALL ski items are DONE:
    Status = DONE

If user clicks "Ready for Pickup":
    Status = PICKED_UP
    All ski items = PICKED_UP
```

---

## User Interface Changes

### Modal Ski Item Display (When Not Picked Up)
```
✓ Item: Rossignol Experience 80 (WAXING)
✓ Current status: [DONE badge]
✓ Dropdown: [PENDING ▼] ← Enabled, can change
✓ Show all 3 options: PENDING, IN_PROGRESS, DONE, PICKED_UP
```

### Modal Ski Item Display (When Picked Up)
```
✓ Item: Rossignol Experience 80 (WAXING)
✓ Current status: [PICKED_UP badge]
✓ Dropdown: [PICKED_UP ▼] ← DISABLED (read-only)
✓ Can't change status
```

### Modal Header (When Picked Up)
```
Customer: John Doe
Work Order Status: [PICKED_UP badge]
✓ Order has been picked up  ← Green checkmark message
```

### "Ready for Pickup" Button
```
When NOT all items DONE:
    Button: HIDDEN

When all items DONE and NOT picked up:
    Button: VISIBLE and ENABLED ← Green button

When order already PICKED_UP:
    Button: HIDDEN
```

---

## API Behavior

### POST /workorders/{id}/pickup

**Before**:
- Work order status: DONE
- Ski items: [DONE, DONE, DONE]

**After**:
- Work order status: PICKED_UP
- Ski items: [PICKED_UP, PICKED_UP, PICKED_UP]

**Backend Logic**:
```java
public WorkOrder pickupWorkOrder(Long workOrderId) {
    WorkOrder workOrder = findById(workOrderId);
    workOrder.setStatus("PICKED_UP");
    
    // Mark ALL ski items as PICKED_UP
    for (SkiItem ski : workOrder.getSkiItems()) {
        ski.setStatus("PICKED_UP");
    }
    
    save(workOrder);
    return workOrder;
}
```

---

## Merge Prevention Logic

### Before Pickup
```
Customer: john@example.com
Order 1 Status: RECEIVED (open for merging)

New submission from john@example.com:
  System checks: findMostRecentOpenWorkOrder(customer)
  Found: Order 1 (status != PICKED_UP)
  Action: MERGE items into Order 1
  Result: Items added to Order 1
```

### After Pickup
```
Customer: john@example.com
Order 1 Status: PICKED_UP (closed for merging)
Order 1 Skis: [PICKED_UP, PICKED_UP, PICKED_UP]

New submission from john@example.com:
  System checks: findMostRecentOpenWorkOrder(customer)
  Found: Nothing (Order 1 is PICKED_UP, not open)
  Action: CREATE NEW ORDER
  Result: Order 2 created for john@example.com
```

---

## Test Scenario: Complete Pickup Flow

### Step 1: Create Order
```
Customer: Jane Smith (jane@example.com)
Items:
  - Volkl Mantra (REPAIR)
  - Head Absolut (MOUNT)
```

### Step 2: Work on Items
```
Update Volkl → IN_PROGRESS
Update Head → IN_PROGRESS
Progress: 0/2 items done
Order Status: RECEIVED
"Ready for Pickup": HIDDEN
```

### Step 3: Complete Work
```
Update Volkl → DONE
Update Head → DONE
Progress: 2/2 items done
Order Status: DONE (auto-calculated)
"Ready for Pickup": VISIBLE ✓
```

### Step 4: Click "Ready for Pickup"
```
Backend updates:
  - Order 1 status: PICKED_UP
  - Volkl status: PICKED_UP
  - Head status: PICKED_UP

Modal shows:
  ✓ Order has been picked up
  Both dropdowns: DISABLED
  "Ready for Pickup": HIDDEN
```

### Step 5: Same Customer, New Items
```
Jane calls with 2 new skis

System checks:
  - Customer: jane@example.com found
  - Open orders? NO (Order 1 is PICKED_UP)
  - Action: CREATE NEW

Result:
  Order 2 created
  Status: RECEIVED
  Skis: [PENDING, PENDING]
  Progress: 0/2 items done
```

---

## Backend Changes

### WorkOrderService.pickupWorkOrder()
**Before**:
```java
workOrder.setStatus("PICKED_UP");
return workOrderRepository.save(workOrder);
```

**After**:
```java
workOrder.setStatus("PICKED_UP");

// Mark all ski items as PICKED_UP
for (SkiItem skiItem : workOrder.getSkiItems()) {
    skiItem.setStatus("PICKED_UP");
}

return workOrderRepository.save(workOrder);
```

### HTML/JavaScript Updates

**1. Status Dropdown**:
- Added PICKED_UP as option
- Dropdown disabled when status is PICKED_UP

**2. Pickup Button Logic**:
```javascript
const allDone = order.skiItems.every(ski => ski.status === "DONE");
const allPickedUp = order.skiItems.every(ski => ski.status === "PICKED_UP");
pickupBtn.style.display = (allDone && !allPickedUp) ? "block" : "none";
```

**3. Modal Header**:
- Shows "✓ Order has been picked up" when status is PICKED_UP

---

## Status Badge Colors

| Status | Color | Hex | Use Case |
|--------|-------|-----|----------|
| PENDING | Yellow | #fff3cd | New items, work not started |
| IN_PROGRESS | Blue | #cce5ff | Currently being worked on |
| DONE | Green | #d4edda | Work completed, waiting for pickup |
| PICKED_UP | Cyan | #d1ecf1 | Order closed and delivered |
| RECEIVED | Purple | #e7d4f5 | Order received, has pending items |

---

## Complete Testing Checklist

### Test 1: Create and Pickup Order
- [ ] Create work order with 3 items
- [ ] All items show PENDING status
- [ ] Progress shows 0/3
- [ ] "Ready for Pickup" is HIDDEN
- [ ] Click "Manage Status"
- [ ] Modal opens

### Test 2: Work on Items
- [ ] Update item 1 → IN_PROGRESS
- [ ] Item 1 shows IN_PROGRESS badge
- [ ] Progress still 0/3 (only DONE counts)
- [ ] Order status still RECEIVED
- [ ] "Ready for Pickup" still HIDDEN

### Test 3: Complete Items
- [ ] Update item 1 → DONE
- [ ] Progress updates to 1/3
- [ ] Update item 2 → DONE
- [ ] Progress updates to 2/3
- [ ] Update item 3 → DONE
- [ ] Progress updates to 3/3
- [ ] Order status auto-changes to DONE

### Test 4: Ready for Pickup
- [ ] "Ready for Pickup" button appears
- [ ] Button is green
- [ ] Click button
- [ ] Modal closes (or updates)
- [ ] Fetch order again
- [ ] Order status is PICKED_UP
- [ ] All items show PICKED_UP badge
- [ ] "Order has been picked up" message shows

### Test 5: Pickup + Merge Logic
- [ ] Create order 1 with 2 items
- [ ] Update to DONE and pickup
- [ ] Create order 2, same customer
- [ ] New items should create ORDER 2 (not merged)
- [ ] Order 2 has status RECEIVED
- [ ] Order 2 items have status PENDING
- [ ] Order 1 items have status PICKED_UP

### Test 6: Pickup Button Disabled
- [ ] Open picked-up order modal
- [ ] All item dropdowns show DISABLED
- [ ] Can't change PICKED_UP items
- [ ] "Ready for Pickup" button HIDDEN
- [ ] "Order has been picked up" message visible

### Test 7: New Order After Pickup
- [ ] Create order 1, mark as pickup
- [ ] Create order 2 for same customer
- [ ] Verify order 2 is NEW (not merged)
- [ ] Order 2 status is RECEIVED
- [ ] Order 2 items are PENDING
- [ ] Both orders visible in list

---

## Database State After Pickup

### Before Pickup
```
WORK_ORDERS table:
  id=1, customer_id=1, status='DONE'

SKI_ITEMS table:
  id=1, work_order_id=1, status='DONE'
  id=2, work_order_id=1, status='DONE'
```

### After Clicking "Ready for Pickup"
```
WORK_ORDERS table:
  id=1, customer_id=1, status='PICKED_UP'

SKI_ITEMS table:
  id=1, work_order_id=1, status='PICKED_UP'
  id=2, work_order_id=1, status='PICKED_UP'
```

### Customer Creates New Order
```
WORK_ORDERS table:
  id=1, customer_id=1, status='PICKED_UP'
  id=2, customer_id=1, status='RECEIVED'     ← NEW

SKI_ITEMS table:
  id=1, work_order_id=1, status='PICKED_UP'
  id=2, work_order_id=1, status='PICKED_UP'
  id=3, work_order_id=2, status='PENDING'   ← NEW
  id=4, work_order_id=2, status='PENDING'   ← NEW
```

---

## Key Guarantees

✅ **Atomicity**: When marking as picked up, order AND all items updated together
✅ **No Merging After Pickup**: Merge logic prevents any items merging into PICKED_UP orders
✅ **Status Clarity**: UI shows exactly which orders/items are picked up
✅ **Read-Only After Pickup**: Can't change item status once picked up
✅ **Auto-Calculation**: Order status auto-updates based on items
✅ **Cascade Persistence**: All changes saved together in one transaction

---

## Future Enhancements

- Print pickup receipts showing all items
- Customer notification when order is ready
- Bulk item status updates
- History of status changes
- Estimated pickup time
- Store location selection
- Payment/deposit handling
