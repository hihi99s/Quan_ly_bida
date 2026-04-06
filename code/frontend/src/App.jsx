import { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './Layout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import ReportsPage from './pages/ReportsPage';
import TablesPage from './pages/TablesPage';
import ProductsPage from './pages/ProductsPage';
import CustomersPage from './pages/CustomersPage';
import InvoicesPage from './pages/InvoicesPage';
import UsersPage from './pages/UsersPage';
import StaffSchedulePage from './pages/StaffSchedulePage';
import MySchedulesPage from './pages/MySchedulesPage';
import PricesPage from './pages/PricesPage';
import HolidaysPage from './pages/HolidaysPage';
import DiscountsPage from './pages/DiscountsPage';
import ReservationsPage from './pages/ReservationsPage';
import PlaceholderPage from './pages/PlaceholderPage';
import './index.css';

export default function App() {
  const [user, setUser] = useState(null);
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    fetch('/api/auth/me', { credentials: 'include' })
      .then(r => r.json())
      .then(data => {
        if (data.authenticated) setUser(data);
      })
      .catch(() => {})
      .finally(() => setChecking(false));
  }, []);

  if (checking) {
    return (
      <div className="min-h-screen bg-navy-900 flex items-center justify-center">
        <div className="w-12 h-12 border-4 border-cyan/30 border-t-cyan rounded-full animate-spin" />
      </div>
    );
  }

  function handleLogin(data) {
    setUser({ authenticated: true, id: data.id, username: data.username, role: data.role });
  }

  function handleLogout() {
    fetch('/api/auth/logout', { method: 'POST', credentials: 'include' })
      .finally(() => setUser(null));
  }

  if (!user) {
    return (
      <BrowserRouter>
        <Routes>
          <Route path="*" element={<LoginPage onLogin={handleLogin} />} />
        </Routes>
      </BrowserRouter>
    );
  }

  function AdminRoute({ user, children }) {
    if (user?.role !== 'ADMIN') return <Navigate to="/" replace />;
    return children;
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout user={user} onLogout={handleLogout} />}>
          <Route index element={<DashboardPage />} />
          <Route path="tables" element={<TablesPage user={user} />} />
          <Route path="reports" element={<AdminRoute user={user}><ReportsPage /></AdminRoute>} />
          <Route path="products" element={<AdminRoute user={user}><ProductsPage /></AdminRoute>} />
          <Route path="invoices" element={<InvoicesPage />} />
          <Route path="customers" element={<CustomersPage />} />
          <Route path="staff" element={<AdminRoute user={user}><UsersPage /></AdminRoute>} />
          <Route path="schedules" element={<AdminRoute user={user}><StaffSchedulePage /></AdminRoute>} />
          <Route path="my-schedules" element={<MySchedulesPage user={user} />} />
          <Route path="prices" element={<AdminRoute user={user}><PricesPage /></AdminRoute>} />
          <Route path="holidays" element={<AdminRoute user={user}><HolidaysPage /></AdminRoute>} />
          <Route path="discounts" element={<AdminRoute user={user}><DiscountsPage /></AdminRoute>} />
          <Route path="reservations" element={<ReservationsPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
