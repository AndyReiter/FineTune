// Use relative base so API calls go to same origin (supports subdomain dev hosts)
const API_BASE = '';

export const workOrderApi = {
  getAll: async () => {
    const response = await fetch(`${API_BASE}/workorders`);
    if (!response.ok) throw new Error('Failed to fetch work orders');
    return response.json();
  },

  getById: async (id) => {
    const response = await fetch(`${API_BASE}/workorders/${id}`);
    if (!response.ok) throw new Error('Failed to fetch work order');
    return response.json();
  },

  create: async (workOrder) => {
    const response = await fetch(`${API_BASE}/workorders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(workOrder),
    });
    if (!response.ok) throw new Error('Failed to create work order');
    return response.json();
  },

  update: async (id, workOrder) => {
    const response = await fetch(`${API_BASE}/workorders/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(workOrder),
    });
    if (!response.ok) throw new Error('Failed to update work order');
    return response.json();
  },

  delete: async (id) => {
    const response = await fetch(`${API_BASE}/workorders/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok) throw new Error('Failed to delete work order');
  },
};

export const shopApi = {
  getAll: async () => {
    const response = await fetch(`${API_BASE}/shops`);
    if (!response.ok) throw new Error('Failed to fetch shops');
    return response.json();
  },

  getById: async (id) => {
    const response = await fetch(`${API_BASE}/shops/${id}`);
    if (!response.ok) throw new Error('Failed to fetch shop');
    return response.json();
  },

  create: async (shop) => {
    const response = await fetch(`${API_BASE}/shops`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(shop),
    });
    if (!response.ok) throw new Error('Failed to create shop');
    return response.json();
  },

  update: async (id, shop) => {
    const response = await fetch(`${API_BASE}/shops/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(shop),
    });
    if (!response.ok) throw new Error('Failed to update shop');
    return response.json();
  },

  delete: async (id) => {
    const response = await fetch(`${API_BASE}/shops/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok) throw new Error('Failed to delete shop');
  },
};

export const locationApi = {
  getAll: async () => {
    const response = await fetch(`${API_BASE}/locations`);
    if (!response.ok) throw new Error('Failed to fetch locations');
    return response.json();
  },

  getById: async (id) => {
    const response = await fetch(`${API_BASE}/locations/${id}`);
    if (!response.ok) throw new Error('Failed to fetch location');
    return response.json();
  },

  create: async (location) => {
    const response = await fetch(`${API_BASE}/locations`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(location),
    });
    if (!response.ok) throw new Error('Failed to create location');
    return response.json();
  },

  update: async (id, location) => {
    const response = await fetch(`${API_BASE}/locations/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(location),
    });
    if (!response.ok) throw new Error('Failed to update location');
    return response.json();
  },

  delete: async (id) => {
    const response = await fetch(`${API_BASE}/locations/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok) throw new Error('Failed to delete location');
  },
};
