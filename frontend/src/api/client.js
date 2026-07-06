import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('billsplit_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export const authApi = {
  register: (data) => api.post('/api/auth/register', data),
  login: (data) => api.post('/api/auth/login', data),
};

export const groupApi = {
  create: (data) => api.post('/api/groups', data),
  myGroups: () => api.get('/api/groups'),
  addMember: (groupId, data) => api.post(`/api/groups/${groupId}/members`, data),
  members: (groupId) => api.get(`/api/groups/${groupId}/members`),
};

export const expenseApi = {
  add: (data) => api.post('/api/expenses', data),
  byGroup: (groupId) => api.get(`/api/expenses/group/${groupId}`),
  balances: (groupId) => api.get(`/api/expenses/group/${groupId}/balances`),
};

export const settlementApi = {
  suggestions: (groupId) => api.get(`/api/settlements/group/${groupId}/suggestions`),
  record: (data) => api.post('/api/settlements', data),
  history: (groupId) => api.get(`/api/settlements/group/${groupId}`),
};

export const activityApi = {
  recent: () => api.get('/api/activity/recent'),
};

export default api;
