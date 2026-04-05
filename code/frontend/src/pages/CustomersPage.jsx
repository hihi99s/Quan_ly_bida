import { useState, useEffect } from 'react';
import { customerApi, formatMoney } from '../api';

export default function CustomersPage() {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  // Form states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({ name: '', phone: '', email: '' });

  useEffect(() => {
    loadCustomers();
  }, []);

  async function loadCustomers() {
    try {
      const data = await customerApi.getAll();
      const tierMap = { DIAMOND: 5, GOLD: 4, SILVER: 3, BRONZE: 2, REGULAR: 1 };
      data.sort((a, b) => (tierMap[b.membershipTier] || 0) - (tierMap[a.membershipTier] || 0));
      setCustomers(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  function openCreateModal() {
    setEditingId(null);
    setFormData({ name: '', phone: '', email: '' });
    setIsModalOpen(true);
  }

  function openEditModal(c) {
    setEditingId(c.id);
    setFormData({ name: c.name, phone: c.phone, email: c.email || '' });
    setIsModalOpen(true);
  }

  async function handleDelete(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa khách hàng này? Mọi hóa đơn liên quan có thể bị ảnh hưởng!')) return;
    try {
      await customerApi.delete(id);
      loadCustomers();
    } catch (e) {
      alert(e.message);
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      if (editingId) {
        await customerApi.update(editingId, formData);
      } else {
        await customerApi.create(formData);
      }
      setIsModalOpen(false);
      loadCustomers();
    } catch (e) {
      alert(e.message);
    }
  }

  const renderTierBadge = (tier) => {
    const badges = {
      DIAMOND: 'bg-purple-500/20 text-purple-400 border-purple-500/30',
      GOLD: 'bg-amber-500/20 text-amber-400 border-amber-500/30',
      SILVER: 'bg-slate-300/20 text-slate-300 border-slate-300/30',
      BRONZE: 'bg-orange-800/20 text-orange-400 border-orange-800/30',
      REGULAR: 'bg-white/5 text-slate-400 border-white/10'
    };
    return (
      <span className={`px-2.5 py-1 rounded-md text-xs font-bold border ${badges[tier] || badges.REGULAR}`}>
        {tier}
      </span>
    );
  };

  const filtered = customers.filter(c => 
    search === '' || 
    c.name.toLowerCase().includes(search.toLowerCase()) || 
    c.phone.includes(search)
  );

  if (loading) return <div className="flex justify-center h-40 items-center"><div className="w-10 h-10 border-4 animate-spin border-t-cyan border-cyan/30 rounded-full" /></div>;

  return (
    <div className="space-y-6 animate-in">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <h2 className="text-2xl font-bold text-white flex items-center gap-2">👥 Q.Lý Khách Hàng</h2>
        <div className="flex flex-wrap gap-2 w-full sm:w-auto">
          <div className="relative flex-1 sm:w-72">
            <input 
              type="text" 
              placeholder="Tìm kiếm tên, SĐT..." 
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
            + Thêm Mới
          </button>
        </div>
      </div>

      <div className="glass-card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-white/5 border-b border-white/10 text-xs uppercase text-slate-400 font-bold">
                <th className="p-4 rounded-tl-xl">Họ Tên</th>
                <th className="p-4">Số điện thoại</th>
                <th className="p-4 text-center">Hạng thẻ</th>
                <th className="p-4 text-right">Điểm thưởng</th>
                <th className="p-4 text-right">Tổng chi tiêu</th>
                <th className="p-4 rounded-tr-xl text-center">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {filtered.map(c => (
                <tr key={c.id} className="hover:bg-white/5 transition-colors group">
                  <td className="p-4">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-gradient-to-br from-cyan/20 to-blue/20 flex items-center justify-center text-cyan-400 font-bold text-sm">
                        {c.name.charAt(0).toUpperCase()}
                      </div>
                      <span className="font-bold text-white">{c.name}</span>
                    </div>
                  </td>
                  <td className="p-4 text-slate-300 font-mono text-sm">{c.phone}</td>
                  <td className="p-4 text-center">{renderTierBadge(c.membershipTier)}</td>
                  <td className="p-4 text-right">
                    <span className="bg-emerald-500/10 text-emerald-400 px-2.5 py-1 rounded-full text-sm font-bold">
                      ★ {c.points}
                    </span>
                  </td>
                  <td className="p-4 text-right font-black text-white text-lg">
                    {formatMoney(c.totalSpent)}
                  </td>
                  <td className="p-4 text-center">
                    <div className="flex items-center justify-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                      <button onClick={() => openEditModal(c)} className="w-8 h-8 rounded bg-blue-500/20 text-blue-400 hover:bg-blue-500 hover:text-white flex items-center justify-center">✏️</button>
                      <button onClick={() => handleDelete(c.id)} className="w-8 h-8 rounded bg-red-500/20 text-red-400 hover:bg-red-500 hover:text-white flex items-center justify-center">🗑</button>
                    </div>
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr><td colSpan="6" className="p-8 text-center text-slate-500">Khách hàng không tồn tại.</td></tr>
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
              {editingId ? '✏ Cập nhật thông tin' : '+ Thêm khách hàng mới'}
            </h3>
            
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm text-slate-400 mb-1">Họ Tên *</label>
                <input required type="text" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white" />
              </div>
              <div>
                <label className="block text-sm text-slate-400 mb-1">Số điện thoại *</label>
                <input required type="text" value={formData.phone} onChange={e => setFormData({...formData, phone: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white font-mono" />
              </div>
              <div>
                <label className="block text-sm text-slate-400 mb-1">Email</label>
                <input type="email" value={formData.email} onChange={e => setFormData({...formData, email: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white" />
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
