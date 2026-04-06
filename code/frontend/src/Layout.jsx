import { useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';

const navItems = [
  { to: '/', icon: '📊', label: 'Tổng Quan' },
  { to: '/tables', icon: '🎱', label: 'Quản Lý Bàn' },
  { to: '/reports', icon: '📈', label: 'Báo Cáo', adminOnly: true },
  { to: '/products', icon: '🍺', label: 'Sản Phẩm', adminOnly: true },
  { to: '/invoices', icon: '🧾', label: 'Lịch Sử Hóa Đơn' },
  { to: '/customers', icon: '👥', label: 'Khách Hàng' },
  { to: '/staff', icon: '👤', label: 'Quản Lý Nhân Sự', adminOnly: true },
  { to: '/schedules', icon: '📆', label: 'Lịch Làm Việc', adminOnly: true },
  { to: '/my-schedules', icon: '⏰', label: 'Lịch Làm Của Tôi' },
  { to: '/prices', icon: '💲', label: 'Bảng Giá Bida', adminOnly: true },
  { to: '/holidays', icon: '📅', label: 'Cấu Hình Ngày Lễ', adminOnly: true },
  { to: '/discounts', icon: '🏷️', label: 'Mã Giảm Giá', adminOnly: true },
  { to: '/reservations', icon: '📅', label: 'Đặt Bàn' },
];

export default function Layout({ user, onLogout }) {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="flex h-screen overflow-hidden bg-navy-900">
      {/* Mobile overlay */}
      {sidebarOpen && (
        <div className="fixed inset-0 z-40 bg-black/60 lg:hidden" onClick={() => setSidebarOpen(false)} />
      )}

      {/* Sidebar */}
      <aside className={`
        fixed inset-y-0 left-0 z-50 w-64 transform transition-transform duration-300 ease-in-out
        lg:relative lg:translate-x-0
        ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}
        bg-navy-800 border-r border-white/5 flex flex-col
      `}>
        {/* Logo */}
        <div className="flex items-center gap-3 px-5 py-5 border-b border-white/5">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-cyan to-emerald flex items-center justify-center text-xl">
            🎱
          </div>
          <div>
            <h1 className="text-lg font-bold text-white">Billiards Hall</h1>
            <p className="text-xs text-slate-400">Management</p>
          </div>
        </div>

        {/* Nav items */}
        <nav className="flex-1 overflow-y-auto py-4 px-3 space-y-1">
          {navItems.filter(item => !item.adminOnly || user.role === 'ADMIN').map(item => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              onClick={() => setSidebarOpen(false)}
              className={({ isActive }) =>
                `flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200
                ${isActive
                  ? 'bg-cyan/10 text-cyan border-l-3 border-cyan'
                  : 'text-slate-400 hover:text-white hover:bg-white/5'}`
              }
            >
              <span className="text-lg">{item.icon}</span>
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>

        {/* User info + Logout */}
        <div className="px-4 py-3 border-t border-white/5">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-8 h-8 rounded-full bg-gradient-to-br from-cyan to-purple flex items-center justify-center text-xs font-bold text-white">
              {user?.username?.charAt(0).toUpperCase() || 'A'}
            </div>
            <div className="flex-1 min-w-0">
              <div className="text-sm font-semibold text-white truncate">{user?.username}</div>
              <div className="text-xs text-slate-400">{user?.role}</div>
            </div>
          </div>
          <button
            onClick={onLogout}
            className="w-full py-2 rounded-lg text-sm font-medium text-red-400 hover:bg-red-500/10 border border-red-500/20 transition-colors"
          >
            🚪 Đăng xuất
          </button>
        </div>
      </aside>

      {/* Main content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Top bar */}
        <header className="flex items-center justify-between px-4 lg:px-6 py-3 bg-navy-800/50 border-b border-white/5 backdrop-blur-xl">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setSidebarOpen(true)}
              className="lg:hidden p-2 rounded-lg text-slate-400 hover:text-white hover:bg-white/5"
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>
            <h2 className="text-lg font-semibold text-white">Quản Lý Quán Bida</h2>
          </div>

          <div className="flex items-center gap-3">

            {/* User badge */}
            <div className="flex items-center gap-2 px-3 py-2 rounded-lg bg-white/5">
              <div className="w-7 h-7 rounded-full bg-gradient-to-br from-cyan to-purple flex items-center justify-center text-xs font-bold">
                {user?.username?.charAt(0).toUpperCase() || 'A'}
              </div>
              <span className="text-sm text-white hidden sm:block">{user?.username}</span>
            </div>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto p-4 lg:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
