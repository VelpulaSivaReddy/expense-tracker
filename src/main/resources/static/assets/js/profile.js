/* =========================================================
   profile.js
   ========================================================= */

document.addEventListener('DOMContentLoaded', () => {
  renderShell('profile');
  loadProfile();
  wireProfileForm();
  wirePasswordForm();
  wireAvatarUpload();
  wireDarkModeToggle();
});

async function loadProfile() {
  try {
    const user = await Api.get('/users/me');
    document.getElementById('p-fullname').value = user.fullName;
    document.getElementById('p-email').value = user.email;
    document.getElementById('p-phone').value = user.phone || '';
    renderAvatar(user);
  } catch (err) {
    toast(err.message || 'Failed to load profile', 'error');
  }
}

function renderAvatar(user) {
  const el = document.getElementById('profile-avatar');
  if (user.profileImage) {
    el.style.backgroundImage = `url(${user.profileImage})`;
    el.style.backgroundSize = 'cover';
    el.textContent = '';
  } else {
    el.textContent = initials(user.fullName);
  }
}

function wireProfileForm() {
  const form = document.getElementById('profile-form');
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = { fullName: form.fullName.value.trim(), phone: form.phone.value.trim() || null };
    const submitBtn = form.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    try {
      const updated = await Api.put('/users/me', payload);
      const stored = Auth.getUser() || {};
      stored.fullName = updated.fullName;
      localStorage.setItem('et_user', JSON.stringify(stored));
      toast('Profile updated', 'success');
    } catch (err) {
      showFieldErrors(form, err.fieldErrors);
      toast(err.message || 'Failed to update profile', 'error');
    } finally {
      submitBtn.disabled = false;
    }
  });
}

function wirePasswordForm() {
  const form = document.getElementById('password-form');
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = { currentPassword: form.currentPassword.value, newPassword: form.newPassword.value };
    const submitBtn = form.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    try {
      await Api.post('/auth/change-password', payload);
      toast('Password changed successfully', 'success');
      form.reset();
    } catch (err) {
      showFieldErrors(form, err.fieldErrors);
      toast(err.message || 'Failed to change password', 'error');
    } finally {
      submitBtn.disabled = false;
    }
  });
}

function wireAvatarUpload() {
  document.getElementById('avatar-input').addEventListener('change', async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const formData = new FormData();
    formData.append('file', file);

    try {
      const res = await fetch('/api/users/me/profile-image', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${Auth.getToken()}` },
        body: formData
      });
      const body = await res.json();
      if (!res.ok || body.success === false) throw new Error(body.message || 'Upload failed');
      renderAvatar(body.data);
      toast('Profile picture updated', 'success');
    } catch (err) {
      toast(err.message || 'Failed to upload photo', 'error');
    }
  });
}

function wireDarkModeToggle() {
  document.getElementById('dark-mode-toggle').addEventListener('click', () => {
    document.getElementById('theme-toggle').click();
  });
}
