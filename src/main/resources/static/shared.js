// Shared JavaScript utilities for Work Order system

// API endpoints - Make globally available
// Use relative URLs so API calls hit the same host/subdomain as the frontend.
// This avoids CORS issues when running frontend on shopslug.localhost.com.
const ORIGIN = window.location.origin;
window.API_CONFIG = {
  BASE_URL: ORIGIN,
  WORKORDERS: "/workorders",
  BRANDS: "/brands",
  AUTH_LOGIN: "/auth/login",
  CUSTOMERS: "/api/customers",
  AUTH_REFRESH: "/auth/refresh",
};

// Also keep const reference for backward compatibility
const API_CONFIG = window.API_CONFIG;

// Note: Use API_CONFIG.BASE_URL or API_CONFIG.AUTH_LOGIN for backend URLs

console.log('shared.js loaded');

// Helper to mask Authorization header when logging
function _maskHeadersForLog(headers) {
  try {
    const copy = { ...(headers || {}) };
    if (copy.Authorization) {
      const parts = copy.Authorization.split(' ');
      const scheme = parts[0];
      const token = parts.slice(1).join(' ');
      const shown = token ? token.slice(-6) : '';
      copy.Authorization = `${scheme} ****${shown}`;
    }
    return copy;
  } catch (e) {
    return headers;
  }
}

// Authentication utilities
const AuthUtils = {
  // Get JWT token from localStorage
  getToken() {
    return localStorage.getItem('authToken');
  },

  // Store JWT token in localStorage
  setToken(token) {
    localStorage.setItem('authToken', token);
  },

  // Remove JWT token
  clearToken() {
    localStorage.removeItem('authToken');
  },

  // Check if user is authenticated
  isAuthenticated() {
    return !!this.getToken();
  },

  // Require authentication on protected pages; redirect to login if missing
  requireAuth() {
    const token = localStorage.getItem('authToken');
    if (!token) {
      console.warn('AuthUtils.requireAuth: missing authToken, redirecting to /login.html');
      try { window.location.href = '/login.html'; } catch (e) {}
      return false;
    }
    return true;
  },

  // Attempt to refresh auth token by calling /auth/refresh
  async refreshToken() {
    console.debug('AuthUtils.refreshToken: attempting token refresh');
    try {
      const current = localStorage.getItem('authToken');
      const res = await fetch(API_CONFIG.AUTH_REFRESH, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          ...(current ? { Authorization: `Bearer ${current}` } : {})
        }
      });

      if (!res.ok) {
        console.warn('AuthUtils.refreshToken: refresh request failed', res.status);
        return false;
      }

      // Try parsing JSON response; if not JSON, fail gracefully
      let data = null;
      try { data = await res.json(); } catch (e) { console.warn('AuthUtils.refreshToken: non-JSON refresh response'); }

      if (data && data.token) {
        try { localStorage.setItem('authToken', data.token); } catch (e) {}
        console.debug('AuthUtils.refreshToken: token refreshed');
        return true;
      }

      console.warn('AuthUtils.refreshToken: no token in refresh response');
      return false;
    } catch (err) {
      console.error('AuthUtils.refreshToken error', err);
      return false;
    }
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
        credentials: 'include',
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

    console.debug('publicFetch:', url, { headers: _maskHeadersForLog(config.headers) });
    const res = await fetch(url, config);
    console.debug('publicFetch response', res.status, 'for', url);
    return res;
  },

  // Logout function
  logout() {
    this.clearToken();
    window.location.reload();
  }
,
  // Auth fetch wrapper that uses localStorage 'authToken'
  async authFetch(url, options = {}) {
    options = { ...(options || {}) };
    const method = (options.method || 'GET').toUpperCase();

    // Build headers without overwriting user-provided headers
    const headers = {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    };

    const token = localStorage.getItem('authToken');
    const tokenPresent = !!token;
    if (tokenPresent) {
      headers.Authorization = `Bearer ${token}`;
      console.debug('authFetch: attached Authorization header (token present)');
    } else {
      console.debug('authFetch: no authToken found in localStorage');
    }

    console.debug('authFetch: request headers', _maskHeadersForLog(headers));

    // Automatically stringify JSON bodies for non-GET/HEAD requests when body is a plain object
    if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
      if (method !== 'GET' && method !== 'HEAD') {
        try {
          options.body = JSON.stringify(options.body);
        } catch (e) {
          console.warn('authFetch: failed to stringify request body', e);
        }
      } else {
        // GET/HEAD should not have a body; remove it
        delete options.body;
      }
    }

    // Prepare final fetch options
    const fetchOptions = { ...options, headers };

    console.debug(`authFetch: ${method} ${url}`, fetchOptions);

    const response = await fetch(url, { ...fetchOptions, credentials: fetchOptions.credentials || 'include' });

    // Log response status
    console.debug('authFetch: response', response.status, 'for', url);

    // If unauthorized, try refresh once and retry the original request
    if (response.status === 401) {
      console.warn('authFetch: 401 Unauthorized - attempting token refresh');
      try {
        const refreshed = await AuthUtils.refreshToken();
        if (refreshed) {
          console.debug('authFetch: token refresh reported success');
          // update Authorization header with new token and retry once
          const newToken = localStorage.getItem('authToken');
          if (newToken) {
            headers.Authorization = `Bearer ${newToken}`;
            console.debug('authFetch: retrying original request with refreshed token');
            const retryOptions = { ...options, headers, _retry: true };
            const retryRes = await fetch(url, { ...retryOptions, credentials: retryOptions.credentials || 'include' });
            console.debug('authFetch: retry response', retryRes.status, 'for', url);
            if (retryRes.status !== 401) return retryRes;
            // if retry also 401 fallthrough to clear
          }
        }
      } catch (e) {
        console.error('authFetch: error during token refresh attempt', e);
      }
      // Refresh failed or retry failed: clear token and redirect to login only for staff pages
      console.warn('authFetch: refresh failed or retry yielded 401 - clearing token');
      try { localStorage.removeItem('authToken'); } catch (e) {}
      try {
        const path = (window.location && window.location.pathname) ? window.location.pathname.toLowerCase() : '';
        const isPublicPage = path.includes('customer-workorder');
        if (!isPublicPage) {
          window.location.href = '/login.html';
        } else {
          console.warn('authFetch: public page detected, not redirecting to login');
        }
      } catch (e) {}
    }

    return response;
  }
};

// Expose AuthUtils to global window for pages that reference window.AuthUtils
window.AuthUtils = AuthUtils;
// Expose authFetch globally (bound to AuthUtils so 'this' works when called directly)
window.authFetch = AuthUtils.authFetch.bind(AuthUtils);

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
  // Build headers but avoid forcing Content-Type when sending FormData
  const headers = {
    ...(options.headers || {})
  };

  const isFormData = options && options.body && (options.body instanceof FormData);
  if (!isFormData) {
    // Only set JSON content-type when body is not FormData and header not already provided
    if (!headers['Content-Type'] && !headers['content-type']) {
      headers['Content-Type'] = 'application/json';
    }
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(url, { ...options, headers, credentials: options.credentials || 'include' });

  if (response.status === 401) {
    // Clear token and redirect to login for staff-only pages. Public pages (customer-workorder)
    // must NOT be redirected to login when they receive a 401 from a public API.
    AuthUtils.clearToken();
    try {
      const path = (window.location && window.location.pathname) ? window.location.pathname.toLowerCase() : '';
      const isPublicPage = path.includes('customer-workorder');
      if (!isPublicPage) {
        window.location.href = '/login.html';
      } else {
        console.warn('apiFetch: received 401 on public page; not redirecting to login');
      }
    } catch (e) {
      // ignore in non-browser contexts
    }
    throw new Error('Authentication required');
  }

  if (response.status === 403) {
    // Forbidden - clear token as well (token may be invalid) and surface error
    AuthUtils.clearToken();
    throw new Error('Forbidden');
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
      const res = await APIUtils.authenticatedFetch(API_CONFIG.BRANDS);
      if (!res || !res.ok) throw new Error('Failed to load brands');
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
    const res = await APIUtils.authenticatedFetch(`${API_CONFIG.BRANDS}/${brandId}/models`);
    const models = res && res.ok ? await res.json() : [];
    selectEl.innerHTML = '<option value="">Select Model</option>' +
      models.map(m => `<option value="${m.name}">${m.name}</option>`).join("");
  } catch (err) {
    console.error("Failed to load models", err);
    selectEl.innerHTML = '<option value="">Select Model</option>';
  }
}