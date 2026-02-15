const API_BASE = "http://localhost:8080";

// ===============================
// Work Order API
// ===============================

export async function fetchWorkOrders() {
  const res = await fetch(`${API_BASE}/workorders`);
  if (!res.ok) throw new Error("Failed to fetch work orders");
  return res.json();
}

export async function fetchWorkOrder(id) {
  const res = await fetch(`${API_BASE}/workorders/${id}`);
  if (!res.ok) throw new Error("Failed to fetch work order");
  return res.json();
}

export async function createWorkOrder(data) {
  const res = await fetch(`${API_BASE}/workorders`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error("Failed to create work order");
  return res.json();
}

export async function updateWorkOrder(id, data) {
  const res = await fetch(`${API_BASE}/workorders/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error("Failed to update work order");
  return res.json();
}

export async function deleteWorkOrder(id) {
  const res = await fetch(`${API_BASE}/workorders/${id}`, {
    method: "DELETE",
  });
  if (!res.ok) throw new Error("Failed to delete work order");
  return res.json();
}

// ===============================
// Customer API
// ===============================

export async function searchCustomers(query) {
  const params = new URLSearchParams();
  if (query.email) params.append('email', query.email);
  if (query.phone) params.append('phone', query.phone);
  if (query.name) params.append('name', query.name);
  
  const res = await fetch(`${API_BASE}/customers/search?${params.toString()}`);
  if (!res.ok) throw new Error("Failed to search customers");
  return res.json();
}

export async function lookupCustomer(email, phone) {
  const res = await fetch(`${API_BASE}/customers/lookup?email=${encodeURIComponent(email)}&phone=${encodeURIComponent(phone)}`);
  if (!res.ok) {
    if (res.status === 404) return null;
    throw new Error("Failed to lookup customer");
  }
  return res.json();
}

export async function createCustomer(data) {
  const res = await fetch(`${API_BASE}/customers`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error("Failed to create customer");
  return res.json();
}

export async function fetchCustomer(id) {
  const res = await fetch(`${API_BASE}/customers/${id}`);
  if (!res.ok) throw new Error("Failed to fetch customer");
  return res.json();
}

// ===============================
// Equipment API
// ===============================

export async function fetchCustomerEquipment(customerId) {
  const res = await fetch(`${API_BASE}/customers/${customerId}/equipment`);
  if (!res.ok) throw new Error("Failed to fetch customer equipment");
  return res.json();
}

export async function createEquipment(customerId, data) {
  const res = await fetch(`${API_BASE}/customers/${customerId}/equipment`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error("Failed to create equipment");
  return res.json();
}

// ===============================
// Boot API
// ===============================

export async function fetchCustomerBoots(email, phone) {
  const res = await fetch(`${API_BASE}/workorders/customer/boots?email=${encodeURIComponent(email)}&phone=${encodeURIComponent(phone)}`);
  if (!res.ok) {
    if (res.status === 404) return [];
    throw new Error("Failed to fetch customer boots");
  }
  return res.json();
}

export async function createBoot(customerId, data) {
  const res = await fetch(`${API_BASE}/customers/${customerId}/boots`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error("Failed to create boot");
  return res.json();
}

// ===============================
// Ski Models API
// ===============================

export async function fetchSkiModels() {
  const res = await fetch(`${API_BASE}/ski-models`);
  if (!res.ok) throw new Error("Failed to fetch ski models");
  return res.json();
}
