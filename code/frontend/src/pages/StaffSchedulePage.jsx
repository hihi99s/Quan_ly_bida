import { useState, useEffect } from 'react';
import { staffApi, userApi } from '../api';

export default function StaffSchedulePage() {
  const [shifts, setShifts] = useState([]);
  const [schedules, setSchedules] = useState([]);
  const [users, setUsers] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('daily'); // 'daily', 'audit', 'shifts'
  
  // Date control
  const [currentDate, setCurrentDate] = useState(new Date().toLocaleDateString('en-CA'));
  
  // Modals
  const [modalType, setModalType] = useState(null); // 'ASSIGN', 'EDIT_SHIFT', 'BULK_DELETE'
  const [editingItem, setEditingItem] = useState(null);
  
  // Form states
  const [assignForm, setAssignForm] = useState({ userId: '', shiftId: '', date: currentDate });
  const [shiftForm, setShiftForm] = useState({ name: '', startTime: '08:00', endTime: '16:00', maxStaff: '' });
  const [bulkDeleteForm, setBulkDeleteForm] = useState({ from: '', to: '' });

  useEffect(() => {
    loadBaseData();
  }, []);

  useEffect(() => {
    if (activeTab === 'daily') loadSchedules();
    if (activeTab === 'audit') loadAuditLogs();
  }, [activeTab, currentDate]);

  async function loadBaseData() {
    try {
      const [sData, uData] = await Promise.all([
        staffApi.getShifts(),
        userApi.getAll()
      ]);
      setShifts(sData);
      setUsers(uData.filter(u => u.active));
      loadSchedules();
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  async function loadSchedules() {
    try {
      const data = await staffApi.getSchedules(currentDate);
      setSchedules(data);
    } catch (e) {
      console.error(e);
    }
  }

  async function loadAuditLogs() {
    try {
      const res = await staffApi.getAuditLogs();
      setAuditLogs(res.data || []);
    } catch (e) {
      console.error(e);
    }
  }

  // ─── Shift Actions ───
  async function handleSaveShift(e) {
    e.preventDefault();
    try {
      if (editingItem) {
        await staffApi.updateShift(editingItem.id, shiftForm);
      } else {
        await staffApi.createShift(shiftForm);
      }
      const sData = await staffApi.getShifts();
      setShifts(sData);
      setModalType(null);
    } catch (e) {
      alert(e.message);
    }
  }

  async function handleDeleteShift(id) {
    if (!confirm('Xóa ca này?')) return;
    try {
      await staffApi.deleteShift(id);
      const sData = await staffApi.getShifts();
      setShifts(sData);
    } catch (e) {
      alert(e.message);
    }
  }

  // ─── Schedule Actions ───
  async function handleAssign(e) {
    e.preventDefault();
    try {
      if (editingItem) {
        await staffApi.update(editingItem.id, { userId: assignForm.userId, shiftId: assignForm.shiftId });
      } else {
        await staffApi.assign(assignForm);
      }
      loadSchedules();
      setModalType(null);
    } catch (e) {
      alert(e.message);
    }
  }

  async function handleDeleteSchedule(id) {
    if (!confirm('Hủy ca làm này của nhân viên?')) return;
    try {
      await staffApi.delete(id);
      loadSchedules();
    } catch (e) {
      alert(e.message);
    }
  }

  async function handleBulkDelete(e) {
    e.preventDefault();
    if (!confirm(`Xóa toàn bộ lịch từ ${bulkDeleteForm.from} đến ${bulkDeleteForm.to}?`)) return;
    try {
      const res = await staffApi.bulkDelete(bulkDeleteForm.from, bulkDeleteForm.to);
      alert(res.message);
      loadSchedules();
      setModalType(null);
    } catch (e) {
      alert(e.message);
    }
  }

  const isPast = (dateStr) => {
    return new Date(dateStr) < new Date(new Date().toDateString());
  };

  if (loading) return <div className="p-8 text-center text-slate-400">Đang tải dữ liệu...</div>;

  return (
    <div className="space-y-6 animate-in">
      {/* Header & Tabs */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h2 className="text-2xl font-bold text-white mb-1">📅 Lịch Làm Việc</h2>
          <p className="text-sm text-slate-400 text-premium-gradient">Quản lý ca trực và lịch trình nhân sự</p>
        </div>
        
        <div className="flex bg-navy-800 p-1 rounded-xl border border-white/5 shadow-inner">
          <button 
            onClick={() => setActiveTab('daily')}
            className={`px-4 py-2 rounded-lg text-sm font-bold transition-all ${activeTab === 'daily' ? 'bg-cyan-600 text-white shadow-lg shadow-cyan-900/40' : 'text-slate-400 hover:text-white'}`}
          >
            📋 Lịch Trực
          </button>
          <button 
            onClick={() => setActiveTab('shifts')}
            className={`px-4 py-2 rounded-lg text-sm font-bold transition-all ${activeTab === 'shifts' ? 'bg-cyan-600 text-white shadow-lg shadow-cyan-900/40' : 'text-slate-400 hover:text-white'}`}
          >
            🕒 Quản Lý Ca
          </button>
          <button 
            onClick={() => setActiveTab('audit')}
            className={`px-4 py-2 rounded-lg text-sm font-bold transition-all ${activeTab === 'audit' ? 'bg-cyan-600 text-white shadow-lg shadow-cyan-900/40' : 'text-slate-400 hover:text-white'}`}
          >
            📜 Lịch Sử
          </button>
        </div>
      </div>

      {activeTab === 'daily' && (
        <>
          {/* Controls */}
          <div className="flex flex-wrap items-center justify-between gap-4 glass-card p-4">
            <div className="flex items-center gap-3">
              <input 
                type="date" 
                value={currentDate}
                onChange={e => setCurrentDate(e.target.value)}
                className="bg-navy-900 border border-white/10 rounded-lg px-4 py-2 text-white outline-none focus:border-cyan-500"
              />
              <button 
                onClick={() => setCurrentDate(new Date().toLocaleDateString('en-CA'))}
                className="px-4 py-2 bg-white/5 hover:bg-white/10 text-white rounded-lg font-medium transition-colors"
              >
                Hôm nay
              </button>
            </div>
            
            <div className="flex gap-2">
              <button 
                onClick={() => {
                  setBulkDeleteForm({ from: currentDate, to: currentDate });
                  setModalType('BULK_DELETE');
                }}
                className="px-4 py-2 bg-red-500/10 text-red-400 hover:bg-red-500 hover:text-white border border-red-500/20 rounded-lg font-bold transition-all"
              >
                🗑️ Xóa Hàng Loạt
              </button>
              <button 
                onClick={() => {
                  setAssignForm({ userId: '', shiftId: '', date: currentDate });
                  setEditingItem(null);
                  setModalType('ASSIGN');
                }}
                disabled={isPast(currentDate)}
                className="px-4 py-2 bg-cyan-600 hover:bg-cyan-500 text-white font-bold rounded-lg shadow-lg shadow-cyan-900/40 disabled:opacity-50 disabled:grayscale"
              >
                + Xếp Lịch Mới
              </button>
            </div>
          </div>

          {/* Daily Table */}
          <div className="glass-card overflow-hidden">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-white/5 border-b border-white/10 text-xs uppercase text-slate-400 font-bold">
                  <th className="p-4 w-48">Tên Ca</th>
                  <th className="p-4">Thời Gian</th>
                  <th className="p-4 w-24 text-center">Giới Hạn</th>
                  <th className="p-4">Nhân Viên Trực</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5">
                {shifts.map(shift => {
                  const shiftSchedules = schedules.filter(s => s.shiftId === shift.id);
                  return (
                    <tr key={shift.id} className="hover:bg-white/5 transition-colors group">
                      <td className="p-4">
                        <div className="font-bold text-white">{shift.name}</div>
                      </td>
                      <td className="p-4">
                        <span className="px-2 py-1 rounded bg-cyan-500/10 text-cyan-400 text-sm font-mono border border-cyan-500/20">
                          {shift.startTime} - {shift.endTime}
                        </span>
                      </td>
                      <td className="p-4 text-center">
                        <span className={`text-sm font-bold ${shift.maxStaff && shiftSchedules.length >= shift.maxStaff ? 'text-red-400' : 'text-slate-300'}`}>
                          {shiftSchedules.length} / {shift.maxStaff || '∞'}
                        </span>
                      </td>
                      <td className="p-4">
                        <div className="flex flex-wrap gap-2">
                          {shiftSchedules.map(s => (
                            <div key={s.id} className="flex items-center gap-2 bg-white/5 border border-white/10 rounded-full px-3 py-1 text-sm group/tag">
                              <span className="text-white font-medium">{s.fullName}</span>
                              <span className={`w-2 h-2 rounded-full ${
                                s.status === 'CHECKED_OUT' ? 'bg-emerald-500' : 
                                s.status === 'CHECKED_IN' ? 'bg-cyan-500 animate-pulse' : 
                                s.status === 'ABSENT' ? 'bg-red-500' : 'bg-slate-500'
                              }`} title={s.status}></span>
                              
                              {!isPast(s.date) && (
                                <div className="flex gap-1 ml-1 opacity-0 group-hover/tag:opacity-100 transition-opacity">
                                  <button 
                                    onClick={() => {
                                      setEditingItem(s);
                                      setAssignForm({ userId: s.userId, shiftId: s.shiftId, date: s.date });
                                      setModalType('ASSIGN');
                                    }}
                                    className="text-blue-400 hover:text-white"
                                  >✏️</button>
                                  <button 
                                    onClick={() => handleDeleteSchedule(s.id)}
                                    className="text-red-400 hover:text-white"
                                  >✕</button>
                                </div>
                              )}
                            </div>
                          ))}
                          {shiftSchedules.length === 0 && <span className="text-slate-500 italic text-sm">Chưa có ai trực</span>}
                          
                          {!isPast(currentDate) && (!shift.maxStaff || shiftSchedules.length < shift.maxStaff) && (
                            <button 
                              onClick={() => {
                                setAssignForm({ userId: '', shiftId: shift.id.toString(), date: currentDate });
                                setEditingItem(null);
                                setModalType('ASSIGN');
                              }}
                              className="w-7 h-7 rounded-full bg-white/5 hover:bg-white/10 border border-dashed border-white/20 flex items-center justify-center text-slate-400 transition-all"
                            >
                              +
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        </>
      )}

      {activeTab === 'shifts' && (
        <div className="space-y-4">
          <div className="flex justify-end">
            <button 
               onClick={() => {
                 setShiftForm({ name: '', startTime: '08:00', endTime: '16:00', maxStaff: '' });
                 setEditingItem(null);
                 setModalType('EDIT_SHIFT');
               }}
               className="px-4 py-2 bg-cyan-600 hover:bg-cyan-500 text-white font-bold rounded-lg"
            >
              + Tạo Ca Mới
            </button>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {shifts.map(s => (
              <div key={s.id} className="glass-card p-4 border-l-4 border-cyan-500 hover:scale-[1.02] transition-transform">
                <div className="flex justify-between items-start mb-3">
                  <h4 className="font-bold text-lg text-white">{s.name}</h4>
                  <div className="flex gap-2">
                    <button onClick={() => {
                       setEditingItem(s);
                       setShiftForm({ name: s.name, startTime: s.startTime, endTime: s.endTime, maxStaff: s.maxStaff || '' });
                       setModalType('EDIT_SHIFT');
                    }} className="text-slate-400 hover:text-white">✏️</button>
                    <button onClick={() => handleDeleteShift(s.id)} className="text-slate-400 hover:text-red-400">🗑️</button>
                  </div>
                </div>
                <div className="text-3xl font-mono text-cyan-400 font-bold mb-2">
                  {s.startTime.substring(0,5)} - {s.endTime.substring(0,5)}
                </div>
                <div className="text-sm text-slate-400 flex items-center gap-2">
                   👥 Tối đa: <span className="text-white font-bold">{s.maxStaff || 'Không giới hạn'}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {activeTab === 'audit' && (
        <div className="glass-card overflow-hidden">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-white/5 border-b border-white/10 text-xs uppercase text-slate-400 font-bold">
                <th className="p-4">Thời Gian</th>
                <th className="p-4">Người Thực Hiện</th>
                <th className="p-4">Hành Động</th>
                <th className="p-4">Chi Tiết</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {auditLogs.map(log => (
                <tr key={log.id} className="hover:bg-white/5 transition-colors">
                  <td className="p-4 text-slate-400 text-xs font-mono">{new Date(log.performedAt).toLocaleString()}</td>
                  <td className="p-4 font-bold text-cyan-400">{log.performedBy}</td>
                  <td className="p-4">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase border ${
                      log.action === 'CREATED' ? 'bg-emerald-500/10 text-emerald-500 border-emerald-500/20' :
                      log.action === 'UPDATED' ? 'bg-blue-500/10 text-blue-500 border-blue-500/20' :
                      log.action === 'DELETED' ? 'bg-red-500/10 text-red-500 border-red-500/20' :
                      'bg-slate-500/10 text-slate-500 border-slate-500/20'
                    }`}>
                      {log.action}
                    </span>
                  </td>
                  <td className="p-4 text-slate-300 text-sm">{log.details}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* ─── MODALS ─── */}
      
      {modalType === 'ASSIGN' && (
        <div className="fixed inset-0 z-50 bg-black/70 flex items-center justify-center p-4 animate-in">
          <div className="glass-card w-full max-w-md p-6 relative">
            <button onClick={() => setModalType(null)} className="absolute top-4 right-4 text-slate-400 hover:text-white text-xl">✕</button>
            <h3 className="text-xl font-bold text-white mb-6">
              {editingItem ? '✏️ Cập Nhật Lịch Trực' : '🗓️ Phân Công Nhân Viên'}
            </h3>
            <form onSubmit={handleAssign} className="space-y-4">
              <div>
                <label className="block text-sm text-slate-400 mb-1">Ngày làm việc</label>
                <input disabled type="date" value={assignForm.date} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white opacity-50" />
              </div>
              <div>
                <label className="block text-sm text-slate-400 mb-1">Nhân viên *</label>
                <select 
                  required 
                  value={assignForm.userId} 
                  onChange={e => setAssignForm({...assignForm, userId: e.target.value})}
                  className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white outline-none focus:border-cyan-500"
                >
                  <option value="">-- Chọn nhân viên --</option>
                  {users.map(u => <option key={u.id} value={u.id}>{u.fullName} ({u.username})</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm text-slate-400 mb-1">Ca trực *</label>
                <select 
                  required 
                  value={assignForm.shiftId} 
                  onChange={e => setAssignForm({...assignForm, shiftId: e.target.value})}
                  className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white outline-none focus:border-cyan-500"
                >
                  <option value="">-- Chọn ca --</option>
                  {shifts.map(s => <option key={s.id} value={s.id}>{s.name} ({s.startTime} - {s.endTime})</option>)}
                </select>
              </div>
              <div className="pt-4 flex gap-3">
                <button type="button" onClick={() => setModalType(null)} className="flex-1 py-3 rounded-lg font-bold bg-white/10 text-white">Hủy</button>
                <button type="submit" className="flex-[2] py-3 rounded-lg font-bold bg-cyan-600 hover:bg-cyan-500 text-white shadow-lg shadow-cyan-900/40">
                  {editingItem ? 'Cập Nhật' : 'Phân Công'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {modalType === 'EDIT_SHIFT' && (
        <div className="fixed inset-0 z-50 bg-black/70 flex items-center justify-center p-4 animate-in">
          <div className="glass-card w-full max-w-md p-6 relative">
            <button onClick={() => setModalType(null)} className="absolute top-4 right-4 text-slate-400 hover:text-white text-xl">✕</button>
            <h3 className="text-xl font-bold text-white mb-6">
              {editingItem ? '✏️ Chỉnh Sửa Định Nghĩa Ca' : '🕒 Tạo Ca Làm Mới'}
            </h3>
            <form onSubmit={handleSaveShift} className="space-y-4">
              <div>
                <label className="block text-sm text-slate-400 mb-1">Tên ca (VD: Ca Sáng, Ca Gãy) *</label>
                <input required type="text" value={shiftForm.name} onChange={e => setShiftForm({...shiftForm, name: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white outline-none" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-slate-400 mb-1">Giờ bắt đầu *</label>
                  <input required type="time" value={shiftForm.startTime} onChange={e => setShiftForm({...shiftForm, startTime: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white outline-none" />
                </div>
                <div>
                  <label className="block text-sm text-slate-400 mb-1">Giờ kết thúc *</label>
                  <input required type="time" value={shiftForm.endTime} onChange={e => setShiftForm({...shiftForm, endTime: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white outline-none" />
                </div>
              </div>
              <div>
                <label className="block text-sm text-slate-400 mb-1">Số lượng nhân viên tối đa (Trống = không giới hạn)</label>
                <input type="number" min="1" value={shiftForm.maxStaff} onChange={e => setShiftForm({...shiftForm, maxStaff: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white outline-none" />
              </div>
              <div className="pt-4 flex gap-3">
                <button type="button" onClick={() => setModalType(null)} className="flex-1 py-3 rounded-lg font-bold bg-white/10 text-white">Hủy</button>
                <button type="submit" className="flex-[2] py-3 rounded-lg font-bold bg-cyan-600 hover:bg-cyan-500 text-white shadow-lg shadow-cyan-900/40">Lưu Định Nghĩa</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {modalType === 'BULK_DELETE' && (
        <div className="fixed inset-0 z-50 bg-black/70 flex items-center justify-center p-4 animate-in">
          <div className="glass-card w-full max-w-md p-6 relative text-center">
            <button onClick={() => setModalType(null)} className="absolute top-4 right-4 text-slate-400 hover:text-white text-xl">✕</button>
            <div className="w-16 h-16 bg-red-500/20 text-red-500 rounded-full flex items-center justify-center text-3xl mx-auto mb-4">⚠️</div>
            <h3 className="text-xl font-bold text-white mb-2">Hủy Lịch Hàng Loạt</h3>
            <p className="text-slate-400 text-sm mb-6">Xóa toàn bộ lịch trực trong khoảng thời gian đã chọn. Không thể khôi phục!</p>
            
            <form onSubmit={handleBulkDelete} className="space-y-4 text-left">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-slate-400 mb-1">Từ ngày</label>
                  <input required type="date" value={bulkDeleteForm.from} onChange={e => setBulkDeleteForm({...bulkDeleteForm, from: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white outline-none" />
                </div>
                <div>
                  <label className="block text-sm text-slate-400 mb-1">Đến ngày</label>
                  <input required type="date" value={bulkDeleteForm.to} onChange={e => setBulkDeleteForm({...bulkDeleteForm, to: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white outline-none" />
                </div>
              </div>
              <div className="pt-4 flex gap-3">
                <button type="button" onClick={() => setModalType(null)} className="flex-1 py-3 rounded-lg font-bold bg-white/10 text-white">Hủy</button>
                <button type="submit" className="flex-[2] py-3 rounded-lg font-bold bg-red-600 hover:bg-red-500 text-white shadow-lg shadow-red-900/40">Xác Nhận Xóa</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
