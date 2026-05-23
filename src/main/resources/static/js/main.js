/* ===== 公共工具函数 ===== */

// 角色权限映射
const ROLE = {
  VOLUNTEER: 1,
  SCHOOL: 2,
  ADMIN: 3
};

const ROLE_NAME = { 1: '志愿者', 2: '学校用户', 3: '管理员' };

// ===== 用户认证 =====
function getToken() {
  return localStorage.getItem('token');
}

function setToken(token) {
  localStorage.setItem('token', token);
}

function getCurrentUser() {
  const user = localStorage.getItem('currentUser');
  return user ? JSON.parse(user) : null;
}

function setCurrentUser(user) {
  localStorage.setItem('currentUser', JSON.stringify(user));
}

function isLoggedIn() {
  return !!getToken();
}

function getUserPermission() {
  const user = getCurrentUser();
  return user ? user.userPermission : null;
}

function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('currentUser');
  window.location.href = '../index.html';
}

// ===== Toast 提示 =====
function showToast(message, type = 'success') {
  const existing = document.querySelector('.toast');
  if (existing) existing.remove();

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  document.body.appendChild(toast);

  setTimeout(() => toast.remove(), 2800);
}

// ===== 模态框 =====
function showModal(title, contentHtml, onConfirm) {
  const overlay = document.createElement('div');
  overlay.className = 'modal-overlay show';
  overlay.innerHTML = `
    <div class="modal">
      <div class="modal-header">
        <h3 class="modal-title">${title}</h3>
        <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">&times;</button>
      </div>
      <div class="modal-body">${contentHtml}</div>
      <div class="modal-footer">
        <button class="btn btn-outline" onclick="this.closest('.modal-overlay').remove()">取消</button>
        <button class="btn btn-primary" id="modal-confirm">确认</button>
      </div>
    </div>
  `;
  document.body.appendChild(overlay);

  overlay.querySelector('#modal-confirm').addEventListener('click', () => {
    if (onConfirm) onConfirm();
    overlay.remove();
  });

  overlay.addEventListener('click', (e) => {
    if (e.target === overlay) overlay.remove();
  });
}

// ===== 页面加载时初始化导航栏 =====
function initHeader() {
  const headerPlaceholder = document.getElementById('header-placeholder');
  if (!headerPlaceholder) return;

  const currentPath = window.location.pathname;
  const pageName = currentPath.split('/').pop().replace('.html', '');

  const user = getCurrentUser();
  const isLogged = isLoggedIn();
  const permission = getUserPermission();

  let navLinks = '';
  let actionsHtml = '';

  // 未登录
  if (!isLogged) {
    navLinks = `
      <a href="../index.html" class="${pageName === 'index' || pageName === '' ? 'active' : ''}">首页</a>
      <a href="../pages/activities.html" class="${pageName === 'activities' || pageName === 'activity-detail' ? 'active' : ''}">支教活动</a>
      <a href="../pages/discussion.html" class="${pageName === 'discussion' ? 'active' : ''}">讨论区</a>
    `;
    actionsHtml = `
      <a href="../pages/login.html" class="btn btn-outline btn-sm">登录</a>
      <a href="../pages/register.html" class="btn btn-primary btn-sm">注册</a>
    `;
  }

  // 志愿者
  if (isLogged && permission === ROLE.VOLUNTEER) {
    navLinks = `
      <a href="../index.html" class="${pageName === 'index' || pageName === '' ? 'active' : ''}">首页</a>
      <a href="../pages/activities.html" class="${pageName === 'activities' || pageName === 'activity-detail' ? 'active' : ''}">支教活动</a>
      <a href="../pages/discussion.html" class="${pageName === 'discussion' ? 'active' : ''}">讨论区</a>
    `;
    actionsHtml = `
      <span style="font-size:14px;color:var(--text-secondary);">${user.userName || user.userId || '志愿者'}</span>
      <a href="../pages/personal-center.html" class="btn btn-outline btn-sm">个人中心</a>
      <button class="btn btn-outline btn-sm" onclick="logout()">退出</button>
    `;
  }

  // 学校用户
  if (isLogged && permission === ROLE.SCHOOL) {
    navLinks = `
      <a href="../index.html" class="${pageName === 'index' || pageName === '' ? 'active' : ''}">首页</a>
      <a href="../pages/activities.html" class="${pageName === 'activities' || pageName === 'activity-detail' ? 'active' : ''}">支教活动</a>
      <a href="../pages/discussion.html" class="${pageName === 'discussion' ? 'active' : ''}">讨论区</a>
      <a href="../pages/publish-activity.html" class="${pageName === 'publish-activity' ? 'active' : ''}">发布活动</a>
    `;
    actionsHtml = `
      <span style="font-size:14px;color:var(--text-secondary);">${user.schoolName || user.userId || '学校用户'}</span>
      <a href="../pages/personal-center.html" class="btn btn-outline btn-sm">个人中心</a>
      <button class="btn btn-outline btn-sm" onclick="logout()">退出</button>
    `;
  }

  // 管理员
  if (isLogged && permission === ROLE.ADMIN) {
    navLinks = `
      <a href="../index.html" class="${pageName === 'index' || pageName === '' ? 'active' : ''}">首页</a>
      <a href="../pages/activities.html" class="${pageName === 'activities' || pageName === 'activity-detail' ? 'active' : ''}">支教活动</a>
      <a href="../pages/discussion.html" class="${pageName === 'discussion' ? 'active' : ''}">讨论区</a>
      <a href="../pages/admin-review.html" class="${pageName === 'admin-review' ? 'active' : ''}">审核管理</a>
      <a href="../pages/admin-users.html" class="${pageName === 'admin-users' ? 'active' : ''}">用户管理</a>
    `;
    actionsHtml = `
      <span style="font-size:14px;color:var(--text-secondary);">${user.userId || '管理员'}</span>
      <a href="../pages/personal-center.html" class="btn btn-outline btn-sm">个人中心</a>
      <button class="btn btn-outline btn-sm" onclick="logout()">退出</button>
    `;
  }

  headerPlaceholder.innerHTML = `
    <header class="header">
      <div class="header-inner">
        <a href="../index.html" class="header-logo">
          <div class="logo-icon">&#9998;</div>
          <span>支教信息平台</span>
        </a>
        <nav class="header-nav">${navLinks}</nav>
        <div class="header-actions">${actionsHtml}</div>
      </div>
    </header>
  `;
}

// ===== 密码可见切换 =====
const EYE_OPEN = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>';
const EYE_CLOSED = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>';

function initPasswordToggles() {
  document.querySelectorAll('.password-toggle').forEach(btn => {
    btn.addEventListener('click', () => {
      const input = btn.parentElement.querySelector('input');
      const isPassword = input.type === 'password';
      input.type = isPassword ? 'text' : 'password';
      btn.innerHTML = isPassword ? EYE_OPEN : EYE_CLOSED;
    });
  });
}

// ===== 页面初始化 =====
document.addEventListener('DOMContentLoaded', () => {
  initHeader();
  initPasswordToggles();
});
