/* =========================================================
   categories.js
   ========================================================= */

document.addEventListener('DOMContentLoaded', () => {
  renderShell('categories');
  loadCategories();
  wireCategoryModal();
});

async function loadCategories() {
  try {
    const categories = await Api.get('/categories');
    categoryLookup = Object.fromEntries(categories.map(c => [c.categoryId, c]));
    const defaults = categories.filter(c => c.isDefault);
    const custom = categories.filter(c => !c.isDefault);

    renderGrid('default-category-grid', defaults, false);
    renderGrid('custom-category-grid', custom, true);
  } catch (err) {
    toast(err.message || 'Failed to load categories', 'error');
  }
}

let categoryLookup = {};

function renderGrid(elId, list, editable) {
  const el = document.getElementById(elId);
  if (!list.length) {
    el.innerHTML = editable
      ? `<div class="empty-state" style="grid-column:1/-1;"><div class="icon">&#127991;</div><h4>No custom categories yet</h4><p>Create one to organize expenses your way.</p></div>`
      : `<div class="empty-state" style="grid-column:1/-1;">No default categories found.</div>`;
    return;
  }

  el.innerHTML = list.map(c => `
    <div class="card category-card hoverable">
      <div class="cat-icon" style="background:${c.color}22;color:${c.color}">&#127991;</div>
      <div>
        <div class="cat-name">${escapeHtml(c.categoryName)}</div>
        <div class="cat-meta">${editable ? 'Custom' : 'Default'}</div>
      </div>
      ${editable ? `
        <div class="cat-actions">
          <button class="btn-icon" title="Edit" data-edit-category="${c.categoryId}">&#9998;</button>
          <button class="btn-icon" title="Delete" data-delete-category="${c.categoryId}">&#128465;</button>
        </div>
      ` : ''}
    </div>
  `).join('');

  el.querySelectorAll('[data-edit-category]').forEach(btn => {
    btn.addEventListener('click', () => editCategory(categoryLookup[btn.dataset.editCategory]));
  });
  el.querySelectorAll('[data-delete-category]').forEach(btn => {
    btn.addEventListener('click', () => deleteCategory(Number(btn.dataset.deleteCategory)));
  });
}

function wireCategoryModal() {
  const modal = document.getElementById('category-modal');
  const form = document.getElementById('category-form');

  document.getElementById('add-category-btn').addEventListener('click', () => openCategoryModal());
  document.getElementById('category-modal-close').addEventListener('click', closeCategoryModal);
  document.getElementById('category-cancel-btn').addEventListener('click', closeCategoryModal);
  modal.addEventListener('click', (e) => { if (e.target === modal) closeCategoryModal(); });

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
      categoryName: form.categoryName.value.trim(),
      color: form.color.value,
      icon: form.icon.value.trim() || 'tag'
    };
    const categoryId = form.categoryId.value;
    const submitBtn = form.querySelector('button[type="submit"]');
    submitBtn.disabled = true;

    try {
      if (categoryId) {
        await Api.put(`/categories/${categoryId}`, payload);
        toast('Category updated', 'success');
      } else {
        await Api.post('/categories', payload);
        toast('Category created', 'success');
      }
      closeCategoryModal();
      loadCategories();
    } catch (err) {
      showFieldErrors(form, err.fieldErrors);
      toast(err.message || 'Failed to save category', 'error');
    } finally {
      submitBtn.disabled = false;
    }
  });
}

function openCategoryModal(category = null) {
  const form = document.getElementById('category-form');
  form.reset();
  document.getElementById('category-modal-title').textContent = category ? 'Edit Category' : 'New Category';
  form.categoryId.value = category ? category.categoryId : '';
  form.categoryName.value = category ? category.categoryName : '';
  form.color.value = category ? category.color : '#22C55E';
  form.icon.value = category ? category.icon : 'tag';
  document.getElementById('category-modal').classList.add('open');
}

function closeCategoryModal() {
  document.getElementById('category-modal').classList.remove('open');
}

function editCategory(category) {
  openCategoryModal(category);
}

async function deleteCategory(id) {
  if (!confirmDialog('Delete this category? Expenses using it must be reassigned first.')) return;
  try {
    await Api.del(`/categories/${id}`);
    toast('Category deleted', 'success');
    loadCategories();
  } catch (err) {
    toast(err.message || 'Failed to delete category', 'error');
  }
}
