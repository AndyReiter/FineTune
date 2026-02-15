// Shared JavaScript utilities for Work Order system

// API endpoints
const API_CONFIG = {
  WORKORDERS: "http://localhost:8080/workorders",
  BRANDS: "http://localhost:8080/brands",
  AUTH_LOGIN: "http://localhost:8080/auth/login",
  CUSTOMERS: "http://localhost:8080/api/customers"
};

// Authentication utilities
const AuthUtils = {
  // Get JWT token from localStorage
  getToken() {
    return localStorage.getItem('jwt_token');
  },

  // Store JWT token in localStorage
  setToken(token) {
    localStorage.setItem('jwt_token', token);
  },

  // Remove JWT token
  clearToken() {
    localStorage.removeItem('jwt_token');
  },

  // Check if user is authenticated
  isAuthenticated() {
    return !!this.getToken();
  },

  // Get headers with Authorization if authenticated
  getAuthHeaders() {
    const headers = { "Content-Type": "application/json" };
    const token = this.getToken();
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
    return headers;
  },

  // Login function
  async login(email, password) {
    try {
      const response = await fetch(API_CONFIG.AUTH_LOGIN, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password })
      });

      if (!response.ok) {
        throw new Error("Invalid credentials");
      }

      const data = await response.json();
      this.setToken(data.token);
      return data;
    } catch (error) {
      console.error("Login error:", error);
      throw error;
    }
  },

  // Logout function
  logout() {
    this.clearToken();
    window.location.reload();
  }
};

// API utilities
const APIUtils = {
  // Make authenticated API request
  async authenticatedFetch(url, options = {}) {
    const config = {
      ...options,
      headers: {
        ...AuthUtils.getAuthHeaders(),
        ...(options.headers || {})
      }
    };

    const response = await fetch(url, config);
    
    // Handle unauthorized responses
    if (response.status === 401 || response.status === 403) {
      AuthUtils.clearToken();
      throw new Error("Authentication required");
    }

    return response;
  }
};

// Form validation utilities
const ValidationUtils = {
  // Validate email format
  isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  },

  // Validate phone format (basic 10-digit validation)
  isValidPhone(phone) {
    const phoneRegex = /^\d{10}$/;
    return phoneRegex.test(phone.replace(/\D/g, ''));
  },

  // Show validation message
  showMessage(elementId, message, isError = false) {
    const element = document.getElementById(elementId);
    if (element) {
      element.textContent = message;
      element.className = `message ${isError ? 'error' : 'success'}`;
    }
  },

  // Clear validation message
  clearMessage(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
      element.textContent = "";
      element.className = "message";
    }
  }
};

// Status badge utility
function getStatusBadge(status) {
  return `<span class="status-badge status-${status}">${status}</span>`;
}

// Brand/Model dropdown utilities
let brandsCache = null; // cache brands to minimize network calls

/**
 * Fetch brands once and populate a given select element.
 * Option value = brand.id, option text = brand.name
 */
async function populateBrandSelect(selectEl) {
  try {
    if (!brandsCache) {
      const res = await fetch(API_CONFIG.BRANDS);
      brandsCache = await res.json();
    }
    // Reset options (preserve placeholder)
    selectEl.innerHTML = '<option value="">Select Make</option>' +
      brandsCache.map(b => `<option value="${b.id}">${b.name}</option>`).join("");
  } catch (err) {
    console.error("Failed to load brands", err);
  }
}

/**
 * Populate models for a selected brand id.
 * Option value/text = model.name to submit STRINGs as required.
 */
async function populateModelSelect(selectEl, brandId) {
  try {
    if (!brandId) {
      selectEl.innerHTML = '<option value="">Select Model</option>';
      return;
    }
    const res = await fetch(`${API_CONFIG.BRANDS}/${brandId}/models`);
    const models = await res.json();
    selectEl.innerHTML = '<option value="">Select Model</option>' +
      models.map(m => `<option value="${m.name}">${m.name}</option>`).join("");
  } catch (err) {
    console.error("Failed to load models", err);
    selectEl.innerHTML = '<option value="">Select Model</option>';
  }
}