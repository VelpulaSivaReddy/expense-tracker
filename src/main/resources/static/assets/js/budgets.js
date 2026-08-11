/* =========================================================
   budgets.js
   ========================================================= */

document.addEventListener('DOMContentLoaded', () => {
  renderShell('budgets');
  loadBudgetStatus();
  wireBudgetForm();
});

async function loadBudgetStatus() {
  try {
    const b = await Api.get('/budgets');
    renderProgress(b);
    document.getElementById('b-daily').value = b.dailyBudget;
    document.getElementById('b-weekly').value = b.weeklyBudget;
    document.getElementById('b-monthly').value = b.monthlyBudget;
  } catch (err) {
    toast(err.message || 'Failed to load budget status', 'error');
  }
}

function renderProgress(b) {
  const rows = [
    { label: 'Daily', used: b.dailyUsed, budget: b.dailyBudget, pct: b.dailyPercentUsed, exceeded: b.dailyExceeded },
    { label: 'Weekly', used: b.weeklyUsed, budget: b.weeklyBudget, pct: b.weeklyPercentUsed, exceeded: b.weeklyExceeded },
    { label: 'Monthly', used: b.monthlyUsed, budget: b.monthlyBudget, pct: b.monthlyPercentUsed, exceeded: b.monthlyExceeded },
  ];

  document.getElementById('budget-progress-body').innerHTML = rows.map(r => `
    <div class="mb-16">
      <div class="flex justify-between items-center mb-8">
        <strong>${r.label}</strong>
        <span class="mono ${r.exceeded ? 'text-danger' : 'text-secondary'}">${formatCurrency(r.used)} / ${formatCurrency(r.budget)}</span>
      </div>
      <div class="progress-bar">
        <div class="fill ${r.exceeded ? 'exceeded' : ''}" style="width:${Math.min(r.pct, 100)}%"></div>
      </div>
      ${r.exceeded ? `<div class="text-danger mt-8" style="font-size:12.5px;font-weight:600;">&#9888; Budget exceeded by ${formatCurrency(Number(r.used) - Number(r.budget))}</div>` : ''}
    </div>
  `).join('');
}

function wireBudgetForm() {
  const form = document.getElementById('budget-form');
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
      dailyBudget: parseFloat(form.dailyBudget.value || 0),
      weeklyBudget: parseFloat(form.weeklyBudget.value || 0),
      monthlyBudget: parseFloat(form.monthlyBudget.value || 0)
    };
    const submitBtn = form.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    try {
      const updated = await Api.post('/budgets', payload);
      renderProgress(updated);
      toast('Budgets saved', 'success');
    } catch (err) {
      showFieldErrors(form, err.fieldErrors);
      toast(err.message || 'Failed to save budgets', 'error');
    } finally {
      submitBtn.disabled = false;
    }
  });
}
