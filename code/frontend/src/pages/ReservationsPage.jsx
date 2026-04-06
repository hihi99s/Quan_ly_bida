import { useState, useEffect } from 'react';
import { reservationApi, tableApi, customerApi, formatMoney, formatTime } from '../api';

export default function ReservationsPage() {
  const [reservations, setReservations] = useState([]);
  const [tables, setTables] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);

  // Form states
  const [formData, setFormData] = useState({
    tableId: '',
    customerId: '',
    startTime: '',
    note: ''
  });

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      const [resvs, tbls, custs] = await Promise.all([
        reservationApi.getPending(),
        tableApi.getAll(),
        customerApi.getAll()
      ]);
      setReservations(resvs);
      setTables(tbls);
      setCustomers(custs);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      const customer = customers.find(c => String(c.id) === String(formData.customerId));
      const payload = {
        tableId: formData.tableId,
        customerName: customer ? customer.name : 'Khách không tên',
        customerPhone: customer ? customer.phone : '',
        reservedTime: formData.startTime, // Giữ nguyên format YYYY-MM-DDTHH:mm từ input
        note: formData.note
      };
      await reservationApi.create(payload);
      setShowModal(false);
      setFormData({ tableId: '', customerId: '', startTime: '', note: '' });
      loadData();
      alert('Đã đặt bàn thành công!');
    } catch (e) {
      alert('Lỗi đặt bàn: ' + e.message);
    }
  }

  async function handleCancel(id) {
    if (!confirm('Bạn có chắc muốn hủy lịch đặt này?')) return;
    try {
      await reservationApi.cancel(id);
      loadData();
    } catch (e) {
      alert('Lỗi hủy đặt bàn: ' + e.message);
    }
  }

  if (loading) return <div className="flex justify-center p-20"><div className="w-10 h-10 border-4 animate-spin border-t-cyan border-cyan/30 rounded-full" /></div>;

  return (
    <>
      <div className="space-y-6 animate-in">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-white flex items-center gap-2">📅 Quản Lý Đặt Bàn</h2>
        <button 
          onClick={() => setShowModal(true)}
          className="bg-cyan-600 hover:bg-cyan-500 text-white px-4 py-2 rounded-lg font-bold flex items-center gap-2 transition-all shadow-lg hover:shadow-cyan-500/20"
        >
          <span>+</span> Đặt Bàn Mới
        </button>
      </div>

      <div className="glass-card overflow-hidden">
        <table className="w-full text-left border-collapse font-sans">
          <thead>
            <tr className="bg-white/5 text-slate-400 text-xs uppercase font-bold tracking-wider">
              <th className="p-4 border-b border-white/10">Bàn</th>
              <th className="p-4 border-b border-white/10">Khách Hàng</th>
              <th className="p-4 border-b border-white/10">Thời Gian</th>
              <th className="p-4 border-b border-white/10">Ghi Chú</th>
              <th className="p-4 border-b border-white/10">Trạng Thái</th>
              <th className="p-4 border-b border-white/10 text-right">Thao Tác</th>
            </tr>
          </thead>
          <tbody className="text-slate-300 text-sm">
            {reservations.length === 0 ? (
              <tr><td colSpan="6" className="p-10 text-center text-slate-500 italic">Không có lịch đặt bàn nào đang chờ</td></tr>
            ) : (
              reservations.map(r => (
                <tr key={r.id} className="hover:bg-white/5 transition border-b border-white/5 last:border-0">
                  <td className="p-4 font-bold text-white">{r.tableName || 'Bàn không rõ'}</td>
                  <td className="p-4">
                    <div className="font-medium text-slate-200">{r.customerName}</div>
                    <div className="text-xs text-slate-500">{r.customerPhone}</div>
                  </td>
                  <td className="px-4 py-4 text-white font-mono">{r.reservedTime?.replace('T', ' ')}</td>
                  <td className="p-4 italic text-xs truncate max-w-xs">{r.note || '-'}</td>
                  <td className="p-4">
                    <span className="px-2 py-1 rounded-full bg-amber-500/20 text-amber-500 text-[10px] font-bold uppercase border border-amber-500/20">
                      PENDING
                    </span>
                  </td>
                  <td className="p-4 text-right">
                    <button 
                      onClick={() => handleCancel(r.id)}
                      className="text-red-400 hover:text-red-300 font-bold px-2 py-1"
                    >
                      Hủy Đặt
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      </div>
 
      {showModal && (
        <div className="fixed inset-0 z-50 bg-black/80 flex items-start sm:items-center justify-center p-4 overflow-y-auto py-6 sm:py-10 animate-in scroll-smooth">
          <form onSubmit={handleSubmit} className="glass-card w-full max-w-md p-6 my-auto relative">
            <button type="button" onClick={() => setShowModal(false)} className="absolute top-4 right-4 text-slate-400 hover:text-white text-xl">✕</button>
            <h3 className="text-xl font-bold text-white mb-6 flex items-center gap-2">📝 Form Đặt Bàn</h3>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm text-slate-400 mb-1 font-medium">Chọn Bàn</label>
                <select 
                  required
                  value={formData.tableId} 
                  onChange={e => setFormData({...formData, tableId: e.target.value})}
                  className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white focus:border-cyan-500 outline-none"
                >
                  <option value="">-- Chọn bàn --</option>
                  {tables.map(t => (
                    <option key={t.id} value={t.id}>
                      {t.name} - {t.tableType} ({t.status})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm text-slate-400 mb-1 font-medium">Khách Hàng</label>
                <select 
                  required
                  value={formData.customerId} 
                  onChange={e => setFormData({...formData, customerId: e.target.value})}
                  className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white focus:border-cyan-500 outline-none"
                >
                  <option value="">-- Chọn khách hàng --</option>
                  {customers.map(c => (
                    <option key={c.id} value={c.id}>{c.name} - {c.phone}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm text-slate-400 mb-1 font-medium">Thời Gian Đến</label>
                <input 
                  required
                  type="datetime-local" 
                  value={formData.startTime} 
                  onChange={e => setFormData({...formData, startTime: e.target.value})}
                  className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white focus:border-cyan-500 outline-none font-mono"
                />
              </div>

              <div>
                <label className="block text-sm text-slate-400 mb-1 font-medium">Ghi Chú</label>
                <textarea 
                  value={formData.note} 
                  onChange={e => setFormData({...formData, note: e.target.value})}
                  className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white focus:border-cyan-500 outline-none h-20"
                  placeholder="Vd: 4 người lớn..."
                ></textarea>
              </div>

              <div className="pt-4 flex gap-3">
                <button type="button" onClick={() => setShowModal(false)} className="flex-1 py-3 rounded-xl font-bold bg-white/10 text-white hover:bg-white/20 transition-all">Hủy</button>
                <button type="submit" className="flex-1 py-3 rounded-xl font-bold bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 text-white shadow-lg shadow-cyan-900/40 transition-all">Đặt Bàn Ngay</button>
              </div>
            </div>
          </form>
        </div>
      )}
    </>
  );
}
