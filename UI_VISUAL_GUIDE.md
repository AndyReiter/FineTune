# UI Visual Guide

## Main Work Orders List

```
┌────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│ Work Orders (MVP)                                                                                      │
├────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                                        │
│ All Work Orders                                                                                      │
│                                                                                                        │
│ ID │ Customer    │ Phone      │ Email          │ Skis                      │ Work Order │ Created  │ A │
│    │             │            │                │                           │ Status     │ At       │ c │
├────┼─────────────┼────────────┼────────────────┼───────────────────────────┼────────────┼──────────┼───┤
│ 1  │ John Doe    │ 555-123... │ john@example.. │ Rossignol Experience 80  │ RECEIVED   │ 2026-01- │ M │
│    │             │            │                │ (WAXING) [PENDING]        │ 1/2 items  │ 31 11:00│ a │
│    │             │            │                │ Atomic Vantage           │ done       │          │ n │
│    │             │            │                │ (TUNE) [IN_PROGRESS]      │            │          │ a │
│    │             │            │                │                           │            │          │ g │
│    │             │            │                │                           │            │          │ e │
├────┼─────────────┼────────────┼────────────────┼───────────────────────────┼────────────┼──────────┼───┤
│ 2  │ Jane Smith  │ 555-456... │ jane@example.. │ Volkl Mantra             │ DONE       │ 2026-01- │ M │
│    │             │            │                │ (REPAIR) [DONE]           │ 2/2 items  │ 30 14:30│ a │
│    │             │            │                │ Head Absolut             │ done       │          │ n │
│    │             │            │                │ (MOUNT) [DONE]            │            │          │ a │
│    │             │            │                │                           │            │          │ g │
│    │             │            │                │                           │            │          │ e │
└────┴─────────────┴────────────┴────────────────┴───────────────────────────┴────────────┴──────────┴───┘
```

---

## Color-Coded Status Badges

```
┌──────────────┐    ┌────────────────┐    ┌──────────┐    ┌──────────┐    ┌─────────────┐
│ PENDING      │    │ IN_PROGRESS    │    │ DONE     │    │ RECEIVED │    │ PICKED_UP   │
├──────────────┤    ├────────────────┤    ├──────────┤    ├──────────┤    ├─────────────┤
│ Yellow badge │    │ Blue badge     │    │ Green    │    │ Purple   │    │ Cyan badge  │
│ #fff3cd      │    │ #cce5ff        │    │ #d4edda  │    │ #e7d4f5  │    │ #d1ecf1     │
└──────────────┘    └────────────────┘    └──────────┘    └──────────┘    └─────────────┘
```

---

## Status Management Modal

```
┌────────────────────────────────────────────────────────────────┐
│ ✕                                                              │
│ Edit Work Order Status                                         │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│ Customer: John Doe                                             │
│ Work Order Status: [RECEIVED]                                 │
│                                                                │
│ Ski Items                                                      │
│                                                                │
│ ┌────────────────────────────────────────────────────────────┐│
│ │ Rossignol Experience 80 (WAXING)                           ││
│ │ Current status: [PENDING]                                  ││
│ │                                    ┌──────────────────┐    ││
│ │                                    │ PENDING     ▼    │    ││
│ │                                    └──────────────────┘    ││
│ └────────────────────────────────────────────────────────────┘│
│                                                                │
│ ┌────────────────────────────────────────────────────────────┐│
│ │ Atomic Vantage (TUNE)                                      ││
│ │ Current status: [IN_PROGRESS]                              ││
│ │                                    ┌──────────────────┐    ││
│ │                                    │ IN_PROGRESS ▼    │    ││
│ │                                    └──────────────────┘    ││
│ └────────────────────────────────────────────────────────────┘│
│                                                                │
├────────────────────────────────────────────────────────────────┤
│ [Cancel]                       [Ready for Pickup]             │
│ (Visible only when ALL items are DONE)                         │
└────────────────────────────────────────────────────────────────┘
```

---

## Status Update Workflow

### Step 1: View All Orders (Initial State)
```
Order #1: John Doe
├─ Rossignol Experience 80 (WAXING) [PENDING]
├─ Atomic Vantage (TUNE) [PENDING]
Work Order Status: RECEIVED
Progress: 0/2 items done
Action: [Manage Status]
```

### Step 2: Open Modal & Update First Item
```
Modal Opens →
User clicks dropdown on first ski →
Selects IN_PROGRESS →
PATCH /workorders/1/skis/1/status { "status": "IN_PROGRESS" }
```

### Step 3: After First Item Updated
```
Modal refreshes automatically
Display updates:
├─ Rossignol Experience 80 (WAXING) [IN_PROGRESS] ✓
├─ Atomic Vantage (TUNE) [PENDING]
Work Order Status: RECEIVED (still has pending items)
Progress: 0/2 items done
"Ready for Pickup" button: HIDDEN
```

### Step 4: Update Second Item to IN_PROGRESS
```
User updates second dropdown to IN_PROGRESS
PATCH /workorders/1/skis/2/status { "status": "IN_PROGRESS" }

Modal updates:
├─ Rossignol Experience 80 (WAXING) [IN_PROGRESS]
├─ Atomic Vantage (TUNE) [IN_PROGRESS]
Work Order Status: RECEIVED (no items DONE yet)
Progress: 0/2 items done
"Ready for Pickup" button: HIDDEN
```

### Step 5: Update First Item to DONE
```
User updates first dropdown to DONE
PATCH /workorders/1/skis/1/status { "status": "DONE" }

Modal updates:
├─ Rossignol Experience 80 (WAXING) [DONE] ✓
├─ Atomic Vantage (TUNE) [IN_PROGRESS]
Work Order Status: RECEIVED (not all items DONE)
Progress: 1/2 items done
"Ready for Pickup" button: HIDDEN
```

### Step 6: Update Second Item to DONE
```
User updates second dropdown to DONE
PATCH /workorders/1/skis/2/status { "status": "DONE" }

Modal updates:
├─ Rossignol Experience 80 (WAXING) [DONE] ✓
├─ Atomic Vantage (TUNE) [DONE] ✓
Work Order Status: DONE (all items DONE - auto-calculated)
Progress: 2/2 items done
"Ready for Pickup" button: VISIBLE ✓ (enabled)
```

### Step 7: Click "Ready for Pickup"
```
User clicks [Ready for Pickup]
POST /workorders/1/pickup

Modal closes
Main list refreshes:
Order #1 now shows:
├─ Rossignol Experience 80 (WAXING) [DONE]
├─ Atomic Vantage (TUNE) [DONE]
Work Order Status: PICKED_UP ← Changed from DONE
Progress: 2/2 items done
```

### Step 8: Same Customer, New Order
```
John Doe submits new order with 1 new ski
System checks: Does John have open orders? NO (last one is PICKED_UP)
Result: CREATE NEW WORK ORDER (don't merge)
New Order #2: John Doe
├─ Head Absolut (MOUNT) [PENDING]
Work Order Status: RECEIVED
Progress: 0/1 items done
```

---

## Button States

### "Manage Status" Button (Always Visible)
```
[Manage Status] ← Visible on all work orders
                  Clicking opens the modal
```

### "Ready for Pickup" Button (Conditional)
```
When ANY item is not DONE:
    [Ready for Pickup] ← HIDDEN (display: none)

When ALL items are DONE:
    [Ready for Pickup] ← VISIBLE (display: block)
                      ← ENABLED (can click)
                      ← GREEN button color
```

---

## Data Flow Diagram

```
User Action
    ↓
JavaScript Event Handler
    ↓
API Request (PATCH/POST)
    ↓
Backend Controller
    ↓
Service Layer (Business Logic)
    ↓
Database Update
    ↓
Response (Updated WorkOrder)
    ↓
JavaScript Handler
    ↓
Modal Reload + List Refresh
    ↓
UI Updates Automatically
```

---

## Error Handling

### When Status Update Fails
```
User selects status dropdown
JavaScript sends PATCH request
Server returns 404 or 500 error
Alert shown: "Error updating ski status: [error message]"
Modal remains open
User can retry or cancel
```

### When Pickup Fails
```
User clicks "Ready for Pickup"
JavaScript sends POST request
Server returns 404 or 500 error
Alert shown: "Error marking as pickup: [error message]"
Modal remains open
User can retry or cancel
```

### When API Unreachable
```
User clicks "Manage Status"
JavaScript tries to fetch /workorders/{id}
Fetch fails (no server response)
Modal shows: "Failed to load work order."
User can close modal and retry
```

---

## Responsive Breakpoints

### Desktop (Max-width 100%)
- Table fully visible with all columns
- Modal width: 500px
- Buttons side-by-side in modal footer

### Tablet (Max-width 900px)
- Ski items column may wrap
- Modal width: 90%
- Buttons remain side-by-side

### Mobile (Max-width 600px)
- Table scrollable horizontally
- Modal width: 90%
- Single-column layout for modal footer
- Buttons stack vertically if needed

---

## Integration Points

### Frontend → Backend API Calls

1. **Create Work Order**
   - URL: POST /workorders
   - Body: CreateWorkOrderRequest
   - Response: WorkOrderResponse

2. **List All Orders**
   - URL: GET /workorders
   - Response: List<WorkOrderResponse>

3. **Get Single Order**
   - URL: GET /workorders/{id}
   - Response: WorkOrderResponse

4. **Update Ski Item Status** (NEW)
   - URL: PATCH /workorders/{orderId}/skis/{skiId}/status
   - Body: { "status": "DONE" }
   - Response: WorkOrderResponse

5. **Mark Order as Pickup** (NEW)
   - URL: POST /workorders/{orderId}/pickup
   - Body: (empty)
   - Response: WorkOrderResponse

---

## Summary Statistics

| Element | Count |
|---------|-------|
| New CSS classes | 15+ |
| New JS functions | 6 |
| New API endpoints | 2 |
| Status badge colors | 5 |
| Button types | 4 |
| Modal sections | 3 |
| Form fields in create | 4 |

