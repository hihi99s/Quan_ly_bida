import { useState, useEffect } from 'react';
import { holidayApi } from '../api';

export default function HolidaysPage() {
  const [holidays, setHolidays] = useState([]);
  const [loading, setLoading] = useState(true);

  // Form states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({ 
    name: '', date: '', recurring: false 
  });

  useEffect(() => {
    loadHolidays();
  }, []);

  async function loadHolidays() {
    try {
      const data = await holidayApi.getAll();
      setHolidays(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  function openCreateModal() {
    setEditingId(null);
    setFormData({ name: '', date: new Date().toISOString().substring(0, 10), recurring: false });
    setIsModalOpen(true);
  }

  function openEditModal(h) {
    setEditingId(h.id);
    setFormData({ 
      name: h.name, 
      date: h.date, 
      recurring: h.recurring 
    });
    setIsModalOpen(true);
  }

  async function handleDelete(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa hệ thống ngày lễ này? Nó sẽ ảnh hưởng đến việc tính giá.')) return;
    try {
      await holidayApi.delete(id);
      loadHolidays();
    } catch (e) {
      alert(e.message);
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      if (editingId) {
        await holidayApi.update(editingId, formData);
      } else {
        await holidayApi.create(formData);
      }
      setIsModalOpen(false);
      loadHolidays();
    } catch (e) {
      alert(e.message);
    }
  }

  if (loading) return <div className="flex justify-center h-40 items-center"><div className="w-10 h-10 border-4 animate-spin border-t-cyan border-cyan/30 rounded-full" /></div>;

  return (
    <div className="space-y-6 animate-in">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <h2 className="text-2xl font-bold text-white flex items-center gap-2">📅 Cấu Hình Ngày Lễ Tết</h2>
        <button 
          onClick={openCreateModal}
          className="px-4 py-2.5 bg-amber-600 hover:bg-amber-500 text-white font-bold rounded-lg whitespace-nowrap"
        >
          + Thêm Ngày Lễ Mới
        </button>
      </div>

      <div className="glass-card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-white/5 border-b border-white/10 text-xs uppercase text-slate-400 font-bold">
                <th className="p-4 rounded-tl-xl text-center">STT</th>
                <th className="p-4">Tên Sự Kiện Lễ</th>
                <th className="p-4 text-center">Ngày/Tháng/Năm</th>
                <th className="p-4 text-center">Lặp Lại Hằng Năm</th>
                <th className="p-4 rounded-tr-xl text-center">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {holidays.map((h, i) => (
                <tr key={h.id} className="hover:bg-white/5 transition-colors group">
                  <td className="p-4 text-center text-slate-500">{i + 1}</td>
                  <td className="p-4 text-amber-400 font-bold">{h.name}</td>
                  <td className="p-4 text-center font-mono text-white text-lg font-black">{new Date(h.date).toLocaleDateString('vi-VN')}</td>
                  <td className="p-4 text-center">
                    {h.recurring ? 
                      <span className="px-2 py-1 bg-emerald-500/20 text-emerald-400 rounded text-xs">Có lặp lại</span> : 
                      <span className="px-2 py-1 bg-slate-500/20 text-slate-400 rounded text-xs">Chỉ 1 lần</span>}
                  </td>
                  <td className="p-4 text-center">
                    <div className="flex items-center justify-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                      <button onClick={() => openEditModal(h)} className="w-8 h-8 rounded bg-blue-500/20 text-blue-400 hover:bg-blue-500 hover:text-white flex items-center justify-center" title="Sửa">✏️</button>
                      <button onClick={() => handleDelete(h.id)} className="w-8 h-8 rounded bg-red-500/20 text-red-400 hover:bg-red-500 hover:text-white flex items-center justify-center" title="Xóa">🗑</button>
                    </div>
                  </td>
                </tr>
              ))}
              {holidays.length === 0 && (
                <tr><td colSpan="5" className="p-8 text-center text-slate-500">Chưa có cấu hình ngày lễ nào.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="p-4 bg-blue-500/10 border border-blue-500/20 rounded-xl text-blue-200 text-sm flex gap-3 mt-4">
        <span className="text-xl">ℹ️</span>
        <div>
          <strong className="block text-blue-400 mb-1">Cơ chế ngày lễ:</strong>
          Khi Tích chọn "Lặp lại hằng năm", hệ thống Bida sẽ tự động áp giá Ngày Lễ vào đúng ngày/tháng này cho tất cả các năm tiếp theo (Vd: Trung Thu, Tết Dương Lịch). Nếu không tick, ngày lễ chỉ giới hạn đúng thời điểm đó.
        </div>
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-black/70 flex items-center justify-center p-4 animate-in">
          <div className="glass-card w-full max-w-md p-6 relative">
            <button onClick={() => setIsModalOpen(false)} className="absolute top-4 right-4 text-slate-400 hover:text-white text-xl">✕</button>
            <h3 className="text-xl font-bold text-white mb-6">
              {editingId ? '✏ Cập nhật ngày lễ' : '📅 Thêm cấu hình Lễ/Tết'}
            </h3>
            
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm text-slate-400 mb-1">Tên ngày lễ / Sự kiện *</label>
                <input required type="text" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-amber-400 font-bold" />
              </div>
              <div>
                <label className="block text-sm text-slate-400 mb-1">Chọn Ngày *</label>
                <input required type="date" value={formData.date} onChange={e => setFormData({...formData, date: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white" />
              </div>
              <div className="flex items-center gap-3 mt-4">
                <input type="checkbox" id="recurring" checked={formData.recurring} onChange={e => setFormData({...formData, recurring: e.target.checked})} className="w-5 h-5 accent-amber-500" />
                <label htmlFor="recurring" className="text-slate-300 font-bold cursor-pointer">Lặp lại hằng năm (Không cần năm)</label>
              </div>
              
              <div className="pt-6 flex gap-3">
                <button type="button" onClick={() => setIsModalOpen(false)} className="flex-1 py-2.5 rounded-lg font-bold bg-white/10 text-white hover:bg-white/20">Hủy</button>
                <button type="submit" className="flex-1 py-2.5 rounded-lg font-bold bg-amber-600 hover:bg-amber-500 text-white">Lưu Ngày Lễ</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
