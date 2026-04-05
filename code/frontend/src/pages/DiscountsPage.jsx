import { useState, useEffect } from 'react';
import { discountApi } from '../api';

export default function DiscountsPage() {
  const [discounts, setDiscounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  // Form states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({ 
    code: '', discountPercent: '', maxUsageCount: '', expiryDate: '' 
  });

  useEffect(() => {
    loadDiscounts();
  }, []);

  async function loadDiscounts() {
    try {
      const data = await discountApi.getAll();
      setDiscounts(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  function openCreateModal() {
    setEditingId(null);
    setFormData({ code: '', discountPercent: '', maxUsageCount: '', expiryDate: '' });
    setIsModalOpen(true);
  }

  function openEditModal(d) {
    setEditingId(d.id);
    setFormData({ 
      code: d.code, 
      discountPercent: d.discountPercent, 
      maxUsageCount: d.maxUsageCount || '', 
      expiryDate: d.expiryDate || '' 
    });
    setIsModalOpen(true);
  }

  async function handleToggle(id) {
    try {
      await discountApi.toggle(id);
      loadDiscounts();
    } catch (e) {
      alert(e.message);
    }
  }

  async function handleDelete(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa mã này? Nếu mã đã được khách dùng thì sẽ KHÔNG xóa được.')) return;
    try {
      await discountApi.delete(id);
      loadDiscounts();
    } catch (e) {
      alert(e.message);
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      const dataToSubmit = {
        code: formData.code,
        discountPercent: parseFloat(formData.discountPercent),
        maxUsageCount: formData.maxUsageCount ? parseInt(formData.maxUsageCount) : null,
        expiryDate: formData.expiryDate ? formData.expiryDate : null
      };

      if (editingId) {
        await discountApi.update(editingId, dataToSubmit);
      } else {
        await discountApi.create(dataToSubmit);
      }
      setIsModalOpen(false);
      loadDiscounts();
    } catch (e) {
      alert(e.message);
    }
  }

  const filtered = discounts.filter(d => search === '' || d.code.toLowerCase().includes(search.toLowerCase()));

  if (loading) return <div className="flex justify-center h-40 items-center"><div className="w-10 h-10 border-4 animate-spin border-t-cyan border-cyan/30 rounded-full" /></div>;

  return (
    <div className="space-y-6 animate-in">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <h2 className="text-2xl font-bold text-white flex items-center gap-2">🏷️ Quản Lý Mã Giảm Giá</h2>
        <div className="flex flex-wrap gap-2 w-full sm:w-auto">
          <div className="relative flex-1 sm:w-72">
            <input 
              type="text" 
              placeholder="Tìm theo mã code..." 
              value={search}
              onChange={e => setSearch(e.target.value)}
              className="w-full bg-white/5 border border-white/10 rounded-lg pl-10 pr-4 py-2.5 text-white outline-none focus:border-cyan-500 font-mono text-sm uppercase"
            />
            <span className="absolute left-3 top-2.5 text-slate-400">🔍</span>
          </div>
          <button 
            onClick={openCreateModal}
            className="px-4 py-2.5 bg-purple-600 hover:bg-purple-500 text-white font-bold rounded-lg whitespace-nowrap"
          >
            + Cấp Mã Mới
          </button>
        </div>
      </div>

      <div className="glass-card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-white/5 border-b border-white/10 text-xs uppercase text-slate-400 font-bold">
                <th className="p-4 rounded-tl-xl text-center">Trạng Thái</th>
                <th className="p-4">Mã Code</th>
                <th className="p-4 text-center">Tỷ lệ giảm (%)</th>
                <th className="p-4 text-center">Lượt dùng</th>
                <th className="p-4 text-center">Hạn sử dụng</th>
                <th className="p-4 rounded-tr-xl text-center">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {filtered.map(d => {
                const isExpired = d.expiryDate && new Date(d.expiryDate) < new Date(new Date().toDateString());
                const isUsedUp = d.maxUsageCount != null && d.usageCount >= d.maxUsageCount;
                const isUsable = d.active && !isExpired && !isUsedUp;

                return (
                  <tr key={d.id} className={`transition-colors group ${isUsable ? 'hover:bg-white/5' : 'bg-red-500/5 opacity-60 block-row'}`}>
                    <td className="p-4 text-center">
                      {isUsable ? 
                          <span className="inline-block px-2 py-1 bg-emerald-500/20 text-emerald-400 rounded text-xs font-bold shadow-[0_0_10px_#10b98140]" title="Đang có hiệu lực">HIỆU LỰC</span> : 
                          <span className="inline-block px-2 py-1 bg-red-500/20 text-red-400 rounded text-xs font-bold" title="Vô hiệu / Hết hạn">VÔ HIỆU</span>
                      }
                    </td>
                    <td className="p-4 text-purple-400 font-black tracking-widest text-lg font-mono">{d.code}</td>
                    <td className="p-4 text-center text-white font-black text-xl">-{parseFloat(d.discountPercent)}<span className="text-sm font-normal">%</span></td>
                    <td className="p-4 text-center">
                      <div className="flex flex-col items-center">
                        <span className="font-bold text-white">{d.usageCount} <span className="text-slate-400 font-normal">/ {d.maxUsageCount == null ? '∞' : d.maxUsageCount}</span></span>
                        {isUsedUp && <span className="text-[10px] text-red-400 bg-red-500/20 px-1 rounded uppercase">Hết lượt</span>}
                      </div>
                    </td>
                    <td className="p-4 text-center">
                      {d.expiryDate ? (
                        <div className="flex flex-col items-center">
                          <span className="font-mono text-slate-300">{new Date(d.expiryDate).toLocaleDateString('vi-VN')}</span>
                          {isExpired && <span className="text-[10px] text-red-400 bg-red-500/20 px-1 rounded uppercase">Hết hạn</span>}
                        </div>
                      ) : (
                        <span className="text-slate-500 font-mono">Vĩnh viễn</span>
                      )}
                    </td>
                    <td className="p-4 text-center">
                      <div className="flex items-center justify-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                        <button onClick={() => openEditModal(d)} className="w-8 h-8 rounded bg-blue-500/20 text-blue-400 hover:bg-blue-500 hover:text-white flex items-center justify-center" title="Sửa mã">✏️</button>
                        <button onClick={() => handleToggle(d.id)} className={`w-8 h-8 rounded ${d.active ? 'bg-orange-500/20 text-orange-400 hover:bg-orange-500' : 'bg-emerald-500/20 text-emerald-400 hover:bg-emerald-500'} hover:text-white flex items-center justify-center`} title={d.active ? "Tạm khóa mã" : "Mở khóa mã"}>
                          {d.active ? '🔒' : '🔓'}
                        </button>
                        <button onClick={() => handleDelete(d.id)} className="w-8 h-8 rounded bg-red-500/20 text-red-400 hover:bg-red-500 hover:text-white flex items-center justify-center" title="Xóa mã (nếu chưa ai dùng)">🗑</button>
                      </div>
                    </td>
                  </tr>
                );
              })}
              {filtered.length === 0 && (
                <tr><td colSpan="6" className="p-8 text-center text-slate-500">Chưa có mã giảm giá nào.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="p-4 bg-purple-500/10 border border-purple-500/20 rounded-xl text-purple-200 text-sm flex gap-3 mt-4">
        <span className="text-xl">⚠️</span>
        <div>
          <strong className="block text-purple-400 mb-1">Cơ chế bảo vệ dữ liệu:</strong>
          Đối với mã Giảm giá ĐÃ ĐƯỢC ÁP DỤNG TRÊN HÓA ĐƠN thì bạn sẽ không được phép Xóa (Vì ảnh hưởng lưu vết Lịch sử Hóa đơn). Thay vì xóa, bạn có thể ấn nút 🔒 Tạm Khóa. Mã sẽ tự động hết hiệu lực nếu bị Khóa, Hết Lượt hoặc Hết hạn Ngày.
        </div>
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-black/70 flex items-center justify-center p-4 animate-in">
          <div className="glass-card w-full max-w-md p-6 relative flex flex-col max-h-[90vh]">
            <button onClick={() => setIsModalOpen(false)} className="absolute top-4 right-4 text-slate-400 hover:text-white text-xl">✕</button>
            <h3 className="text-xl font-bold text-white mb-6">
              {editingId ? '✏ Cập nhật Mã Giảm Giá' : '🏷️ Cấp mới Mã Giảm giá'}
            </h3>
            
            <form onSubmit={handleSubmit} className="space-y-4 overflow-y-auto pr-2 scrollbar-none">
              <div>
                <label className="block text-sm text-slate-400 mb-1">Mã Code (CODE) *</label>
                <input required type="text" value={formData.code} onChange={e => setFormData({...formData, code: e.target.value.toUpperCase()})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-purple-400 font-bold font-mono tracking-widest uppercase" placeholder="VD: TET2024, SIEUSALE" />
              </div>
              <div>
                <label className="block text-sm text-slate-400 mb-1">Tỷ lệ giảm giá (%) *</label>
                <div className="relative">
                  <input required type="number" min="0.01" max="100" step="any" value={formData.discountPercent} onChange={e => setFormData({...formData, discountPercent: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 pr-8 text-white text-xl font-black text-center" />
                  <span className="absolute right-3 top-2.5 text-slate-400">%</span>
                </div>
              </div>
              <div>
                <label className="block text-sm text-slate-400 mb-1">Giới hạn số lượt dùng (Bỏ trống = Vô hạn)</label>
                <input type="number" min="1" value={formData.maxUsageCount} onChange={e => setFormData({...formData, maxUsageCount: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white" placeholder="VD: 100 khách đầu tiên" />
              </div>
              <div>
                <label className="block text-sm text-slate-400 mb-1">Hạn sử dụng (Bỏ trống = Vĩnh viễn)</label>
                <input type="date" value={formData.expiryDate} onChange={e => setFormData({...formData, expiryDate: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white font-mono" />
              </div>
              
              <div className="pt-4 flex gap-3 sticky bottom-0 bg-navy-800 p-2 rounded-lg mt-2">
                <button type="button" onClick={() => setIsModalOpen(false)} className="flex-1 py-2.5 rounded-lg font-bold bg-white/10 text-white hover:bg-white/20">Hủy</button>
                <button type="submit" className="flex-1 py-2.5 rounded-lg font-bold bg-purple-600 hover:bg-purple-500 text-white">Phát Hành Mã</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
