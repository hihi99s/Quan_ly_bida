import { useState, useEffect } from 'react';
import { Bar, Line, Pie } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, PointElement, LineElement, ArcElement, Filler, Tooltip, Legend } from 'chart.js';
import { reportApi, formatMoney } from '../api';

ChartJS.register(CategoryScale, LinearScale, BarElement, PointElement, LineElement, ArcElement, Filler, Tooltip, Legend);

export default function ReportsPage() {
  const [kpis, setKpis] = useState(null);
  const [daily, setDaily] = useState([]);
  const [monthly, setMonthly] = useState([]);
  const [tableTypes, setTableTypes] = useState([]);
  const [tableStats, setTableStats] = useState([]);
  const [staffPerf, setStaffPerf] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      reportApi.getKpis(),
      reportApi.getDaily(),
      reportApi.getMonthly(),
      reportApi.getTableTypes(),
      reportApi.getTables(),
      reportApi.getStaff(),
    ]).then(([k, d, m, tt, ts, sp]) => {
      setKpis(k); setDaily(d); setMonthly(m); setTableTypes(tt); setTableStats(ts); setStaffPerf(sp);
    }).catch(console.error).finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="flex items-center justify-center h-64"><div className="w-10 h-10 border-4 border-cyan/30 border-t-cyan rounded-full animate-spin" /></div>;

  const shortMoney = v => v >= 1e6 ? (v / 1e6).toFixed(1) + 'tr' : v >= 1e3 ? (v / 1e3).toFixed(0) + 'k' : v;
  const chartColors = { grid: 'rgba(255,255,255,0.05)', tick: '#64748b' };

  function renderChange(val) {
    if (val == null) return null;
    const up = val >= 0;
    return <span className={`text-xs px-2 py-0.5 rounded-full ${up ? 'bg-emerald-500/20 text-emerald-400' : 'bg-red-500/20 text-red-400'}`}>{up ? '↑' : '↓'} {Math.abs(val).toFixed(1)}%</span>;
  }

  const kpiCards = kpis ? [
    { label: 'Hôm nay', value: kpis.todayRevenue, change: kpis.todayChange, css: 'kpi-cyan' },
    { label: 'Tuần này', value: kpis.weekRevenue, change: kpis.weekChange, css: 'kpi-emerald' },
    { label: 'Tháng này', value: kpis.monthRevenue, change: kpis.monthChange, css: 'kpi-purple' },
  ] : [];

  const barConfig = {
    labels: daily.map(d => { const p = d.date?.split('-'); return p ? `${p[2]}/${p[1]}` : ''; }),
    datasets: [{ label: 'Doanh thu', data: daily.map(d => d.revenue || 0), backgroundColor: 'rgba(6, 182, 212, 0.7)', borderColor: '#06b6d4', borderWidth: 1, borderRadius: 6 }],
  };
  const lineConfig = {
    labels: monthly.map(d => { const p = d.month?.split('-'); return p ? `T${parseInt(p[1])}/${p[0].substring(2)}` : ''; }),
    datasets: [{ label: 'Doanh thu', data: monthly.map(d => d.revenue || 0), borderColor: '#10b981', backgroundColor: 'rgba(16, 185, 129, 0.1)', borderWidth: 2.5, fill: true, tension: 0.3, pointRadius: 4, pointBackgroundColor: '#10b981' }],
  };
  const pieConfig = {
    labels: tableTypes.map(d => d.tableType),
    datasets: [{ data: tableTypes.map(d => d.revenue || 0), backgroundColor: ['#06b6d4', '#10b981', '#f43f5e', '#f59e0b', '#8b5cf6'], borderWidth: 2, borderColor: '#0f172a' }],
  };

  const chartOpts = (yFmt) => ({
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { display: false }, tooltip: { callbacks: { label: ctx => formatMoney(ctx.parsed?.y ?? ctx.parsed) } } },
    scales: yFmt ? {
      x: { grid: { color: chartColors.grid }, ticks: { color: chartColors.tick, font: { size: 11 } } },
      y: { grid: { color: chartColors.grid }, ticks: { color: chartColors.tick, callback: v => shortMoney(v) } },
    } : undefined,
  });

  return (
    <div className="space-y-6 animate-in">
      <h2 className="text-2xl font-bold text-white flex items-center gap-2">📊 Báo Cáo & Thống Kê</h2>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {kpiCards.map((k, i) => (
          <div key={i} className={`kpi-card ${k.css} text-white`}>
            <div className="text-xs font-medium opacity-80 mb-1">{k.label}</div>
            <div className="text-2xl font-extrabold">{formatMoney(k.value)}</div>
            <div className="mt-1">{renderChange(k.change)}</div>
          </div>
        ))}
      </div>

      {/* Charts Row 1 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="glass-card p-5">
          <h3 className="text-base font-bold text-white mb-4">📊 Doanh Thu 7 Ngày</h3>
          <div className="h-72"><Bar data={barConfig} options={chartOpts(true)} /></div>
        </div>
        <div className="glass-card p-5">
          <h3 className="text-base font-bold text-white mb-4">📈 Doanh Thu 12 Tháng</h3>
          <div className="h-72"><Line data={lineConfig} options={chartOpts(true)} /></div>
        </div>
      </div>

      {/* Charts Row 2 */}
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        <div className="lg:col-span-2 glass-card p-5">
          <h3 className="text-base font-bold text-white mb-4">🥧 Doanh Thu Theo Loại Bàn</h3>
          <div className="h-64"><Pie data={pieConfig} options={{ responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { color: '#94a3b8', padding: 12, usePointStyle: true } }, tooltip: { callbacks: { label: ctx => ctx.label + ': ' + formatMoney(ctx.parsed) } } } }} /></div>
        </div>
        <div className="lg:col-span-3 glass-card p-5">
          <h3 className="text-base font-bold text-white mb-4">🎱 Thống Kê Bàn Chơi</h3>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead><tr className="text-slate-400 text-xs uppercase border-b border-white/10">
                <th className="text-left py-2 px-3">#</th><th className="text-left py-2 px-3">Bàn</th><th className="text-center py-2 px-3">Phiên</th><th className="text-right py-2 px-3">Doanh thu</th>
              </tr></thead>
              <tbody>
                {tableStats.map((r, i) => (
                  <tr key={i} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                    <td className="py-2.5 px-3 text-slate-500 font-semibold">{i + 1}</td>
                    <td className="py-2.5 px-3 font-semibold text-white">{r.tableId || '-'}</td>
                    <td className="py-2.5 px-3 text-center"><span className="px-2.5 py-1 rounded-full bg-cyan/20 text-cyan-400 text-xs font-bold">{r.sessionCount || 0}</span></td>
                    <td className="py-2.5 px-3 text-right font-bold text-emerald-400">{formatMoney(r.totalRevenue)}</td>
                  </tr>
                ))}
                {tableStats.length === 0 && <tr><td colSpan={4} className="py-6 text-center text-slate-500">Chưa có dữ liệu</td></tr>}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Staff Performance */}
      <div className="glass-card p-5">
        <h3 className="text-base font-bold text-white mb-4">👤 Hiệu Suất Nhân Viên</h3>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead><tr className="text-slate-400 text-xs uppercase border-b border-white/10">
              <th className="text-left py-2 px-3">#</th><th className="text-left py-2 px-3">Nhân viên</th><th className="text-center py-2 px-3">Phiên phục vụ</th><th className="text-right py-2 px-3">Doanh thu</th>
            </tr></thead>
            <tbody>
              {staffPerf.map((r, i) => (
                <tr key={i} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                  <td className="py-2.5 px-3 text-slate-500 font-semibold">{i + 1}</td>
                  <td className="py-2.5 px-3 font-semibold text-white">👤 {r.staffName || 'N/A'}</td>
                  <td className="py-2.5 px-3 text-center"><span className="px-2.5 py-1 rounded-full bg-purple/20 text-purple-400 text-xs font-bold">{r.sessionCount || 0}</span></td>
                  <td className="py-2.5 px-3 text-right font-bold text-emerald-400">{formatMoney(r.totalRevenue)}</td>
                </tr>
              ))}
              {staffPerf.length === 0 && <tr><td colSpan={4} className="py-6 text-center text-slate-500">Chưa có dữ liệu</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
