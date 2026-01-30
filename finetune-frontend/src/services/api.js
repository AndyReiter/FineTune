const API_BASE = "http://localhost:8080";

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
