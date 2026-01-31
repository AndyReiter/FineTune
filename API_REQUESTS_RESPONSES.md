# API Request/Response Examples

## 1. GET /workorders (List All Orders)

### Request
```
GET /workorders
Accept: application/json
```

### Response (HTTP 200)
```json
[
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
        "status": "PENDING"
      },
      {
        "id": 2,
        "skiMake": "Atomic",
        "skiModel": "Vantage",
        "serviceType": "TUNE",
        "status": "IN_PROGRESS"
      }
    ]
  },
  {
    "id": 2,
    "status": "DONE",
    "createdAt": "2026-01-30T14:30:00",
    "customerId": 2,
    "customerName": "Jane Smith",
    "customerEmail": "jane@example.com",
    "customerPhone": "5554567890",
    "skiItems": [
      {
        "id": 3,
        "skiMake": "Volkl",
        "skiModel": "Mantra",
        "serviceType": "REPAIR",
        "status": "DONE"
      },
      {
        "id": 4,
        "skiMake": "Head",
        "skiModel": "Absolut",
        "serviceType": "MOUNT",
        "status": "DONE"
      }
    ]
  }
]
```

---

## 2. GET /workorders/{id} (Get Single Order)

### Request
```
GET /workorders/1
Accept: application/json
```

### Response (HTTP 200)
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
      "status": "PENDING"
    },
    {
      "id": 2,
      "skiMake": "Atomic",
      "skiModel": "Vantage",
      "serviceType": "TUNE",
      "status": "IN_PROGRESS"
    }
  ]
}
```

### Response (HTTP 404 - Not Found)
```
Order not found
```

---

## 3. POST /workorders (Create New Order)

### Request
```
POST /workorders
Content-Type: application/json

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
    },
    {
      "skiMake": "Atomic",
      "skiModel": "Vantage",
      "serviceType": "TUNE"
    }
  ]
}
```

### Response (HTTP 201 - Created)
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
      "status": "PENDING"
    },
    {
      "id": 2,
      "skiMake": "Atomic",
      "skiModel": "Vantage",
      "serviceType": "TUNE",
      "status": "PENDING"
    }
  ]
}
```

---

## 4. POST /workorders (Merge Items - Same Customer, Open Order)

### Request (Same as above, but customer has existing open order)
```
POST /workorders
Content-Type: application/json

{
  "customerFirstName": "John",
  "customerLastName": "Doe",
  "email": "john@example.com",
  "phone": "5551234567",
  "skis": [
    {
      "skiMake": "Head",
      "skiModel": "Absolut",
      "serviceType": "MOUNT"
    }
  ]
}
```

### Response (HTTP 201)
Note: New items merged into existing order #1, not created new order
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
      "status": "PENDING"
    },
    {
      "id": 2,
      "skiMake": "Atomic",
      "skiModel": "Vantage",
      "serviceType": "TUNE",
      "status": "IN_PROGRESS"
    },
    {
      "id": 3,
      "skiMake": "Head",
      "skiModel": "Absolut",
      "serviceType": "MOUNT",
      "status": "PENDING"
    }
  ]
}
```

---

## 5. PATCH /workorders/{orderId}/skis/{skiId}/status (NEW)

### Request
```
PATCH /workorders/1/skis/1/status
Content-Type: application/json

{
  "status": "DONE"
}
```

### Response (HTTP 200)
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
      "status": "DONE"
    },
    {
      "id": 2,
      "skiMake": "Atomic",
      "skiModel": "Vantage",
      "serviceType": "TUNE",
      "status": "IN_PROGRESS"
    },
    {
      "id": 3,
      "skiMake": "Head",
      "skiModel": "Absolut",
      "serviceType": "MOUNT",
      "status": "PENDING"
    }
  ]
}
```

### Response (HTTP 404 - Work Order Not Found)
```
Work order not found: 999
```

### Response (HTTP 404 - Ski Item Not Found)
```
Ski item not found in work order: 999
```

---

## 6. PATCH /workorders/{orderId}/skis/{skiId}/status (All Items DONE)

### Request
```
PATCH /workorders/1/skis/3/status
Content-Type: application/json

{
  "status": "DONE"
}
```

### Response (HTTP 200)
Note: Work order status automatically changes to DONE when all items are DONE
```json
{
  "id": 1,
  "status": "DONE",
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
    },
    {
      "id": 2,
      "skiMake": "Atomic",
      "skiModel": "Vantage",
      "serviceType": "TUNE",
      "status": "DONE"
    },
    {
      "id": 3,
      "skiMake": "Head",
      "skiModel": "Absolut",
      "serviceType": "MOUNT",
      "status": "DONE"
    }
  ]
}
```

---

## 7. POST /workorders/{id}/pickup (Mark as Ready for Pickup)

### Request
```
POST /workorders/1/pickup
Content-Type: application/json
```

### Response (HTTP 200)
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
    },
    {
      "id": 2,
      "skiMake": "Atomic",
      "skiModel": "Vantage",
      "serviceType": "TUNE",
      "status": "DONE"
    },
    {
      "id": 3,
      "skiMake": "Head",
      "skiModel": "Absolut",
      "serviceType": "MOUNT",
      "status": "DONE"
    }
  ]
}
```

### Response (HTTP 404 - Not Found)
```
Work order not found: 999
```

---

## 8. POST /workorders (After Pickup - New Order Created)

### Request (Same customer, order 1 is PICKED_UP)
```
POST /workorders
Content-Type: application/json

{
  "customerFirstName": "John",
  "customerLastName": "Doe",
  "email": "john@example.com",
  "phone": "5551234567",
  "skis": [
    {
      "skiMake": "Salomon",
      "skiModel": "QST",
      "serviceType": "REPAIR"
    }
  ]
}
```

### Response (HTTP 201)
Note: NEW order created (id: 4), not merged into order 1
Because order 1 has status PICKED_UP (not open for merging)
```json
{
  "id": 4,
  "status": "RECEIVED",
  "createdAt": "2026-01-31T12:30:00",
  "customerId": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "customerPhone": "5551234567",
  "skiItems": [
    {
      "id": 5,
      "skiMake": "Salomon",
      "skiModel": "QST",
      "serviceType": "REPAIR",
      "status": "PENDING"
    }
  ]
}
```

---

## Status Codes Summary

| Code | Meaning | When |
|------|---------|------|
| 200 | OK | GET, PATCH, POST pickup successful |
| 201 | Created | POST /workorders successful (new or merged) |
| 404 | Not Found | Resource (order, ski) doesn't exist |
| 400 | Bad Request | Invalid request body |
| 500 | Server Error | Database or processing error |

---

## JavaScript Request Examples

### Fetch All Orders
```javascript
fetch('http://localhost:8080/workorders')
  .then(r => r.json())
  .then(data => console.log(data))
```

### Fetch Single Order (for modal)
```javascript
fetch('http://localhost:8080/workorders/1')
  .then(r => r.json())
  .then(data => populateModal(data))
```

### Update Ski Item Status
```javascript
fetch('http://localhost:8080/workorders/1/skis/1/status', {
  method: 'PATCH',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ status: 'DONE' })
})
  .then(r => r.json())
  .then(data => {
    reloadModal(data)
    reloadList()
  })
```

### Mark as Pickup
```javascript
fetch('http://localhost:8080/workorders/1/pickup', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' }
})
  .then(r => r.json())
  .then(data => {
    closeModal()
    reloadList()
  })
```

### Create Work Order
```javascript
fetch('http://localhost:8080/workorders', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    customerFirstName: 'John',
    customerLastName: 'Doe',
    email: 'john@example.com',
    phone: '5551234567',
    skis: [
      {
        skiMake: 'Rossignol',
        skiModel: 'Experience 80',
        serviceType: 'WAXING'
      }
    ]
  })
})
  .then(r => {
    if (!r.ok) throw new Error('Failed')
    return r.json()
  })
  .then(data => {
    showMessage('Order created!')
    reloadList()
  })
  .catch(err => showError(err.message))
```

---

## Valid Status Values

### For Ski Items
- PENDING (default, new items)
- IN_PROGRESS (being worked on)
- DONE (work completed)

### For Work Orders
- RECEIVED (has pending items, initial status)
- DONE (auto-set when all items are DONE)
- PICKED_UP (closed, no more merges allowed)

---

## Error Response Examples

### Validation Error
```
HTTP 400
"Invalid request body"
```

### Not Found Error
```
HTTP 404
"Work order not found: 999"
```

### Conflict Error
```
HTTP 400
"Ski item not found in work order: 999"
```

### Server Error
```
HTTP 500
"Internal server error"
```

---

## Testing with cURL

### Get All Orders
```bash
curl -X GET http://localhost:8080/workorders \
  -H "Accept: application/json"
```

### Get Single Order
```bash
curl -X GET http://localhost:8080/workorders/1 \
  -H "Accept: application/json"
```

### Create Order
```bash
curl -X POST http://localhost:8080/workorders \
  -H "Content-Type: application/json" \
  -d '{
    "customerFirstName": "John",
    "customerLastName": "Doe",
    "email": "john@example.com",
    "phone": "5551234567",
    "skis": [{
      "skiMake": "Rossignol",
      "skiModel": "Experience 80",
      "serviceType": "WAXING"
    }]
  }'
```

### Update Ski Status
```bash
curl -X PATCH http://localhost:8080/workorders/1/skis/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "DONE"}'
```

### Mark as Pickup
```bash
curl -X POST http://localhost:8080/workorders/1/pickup \
  -H "Content-Type: application/json"
```

---

## Testing with Postman

1. **Create Collection**: "Work Order API"
2. **Base URL**: http://localhost:8080

3. **Requests**:
   - GET /workorders (List)
   - GET /workorders/1 (Get One)
   - POST /workorders (Create)
   - PATCH /workorders/1/skis/1/status (Update Status)
   - POST /workorders/1/pickup (Pickup)

4. **Body Type**: JSON for all POST/PATCH requests
5. **Header**: Content-Type: application/json

