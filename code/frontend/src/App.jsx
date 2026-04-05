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
import StaffPage from './pages/StaffPage';
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
    setUser({ authenticated: true, username: data.username, role: data.role });
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

  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout user={user} onLogout={handleLogout} />}>
          <Route index element={<DashboardPage />} />
          <Route path="tables" element={<TablesPage />} />
          <Route path="reports" element={<ReportsPage />} />
          <Route path="products" element={<ProductsPage />} />
          <Route path="invoices" element={<InvoicesPage />} />
          <Route path="customers" element={<CustomersPage />} />
          <Route path="staff" element={<StaffPage />} />
          <Route path="prices" element={<PricesPage />} />
          <Route path="holidays" element={<HolidaysPage />} />
          <Route path="discounts" element={<DiscountsPage />} />
          <Route path="reservations" element={<ReservationsPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
