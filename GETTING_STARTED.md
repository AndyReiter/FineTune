# Quick Start Guide - Status Management Feature

## In 5 Minutes

### 1. Build & Start
```bash
cd C:\Users\Hoya\Desktop\FineTune
mvnw.cmd clean package
mvnw.cmd spring-boot:run
```

### 2. Open Browser
```
http://localhost:8080
```

### 3. Create Test Order
- First Name: John
- Last Name: Doe
- Phone: 5551234567
- Email: john@example.com
- Add Ski: Rossignol, Experience 80, WAXING
- Add Ski: Atomic, Vantage, TUNE
- Click "Create Work Order"

### 4. Test Status Updates
- See order in list with 2 skis, both PENDING
- Click "Manage Status"
- Modal opens showing both items
- Change first ski to "IN_PROGRESS"
- Modal auto-updates, progress shows "0/2"
- Change first ski to "DONE"
- Progress shows "1/2"
- Change second ski to "DONE"
- Progress shows "2/2"
- Modal now shows "Ready for Pickup" button
- Click button
- Order status changes to "PICKED_UP"

### 5. Test Merge Logic
- Create new order, same customer (john@example.com)
- Add new ski: Head, Absolut, MOUNT
- System detects PICKED_UP status
- Creates NEW order (doesn't merge)
- Previous order: PICKED_UP
- New order: RECEIVED

---

## Key Features Demonstrated

| Feature | How to Use |
|---------|-----------|
| Create Order | Fill form, click Create |
| View Status | See color badges in list |
| Update Status | Click "Manage Status" → dropdown |
| Track Progress | See "X/Y items done" |
| Complete Order | Update all items → "Ready for Pickup" appears |
| Merge Items | Same customer, open order → auto-merges |

---

## API Endpoints Added

```
✓ PATCH /workorders/{orderId}/skis/{skiId}/status
  Update individual ski item status

✓ POST /workorders/{orderId}/pickup
  Mark work order as ready for pickup
```

---

## Status Values

### Ski Item Status
- PENDING (new, default)
- IN_PROGRESS (being worked on)
- DONE (completed)

### Work Order Status
- RECEIVED (has work in progress)
- DONE (all items done - auto-calculated)
- PICKED_UP (order closed)

---

## Testing Checklist

- [ ] Load index.html in browser
- [ ] Create work order with 3 items
- [ ] All items show as PENDING (yellow badge)
- [ ] Progress shows "0/3 items done"
- [ ] Click "Manage Status"
- [ ] Modal opens with customer name
- [ ] See dropdowns for each item
- [ ] Change item 1 to IN_PROGRESS
- [ ] Progress stays "0/3" (only DONE counts)
- [ ] Change item 1 to DONE
- [ ] Progress updates to "1/3"
- [ ] Change items 2 and 3 to DONE
- [ ] Progress shows "3/3 items done"
- [ ] Order status auto-changes to DONE
- [ ] "Ready for Pickup" button appears
- [ ] Click button
- [ ] Order status changes to PICKED_UP
- [ ] Modal closes, list refreshes
- [ ] Create new order for same customer
- [ ] New order created (not merged, because previous was PICKED_UP)

---

## Documentation Files

Navigate to project root to find:
- `COMPLETE_UPDATE_SUMMARY.md` - Full overview
- `HTML_UI_UPDATE_SUMMARY.md` - Features & testing
- `UI_VISUAL_GUIDE.md` - Mockups & workflows
- `API_REQUESTS_RESPONSES.md` - API examples
- `MERGE_IMPLEMENTATION_REFERENCE.md` - Merge logic

---

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Modal won't open | Check browser console for errors, verify backend is running |
| Status updates fail | Check Network tab, verify ski item ID is correct |
| "Ready for Pickup" button missing | Make sure ALL items are marked DONE, not just some |
| Getting 404 errors | Verify work order ID and ski item ID exist |
| Orders not merging | Check if previous order has status PICKED_UP (won't merge) |

---

## Next Steps After Testing

1. ✅ Verify all status updates work
2. ✅ Verify merge logic works (if customer has open order)
3. ✅ Verify "Ready for Pickup" button appears only when needed
4. ✅ Test with multiple orders
5. ⏳ Add unit tests for service methods
6. ⏳ Add integration tests for API endpoints
7. ⏳ Deploy to production

---

## Success - You're Done! 🎉

If all tests pass:
- ✅ Individual ski item status tracking works
- ✅ Work order automatically calculates overall status
- ✅ UI updates in real-time without page reload
- ✅ "Ready for Pickup" button works correctly
- ✅ Merge logic prevents unnecessary new orders

Your shop management system is now complete with full status tracking!
