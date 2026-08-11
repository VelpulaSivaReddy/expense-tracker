/* =========================================================
   layout.js — renders the sidebar + topbar app shell
   Every authenticated page calls renderShell('pageKey') on load.
   ========================================================= */

const NAV_ITEMS = [
  { key: 'dashboard', label: 'Dashboard', href: '/dashboard.html', icon: '&#9635;' },
  { key: 'expenses', label: 'Expenses', href: '/expenses.html', icon: '&#128179;' },
  { key: 'categories', label: 'Categories', href: '/categories.html', icon: '&#127991;' },
  { key: 'budgets', label: 'Budgets', href: '/budgets.html', icon: '&#127919;' },
  { key: 'reports', label: 'Reports', href: '/reports.html', icon: '&#128202;' },
  { key: 'profile', label: 'Profile & Settings', href: '/profile.html', icon: '&#128100;' }
];

function renderShell(activeKey) {
  Auth.requireAuth();
  const user = Auth.getUser() || {};

  const navHtml = NAV_ITEMS.map(item => `
    <a class="nav-link ${item.key === activeKey ? 'active' : ''}" href="${item.href}">
      <span class="icon">${item.icon}</span> ${item.label}
    </a>
  `).join('');

  const shell = document.createElement('div');
  shell.innerHTML = `
    <div class="sidebar-overlay" id="sidebar-overlay"></div>
    <aside class="sidebar" id="sidebar">
      <div class="brand">
        <span class="brand-mark">&#128176;</span> ExpenseTracker
      </div>
      <div class="nav-group">
        <div class="nav-label">Overview</div>
        ${navHtml}
      </div>
      <div class="logout-link">
        <a class="nav-link" href="#" id="logout-btn">
          <span class="icon">&#8618;</span> Logout
        </a>
      </div>
    </aside>
    <div class="main-col">
      <header class="topbar">
        <button class="icon-btn menu-toggle" id="menu-toggle" aria-label="Toggle menu">&#9776;</button>
        <div class="search-box">
          <span class="icon">&#128269;</span>
          <input type="text" id="global-search" placeholder="Search expenses…" autocomplete="off">
        </div>
        <div class="topbar-spacer"></div>
        <div class="topbar-actions">
          <button class="icon-btn" id="theme-toggle" title="Toggle dark mode">&#127769;</button>
          <button class="icon-btn" id="notif-btn" title="Notifications">
            &#128276;<span class="dot hidden" id="notif-dot"></span>
          </button>
          <div class="profile-chip" id="profile-chip" style="cursor:pointer">
            <span class="avatar" id="avatar-el">${initials(user.fullName)}</span>
            <span class="name">${escapeHtml(user.fullName || 'Account')}</span>
          </div>
        </div>
        <div class="dropdown-panel" id="notif-panel"></div>
      </header>
      <main class="main-content" id="main-content-slot"></main>
    </div>
  `;

  // Move any existing body content into the new main-content-slot
  const existingContent = Array.from(document.body.children);
  document.body.prepend(shell);
  const slot = document.getElementById('main-content-slot');
  existingContent.forEach(node => {
    if (node !== shell) slot.appendChild(node);
  });

  wireShellEvents();
  loadNotifications();
  applyStoredTheme();
}

function wireShellEvents() {
  document.getElementById('logout-btn').addEventListener('click', (e) => {
    e.preventDefault();
    if (confirmDialog('Log out of ExpenseTracker?')) Auth.logout();
  });

  document.getElementById('menu-toggle').addEventListener('click', () => {
    document.getElementById('sidebar').classList.toggle('open');
    document.getElementById('sidebar-overlay').classList.toggle('open');
  });
  document.getElementById('sidebar-overlay').addEventListener('click', () => {
    document.getElementById('sidebar').classList.remove('open');
    document.getElementById('sidebar-overlay').classList.remove('open');
  });

  document.getElementById('theme-toggle').addEventListener('click', () => {
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    document.documentElement.setAttribute('data-theme', isDark ? 'light' : 'dark');
    localStorage.setItem('et_theme', isDark ? 'light' : 'dark');
  });

  const notifBtn = document.getElementById('notif-btn');
  const notifPanel = document.getElementById('notif-panel');
  notifBtn.addEventListener('click', () => notifPanel.classList.toggle('open'));
  document.addEventListener('click', (e) => {
    if (!notifBtn.contains(e.target) && !notifPanel.contains(e.target)) {
      notifPanel.classList.remove('open');
    }
  });

  const search = document.getElementById('global-search');
  search.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && search.value.trim()) {
      window.location.href = `/expenses.html?q=${encodeURIComponent(search.value.trim())}`;
    }
  });
}

function applyStoredTheme() {
  const theme = localStorage.getItem('et_theme');
  if (theme) document.documentElement.setAttribute('data-theme', theme);
}

async function loadNotifications() {
  try {
    const budget = await Api.get('/budgets');
    const panel = document.getElementById('notif-panel');
    const items = [];
    if (budget.dailyExceeded) items.push({ text: 'Daily budget exceeded', type: 'warn' });
    if (budget.weeklyExceeded) items.push({ text: 'Weekly budget exceeded', type: 'warn' });
    if (budget.monthlyExceeded) items.push({ text: 'Monthly budget exceeded', type: 'warn' });

    if (items.length) {
      document.getElementById('notif-dot').classList.remove('hidden');
      panel.innerHTML = `<div class="dp-title">Budget alerts</div>` +
        items.map(i => `<div class="dp-item">&#9888; ${i.text}</div>`).join('');
    } else {
      panel.innerHTML = `<div class="dp-title">Notifications</div><div class="dp-empty">You're all caught up</div>`;
    }
  } catch { /* silent fail on notifications */ }
}
