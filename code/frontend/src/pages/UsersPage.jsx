import { useState, useEffect } from 'react';
import { userApi } from '../api';

export default function UsersPage() {
  const [staff, setStaff] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  // Form states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({ 
    username: '', fullName: '', password: '', role: 'STAFF', active: true, phone: '', email: ''
  });

  useEffect(() => {
    loadStaff();
  }, []);

  async function loadStaff() {
    try {
      const data = await userApi.getAll();
      setStaff(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  function openCreateModal() {
    setEditingId(null);
    setFormData({ username: '', fullName: '', password: '', role: 'STAFF', active: true, phone: '', email: '' });
    setIsModalOpen(true);
  }

  function openEditModal(u) {
    setEditingId(u.id);
    setFormData({ 
      username: u.username, 
      fullName: u.fullName, 
      password: '', // Không nạp password cũ
      role: u.role, 
      active: u.active, 
      phone: u.phone, 
      email: u.email 
    });
    setIsModalOpen(true);
  }

  async function handleDelete(id) {
    if (!confirm('Bạn có chắc chắn muốn Tạm khóa nhân viên này? Lịch sử hóa đơn vẫn sẽ được giữ.')) return;
    try {
      await userApi.delete(id);
      loadStaff();
    } catch (e) {
      alert(e.message);
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      if (editingId) {
        await userApi.update(editingId, formData);
      } else {
        if(!formData.password) {
            alert("Vui lòng nhập mật khẩu cho nhân viên mới");
            return;
        }
        await userApi.create(formData);
      }
      setIsModalOpen(false);
      loadStaff();
    } catch (e) {
      alert(e.message);
    }
  }

  const filtered = staff.filter(u => 
    search === '' || 
    u.fullName?.toLowerCase().includes(search.toLowerCase()) || 
    u.username?.toLowerCase().includes(search.toLowerCase())
  );

  if (loading) return <div className="flex justify-center h-40 items-center"><div className="w-10 h-10 border-4 animate-spin border-t-cyan border-cyan/30 rounded-full" /></div>;

  return (
    <div className="space-y-6 animate-in">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <h2 className="text-2xl font-bold text-white flex items-center gap-2">👤 Quản Lý Nhân Sự</h2>
        <div className="flex flex-wrap gap-2 w-full sm:w-auto">
          <div className="relative flex-1 sm:w-72">
            <input 
              type="text" 
              placeholder="Tìm kiếm tài khoản, tên..." 
              value={search}
              onChange={e => setSearch(e.target.value)}
              className="w-full bg-white/5 border border-white/10 rounded-lg pl-10 pr-4 py-2.5 text-white outline-none focus:border-cyan-500"
            />
            <span className="absolute left-3 top-2.5 text-slate-400">🔍</span>
          </div>
          <button 
            onClick={openCreateModal}
            className="px-4 py-2.5 bg-cyan-600 hover:bg-cyan-500 text-white font-bold rounded-lg whitespace-nowrap"
          >
            + Cấp Tài Khoản Mới
          </button>
        </div>
      </div>

      <div className="glass-card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-white/5 border-b border-white/10 text-xs uppercase text-slate-400 font-bold">
                <th className="p-4 rounded-tl-xl text-center">Trạng Thái</th>
                <th className="p-4">Tài khoản (Login)</th>
                <th className="p-4">Họ và Tên</th>
                <th className="p-4">Quyền hạn</th>
                <th className="p-4">Số điện thoại</th>
                <th className="p-4 rounded-tr-xl text-center">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {filtered.map(u => (
                <tr key={u.id} className={`transition-colors group ${u.active ? 'hover:bg-white/5' : 'bg-red-500/5 opacity-50 block-row'}`}>
                  <td className="p-4 text-center">
                    {u.active ? 
                        <span className="inline-block w-3 h-3 rounded-full bg-emerald-500 shadow-[0_0_10px_#10b981]" title="Đang hoạt động"></span> : 
                        <span className="inline-block w-3 h-3 rounded-full bg-red-500" title="Đã khóa"></span>}
                  </td>
                  <td className="p-4 font-mono font-bold text-cyan-400">{u.username}</td>
                  <td className="p-4 text-white font-bold">{u.fullName}</td>
                  <td className="p-4">
                    <span className={`px-2 py-1 text-xs font-bold rounded ${u.role === 'ADMIN' ? 'bg-purple-500/20 text-purple-400 border border-purple-500/30' : 'bg-blue-500/20 text-blue-400 border border-blue-500/30'}`}>
                      {u.role}
                    </span>
                  </td>
                  <td className="p-4 font-mono text-sm text-slate-300">{u.phone || '---'}</td>
                  <td className="p-4 text-center">
                    <div className="flex items-center justify-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                      <button onClick={() => openEditModal(u)} className="w-8 h-8 rounded bg-blue-500/20 text-blue-400 hover:bg-blue-500 hover:text-white flex items-center justify-center" title="Sửa thông tin">✏️</button>
                      {u.active && (
                          <button onClick={() => handleDelete(u.id)} className="w-8 h-8 rounded bg-red-500/20 text-red-400 hover:bg-red-500 hover:text-white flex items-center justify-center" title="Khóa tài khoản">🔒</button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr><td colSpan="6" className="p-8 text-center text-slate-500">Chưa có dữ liệu nhân viên.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-black/70 flex items-center justify-center p-4 animate-in">
          <div className="glass-card w-full max-w-md p-6 relative">
            <button onClick={() => setIsModalOpen(false)} className="absolute top-4 right-4 text-slate-400 hover:text-white text-xl">✕</button>
            <h3 className="text-xl font-bold text-white mb-6">
              {editingId ? '✏ Cập nhật tài khoản' : '👤 Cấp tài khoản mới'}
            </h3>
            
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                  <div className="col-span-2">
                    <label className="block text-sm text-slate-400 mb-1">Tài khoản (Tên đăng nhập) *</label>
                    <input required type="text" value={formData.username} disabled={!!editingId} onChange={e => setFormData({...formData, username: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white disabled:opacity-50" />
                  </div>
                  <div>
                    <label className="block text-sm text-slate-400 mb-1">Họ và Tên *</label>
                    <input required type="text" value={formData.fullName} onChange={e => setFormData({...formData, fullName: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white" />
                  </div>
                  <div>
                    <label className="block text-sm text-slate-400 mb-1">Số điện thoại</label>
                    <input type="text" value={formData.phone} onChange={e => setFormData({...formData, phone: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white" />
                  </div>
                  <div className="col-span-2">
                    <label className="block text-sm text-slate-400 mb-1">Mật khẩu {editingId ? '(Để trống nếu không đổi)' : '*'}</label>
                    <input type="password" value={formData.password} onChange={e => setFormData({...formData, password: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white" />
                  </div>
                  <div>
                    <label className="block text-sm text-slate-400 mb-1">Quyền hạn *</label>
                    <select value={formData.role} onChange={e => setFormData({...formData, role: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white">
                      <option value="STAFF">Staff (Nhân viên)</option>
                      <option value="ADMIN">Admin (Quản lý)</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm text-slate-400 mb-1">Trạng thái *</label>
                    <select value={formData.active} onChange={e => setFormData({...formData, active: e.target.value === 'true'})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white">
                      <option value="true">Đang làm việc (Mở khóa)</option>
                      <option value="false">Đã nghỉ việc (Khóa)</option>
                    </select>
                  </div>
              </div>
              
              <div className="pt-4 flex gap-3">
                <button type="button" onClick={() => setIsModalOpen(false)} className="flex-1 py-2.5 rounded-lg font-bold bg-white/10 text-white hover:bg-white/20">Hủy</button>
                <button type="submit" className="flex-1 py-2.5 rounded-lg font-bold bg-cyan-600 hover:bg-cyan-500 text-white">Lưu Lại</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
