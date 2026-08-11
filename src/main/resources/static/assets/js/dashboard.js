/* =========================================================
   dashboard.js
   ========================================================= */

document.addEventListener('DOMContentLoaded', () => {
  renderShell('dashboard');
  greet();
  loadDashboard();
  loadBudgetRings();
  wireExport();
});

function greet() {
  const user = Auth.getUser();
  const hour = new Date().getHours();
  const part = hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening';
  const el = document.getElementById('greeting-heading');
  if (el && user) el.textContent = `${part}, ${user.fullName.split(' ')[0]}`;
}

async function loadDashboard() {
  try {
    const data = await Api.get('/dashboard');
    renderStats(data);
    renderMonthlyChart(data.monthlyOverview || []);
    renderRecentTransactions(data.recentTransactions || []);
  } catch (err) {
    toast(err.message || 'Failed to load dashboard', 'error');
  }
}

function renderStats(data) {
  const stats = [
    { label: 'Total Expenses', value: data.totalExpenses, icon: '&#128179;' },
    { label: "Today's Expenses", value: data.todayExpenses, icon: '&#9728;' },
    { label: 'This Week', value: data.weekExpenses, icon: '&#128197;' },
    { label: 'This Month', value: data.monthExpenses, icon: '&#128198;' },
  ];

  document.getElementById('stat-grid').innerHTML = stats.map(s => `
    <div class="card stat-card hoverable">
      <div class="stat-icon">${s.icon}</div>
      <div class="stat-label">${s.label}</div>
      <div class="stat-value">${formatCurrency(s.value)}</div>
    </div>
  `).join('') + `
    <div class="card stat-card hoverable">
      <div class="stat-icon">&#127919;</div>
      <div class="stat-label">Remaining Budget</div>
      <div class="stat-value" style="color:${Number(data.remainingBudget) < 0 ? 'var(--danger)' : 'var(--green-600)'}">${formatCurrency(data.remainingBudget)}</div>
    </div>
    <div class="card stat-card hoverable">
      <div class="stat-icon">&#127991;</div>
      <div class="stat-label">Total Categories</div>
      <div class="stat-value">${data.totalCategories}</div>
    </div>
  `;
}

let monthlyChartInstance = null;
function renderMonthlyChart(monthly) {
  const ctx = document.getElementById('monthly-chart');
  const labels = monthly.map(m => formatMonthLabel(m.month));
  const values = monthly.map(m => Number(m.total));

  if (monthlyChartInstance) monthlyChartInstance.destroy();
  monthlyChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels,
      datasets: [{
        label: 'Expenses',
        data: values,
        backgroundColor: '#22C55E',
        borderRadius: 6,
        maxBarThickness: 36
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: {
        y: { beginAtZero: true, grid: { color: '#F1F5F9' }, ticks: { callback: (v) => '₹' + v } },
        x: { grid: { display: false } }
      }
    }
  });
}

function renderRecentTransactions(list) {
  const tbody = document.getElementById('recent-tbody');
  if (!list.length) {
    tbody.innerHTML = `<tr><td colspan="5"><div class="empty-state"><div class="icon">&#128179;</div><h4>No transactions yet</h4><p>Add your first expense to see it here.</p></div></td></tr>`;
    return;
  }
  tbody.innerHTML = list.map(e => `
    <tr>
      <td>${formatDate(e.expenseDate)}</td>
      <td><strong>${escapeHtml(e.title)}</strong></td>
      <td><span class="badge" style="background:${e.categoryColor}22;color:${e.categoryColor}"><span class="badge-dot"></span>${escapeHtml(e.categoryName)}</span></td>
      <td>${paymentMethodLabel(e.paymentMethod)}</td>
      <td class="amount" style="text-align:right;">${formatCurrency(e.amount)}</td>
    </tr>
  `).join('');
}

async function loadBudgetRings() {
  try {
    const b = await Api.get('/budgets');
    const rings = [
      { label: 'Daily', pct: b.dailyPercentUsed, exceeded: b.dailyExceeded, remaining: b.dailyRemaining },
      { label: 'Weekly', pct: b.weeklyPercentUsed, exceeded: b.weeklyExceeded, remaining: b.weeklyRemaining },
      { label: 'Monthly', pct: b.monthlyPercentUsed, exceeded: b.monthlyExceeded, remaining: b.monthlyRemaining },
    ];
    document.getElementById('ring-row').innerHTML = rings.map(r => `
      <div class="ring-col">
        <div class="budget-ring ${r.exceeded ? 'exceeded' : ''}" style="--pct:${Math.min(r.pct, 100)}">
          <span class="ring-value">${Math.round(r.pct)}%</span>
        </div>
        <div class="ring-label">${r.label}<br>${formatCurrency(r.remaining)} left</div>
      </div>
    `).join('');

    if (b.dailyExceeded || b.weeklyExceeded || b.monthlyExceeded) {
      toast('You have exceeded one or more budgets', 'warn');
    }
  } catch (err) {
    document.getElementById('ring-row').innerHTML = `<p class="text-secondary">Set a budget to see progress here.</p>`;
  }
}

function wireExport() {
  document.getElementById('export-btn').addEventListener('click', async () => {
    try {
      await Api.downloadFile('/export/csv', 'expenses.csv');
      toast('Export started', 'success');
    } catch (err) {
      toast('Export failed', 'error');
    }
  });
}
