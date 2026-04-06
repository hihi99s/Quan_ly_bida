const API_BASE = '/api';

async function request(url, options = {}) {
  const res = await fetch(`${API_BASE}${url}`, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(err.message || 'Lỗi server');
  }
  return res.json();
}

// ─── Dashboard ───
export const dashboardApi = {
  getSummary: () => request('/dashboard/summary'),
  getKpis: () => request('/dashboard/kpis'),
  getRevenueChart: () => request('/dashboard/revenue-chart'),
  getTopProducts: () => request('/dashboard/top-products'),
  getTopCustomers: () => request('/dashboard/top-customers'),
  getCustomerStats: () => request('/dashboard/customer-stats'),
  getReservations: () => request('/dashboard/reservations'),
  getStaffToday: () => request('/dashboard/staff-today'),
  getLowStock: () => request('/dashboard/low-stock'),
  getTableAnalytics: () => request('/dashboard/table-analytics'),
  getFull: () => request('/dashboard/full'),
};

// ─── Tables ───
export const tableApi = {
  getAll: () => request('/tables'),
  start: (id, customerId) => request(`/tables/${id}/start${customerId ? `?customerId=${customerId}` : ''}`, { method: 'POST' }),
  end: (id, discountCode, manualTableCharge) => {
    let url = `/tables/${id}/end?`;
    if (discountCode) url += `discountCode=${encodeURIComponent(discountCode)}&`;
    if (manualTableCharge) url += `manualTableCharge=${manualTableCharge}&`;
    return request(url, { method: 'POST' });
  },
  pause: (id) => request(`/tables/${id}/pause`, { method: 'POST' }),
  resume: (id) => request(`/tables/${id}/resume`, { method: 'POST' }),
  transfer: (id, targetId) => request(`/tables/${id}/transfer?targetTableId=${targetId}`, { method: 'POST' }),
  maintenance: (id, enable) => request(`/tables/${id}/maintenance?enable=${enable}`, { method: 'POST' }),
  getOrders: (tableId) => request(`/tables/${tableId}/orders`),
  addOrder: (tableId, productId, quantity) => request(`/tables/${tableId}/orders?productId=${productId}&quantity=${quantity}`, { method: 'POST' }),
  removeOrder: (orderItemId) => request(`/tables/orders/${orderItemId}`, { method: 'DELETE' }),
  create: (data) => request('/tables', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => request(`/tables/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => request(`/tables/${id}`, { method: 'DELETE' }),
};

// ─── Products ───
export const productApi = {
  getAll: () => request('/products'),
  create: (data) => request('/products', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => request(`/products/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => request(`/products/${id}`, { method: 'DELETE' }),
};

// ─── Invoices ───
export const invoiceApi = {
  getAll: () => request('/invoices'),
  getById: (id) => request(`/invoices/${id}`),
};

// ─── Users (Staff) ───
export const userApi = {
  getAll: () => request('/users'),
  create: (data) => request('/users', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => request(`/users/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => request(`/users/${id}`, { method: 'DELETE' }),
};

// ─── Prices (PriceRules) ───
export const priceApi = {
  getAll: () => request('/prices'),
  create: (data) => request('/prices', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => request(`/prices/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => request(`/prices/${id}`, { method: 'DELETE' }),
};

// ─── Holidays ───
export const holidayApi = {
  getAll: () => request('/holidays'),
  create: (data) => request('/holidays', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => request(`/holidays/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => request(`/holidays/${id}`, { method: 'DELETE' }),
};

// ─── Discounts ───
export const discountApi = {
  getAll: () => request('/discounts'),
  create: (data) => request('/discounts', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => request(`/discounts/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  toggle: (id) => request(`/discounts/${id}/toggle`, { method: 'PUT' }),
  delete: (id) => request(`/discounts/${id}`, { method: 'DELETE' }),
};

// ─── Reports ───
export const reportApi = {
  getKpis: () => request('/reports/kpis'),
  getDaily: () => request('/reports/daily'),
  getMonthly: () => request('/reports/monthly'),
  getTableTypes: () => request('/reports/table-types'),
  getTables: () => request('/reports/tables'),
  getStaff: () => request('/reports/staff'),
  getHeatmap: () => request('/reports/heatmap'),
  getProducts: () => request('/reports/products'),
};

// ─── Customers ───
export const customerApi = {
  getAll: () => request('/customers'),
  search: (keyword) => request(`/customers/search?keyword=${encodeURIComponent(keyword)}`),
  create: (data) => request('/customers', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => request(`/customers/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => request(`/customers/${id}`, { method: 'DELETE' }),
};

// ─── Reservations ───
export const reservationApi = {
  create: (data) => request('/reservations', { method: 'POST', body: JSON.stringify(data) }),
  cancel: (id) => request(`/reservations/${id}/cancel`, { method: 'POST' }),
  getPending: () => request('/reservations/pending'),
};

// ─── Staff & Shift Scheduling ───
export const staffApi = {
  // Shifts
  getShifts: () => request('/staff/shifts'),
  createShift: (data) => request('/staff/shifts', { method: 'POST', body: JSON.stringify(data) }),
  updateShift: (id, data) => request(`/staff/shifts/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteShift: (id) => request(`/staff/shifts/${id}`, { method: 'DELETE' }),
  
  // Schedules
  getSchedules: (date, weekStart) => {
    let url = '/staff/schedules?';
    if (date) url += `date=${date}&`;
    if (weekStart) url += `weekStart=${weekStart}&`;
    return request(url);
  },
  getSchedulesByUser: (userId, from, to) => 
    request(`/staff/schedules/user/${userId}?from=${from}&to=${to}`),
  assign: (data) => request('/staff/schedules', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => request(`/staff/schedules/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => request(`/staff/schedules/${id}`, { method: 'DELETE' }),
  bulkDelete: (from, to) => request(`/staff/schedules/bulk?from=${from}&to=${to}`, { method: 'DELETE' }),
  checkIn: (id) => request(`/staff/schedules/${id}/checkin`, { method: 'POST' }),
  checkOut: (id) => request(`/staff/schedules/${id}/checkout`, { method: 'POST' }),
  getLate: () => request('/staff/schedules/late'),
  
  // Audit
  getAuditLogs: () => request('/staff/audit-logs'),
};

export function formatMoney(amount) {
  if (amount == null || isNaN(amount)) return '0đ';
  return new Intl.NumberFormat('vi-VN').format(Math.round(amount)) + 'đ';
}

export function formatTime(minutes) {
  if (!minutes || minutes < 0) return '00:00';
  const h = Math.floor(minutes / 60);
  const m = Math.floor(minutes % 60);
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
}
