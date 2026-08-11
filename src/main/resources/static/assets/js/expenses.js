/* =========================================================
   expenses.js
   ========================================================= */

const state = {
  page: 0,
  size: 10,
  sortBy: 'expenseDate',
  sortDir: 'desc',
  totalPages: 1,
  categories: []
};

document.addEventListener('DOMContentLoaded', async () => {
  renderShell('expenses');
  await loadCategories();
  wireFilters();
  wireModal();
  applyUrlParams();
  loadExpenses();
});

function applyUrlParams() {
  const params = new URLSearchParams(window.location.search);
  if (params.get('q')) document.getElementById('f-keyword').value = params.get('q');
  if (params.get('new') === '1') openExpenseModal();
}

async function loadCategories() {
  try {
    state.categories = await Api.get('/categories');
    const filterSelect = document.getElementById('f-category');
    const formSelect = document.getElementById('exp-category');
    const options = state.categories.map(c => `<option value="${c.categoryId}">${escapeHtml(c.categoryName)}</option>`).join('');
    filterSelect.innerHTML = '<option value="">All Categories</option>' + options;
    formSelect.innerHTML = options;
  } catch (err) {
    toast('Failed to load categories', 'error');
  }
}

function buildQuery() {
  const params = new URLSearchParams();
  const keyword = document.getElementById('f-keyword').value.trim();
  const categoryId = document.getElementById('f-category').value;
  const paymentMethod = document.getElementById('f-payment').value;
  const startDate = document.getElementById('f-start').value;
  const endDate = document.getElementById('f-end').value;
  const minAmount = document.getElementById('f-min').value;
  const maxAmount = document.getElementById('f-max').value;

  if (keyword) params.set('keyword', keyword);
  if (categoryId) params.set('categoryId', categoryId);
  if (paymentMethod) params.set('paymentMethod', paymentMethod);
  if (startDate) params.set('startDate', startDate);
  if (endDate) params.set('endDate', endDate);
  if (minAmount) params.set('minAmount', minAmount);
  if (maxAmount) params.set('maxAmount', maxAmount);
  params.set('sortBy', state.sortBy);
  params.set('sortDir', state.sortDir);
  params.set('page', state.page);
  params.set('size', state.size);
  return params.toString();
}

async function loadExpenses() {
  const tbody = document.getElementById('expenses-tbody');
  tbody.innerHTML = `<tr><td colspan="6"><div class="skeleton" style="height:20px;"></div></td></tr>`;
  try {
    const data = await Api.get(`/expenses?${buildQuery()}`);
    state.totalPages = data.totalPages;
    renderTable(data.content);
    renderPagination(data);
    updateSortHeaders();
  } catch (err) {
    toast(err.message || 'Failed to load expenses', 'error');
  }
}

function renderTable(list) {
  const tbody = document.getElementById('expenses-tbody');
  if (!list.length) {
    tbody.innerHTML = `<tr><td colspan="6"><div class="empty-state"><div class="icon">&#128179;</div><h4>No expenses found</h4><p>Try adjusting your filters or add a new expense.</p></div></td></tr>`;
    return;
  }
  tbody.innerHTML = list.map(e => `
    <tr>
      <td>${formatDate(e.expenseDate)}</td>
      <td>
        <strong>${escapeHtml(e.title)}</strong>
        ${e.description ? `<div class="text-secondary" style="font-size:12.5px;">${escapeHtml(e.description)}</div>` : ''}
      </td>
      <td><span class="badge" style="background:${e.categoryColor}22;color:${e.categoryColor}"><span class="badge-dot"></span>${escapeHtml(e.categoryName)}</span></td>
      <td class="amount" style="text-align:right;">${formatCurrency(e.amount)}</td>
      <td>${paymentMethodLabel(e.paymentMethod)}</td>
      <td>
        <div class="row-actions">
          <button class="btn-icon" title="View" onclick="viewExpense(${e.expenseId})">&#128065;</button>
          <button class="btn-icon" title="Edit" onclick="editExpense(${e.expenseId})">&#9998;</button>
          <button class="btn-icon" title="Delete" onclick="deleteExpense(${e.expenseId})">&#128465;</button>
        </div>
      </td>
    </tr>
  `).join('');
}

function renderPagination(data) {
  document.getElementById('pg-info').textContent =
    `Page ${data.pageNumber + 1} of ${Math.max(data.totalPages, 1)} · ${data.totalElements} total`;
  document.getElementById('pg-prev').disabled = data.pageNumber === 0;
  document.getElementById('pg-next').disabled = data.last;
}

function updateSortHeaders() {
  document.querySelectorAll('th[data-sort]').forEach(th => {
    th.classList.toggle('sorted', th.dataset.sort === state.sortBy);
  });
}

function wireFilters() {
  const debouncedReload = debounce(() => { state.page = 0; loadExpenses(); }, 400);

  ['f-keyword', 'f-min', 'f-max'].forEach(id => {
    document.getElementById(id).addEventListener('input', debouncedReload);
  });
  ['f-category', 'f-payment', 'f-start', 'f-end'].forEach(id => {
    document.getElementById(id).addEventListener('change', () => { state.page = 0; loadExpenses(); });
  });

  document.getElementById('clear-filters-btn').addEventListener('click', () => {
    ['f-keyword', 'f-category', 'f-payment', 'f-start', 'f-end', 'f-min', 'f-max'].forEach(id => document.getElementById(id).value = '');
    state.page = 0;
    loadExpenses();
  });

  document.getElementById('pg-prev').addEventListener('click', () => { if (state.page > 0) { state.page--; loadExpenses(); } });
  document.getElementById('pg-next').addEventListener('click', () => { if (state.page < state.totalPages - 1) { state.page++; loadExpenses(); } });

  document.querySelectorAll('th[data-sort]').forEach(th => {
    th.addEventListener('click', () => {
      const col = th.dataset.sort;
      if (state.sortBy === col) {
        state.sortDir = state.sortDir === 'asc' ? 'desc' : 'asc';
      } else {
        state.sortBy = col;
        state.sortDir = 'asc';
      }
      loadExpenses();
    });
  });

  document.getElementById('export-excel-btn').addEventListener('click', async () => {
    try { await Api.downloadFile('/export/excel', 'expenses.xlsx'); toast('Excel export ready', 'success'); }
    catch { toast('Export failed', 'error'); }
  });
  document.getElementById('export-pdf-btn').addEventListener('click', async () => {
    try { await Api.downloadFile('/export/pdf', 'expenses.pdf'); toast('PDF export ready', 'success'); }
    catch { toast('Export failed', 'error'); }
  });
}

/* ---------------- Modal: Add / Edit ---------------- */
function wireModal() {
  const modal = document.getElementById('expense-modal');
  const form = document.getElementById('expense-form');

  document.getElementById('add-expense-btn').addEventListener('click', () => openExpenseModal());
  document.getElementById('expense-modal-close').addEventListener('click', closeExpenseModal);
  document.getElementById('expense-cancel-btn').addEventListener('click', closeExpenseModal);
  modal.addEventListener('click', (e) => { if (e.target === modal) closeExpenseModal(); });

  document.getElementById('details-modal-close').addEventListener('click', () => {
    document.getElementById('details-modal').classList.remove('open');
  });

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
      title: form.title.value.trim(),
      amount: parseFloat(form.amount.value),
      categoryId: parseInt(form.categoryId.value, 10),
      paymentMethod: form.paymentMethod.value,
      description: form.description.value.trim() || null,
      notes: form.notes.value.trim() || null,
      expenseDate: form.expenseDate.value
    };

    const expenseId = form.expenseId.value;
    const submitBtn = form.querySelector('button[type="submit"]');
    submitBtn.disabled = true;

    try {
      if (expenseId) {
        await Api.put(`/expenses/${expenseId}`, payload);
        toast('Expense updated', 'success');
      } else {
        await Api.post('/expenses', payload);
        toast('Expense added', 'success');
      }
      closeExpenseModal();
      loadExpenses();
    } catch (err) {
      showFieldErrors(form, err.fieldErrors);
      toast(err.message || 'Failed to save expense', 'error');
    } finally {
      submitBtn.disabled = false;
    }
  });
}

function openExpenseModal(expense = null) {
  const form = document.getElementById('expense-form');
  form.reset();
  document.getElementById('expense-modal-title').textContent = expense ? 'Edit Expense' : 'Add Expense';
  form.expenseId.value = expense ? expense.expenseId : '';

  if (expense) {
    form.title.value = expense.title;
    form.amount.value = expense.amount;
    form.categoryId.value = expense.categoryId;
    form.paymentMethod.value = expense.paymentMethod;
    form.description.value = expense.description || '';
    form.notes.value = expense.notes || '';
    form.expenseDate.value = expense.expenseDate;
  } else {
    form.expenseDate.value = new Date().toISOString().slice(0, 10);
  }

  document.getElementById('expense-modal').classList.add('open');
}

function closeExpenseModal() {
  document.getElementById('expense-modal').classList.remove('open');
}

async function editExpense(id) {
  try {
    const expense = await Api.get(`/expenses/${id}`);
    openExpenseModal(expense);
  } catch (err) {
    toast('Failed to load expense', 'error');
  }
}

async function viewExpense(id) {
  try {
    const e = await Api.get(`/expenses/${id}`);
    document.getElementById('details-body').innerHTML = `
      <div class="field"><label>Title</label><div>${escapeHtml(e.title)}</div></div>
      <div class="field-row">
        <div class="field"><label>Amount</label><div class="mono">${formatCurrency(e.amount)}</div></div>
        <div class="field"><label>Date</label><div>${formatDate(e.expenseDate)}</div></div>
      </div>
      <div class="field-row">
        <div class="field"><label>Category</label><div>${escapeHtml(e.categoryName)}</div></div>
        <div class="field"><label>Payment</label><div>${paymentMethodLabel(e.paymentMethod)}</div></div>
      </div>
      ${e.description ? `<div class="field"><label>Description</label><div>${escapeHtml(e.description)}</div></div>` : ''}
      ${e.notes ? `<div class="field"><label>Notes</label><div>${escapeHtml(e.notes)}</div></div>` : ''}
    `;
    document.getElementById('details-modal').classList.add('open');
  } catch (err) {
    toast('Failed to load expense details', 'error');
  }
}

async function deleteExpense(id) {
  if (!confirmDialog('Delete this expense? This cannot be undone.')) return;
  try {
    await Api.del(`/expenses/${id}`);
    toast('Expense deleted', 'success');
    loadExpenses();
  } catch (err) {
    toast(err.message || 'Failed to delete expense', 'error');
  }
}
