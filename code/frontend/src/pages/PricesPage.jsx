import { useState, useEffect } from 'react';
import { priceApi, formatMoney } from '../api';

export default function PricesPage() {
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(true);

  // Form states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({ 
    tableType: 'POOL', dayType: 'WEEKDAY', startTime: '00:00', endTime: '23:59', pricePerHour: 0 
  });

  useEffect(() => {
    loadRules();
  }, []);

  async function loadRules() {
    try {
      const data = await priceApi.getAll();
      setRules(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  function openCreateModal() {
    setEditingId(null);
    setFormData({ tableType: 'POOL', dayType: 'WEEKDAY', startTime: '08:00', endTime: '18:00', pricePerHour: 50000 });
    setIsModalOpen(true);
  }

  function openEditModal(r) {
    setEditingId(r.id);
    // Format thời gian thành HH:mm để bind vào <input type="time">
    const formatTime = (timeStr) => timeStr.length === 8 ? timeStr.substring(0, 5) : timeStr;
    setFormData({ 
      tableType: r.tableType, 
      dayType: r.dayType, 
      startTime: formatTime(r.startTime), 
      endTime: formatTime(r.endTime), 
      pricePerHour: r.pricePerHour 
    });
    setIsModalOpen(true);
  }

  async function handleDelete(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa khung giá này? Cẩn thận ảnh hưởng đến giá trên hệ thống.')) return;
    try {
      await priceApi.delete(id);
      loadRules();
    } catch (e) {
      alert(e.message);
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      // Backend expects startTime / endTime as valid LocalTime string. HH:mm is accepted.
      if (editingId) {
        await priceApi.update(editingId, formData);
      } else {
        await priceApi.create(formData);
      }
      setIsModalOpen(false);
      loadRules();
    } catch (e) {
      alert(e.message);
    }
  }

  if (loading) return <div className="flex justify-center h-40 items-center"><div className="w-10 h-10 border-4 animate-spin border-t-cyan border-cyan/30 rounded-full" /></div>;

  return (
    <div className="space-y-6 animate-in">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <h2 className="text-2xl font-bold text-white flex items-center gap-2">💲 Bảng Giá Bida</h2>
        <button 
          onClick={openCreateModal}
          className="px-4 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white font-bold rounded-lg whitespace-nowrap"
        >
          + Thêm Khung Giờ Mới
        </button>
      </div>

      <div className="glass-card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-white/5 border-b border-white/10 text-xs uppercase text-slate-400 font-bold">
                <th className="p-4 rounded-tl-xl text-center">Loại Bàn</th>
                <th className="p-4 text-center">Ngày Lễ / Thường</th>
                <th className="p-4 text-center">Giờ Bắt Đầu</th>
                <th className="p-4 text-center">Giờ Kết Thúc</th>
                <th className="p-4 text-right">Giá / Giờ</th>
                <th className="p-4 rounded-tr-xl text-center">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {rules.map(r => (
                <tr key={r.id} className="hover:bg-white/5 transition-colors group">
                  <td className="p-4 text-center">
                    <span className={`px-3 py-1 rounded text-xs font-bold ${r.tableType === 'POOL' ? 'bg-blue-500/20 text-blue-400' : r.tableType === 'CAROM' ? 'bg-amber-500/20 text-amber-400' : 'bg-purple-500/20 text-purple-400'}`}>
                      {r.tableType}
                    </span>
                  </td>
                  <td className="p-4 text-center">
                    <span className={`px-2 py-1 rounded text-xs border ${r.dayType === 'WEEKDAY' ? 'border-slate-500 text-slate-300' : r.dayType === 'WEEKEND' ? 'border-orange-500/50 text-orange-400' : 'border-rose-500 text-rose-400'}`}>
                      {r.dayType === 'WEEKDAY' ? 'Ngày thường' : r.dayType === 'WEEKEND' ? 'Cuối Tuần' : 'Lễ / Tết'}
                    </span>
                  </td>
                  <td className="p-4 text-center font-mono text-slate-200">{r.startTime}</td>
                  <td className="p-4 text-center font-mono text-slate-200">{r.endTime}</td>
                  <td className="p-4 text-right">
                    <span className="text-xl font-black text-emerald-400">{formatMoney(r.pricePerHour)}</span>
                  </td>
                  <td className="p-4 text-center">
                    <div className="flex items-center justify-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                      <button onClick={() => openEditModal(r)} className="w-8 h-8 rounded bg-blue-500/20 text-blue-400 hover:bg-blue-500 hover:text-white flex items-center justify-center" title="Sửa">✏️</button>
                      <button onClick={() => handleDelete(r.id)} className="w-8 h-8 rounded bg-red-500/20 text-red-400 hover:bg-red-500 hover:text-white flex items-center justify-center" title="Xóa">🗑</button>
                    </div>
                  </td>
                </tr>
              ))}
              {rules.length === 0 && (
                <tr><td colSpan="6" className="p-8 text-center text-slate-500">Chưa có khung giá nào được cài đặt.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="p-4 bg-yellow-500/10 border border-yellow-500/20 rounded-xl text-yellow-200 text-sm flex gap-3 mt-4">
        <span className="text-xl">⚠️</span>
        <div>
          <strong className="block text-yellow-500 mb-1">Lưu ý quan trọng:</strong>
          Các khung giờ <strong>KHÔNG ĐƯỢC</strong> đè lên nhau (overlap) đối với cùng 1 loại bàn trong 1 loại ngày. Hãy chia giờ chuẩn xác (vd: 00:00-18:00 và 18:00-23:59).
        </div>
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-black/70 flex items-center justify-center p-4 animate-in">
          <div className="glass-card w-full max-w-md p-6 relative">
            <button onClick={() => setIsModalOpen(false)} className="absolute top-4 right-4 text-slate-400 hover:text-white text-xl">✕</button>
            <h3 className="text-xl font-bold text-white mb-6">
              {editingId ? '✏ Cập nhật khung giá' : '💰 Thêm khung giá mới'}
            </h3>
            
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm text-slate-400 mb-1">Loại Bàn *</label>
                    <select disabled={!!editingId} value={formData.tableType} onChange={e => setFormData({...formData, tableType: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white disabled:opacity-50">
                      <option value="POOL">Pool (Lỗ)</option>
                      <option value="CAROM">Carom (Phăng)</option>
                      <option value="SNOOKER">Snooker</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm text-slate-400 mb-1">Loại Ngày *</label>
                    <select disabled={!!editingId} value={formData.dayType} onChange={e => setFormData({...formData, dayType: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white disabled:opacity-50">
                      <option value="WEEKDAY">Ngày Thường (T2-T6)</option>
                      <option value="WEEKEND">Cuối Tuần (T7, CN)</option>
                      <option value="HOLIDAY">Lễ / Tết</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm text-slate-400 mb-1">Giờ bắt đầu *</label>
                    <input required type="time" value={formData.startTime} onChange={e => setFormData({...formData, startTime: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white font-mono" />
                  </div>
                  <div>
                    <label className="block text-sm text-slate-400 mb-1">Giờ kết thúc *</label>
                    <input required type="time" value={formData.endTime} onChange={e => setFormData({...formData, endTime: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white font-mono" />
                  </div>
                  <div className="col-span-2">
                    <label className="block text-sm text-slate-400 mb-1">Giá mỗi giờ (VNĐ) *</label>
                    <input required type="number" min="0" value={formData.pricePerHour} onChange={e => setFormData({...formData, pricePerHour: parseFloat(e.target.value)})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white text-xl font-mono text-emerald-400 font-bold" />
                  </div>
              </div>
              
              <div className="pt-4 flex gap-3">
                <button type="button" onClick={() => setIsModalOpen(false)} className="flex-1 py-2.5 rounded-lg font-bold bg-white/10 text-white hover:bg-white/20">Hủy</button>
                <button type="submit" className="flex-1 py-2.5 rounded-lg font-bold bg-emerald-600 hover:bg-emerald-500 text-white">Lưu Bảng Giá</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
