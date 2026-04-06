import { useState, useEffect } from 'react';
import { staffApi } from '../api';

export default function MySchedulesPage({ user }) {
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Date range for current week
  const today = new Date();
  const firstDay = new Date(today.setDate(today.getDate() - today.getDay() + 1)).toISOString().split('T')[0];
  const lastDay = new Date(today.setDate(today.getDate() - today.getDay() + 7)).toISOString().split('T')[0];

  useEffect(() => {
    loadMySchedules();
  }, [user]);

  async function loadMySchedules() {
    if (!user || !user.id) return;
    try {
      // Fetch schedules for a wide range around current date
      const from = new Date(new Date().setDate(new Date().getDate() - 7)).toISOString().split('T')[0];
      const to = new Date(new Date().setDate(new Date().getDate() + 14)).toISOString().split('T')[0];
      
      const data = await staffApi.getSchedulesByUser(user.id, from, to);
      // Sort by date then start time
      data.sort((a, b) => {
        if (a.date !== b.date) return a.date.localeCompare(b.date);
        return a.shiftStartTime.localeCompare(b.shiftStartTime);
      });
      setSchedules(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  async function handleCheckIn(id) {
    try {
      await staffApi.checkIn(id);
      alert('Check-in thành công!');
      loadMySchedules();
    } catch (e) {
      alert(e.message);
    }
  }

  async function handleCheckOut(id) {
    try {
      await staffApi.checkOut(id);
      alert('Check-out thành công!');
      loadMySchedules();
    } catch (e) {
      alert(e.message);
    }
  }

  const getStatusBadge = (status) => {
    switch (status) {
      case 'SCHEDULED': return <span className="px-2 py-0.5 rounded-full bg-slate-500/20 text-slate-400 text-xs font-bold border border-slate-500/30">📅 CHƯA ĐẾN</span>;
      case 'CHECKED_IN': return <span className="px-2 py-0.5 rounded-full bg-cyan-500/20 text-cyan-400 text-xs font-bold border border-cyan-500/30 animate-pulse">🏃 ĐANG LÀM</span>;
      case 'CHECKED_OUT': return <span className="px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 text-xs font-bold border border-emerald-500/30">✅ HOÀN THÀNH</span>;
      case 'ABSENT': return <span className="px-2 py-0.5 rounded-full bg-red-500/20 text-red-400 text-xs font-bold border border-red-500/30">❌ VẮNG MẶT</span>;
      default: return null;
    }
  };

  if (loading) return <div className="p-8 text-center text-slate-400">Đang tải lịch làm việc...</div>;

  return (
    <div className="space-y-6 animate-in">
      <div>
        <h2 className="text-2xl font-bold text-white mb-1">⏰ Lịch Làm Của Tôi</h2>
        <p className="text-sm text-slate-400">Xem và quản lý thời gian trực của bạn</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {schedules.map(s => {
          const todayStr = new Date().toLocaleDateString('en-CA');
          const isToday = s.date === todayStr;
          return (
            <div key={s.id} className={`glass-card p-5 border-l-4 transition-all hover:scale-[1.02] ${isToday ? 'border-cyan-500 bg-cyan-500/5' : 'border-white/10'}`}>
              <div className="flex justify-between items-start mb-4">
                <div>
                  <div className="text-xs font-bold text-slate-500 uppercase mb-1">{isToday ? '⭐️ Hôm Nay' : s.date}</div>
                  <h3 className="text-lg font-bold text-white">{s.shiftName}</h3>
                </div>
                {getStatusBadge(s.status)}
              </div>

              <div className="space-y-3 mb-6">
                <div className="flex items-center gap-3 text-cyan-400 font-mono font-bold text-xl">
                  <span>🕒</span>
                  <span>{s.shiftStartTime.substring(0,5)} - {s.shiftEndTime.substring(0,5)}</span>
                </div>
                
                {(s.checkInTime || s.checkOutTime) && (
                  <div className="text-xs space-y-1 p-2 rounded bg-black/20 border border-white/5">
                    {s.checkInTime && <div className="text-slate-400">📥 Check-in: <span className="text-white">{new Date(s.checkInTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span></div>}
                    {s.checkOutTime && <div className="text-slate-400">📤 Check-out: <span className="text-white">{new Date(s.checkOutTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span></div>}
                  </div>
                )}
              </div>

              <div className="flex gap-2">
                {s.status === 'SCHEDULED' && isToday && (
                  <button 
                    onClick={() => handleCheckIn(s.id)}
                    className="flex-1 py-2 rounded-lg bg-cyan-600 hover:bg-cyan-500 text-white font-bold text-sm transition-all shadow-lg shadow-cyan-900/40"
                  >
                    Check-in
                  </button>
                )}
                {s.status === 'CHECKED_IN' && isToday && (
                  <button 
                    onClick={() => handleCheckOut(s.id)}
                    className="flex-1 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-sm transition-all shadow-lg shadow-emerald-900/40"
                  >
                    Check-out
                  </button>
                )}
              </div>
            </div>
          );
        })}
        {schedules.length === 0 && (
          <div className="col-span-full p-12 glass-card text-center text-slate-500">
            Bạn chưa được phân công ca làm nào trong thời gian này.
          </div>
        )}
      </div>
    </div>
  );
}
