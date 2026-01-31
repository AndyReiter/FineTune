# Pickup UI States - Visual Reference

## Modal States at Each Step

### Step 1: Order Created (All Items PENDING)

```
┌────────────────────────────────────────────────┐
│ Edit Work Order Status                         │
├────────────────────────────────────────────────┤
│                                                │
│ Customer: John Doe                             │
│ Work Order Status: [RECEIVED badge]            │
│                                                │
│ Ski Items                                      │
│                                                │
│ ┌────────────────────────────────────────────┐│
│ │ Rossignol Experience 80 (WAXING)           ││
│ │ Current status: [PENDING]                  ││
│ │                    [PENDING ▼] ← Enabled   ││
│ └────────────────────────────────────────────┘│
│                                                │
│ ┌────────────────────────────────────────────┐│
│ │ Atomic Vantage (TUNE)                      ││
│ │ Current status: [PENDING]                  ││
│ │                    [PENDING ▼] ← Enabled   ││
│ └────────────────────────────────────────────┘│
│                                                │
├────────────────────────────────────────────────┤
│ [Cancel]                 (no pickup button)   │
└────────────────────────────────────────────────┘

Status Dropdowns Available:
  - PENDING
  - IN_PROGRESS
  - DONE
  - PICKED_UP
```

---

## Step 2: Items In Progress

```
┌────────────────────────────────────────────────┐
│ Edit Work Order Status                         │
├────────────────────────────────────────────────┤
│                                                │
│ Customer: John Doe                             │
│ Work Order Status: [RECEIVED badge]            │
│                                                │
│ Ski Items                                      │
│                                                │
│ ┌────────────────────────────────────────────┐│
│ │ Rossignol Experience 80 (WAXING)           ││
│ │ Current status: [IN_PROGRESS]              ││
│ │                    [IN_PROGRESS ▼]         ││
│ └────────────────────────────────────────────┘│
│                                                │
│ ┌────────────────────────────────────────────┐│
│ │ Atomic Vantage (TUNE)                      ││
│ │ Current status: [IN_PROGRESS]              ││
│ │                    [IN_PROGRESS ▼]         ││
│ └────────────────────────────────────────────┘│
│                                                │
├────────────────────────────────────────────────┤
│ [Cancel]                 (no pickup button)   │
└────────────────────────────────────────────────┘

Progress Counter (in main list): 0/2 items done
Order Status (in main list): [RECEIVED]
```

---

## Step 3: One Item Done

```
┌────────────────────────────────────────────────┐
│ Edit Work Order Status                         │
├────────────────────────────────────────────────┤
│                                                │
│ Customer: John Doe                             │
│ Work Order Status: [RECEIVED badge]            │
│                                                │
│ Ski Items                                      │
│                                                │
│ ┌────────────────────────────────────────────┐│
│ │ Rossignol Experience 80 (WAXING)           ││
│ │ Current status: [DONE]                     ││
│ │                    [DONE ▼]                ││
│ └────────────────────────────────────────────┘│
│                                                │
│ ┌────────────────────────────────────────────┐│
│ │ Atomic Vantage (TUNE)                      ││
│ │ Current status: [IN_PROGRESS]              ││
│ │                    [IN_PROGRESS ▼]         ││
│ └────────────────────────────────────────────┘│
│                                                │
├────────────────────────────────────────────────┤
│ [Cancel]                 (no pickup button)   │
└────────────────────────────────────────────────┘

Progress Counter (in main list): 1/2 items done
Order Status (in main list): [RECEIVED]
```

---

## Step 4: All Items Done (Ready for Pickup)

```
┌────────────────────────────────────────────────┐
│ Edit Work Order Status                         │
├────────────────────────────────────────────────┤
│                                                │
│ Customer: John Doe                             │
│ Work Order Status: [DONE badge]                │
│                                                │
│ Ski Items                                      │
│                                                │
│ ┌────────────────────────────────────────────┐│
│ │ Rossignol Experience 80 (WAXING)           ││
│ │ Current status: [DONE]                     ││
│ │                    [DONE ▼]                ││
│ └────────────────────────────────────────────┘│
│                                                │
│ ┌────────────────────────────────────────────┐│
│ │ Atomic Vantage (TUNE)                      ││
│ │ Current status: [DONE]                     ││
│ │                    [DONE ▼]                ││
│ └────────────────────────────────────────────┘│
│                                                │
├────────────────────────────────────────────────┤
│ [Cancel]      [Ready for Pickup] ← GREEN BTN  │
└────────────────────────────────────────────────┘

Progress Counter (in main list): 2/2 items done ✓
Order Status (in main list): [DONE]
```

---

## Step 5: After Clicking "Ready for Pickup"

```
┌────────────────────────────────────────────────┐
│ Edit Work Order Status                         │
├────────────────────────────────────────────────┤
│                                                │
│ Customer: John Doe                             │
│ Work Order Status: [PICKED_UP badge]           │
│ ✓ Order has been picked up                     │
│                                                │
│ Ski Items                                      │
│                                                │
│ ┌────────────────────────────────────────────┐│
│ │ Rossignol Experience 80 (WAXING)           ││
│ │ Current status: [PICKED_UP]                ││
│ │                    [PICKED_UP ▼] DISABLED  ││
│ └────────────────────────────────────────────┘│
│                                                │
│ ┌────────────────────────────────────────────┐│
│ │ Atomic Vantage (TUNE)                      ││
│ │ Current status: [PICKED_UP]                ││
│ │                    [PICKED_UP ▼] DISABLED  ││
│ └────────────────────────────────────────────┘│
│                                                │
├────────────────────────────────────────────────┤
│ [Cancel]                 (no pickup button)   │
└────────────────────────────────────────────────┘

Progress Counter (in main list): 2/2 items done
Order Status (in main list): [PICKED_UP]
Dropdowns: GREYED OUT (can't click)
```

---

## Main List View - Progressive States

### State 1: New Order (All Items PENDING)
```
┌─────────────────────────────────────────────────────────────────────────┐
│ ID │ Customer   │ Skis                          │ Status      │ Actions │
├────┼────────────┼───────────────────────────────┼─────────────┼─────────┤
│ 1  │ John Doe   │ • Rossignol (WAXING) [PENDING]│ RECEIVED    │ Manage  │
│    │            │ • Atomic (TUNE) [PENDING]     │ 0/2 items   │ Status  │
│    │            │                               │ done        │         │
└────┴────────────┴───────────────────────────────┴─────────────┴─────────┘
```

### State 2: Work In Progress
```
┌─────────────────────────────────────────────────────────────────────────┐
│ ID │ Customer   │ Skis                              │ Status  │ Actions │
├────┼────────────┼───────────────────────────────────┼─────────┼─────────┤
│ 1  │ John Doe   │ • Rossignol (WAXING) [DONE]      │ DONE    │ Manage  │
│    │            │ • Atomic (TUNE) [DONE]           │ 2/2     │ Status  │
│    │            │                                  │ items   │         │
│    │            │                                  │ done    │         │
└────┴────────────┴───────────────────────────────────┴─────────┴─────────┘
```

### State 3: Picked Up
```
┌─────────────────────────────────────────────────────────────────────────┐
│ ID │ Customer   │ Skis                              │ Status   │ Actions │
├────┼────────────┼───────────────────────────────────┼──────────┼─────────┤
│ 1  │ John Doe   │ • Rossignol (WAXING) [PICKED_UP]│ PICKED_UP│ Manage  │
│    │            │ • Atomic (TUNE) [PICKED_UP]     │ 2/2      │ Status  │
│    │            │                                  │ items    │         │
│    │            │                                  │ done     │         │
└────┴────────────┴───────────────────────────────────┴──────────┴─────────┘
```

### State 4: Same Customer, New Order
```
┌─────────────────────────────────────────────────────────────────────────┐
│ ID │ Customer   │ Skis                              │ Status      │ Act  │
├────┼────────────┼───────────────────────────────────┼─────────────┼──────┤
│ 1  │ John Doe   │ • Rossignol (WAXING) [PICKED_UP]│ PICKED_UP   │ Mng  │
│    │            │ • Atomic (TUNE) [PICKED_UP]     │ 2/2 done    │ Stat │
├────┼────────────┼───────────────────────────────────┼─────────────┼──────┤
│ 2  │ John Doe   │ • Head (MOUNT) [PENDING]        │ RECEIVED    │ Mng  │
│    │            │                                  │ 0/1 items   │ Stat │
│    │            │                                  │ done        │      │
└────┴────────────┴───────────────────────────────────┴─────────────┴──────┘
```

---

## Dropdown States

### Enabled (Before Pickup)
```
When ski status is: PENDING, IN_PROGRESS, or DONE

[Status Dropdown ▼]
├─ PENDING
├─ IN_PROGRESS
├─ DONE
└─ PICKED_UP

Click: Changes status
Color: Normal (clickable)
```

### Disabled (After Pickup)
```
When ski status is: PICKED_UP

[PICKED_UP ▼] ← Greyed out
├─ PENDING (greyed)
├─ IN_PROGRESS (greyed)
├─ DONE (greyed)
└─ PICKED_UP ← Selected (greyed)

Click: No effect
Color: Grey (disabled)
Text: Appears disabled
```

---

## Button States

### "Manage Status" Button
```
Always visible on every order
Text: "Manage Status"
Color: Blue (#007bff)
Enabled: Always

Click: Opens modal for that order
```

### "Ready for Pickup" Button
```
HIDDEN when:
- ANY item is PENDING
- ANY item is IN_PROGRESS
- Order already PICKED_UP

VISIBLE when:
- ALL items are DONE
- Order NOT yet PICKED_UP

Text: "Ready for Pickup"
Color: Green (#28a745)
Enabled: When visible

Click: POST /workorders/{id}/pickup
```

### "Cancel" Button (in Modal)
```
Always visible
Text: "Cancel"
Color: Gray (#6c757d)
Enabled: Always

Click: Close modal without saving
```

---

## Badge Colors Reference

### In Dropdowns
```
PENDING      ← Selected (yellow)
IN_PROGRESS  ← Selected (blue)
DONE         ← Selected (green)
PICKED_UP    ← Selected (cyan)
```

### In Status Display
```
[RECEIVED]   (purple) - Order has items in progress
[DONE]       (green)  - Order ready for pickup
[PICKED_UP]  (cyan)   - Order closed
[PENDING]    (yellow) - Item not started
[IN_PROGRESS](blue)   - Item being worked on
[DONE]       (green)  - Item completed
[PICKED_UP]  (cyan)   - Item picked up
```

---

## Complete User Journey

```
Customer brings skis
        ↓
CREATE ORDER
  Status: RECEIVED
  All items: PENDING (yellow badges)
        ↓
TECH STARTS WORK
  [Manage Status]
  Update items → IN_PROGRESS (blue badges)
  Progress: 0/3 items done
        ↓
TECH CONTINUES
  Update items → DONE (green badges)
  Progress updates: 0/3 → 1/3 → 2/3 → 3/3
  Order status: DONE (auto-calculated)
        ↓
READY FOR PICKUP
  [Manage Status]
  [Ready for Pickup] button appears ✓
  Click [Ready for Pickup]
        ↓
POST /workorders/{id}/pickup
        ↓
ORDER PICKED UP
  Status: PICKED_UP (cyan badge)
  All items: PICKED_UP (cyan badges)
  Dropdowns: DISABLED
  Message: ✓ Order has been picked up
  [Ready for Pickup]: HIDDEN
        ↓
CUSTOMER RETURNS (SAME PERSON)
  Create order: Same email/phone
  System checks: Find open orders?
  Found: NO (previous order is PICKED_UP)
  Action: CREATE NEW ORDER
        ↓
NEW ORDER CREATED
  Order 2: RECEIVED
  All items: PENDING
  Progress: 0/2 items done
        ↓
REPEAT CYCLE
```

---

## Summary

### Before Pickup
✓ Items can be updated (dropdowns enabled)
✓ "Ready for Pickup" button shown when all DONE
✓ Order can receive merged items
✓ Dropdowns show all 4 options

### After Pickup  
✓ Items locked at PICKED_UP (dropdowns disabled)
✓ "Ready for Pickup" button hidden
✓ Order cannot receive merged items
✓ Dropdowns only show PICKED_UP (disabled)
✓ Message confirms pickup: "✓ Order has been picked up"
✓ New orders for same customer create NEW orders

### Status Guarantees
✓ ATOMIC: All items updated together
✓ CONSISTENT: Order status matches item statuses
✓ PERSISTENT: All changes saved to database
✓ MERGED: No merging into PICKED_UP orders
