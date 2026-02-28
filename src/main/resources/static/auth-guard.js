(async function() {
  try { document.documentElement.style.visibility = 'hidden'; } catch(e) {}

  const REFRESH_INTERVAL_MS = 30*60*1000;
  let refreshTimer = null;

  function isLoginOrSelectPage() {
    const p = window.location.pathname || '';
    return p.endsWith('/login.html') || p.endsWith('/select-shop.html');
  }

  async function startPeriodicRefresh() {
    if (refreshTimer) return;
    refreshTimer = setInterval(() => {
      fetch('/auth/refresh', { method: 'POST', credentials: 'include' }).catch(()=>{});
    }, REFRESH_INTERVAL_MS);
  }

  async function checkAuth() {
    try {
      const res = await fetch('/auth/me', { credentials: 'include' });

      if (res.status === 200) {
        const data = await res.json();
        // Make sure backend returns a user object
        if (data && data.staff) {
          window.userAuthenticated = true; // <-- mark user as authenticated
          await startPeriodicRefresh();
          try { document.documentElement.style.visibility = ''; } catch(e){}
          // Notify other scripts that auth has been confirmed
          try { window.dispatchEvent(new Event('auth:ready')); } catch(e) { console.warn('auth-guard: dispatch auth:ready failed', e); }
          return true;
        }
      }

      if (res.status === 401 || res.status === 403) {
        if (!isLoginOrSelectPage()) window.location.href = '/login.html';
        return false;
      }

      if (res.status === 409) {
        if (!isLoginOrSelectPage()) window.location.href = '/select-shop.html';
        return false;
      }

      // fallback: allow UI to show
      try { document.documentElement.style.visibility = ''; } catch(e){}
      return false;

    } catch(e) {
      console.warn('auth-guard: /auth/me fetch failed', e);
      try { document.documentElement.style.visibility = ''; } catch(e){}
      return false;
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', checkAuth);
  } else {
    checkAuth();
  }
})();