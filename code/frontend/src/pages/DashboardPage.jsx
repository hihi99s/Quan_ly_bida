import { useState, useEffect } from 'react';
import { Line } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Filler, Tooltip } from 'chart.js';
import { dashboardApi, tableApi, formatMoney, formatTime } from '../api';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Filler, Tooltip);

export default function DashboardPage() {
  const [summary, setSummary] = useState(null);
  const [tables, setTables] = useState([]);
  const [chartData, setChartData] = useState(null);
  const [lowStock, setLowStock] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
    const interval = setInterval(() => {
      tableApi.getAll().then(setTables).catch(() => {});
    }, 15000);
    return () => clearInterval(interval);
  }, []);

  async function loadData() {
    try {
      const [sum, tbl, chart, stock] = await Promise.all([
        dashboardApi.getSummary(),
        tableApi.getAll(),
        dashboardApi.getRevenueChart(),
        dashboardApi.getLowStock(),
      ]);
      setSummary(sum);
      setTables(tbl);
      setChartData(chart);
      setLowStock(stock);
    } catch (e) {
      console.error('Dashboard load error:', e);
    } finally {
      setLoading(false);
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-10 h-10 border-4 border-cyan/30 border-t-cyan rounded-full animate-spin" />
      </div>
    );
  }

  const kpis = [
    { label: 'Tổng số bàn', value: summary?.totalTables ?? 0, icon: '🎱', css: 'kpi-emerald', sub: `${summary?.tablesAvailable ?? 0} trống` },
    { label: 'Đang chơi', value: summary?.tablesPlaying ?? 0, icon: '🔴', css: 'kpi-rose', sub: `${Math.round((summary?.tablesPlaying || 0) / (summary?.totalTables || 1) * 100)}% Occupancy` },
    { label: 'Doanh thu hôm nay', value: formatMoney(summary?.revenueToday), icon: '💰', css: 'kpi-cyan', sub: '' },
    { label: 'Hóa đơn hôm nay', value: summary?.ordersToday ?? 0, icon: '🧾', css: 'kpi-purple', sub: '' },
  ];

  // Chart config
  const dailyChart = chartData?.daily || [];
  const lineConfig = {
    labels: dailyChart.map(d => {
      const p = d.date?.split('-');
      return p ? `${p[2]}/${p[1]}` : '';
    }),
    datasets: [{
      label: 'Doanh thu',
      data: dailyChart.map(d => d.revenue || 0),
      borderColor: '#06b6d4',
      backgroundColor: 'rgba(6, 182, 212, 0.1)',
      borderWidth: 2.5,
      fill: true,
      tension: 0.4,
      pointRadius: 5,
      pointBackgroundColor: '#06b6d4',
      pointBorderColor: '#0f172a',
      pointBorderWidth: 2,
    }],
  };

  const lineOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false }, tooltip: { callbacks: { label: ctx => formatMoney(ctx.parsed.y) } } },
    scales: {
      x: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#64748b', font: { size: 11 } } },
      y: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#64748b', callback: v => v >= 1e6 ? (v / 1e6).toFixed(1) + 'tr' : v >= 1e3 ? (v / 1e3).toFixed(0) + 'k' : v } },
    },
  };

  function getCardClass(status) {
    const map = { PLAYING: 'table-card-playing', PAUSED: 'table-card-paused', RESERVED: 'table-card-reserved', MAINTENANCE: 'table-card-maintenance' };
    return map[status] || 'table-card-available';
  }

  function getStatusLabel(status) {
    const map = { AVAILABLE: 'Trống', PLAYING: 'Đang chơi', PAUSED: 'Tạm dừng', RESERVED: 'Đặt trước', MAINTENANCE: 'Bảo trì' };
    return map[status] || status;
  }

  function getStatusColor(status) {
    const map = { AVAILABLE: 'text-emerald-400', PLAYING: 'text-red-400', PAUSED: 'text-amber-400', RESERVED: 'text-blue-400', MAINTENANCE: 'text-gray-400' };
    return map[status] || 'text-gray-400';
  }

  return (
    <div className="space-y-6 animate-in">
      {/* ─── KPI Cards ─── */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {kpis.map((kpi, i) => (
          <div key={i} className={`kpi-card ${kpi.css} text-white`}>
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-medium opacity-80">{kpi.label}</span>
              <span className="text-xl">{kpi.icon}</span>
            </div>
            <div className="text-2xl lg:text-3xl font-extrabold">{kpi.value}</div>
            {kpi.sub && <div className="text-xs mt-1 opacity-70">{kpi.sub}</div>}
          </div>
        ))}
      </div>

      {/* ─── Table Grid + Revenue Chart ─── */}
      <div className="grid grid-cols-1 xl:grid-cols-5 gap-6">
        {/* Table Status - 3 cols */}
        <div className="xl:col-span-3">
          <div className="glass-card p-5">
            <h3 className="text-lg font-bold text-white mb-4 flex items-center gap-2">
              🎱 Trạng Thái Các Bàn
            </h3>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
              {tables.map(table => (
                <div
                  key={table.id}
                  className={`${getCardClass(table.status)} rounded-xl p-4 cursor-pointer transition-all duration-300 hover:-translate-y-1 relative`}
                >
                  {table.orderCount > 0 && (
                    <div className="absolute top-2 right-2 w-5 h-5 rounded-full bg-red-500 text-xs font-bold flex items-center justify-center">
                      {table.orderCount}
                    </div>
                  )}
                  <div className="flex items-center justify-between mb-2">
                    <span className="font-bold text-white text-sm">{table.name}</span>
                    <span className="text-[10px] px-2 py-0.5 rounded-full bg-white/10 font-semibold uppercase">
                      {table.tableType}
                    </span>
                  </div>

                  <div className={`text-xs font-semibold mb-2 ${getStatusColor(table.status)}`}>
                    ● {getStatusLabel(table.status)}
                  </div>

                  {(table.status === 'PLAYING' || table.status === 'PAUSED') && (
                    <div>
                      <div className="text-2xl font-extrabold text-white font-mono">
                        {formatTime(table.playingMinutes)}
                      </div>
                      <div className="text-sm font-bold text-amber-300 mt-1">
                        {formatMoney(table.currentAmount)}
                      </div>
                      {table.customerName && (
                        <div className="text-xs text-slate-300 mt-1">👤 {table.customerName}</div>
                      )}
                    </div>
                  )}

                  {table.status === 'AVAILABLE' && (
                    <div className="text-center mt-3 opacity-50">
                      <span className="text-3xl">▶</span>
                      <div className="text-xs mt-1">Nhấn để bắt đầu</div>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Revenue Chart - 2 cols */}
        <div className="xl:col-span-2">
          <div className="glass-card p-5 h-full">
            <h3 className="text-lg font-bold text-white mb-4 flex items-center gap-2">
              📈 Doanh Thu Tuần Này
            </h3>
            <div className="h-64">
              <Line data={lineConfig} options={lineOptions} />
            </div>
          </div>
        </div>
      </div>

      {/* ─── Low Stock Alerts ─── */}
      {lowStock.length > 0 && (
        <div className="glass-card p-5">
          <h3 className="text-lg font-bold text-amber-400 mb-3 flex items-center gap-2">
            ⚠️ Cảnh báo tồn kho thấp
          </h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            {lowStock.map(p => (
              <div key={p.id} className="bg-amber-900/20 border border-amber-500/30 rounded-xl p-3">
                <div className="font-semibold text-white text-sm">{p.name}</div>
                <div className="flex items-center justify-between mt-2">
                  <span className="text-xs text-slate-400">{p.category}</span>
                  <span className="text-sm font-bold text-amber-400">Còn {p.stockQuantity}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
