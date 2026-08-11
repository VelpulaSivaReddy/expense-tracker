/* =========================================================
   auth.js — login / register / forgot-password page logic
   ========================================================= */

document.addEventListener('DOMContentLoaded', () => {
  if (Auth.isLoggedIn() && document.body.dataset.page !== 'reset') {
    window.location.href = '/dashboard.html';
    return;
  }

  wireLoginForm();
  wireRegisterForm();
  wireForgotForm();
  wireResetForm();
  wirePasswordToggles();
});

function wirePasswordToggles() {
  document.querySelectorAll('.toggle-visibility').forEach(btn => {
    btn.addEventListener('click', () => {
      const input = btn.closest('.password-field').querySelector('input');
      const isPassword = input.type === 'password';
      input.type = isPassword ? 'text' : 'password';
      btn.textContent = isPassword ? 'Hide' : 'Show';
    });
  });
}

function setLoading(button, loading, label) {
  if (!button) return;
  button.disabled = loading;
  button.innerHTML = loading
    ? `<span class="spinner"></span> ${label || 'Please wait…'}`
    : button.dataset.originalLabel || label;
}

function wireLoginForm() {
  const form = document.getElementById('login-form');
  if (!form) return;

  const submitBtn = form.querySelector('button[type="submit"]');
  submitBtn.dataset.originalLabel = submitBtn.innerHTML;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = form.email.value.trim();
    const password = form.password.value;

    setLoading(submitBtn, true, 'Signing in…');
    try {
      const data = await Api.post('/auth/login', { email, password });
      Auth.setSession(data);
      toast('Login successful', 'success');
      setTimeout(() => { window.location.href = '/dashboard.html'; }, 400);
    } catch (err) {
      showFieldErrors(form, err.fieldErrors);
      toast(err.message || 'Login failed', 'error');
    } finally {
      setLoading(submitBtn, false);
    }
  });
}

function wireRegisterForm() {
  const form = document.getElementById('register-form');
  if (!form) return;

  const submitBtn = form.querySelector('button[type="submit"]');
  submitBtn.dataset.originalLabel = submitBtn.innerHTML;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    if (form.password.value !== form.confirmPassword.value) {
      showFieldErrors(form, { confirmPassword: 'Passwords do not match' });
      toast('Passwords do not match', 'error');
      return;
    }

    const payload = {
      fullName: form.fullName.value.trim(),
      email: form.email.value.trim(),
      password: form.password.value,
      phone: form.phone.value.trim() || null
    };

    setLoading(submitBtn, true, 'Creating account…');
    try {
      const data = await Api.post('/auth/register', payload);
      Auth.setSession(data);
      toast('Account created successfully', 'success');
      setTimeout(() => { window.location.href = '/dashboard.html'; }, 400);
    } catch (err) {
      showFieldErrors(form, err.fieldErrors);
      toast(err.message || 'Registration failed', 'error');
    } finally {
      setLoading(submitBtn, false);
    }
  });
}

function wireForgotForm() {
  const form = document.getElementById('forgot-form');
  if (!form) return;

  const submitBtn = form.querySelector('button[type="submit"]');
  submitBtn.dataset.originalLabel = submitBtn.innerHTML;
  const successBox = document.getElementById('forgot-success');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    setLoading(submitBtn, true, 'Sending…');
    try {
      await Api.post('/auth/forgot-password', { email: form.email.value.trim() });
      form.classList.add('hidden');
      if (successBox) successBox.classList.remove('hidden');
    } catch (err) {
      toast(err.message || 'Something went wrong', 'error');
    } finally {
      setLoading(submitBtn, false);
    }
  });
}

function wireResetForm() {
  const form = document.getElementById('reset-form');
  if (!form) return;

  const successBox = document.getElementById('reset-success');
  const invalidBox = document.getElementById('reset-invalid');
  const footerLink = document.getElementById('reset-footer-link');

  const params = new URLSearchParams(window.location.search);
  const token = params.get('token');

  // No token in the URL at all: this isn't a valid reset link, so don't even show the form.
  if (!token) {
    form.classList.add('hidden');
    if (footerLink) footerLink.classList.add('hidden');
    if (invalidBox) invalidBox.classList.remove('hidden');
    return;
  }

  const submitBtn = form.querySelector('button[type="submit"]');
  submitBtn.dataset.originalLabel = submitBtn.innerHTML;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    if (form.newPassword.value !== form.confirmPassword.value) {
      showFieldErrors(form, { confirmPassword: 'Passwords do not match' });
      toast('Passwords do not match', 'error');
      return;
    }

    setLoading(submitBtn, true, 'Resetting…');
    try {
      await Api.post('/auth/reset-password', { token, newPassword: form.newPassword.value });
      form.classList.add('hidden');
      if (footerLink) footerLink.classList.add('hidden');
      if (successBox) successBox.classList.remove('hidden');
    } catch (err) {
      // Expired/used/invalid token: show the same "request a new link" state as a missing token.
      form.classList.add('hidden');
      if (footerLink) footerLink.classList.add('hidden');
      if (invalidBox) invalidBox.classList.remove('hidden');
      toast(err.message || 'This reset link is invalid or expired', 'error');
    } finally {
      setLoading(submitBtn, false);
    }
  });
}
