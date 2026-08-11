/* =========================================================
   api.js — fetch wrapper, auth storage, toasts, formatters
   Loaded on every page before the page-specific script.
   ========================================================= */

const API_BASE = '/api';

const Auth = {
  getToken() { return localStorage.getItem('et_token'); },
  setSession(authResponse) {
    localStorage.setItem('et_token', authResponse.token);
    localStorage.setItem('et_user', JSON.stringify({
      userId: authResponse.userId,
      fullName: authResponse.fullName,
      email: authResponse.email
    }));
  },
  getUser() {
    try { return JSON.parse(localStorage.getItem('et_user')); } catch { return null; }
  },
  clear() {
    localStorage.removeItem('et_token');
    localStorage.removeItem('et_user');
  },
  isLoggedIn() { return !!this.getToken(); },
  logout() {
    this.clear();
    window.location.href = '/login.html';
  },
  requireAuth() {
    if (!this.isLoggedIn()) window.location.href = '/login.html';
  }
};

const Api = {
  async request(path, options = {}) {
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    const token = Auth.getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const res = await fetch(`${API_BASE}${path}`, { ...options, headers });

    if (res.status === 401) {
      Auth.clear();
      if (!window.location.pathname.includes('login')) {
        window.location.href = '/login.html';
      }
      throw new Error('Session expired. Please log in again.');
    }

    const isBlob = options.responseType === 'blob';
    if (isBlob) {
      if (!res.ok) throw new Error('Export failed');
      return res.blob();
    }

    let body = null;
    try { body = await res.json(); } catch { /* no body */ }

    if (!res.ok || (body && body.success === false)) {
      const message = (body && body.message) || `Request failed (${res.status})`;
      const err = new Error(message);
      err.fieldErrors = body && typeof body.data === 'object' ? body.data : null;
      throw err;
    }

    return body ? body.data : null;
  },

  get(path) { return this.request(path, { method: 'GET' }); },
  post(path, data) { return this.request(path, { method: 'POST', body: JSON.stringify(data) }); },
  put(path, data) { return this.request(path, { method: 'PUT', body: JSON.stringify(data) }); },
  del(path) { return this.request(path, { method: 'DELETE' }); },

  async downloadFile(path, filename) {
    const blob = await this.request(path, { method: 'GET', responseType: 'blob' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = filename;
    document.body.appendChild(a); a.click(); a.remove();
    window.URL.revokeObjectURL(url);
  }
};

/* ---------------- Toasts ---------------- */
function ensureToastStack() {
  let stack = document.querySelector('.toast-stack');
  if (!stack) {
    stack = document.createElement('div');
    stack.className = 'toast-stack';
    document.body.appendChild(stack);
  }
  return stack;
}

const ICONS = { success: '✓', error: '⚠', warn: '⚠', info: 'ℹ' };

function toast(message, type = 'success', duration = 3800) {
  const stack = ensureToastStack();
  const el = document.createElement('div');
  el.className = `toast ${type}`;
  el.innerHTML = `<span class="toast-icon">${ICONS[type] || ICONS.info}</span><span>${escapeHtml(message)}</span>`;
  stack.appendChild(el);
  setTimeout(() => {
    el.classList.add('hide');
    setTimeout(() => el.remove(), 200);
  }, duration);
}

/* ---------------- Formatters ---------------- */
function formatCurrency(value) {
  const n = Number(value || 0);
  return '₹' + n.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDate(dateStr) {
  if (!dateStr) return '—';
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

function formatMonthLabel(ym) {
  if (!ym) return '';
  const [y, m] = ym.split('-');
  const d = new Date(Number(y), Number(m) - 1, 1);
  return d.toLocaleDateString('en-IN', { month: 'short', year: '2-digit' });
}

function paymentMethodLabel(pm) {
  const map = {
    CASH: 'Cash', CREDIT_CARD: 'Credit Card', DEBIT_CARD: 'Debit Card',
    UPI: 'UPI', NET_BANKING: 'Net Banking', WALLET: 'Wallet'
  };
  return map[pm] || pm;
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}

function initials(name) {
  if (!name) return '?';
  const parts = name.trim().split(/\s+/);
  return ((parts[0]?.[0] || '') + (parts[1]?.[0] || '')).toUpperCase();
}

function debounce(fn, wait = 350) {
  let t;
  return (...args) => {
    clearTimeout(t);
    t = setTimeout(() => fn(...args), wait);
  };
}

function showFieldErrors(formEl, fieldErrors) {
  formEl.querySelectorAll('.field').forEach(f => {
    f.classList.remove('has-error');
    const errEl = f.querySelector('.error-text');
    if (errEl) errEl.textContent = '';
  });
  if (!fieldErrors) return;
  Object.entries(fieldErrors).forEach(([field, message]) => {
    const input = formEl.querySelector(`[name="${field}"]`);
    if (!input) return;
    const wrap = input.closest('.field');
    if (!wrap) return;
    wrap.classList.add('has-error');
    let errEl = wrap.querySelector('.error-text');
    if (!errEl) {
      errEl = document.createElement('div');
      errEl.className = 'error-text';
      wrap.appendChild(errEl);
    }
    errEl.textContent = message;
  });
}

function confirmDialog(message) {
  return window.confirm(message);
}
