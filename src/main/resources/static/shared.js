// Shared JavaScript utilities for Work Order system

// API endpoints - Make globally available
window.API_CONFIG = {
  BASE_URL: "http://localhost:8080",
  WORKORDERS: "http://localhost:8080/workorders",
  BRANDS: "http://localhost:8080/brands",
  AUTH_LOGIN: "http://localhost:8080/auth/login",
  CUSTOMERS: "http://localhost:8080/api/customers"
};

// Also keep const reference for backward compatibility
const API_CONFIG = window.API_CONFIG;

// Note: Use API_CONFIG.BASE_URL or API_CONFIG.AUTH_LOGIN for backend URLs

console.log('shared.js loaded');

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
    console.log("AuthUtils.login called", email);
    try {
      const response = await fetch(API_CONFIG.AUTH_LOGIN, {
        method: "POST",
        headers: { "Content-Type": "application/json", "Accept": "application/json" },
        body: JSON.stringify({ email, password })
      });

      console.log("AuthUtils.login: received response", response.status);

      // If the server returned HTML (e.g. an error page or redirect to login), surface that instead
      const contentType = response.headers.get('Content-Type') || '';
      if (!response.ok) {
        const text = await response.text().catch(() => null);
        console.error("AuthUtils.login failed response:", response.status, text);
        throw new Error(text || `Login failed with status ${response.status}`);
      }

      if (!contentType.toLowerCase().includes('application/json')) {
        const text = await response.text().catch(() => '<no-body>');
        console.error('AuthUtils.login: expected JSON but received HTML. response.url=', response.url, ' redirected=', response.redirected, ' status=', response.status);
        console.error('AuthUtils.login: HTML response body:', text);
        throw new Error('Server did not return JSON for login. See console for HTML response.');
      }

      const data = await response.json();
      console.log("AuthUtils.login: response body", data);
      this.setToken(data.token);
      console.log("AuthUtils.login: token stored");
      return data;
    } catch (error) {
      console.error("Login error:", error);
      throw error;
    }
  },

  // Make an unauthenticated fetch for public endpoints
  async publicFetch(url, options = {}) {
    const config = {
      ...options,
      headers: {
        ...(options.headers || {})
      }
    };

    return fetch(url, config);
  },

  // Logout function
  logout() {
    this.clearToken();
    window.location.reload();
  }
};

// Expose AuthUtils to global window for pages that reference window.AuthUtils
window.AuthUtils = AuthUtils;

// API utilities
const APIUtils = {
  // Make authenticated API request
  async authenticatedFetch(url, options = {}) {
    return apiFetch(url, options);
  }
};

// Make authenticatedFetch globally available
window.authenticatedFetch = APIUtils.authenticatedFetch;
// Also expose APIUtils for direct usage if needed
window.APIUtils = APIUtils;

/**
 * Generic fetch wrapper that automatically adds Authorization: Bearer <jwt>
 * JWT is read from localStorage via AuthUtils.getToken().
 * Throws on 401/403 after clearing token.
 */
async function apiFetch(url, options = {}) {
  const token = AuthUtils.getToken();
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {})
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(url, { ...options, headers });

  if (response.status === 401 || response.status === 403) {
    AuthUtils.clearToken();
    throw new Error('Authentication required');
  }

  return response;
}

// Expose apiFetch globally for convenience
window.apiFetch = apiFetch;
// Also expose APIUtils for direct usage if needed
window.APIUtils = APIUtils;

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