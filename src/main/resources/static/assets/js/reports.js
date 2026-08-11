/* =========================================================
   reports.js
   ========================================================= */

const PALETTE = ['#22C55E', '#16A34A', '#86EFAC', '#4ADE80', '#15803D', '#BBF7D0', '#059669', '#34D399', '#065F46', '#A7F3D0'];

document.addEventListener('DOMContentLoaded', () => {
  renderShell('reports');
  loadReport();
  wireExport();
});

async function loadReport() {
  try {
    const data = await Api.get('/reports');
    renderComparisonStats(data);
    renderMonthlyTrend(data.monthlyExpenses || []);
    renderCategoryPie(data.categoryBreakdown || []);
    renderWeekly(data.weeklyExpenses || []);
    renderTopCategories(data.topSpendingCategories || []);
  } catch (err) {
    toast(err.message || 'Failed to load report', 'error');
  }
}

function renderComparisonStats(data) {
  const momUp = data.monthOverMonthChangePercent >= 0;
  const yoyUp = data.yearOverYearChangePercent >= 0;

  document.getElementById('comparison-stats').innerHTML = `
    <div class="card stat-card">
      <div class="stat-label">This Month</div>
      <div class="stat-value">${formatCurrency(data.currentMonthTotal)}</div>
      <div class="stat-trend ${momUp ? 'down' : 'up'}">${momUp ? '&#9650;' : '&#9660;'} ${Math.abs(data.monthOverMonthChangePercent).toFixed(1)}% vs last month</div>
    </div>
    <div class="card stat-card">
      <div class="stat-label">Last Month</div>
      <div class="stat-value">${formatCurrency(data.previousMonthTotal)}</div>
    </div>
    <div class="card stat-card">
      <div class="stat-label">This Year</div>
      <div class="stat-value">${formatCurrency(data.currentYearTotal)}</div>
      <div class="stat-trend ${yoyUp ? 'down' : 'up'}">${yoyUp ? '&#9650;' : '&#9660;'} ${Math.abs(data.yearOverYearChangePercent).toFixed(1)}% vs last year</div>
    </div>
    <div class="card stat-card">
      <div class="stat-label">Last Year</div>
      <div class="stat-value">${formatCurrency(data.previousYearTotal)}</div>
    </div>
  `;
}

let trendChart, pieChart, weeklyChart;

function renderMonthlyTrend(monthly) {
  const ctx = document.getElementById('monthly-trend-chart');
  if (trendChart) trendChart.destroy();
  trendChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels: monthly.map(m => formatMonthLabel(m.month)),
      datasets: [{
        label: 'Expenses',
        data: monthly.map(m => Number(m.total)),
        borderColor: '#22C55E',
        backgroundColor: 'rgba(34,197,94,0.12)',
        tension: 0.35,
        fill: true,
        pointRadius: 3,
        pointBackgroundColor: '#16A34A'
      }]
    },
    options: {
      responsive: true, maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: { y: { beginAtZero: true, grid: { color: '#F1F5F9' } }, x: { grid: { display: false } } }
    }
  });
}

function renderCategoryPie(categories) {
  const ctx = document.getElementById('category-pie-chart');
  if (pieChart) pieChart.destroy();
  if (!categories.length) {
    ctx.parentElement.innerHTML = `<div class="empty-state"><div class="icon">&#128202;</div><h4>No data yet</h4><p>Add expenses this month to see the breakdown.</p></div>`;
    return;
  }
  pieChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: categories.map(c => c.categoryName),
      datasets: [{
        data: categories.map(c => Number(c.total)),
        backgroundColor: categories.map((c, i) => c.color || PALETTE[i % PALETTE.length]),
        borderWidth: 2,
        borderColor: '#ffffff'
      }]
    },
    options: {
      responsive: true, maintainAspectRatio: false,
      plugins: { legend: { position: 'bottom', labels: { boxWidth: 10, font: { size: 11 } } } }
    }
  });
}

function renderWeekly(weekly) {
  const ctx = document.getElementById('weekly-chart');
  if (weeklyChart) weeklyChart.destroy();
  weeklyChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: weekly.map(w => formatDate(w.month)),
      datasets: [{ data: weekly.map(w => Number(w.total)), backgroundColor: '#86EFAC', borderRadius: 5, maxBarThickness: 24 }]
    },
    options: {
      responsive: true, maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: { y: { beginAtZero: true, grid: { color: '#F1F5F9' } }, x: { grid: { display: false }, ticks: { font: { size: 10 } } } }
    }
  });
}

function renderTopCategories(list) {
  const el = document.getElementById('top-categories-list');
  if (!list.length) {
    el.innerHTML = `<div class="empty-state"><div class="icon">&#127942;</div><h4>Nothing to rank yet</h4></div>`;
    return;
  }
  el.innerHTML = list.map((c, i) => `
    <div class="mb-16">
      <div class="flex justify-between items-center mb-8">
        <span><strong>#${i + 1}</strong> &nbsp; ${escapeHtml(c.categoryName)}</span>
        <span class="mono">${formatCurrency(c.total)} <span class="text-secondary">(${c.percentage.toFixed(1)}%)</span></span>
      </div>
      <div class="progress-bar"><div class="fill" style="width:${Math.min(c.percentage, 100)}%;background:${c.color || '#22C55E'}"></div></div>
    </div>
  `).join('');
}

function wireExport() {
  document.getElementById('export-excel-btn').addEventListener('click', async () => {
    try { await Api.downloadFile('/export/excel', 'expenses-report.xlsx'); toast('Excel export ready', 'success'); }
    catch { toast('Export failed', 'error'); }
  });
  document.getElementById('export-pdf-btn').addEventListener('click', async () => {
    try { await Api.downloadFile('/export/pdf', 'expenses-report.pdf'); toast('PDF export ready', 'success'); }
    catch { toast('Export failed', 'error'); }
  });
}
