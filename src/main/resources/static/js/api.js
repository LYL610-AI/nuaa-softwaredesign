/* ===== API 请求封装 ===== */

// 部署时改为相对路径，由 Nginx 反向代理到后端 Tomcat
const API_BASE = '/api';

// 通用请求方法
async function request(url, options = {}) {
  const token = localStorage.getItem('token');

  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
      ...options.headers
    },
    ...options
  };

  if (config.body && typeof config.body === 'object') {
    config.body = JSON.stringify(config.body);
  }

  const response = await fetch(`${API_BASE}${url}`, config);
  const result = await response.json();

  if (result.code !== 200) {
    throw new Error(result.message || '请求失败');
  }

  return result.data;
}

// 快捷方法
const api = {
  get(url, params = {}) {
    const filtered = {};
    for (const [k, v] of Object.entries(params)) {
      if (v !== undefined && v !== null && v !== '') filtered[k] = v;
    }
    const query = new URLSearchParams(filtered).toString();
    const fullUrl = query ? `${url}?${query}` : url;
    return request(fullUrl, { method: 'GET' });
  },

  post(url, body = {}) {
    return request(url, { method: 'POST', body });
  },

  put(url, body = {}) {
    return request(url, { method: 'PUT', body });
  },

  delete(url) {
    return request(url, { method: 'DELETE' });
  }
};

/* ===== 各模块 API ===== */

// 用户
const userApi = {
  login:    (phone, password, role) => api.post('/user/login', { phone, password, role }),
  register: (data) => api.post('/user/register', data),
  checkPhone: (phone) => api.get('/user/checkPhone', { phone }),
  checkIdNumber: (idNumber) => api.get('/user/checkIdNumber', { idNumber }),
  checkLicense: (license) => api.get('/user/checkLicense', { license }),
  getInfo:  () => api.get('/user/info'),
  update:   (data) => api.put('/user/update', data),
  changePwd: (oldPwd, newPwd) => api.put('/user/password', { oldPwd, newPwd }),
  adminUpdate: (userId, data) => api.put(`/user/admin-update/${userId}`, data),
  recoverPassword: (data) => api.post('/user/recover-password', data)
};

// 活动
const activityApi = {
  list:   (params) => api.get('/activity/list', params),  // { keyword, region, state, page, pageSize }
  detail: (id) => api.get(`/activity/detail/${id}`),
  create: (data) => api.post('/activity/create', data),
  update: (id, data) => api.put(`/activity/update/${id}`, data),
  review: (id, auditState, reason) => api.put(`/activity/review/${id}`, { auditState, reason }),
  changeState: (id, activityState) => api.put(`/activity/state/${id}`, { activityState }),
  delete: (id) => api.delete(`/activity/delete/${id}`),
  submitSummary: (id, data) => api.post(`/activity/summary/${id}`, data),
  reviewSummary: (id, auditState) => api.put(`/activity/summary/review/${id}`, { auditState })
};

// 报名
const registrationApi = {
  submit: (data) => api.post('/registration/submit', data),
  cancel: (id) => api.delete(`/registration/cancel/${id}`),
  list:   (activityId) => api.get(`/registration/list/${activityId}`),
  review: (id, auditState) => api.put(`/registration/review/${id}`, { auditState }),
  check:  (activityId) => api.get(`/registration/check/${activityId}`),
  count:  (activityId) => api.get(`/registration/count/${activityId}`)
};

// 帖子
const postApi = {
  list:   (params) => api.get('/post/list', params),
  detail: (id) => api.get(`/post/detail/${id}`),
  create: (data) => api.post('/post/create', data),
  delete: (id) => api.delete(`/post/delete/${id}`),
  review: (id, auditState) => api.put(`/post/review/${id}`, { auditState })
};

// 评论
const commentApi = {
  list:   (postId) => api.get(`/comment/list/${postId}`),
  create: (data) => api.post('/comment/create', data),
  delete: (id) => api.delete(`/comment/delete/${id}`)
};

// 文件上传
const fileApi = {
  upload: async (file) => {
    const token = localStorage.getItem('token');
    const formData = new FormData();
    formData.append('file', file);
    const response = await fetch(`${API_BASE}/file/upload`, {
      method: 'POST',
      headers: token ? { 'Authorization': `Bearer ${token}` } : {},
      body: formData
    });
    const result = await response.json();
    if (result.code !== 200) {
      throw new Error(result.message || '上传失败');
    }
    return result.data.url;
  }
};
