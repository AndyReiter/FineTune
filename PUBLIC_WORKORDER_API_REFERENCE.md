# Public Work Order API - Complete Reference

## Overview
The Public Work Order API allows customers to create work orders without authentication, with intelligent equipment selection and daily limit enforcement.

---

## Endpoints

### 1. Equipment Lookup
**Endpoint**: `GET /api/public/workorders/lookup-equipment`

**Query Parameters**:
- `name` - Customer's full name
- `email` - Customer's email address
- `phone` - Customer's phone number

**Response**:
```json
{
  "customerId": 123,
  "customerName": "John Doe",
  "equipment": [
    {
      "id": 1,
      "type": "SKI",
      "brand": "Rossignol",
      "model": "Experience 80",
      "length": 170,
      "serviceType": null,
      "status": null,
      "condition": "USED",
      "abilityLevel": "INTERMEDIATE",
      "boot": null
    }
  ],
  "boots": [
    {
      "id": 1,
      "brand": "Salomon",
      "model": "S/Pro 100",
      "bsl": 285,
      "heightInches": 70,
      "weight": 165,
      "age": 35,
      "abilityLevel": "INTERMEDIATE",
      "active": true
    }
  ]
}
```

---

### 2. Get Boots for Equipment
**Endpoint**: `GET /api/public/workorders/equipment/{equipmentId}/boots`

**Path Parameters**:
- `equipmentId` - ID of the equipment/ski

**Response**:
```json
[
  {
    "id": 1,
    "brand": "Salomon",
    "model": "S/Pro 100",
    "bsl": 285,
    "active": true
  }
]
```

---

### 3. Create Public Work Order
**Endpoint**: `POST /api/public/workorders`

**Request Body**:
```json
{
  "customerFirstName": "John",
  "customerLastName": "Doe",
  "email": "john@example.com",
  "phone": "555-123-4567",
  "promisedBy": "2026-02-25",
  "equipment": [
    {
      "equipmentId": 1,
      "serviceType": "WAXING"
    }
  ]
}
```

**Success Response** (HTTP 201):
```json
{
  "workOrderId": 42,
  "status": "created",
  "error": false,
  "equipmentOptions": [
    {
      "skiId": 1,
      "skiName": "Rossignol Experience 80 170cm - WAXING",
      "associatedBoots": []
    }
  ]
}
```

**Error Response - Daily Limit Exceeded** (HTTP 429):
```json
{
  "error": true,
  "message": "You have reached your daily limit of 25 work orders. Please visit the shop for further service.",
  "status": "error",
  "workOrderId": null,
  "equipmentOptions": []
}
```

---

## Equipment Selection Workflow

### Option 1: Using Existing Equipment
```json
{
  "equipment": [
    {
      "equipmentId": 1,
      "serviceType": "WAXING"
    }
  ]
}
```

### Option 2: Creating New Equipment
```json
{
  "equipment": [
    {
      "newEquipment": {
        "type": "SKI",
        "brand": "Rossignol",
        "model": "Experience 80",
        "length": 170,
        "condition": "USED",
        "abilityLevel": "INTERMEDIATE"
      },
      "serviceType": "WAXING"
    }
  ]
}
```

### Option 3: Using Existing Boot for Mount Service
```json
{
  "equipment": [
    {
      "equipmentId": 1,
      "serviceType": "MOUNT",
      "bootId": 2
    }
  ]
}
```

### Option 4: Creating New Boot for Mount Service
```json
{
  "equipment": [
    {
      "equipmentId": 1,
      "serviceType": "MOUNT",
      "bootBrand": "Salomon",
      "bootModel": "S/Pro 100",
      "bsl": 285,
      "heightInches": 70,
      "weight": 165,
      "age": 35,
      "abilityLevel": "INTERMEDIATE"
    }
  ]
}
```

---

## Customer Matching Logic

The system uses intelligent matching to find or create customers:

1. **Phone Normalization**: Removes all non-digit characters
   - `(555) 123-4567` → `5551234567`
   - `555.123.4567` → `5551234567`

2. **Matching Priority**:
   - First: Email or normalized phone match
   - Second: Case-insensitive full name match
   - Third: Create new customer

3. **Auto-Update**: Existing customer info is updated with latest data

---

## Daily Limit Enforcement

### Configuration
- Default: 25 work orders per 24 hours
- Configurable via `/api/staff-settings`
- Only applies to `customer_created = true` work orders

### How It Works
1. Customer attempts to create work order
2. System counts customer's work orders in past 24 hours where `customer_created = true`
3. If count ≥ limit, returns HTTP 429 with error response
4. Otherwise, creates work order normally

### Checking Current Limit
```
GET /api/staff-settings
```

Response:
```json
{
  "id": 1,
  "maxCustomerWorkOrdersPerDay": 25
}
```

---

## Complete Workflow Example

### Step 1: Lookup Customer Equipment
```bash
GET /api/public/workorders/lookup-equipment
  ?name=John%20Doe
  &email=john@example.com
  &phone=555-123-4567
```

### Step 2: Display Equipment to Customer
- Show all equipment from response
- Allow selection or creation of new equipment
- For mount services, show boots

### Step 3: Create Work Order
```bash
POST /api/public/workorders
Content-Type: application/json

{
  "customerFirstName": "John",
  "customerLastName": "Doe",
  "email": "john@example.com",
  "phone": "555-123-4567",
  "promisedBy": "2026-02-25",
  "equipment": [
    {
      "equipmentId": 1,
      "serviceType": "WAXING"
    },
    {
      "equipmentId": 2,
      "serviceType": "MOUNT",
      "bootId": 3
    }
  ]
}
```

### Step 4: Show Confirmation
Display `workOrderId` and `equipmentOptions` to customer as confirmation.

---

## Database Schema

### WorkOrder Table
```sql
- customer_created: BOOLEAN DEFAULT TRUE
- created_at: TIMESTAMP (for 24-hour limit queries)
```

### StaffSettings Table
```sql
- max_customer_work_orders_per_day: INT DEFAULT 25
```

---

## Error Handling

### Daily Limit Exceeded (429)
```json
{
  "error": true,
  "message": "You have reached your daily limit of 25 work orders...",
  "status": "error"
}
```

### Validation Error (400)
Standard Spring validation errors

### Not Found (404)
- Equipment not found
- Customer not found (for some endpoints)

---

## Response Field Descriptions

### PublicWorkOrderCreationResponse
- `workOrderId` - Unique ID of created work order
- `status` - "created" on success, "error" on failure
- `error` - Boolean indicating if request failed
- `message` - Error message (only present on errors)
- `equipmentOptions` - Array of equipment with details

### EquipmentOptionResponse
- `skiId` - ID of the equipment/ski
- `skiName` - Formatted name (Brand Model Length - ServiceType)
- `associatedBoots` - Array of boots linked to this equipment

### AssociatedBootResponse
- `bootId` - ID of the boot
- `bootName` - Formatted name (Brand Model BSL)

---

## Notes

- All timestamps are in ISO-8601 format
- Phone numbers are normalized automatically
- Email matching is case-insensitive
- Work orders are merged if customer has active orders
- Daily limit is per customer, not global
- `customer_created` flag distinguishes public vs staff work orders
