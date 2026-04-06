import { useState, useEffect } from 'react';
import { tableApi, customerApi, productApi, formatMoney, formatTime } from '../api';

export default function TablesPage({ user }) {
  const [tables, setTables] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [products, setProducts] = useState([]);
  
  const [loading, setLoading] = useState(true);
  const [activeTable, setActiveTable] = useState(null);
  const [modalType, setModalType] = useState(null); // 'START', 'MANAGE', 'CHECKOUT', 'TRANSFER'
  const [orders, setOrders] = useState([]);

  // Form states
  const [selectedCustomerId, setSelectedCustomerId] = useState('');
  const [selectedProductId, setSelectedProductId] = useState('');
  const [orderQuantity, setOrderQuantity] = useState(1);
  const [targetTableId, setTargetTableId] = useState('');
  const [tableFormData, setTableFormData] = useState({ name: '', tableType: 'POOL' });
  
  // Checkout states
  const [checkoutResult, setCheckoutResult] = useState(null);
  const [discountCode, setDiscountCode] = useState('');
  const [manualTableCharge, setManualTableCharge] = useState('');

  // ─── Lấy dữ liệu định kỳ ───
  useEffect(() => {
    loadInitialData();
    const interval = setInterval(loadTables, 5000); // Polling mỗi 5s
    return () => clearInterval(interval);
  }, []);

  async function loadInitialData() {
    try {
      const [tbls, custs, prods] = await Promise.all([
        tableApi.getAll(),
        customerApi.getAll(),
        productApi.getAll()
      ]);
      setTables(tbls);
      setCustomers(custs);
      setProducts(prods);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  async function loadTables() {
    try {
      const tbls = await tableApi.getAll();
      setTables(tbls);
      // Cập nhật activeTable nếu đang mở modal để lấy time/price mới nhất
      setActiveTable(prev => prev ? tbls.find(t => t.id === prev.id) || prev : null);
    } catch (e) {}
  }

  async function loadOrders(tableId) {
    try {
      const res = await tableApi.getOrders(tableId);
      setOrders(res);
    } catch (e) {
      console.error(e);
    }
  }

  // ─── Actions thao tác bàn ───
  function openTable(table) {
    setActiveTable(table);
    if (table.status === 'AVAILABLE' || table.status === 'RESERVED') {
      setModalType('START');
    } else if (table.status === 'PLAYING' || table.status === 'PAUSED') {
      setModalType('MANAGE');
      loadOrders(table.id);
    } else if (table.status === 'MAINTENANCE') {
      if (confirm('Bàn đang bảo trì. Bạn có muốn mở lại bàn này không?')) {
        handleToggleMaintenance(table.id, false);
      }
    } else {
      setModalType(null);
      alert('Trạng thái bàn không hợp lệ.');
    }
  }

  function closeModal() {
    setModalType(null);
    setActiveTable(null);
    setCheckoutResult(null);
    setOrders([]);
    setSelectedCustomerId('');
    setSelectedProductId('');
    setOrderQuantity(1);
    setDiscountCode('');
    setManualTableCharge('');
    setTableFormData({ name: '', tableType: 'POOL' });
  }

  async function handleStartSession() {
    try {
      await tableApi.start(activeTable.id, selectedCustomerId || null);
      await loadTables();
      closeModal();
    } catch (e) {
      alert('Lỗi: ' + e.message);
    }
  }

  async function handlePauseResume() {
    try {
      if (activeTable.status === 'PLAYING') {
        await tableApi.pause(activeTable.id);
      } else {
        await tableApi.resume(activeTable.id);
      }
      await loadTables();
    } catch (e) {
      alert('Lỗi: ' + e.message);
    }
  }

  async function handleAddOrder() {
    if (!selectedProductId || orderQuantity < 1) return;
    try {
      await tableApi.addOrder(activeTable.id, selectedProductId, orderQuantity);
      await loadOrders(activeTable.id);
      await loadTables();
      setSelectedProductId('');
      setOrderQuantity(1);
    } catch (e) {
      alert('Lỗi gọi món: ' + e.message);
    }
  }

  async function handleRemoveOrder(orderItemId) {
    if (!confirm('Bạn có chắc muốn hủy món này?')) return;
    try {
      await tableApi.removeOrder(orderItemId);
      await loadOrders(activeTable.id);
      await loadTables();
    } catch (e) {
      alert('Lỗi hủy món: ' + e.message);
    }
  }

  async function handleTransfer() {
    if (!targetTableId) return;
    try {
      await tableApi.transfer(activeTable.id, targetTableId);
      await loadTables();
      closeModal();
      alert('Đã chuyển bàn thành công!');
    } catch (e) {
      alert('Lỗi chuyển bàn: ' + e.message);
    }
  }

  async function handleToggleMaintenance(tableId, enable) {
    try {
      await tableApi.maintenance(tableId || activeTable.id, enable);
      await loadTables();
      if (modalType === 'MANAGE') closeModal();
    } catch (e) {
      alert('Lỗi thay đổi trạng thái bảo trì: ' + e.message);
    }
  }

  async function handleCheckout() {
    try {
      const res = await tableApi.end(activeTable.id, discountCode, manualTableCharge);
      setCheckoutResult(res);
      await loadTables();
    } catch (e) {
      alert('Lỗi thanh toán: ' + e.message);
    }
  }

  // ─── Admin Actions ───
  function openAddTable() {
    setTableFormData({ name: '', tableType: 'POOL' });
    setModalType('ADD_TABLE');
  }

  function openEditTable(e, table) {
    e.stopPropagation();
    setActiveTable(table);
    setTableFormData({ name: table.name, tableType: table.tableType });
    setModalType('EDIT_TABLE');
  }

  async function handleSaveTable(e) {
    e.preventDefault();
    try {
      if (modalType === 'ADD_TABLE') {
        await tableApi.create(tableFormData);
      } else {
        await tableApi.update(activeTable.id, tableFormData);
      }
      await loadTables();
      closeModal();
    } catch (e) {
      alert('Lỗi lưu bàn: ' + e.message);
    }
  }

  async function handleDeleteTable() {
    if (!confirm(`Bạn có chắc chắn muốn XÓA bàn [${activeTable.name}]? Thao tác này không thể hoàn tác.`)) return;
    try {
      await tableApi.delete(activeTable.id);
      await loadTables();
      closeModal();
    } catch (e) {
      alert('Lỗi xóa bàn: ' + e.message);
    }
  }

  // ─── Rendering Helpers ───
  function getCardClass(status) {
    const map = { PLAYING: 'table-card-playing', PAUSED: 'table-card-paused', RESERVED: 'table-card-reserved', MAINTENANCE: 'table-card-maintenance' };
    return map[status] || 'table-card-available';
  }

  function formatDateTime(isoString) {
    if (!isoString) return '';
    const d = new Date(isoString);
    return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' + d.toLocaleDateString('vi-VN');
  }

  if (loading) {
    return <div className="flex justify-center h-40 items-center"><div className="w-10 h-10 border-4 animate-spin border-t-cyan border-cyan/30 rounded-full" /></div>;
  }

  return (
    <div className="space-y-6 animate-in">
      <div className="flex items-center justify-between gap-4">
        <h2 className="text-2xl font-bold text-white flex items-center gap-2">🎱 Quản Lý Bàn Chơi</h2>
        <div className="flex flex-1 justify-center gap-4 text-[10px] sm:text-xs font-semibold whitespace-nowrap overflow-x-auto no-scrollbar py-1">
           <div className="flex items-center gap-1.5"><span className="w-2.5 h-2.5 rounded-full bg-emerald-500" /> Trống</div>
           <div className="flex items-center gap-1.5"><span className="w-2.5 h-2.5 rounded-full bg-red-500 animate-pulse" /> Đang chơi</div>
           <div className="flex items-center gap-1.5"><span className="w-2.5 h-2.5 rounded-full bg-amber-500" /> Tạm dừng</div>
        </div>
        {user?.role === 'ADMIN' && (
          <button 
            onClick={openAddTable}
            className="px-4 py-2 bg-cyan-600 hover:bg-cyan-500 text-white font-bold rounded-lg whitespace-nowrap text-sm shadow-lg shadow-cyan-900/40"
          >
            + Thêm Bàn Mới
          </button>
        )}
      </div>

      {/* Grid of Tables */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 xl:grid-cols-6 gap-4">
        {tables.map(table => (
          <div
            key={table.id}
            onClick={() => openTable(table)}
            className={`${getCardClass(table.status)} rounded-xl p-4 cursor-pointer transition-all duration-300 hover:scale-105 relative select-none shadow-lg`}
          >
            {table.orderCount > 0 && (
              <div className="absolute -top-2 -right-2 w-6 h-6 rounded-full bg-red-500 text-xs font-bold text-white flex items-center justify-center border-2 border-navy-900 shadow-md">
                {table.orderCount}
              </div>
            )}
            
            <div className="flex items-center justify-between mb-3 border-b border-white/10 pb-2">
              <span className="font-extrabold text-white text-base tracking-wide flex items-center gap-2">
                {table.name}
                {user?.role === 'ADMIN' && table.status === 'AVAILABLE' && (
                  <button 
                    onClick={(e) => openEditTable(e, table)}
                    className="p-1 hover:bg-white/10 rounded-md transition-colors"
                  >
                    ✏️
                  </button>
                )}
              </span>
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-white/10 font-bold uppercase text-slate-300">
                {table.tableType}
              </span>
            </div>

            {(table.status === 'PLAYING' || table.status === 'PAUSED') ? (
              <div className="space-y-1 relative z-10">
                <div className="text-3xl font-extrabold text-white font-mono drop-shadow-lg">
                  {formatTime(table.playingMinutes)}
                </div>
                <div className="text-sm font-bold text-amber-300">
                  {formatMoney(table.currentAmount)}
                </div>
                {table.customerName && (
                  <div className="text-xs text-cyan-200 font-medium truncate mt-2 bg-black/20 p-1 rounded">
                    👤 {table.customerName}
                  </div>
                )}
              </div>
            ) : (
              <div className="text-center py-4 opacity-70">
                <div className="text-4xl mb-1 text-emerald-400">▶</div>
                <div className="text-xs font-medium text-emerald-200">Nhấn để mở bàn</div>
              </div>
            )}
          </div>
        ))}
      </div>

      {/* ─── MODAL START ─── */}
      {modalType === 'START' && activeTable && (
        <div className="fixed inset-0 z-50 bg-black/70 flex items-start sm:items-center justify-center p-4 overflow-y-auto py-6 sm:py-10 animate-in scroll-smooth">
          <div className="glass-card w-full max-w-md p-6 relative">
            <button onClick={closeModal} className="absolute top-4 right-4 text-slate-400 hover:text-white text-xl">✕</button>
            <h3 className="text-xl font-bold text-white mb-4">▶ Bắt Đầu - {activeTable.name}</h3>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm text-slate-400 mb-1">Khách Hàng (Tùy chọn)</label>
                <select 
                  value={selectedCustomerId} 
                  onChange={e => setSelectedCustomerId(e.target.value)}
                  className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white"
                >
                  <option value="">-- Khách lẻ --</option>
                  {customers.map(c => <option key={c.id} value={c.id}>{c.name} - {c.phone}</option>)}
                </select>
              </div>
              <div className="pt-4 flex gap-3">
                <button onClick={() => handleToggleMaintenance(activeTable.id, true)} className="flex-1 py-2.5 rounded-lg font-bold bg-slate-800 text-slate-400 hover:bg-slate-700">Bảo Trì</button>
                <button onClick={handleStartSession} className="flex-[2] py-2.5 rounded-lg font-bold bg-emerald-600 hover:bg-emerald-500 text-white">Mở Bàn Ngay</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ─── MODAL MANAGE (Tương tác bàn đang chơi) ─── */}
      {modalType === 'MANAGE' && activeTable && (
        <div className="fixed inset-0 z-50 bg-black/70 flex items-start sm:items-center justify-center p-4 overflow-y-auto py-6 sm:py-10 animate-in scroll-smooth">
          <div className="glass-card w-full max-w-3xl flex flex-col max-h-[90vh]">
            {/* Header */}
            <div className="p-4 border-b border-white/10 flex justify-between items-center bg-black/20 rounded-t-xl">
              <h3 className="text-xl font-bold text-white flex items-center gap-2">
                {activeTable.name} 
                <span className={`text-xs px-2 py-0.5 rounded-full uppercase ${activeTable.status === 'PLAYING' ? 'bg-red-500/20 text-red-400' : 'bg-amber-500/20 text-amber-400'}`}>
                  {activeTable.status}
                </span>
              </h3>
              <button onClick={closeModal} className="text-slate-400 hover:text-white text-2xl leading-none">✕</button>
            </div>
            
            <div className="flex-1 overflow-y-auto p-5 grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Cột trái: Thông tin & Thao tác */}
              <div className="space-y-6">
                <div className="bg-navy-900/50 p-4 rounded-xl border border-white/5">
                  <div className="text-slate-400 text-xs mb-1">Thời gian chơi</div>
                  <div className="text-4xl font-mono font-extrabold text-white">{formatTime(activeTable.playingMinutes)}</div>
                  
                  <div className="text-slate-400 text-xs mt-3 mb-1">Tạm tính</div>
                  <div className="text-2xl font-bold text-amber-400">{formatMoney(activeTable.currentAmount)}</div>

                  {activeTable.customerName && (
                    <div className="mt-3 py-1.5 px-3 bg-cyan-900/30 text-cyan-300 rounded text-sm font-medium border border-cyan-500/20">
                      Khách: {activeTable.customerName}
                    </div>
                  )}
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <button 
                    onClick={handlePauseResume}
                    className="py-3 rounded-lg font-bold bg-amber-500/10 text-amber-400 border border-amber-500/30 hover:bg-amber-500/20"
                  >
                    {activeTable.status === 'PLAYING' ? '⏸ Tạm Dừng' : '▶ Tiếp Tục'}
                  </button>
                  <button 
                    onClick={() => setModalType('TRANSFER')}
                    className="py-3 rounded-lg font-bold bg-blue-500/10 text-blue-400 border border-blue-500/30 hover:bg-blue-500/20"
                  >
                    🔁 Chuyển Bàn
                  </button>
                  <button 
                    onClick={() => handleToggleMaintenance(activeTable.id, true)}
                    className="py-3 rounded-lg font-bold bg-slate-500/10 text-slate-400 border border-slate-500/30 hover:bg-slate-500/20"
                  >
                    🛠 Bảo Trì
                  </button>
                  <button 
                    onClick={() => setModalType('CHECKOUT')}
                    className="py-3.5 rounded-lg font-bold bg-gradient-to-r from-emerald-600 to-teal-500 text-white shadow-lg hover:to-teal-400 text-lg flex-1"
                  >
                    💰 Bấm Thanh Toán
                  </button>
                </div>
              </div>

              {/* Cột phải: Order món */}
              <div className="flex flex-col h-full">
                <h4 className="font-bold text-white mb-3">🍺 Gọi Món Đồ Uống/Đồ Ăn</h4>
                
                {/* Form gọi thêm */}
                <div className="flex flex-nowrap items-center gap-2 mb-4 bg-navy-900 p-2 rounded-lg border border-white/10">
                  <div className="flex-1 flex items-center gap-2 min-w-0">
                    {selectedProductId && products.find(p => p.id === parseInt(selectedProductId))?.imageUrl && (
                      <img 
                        src={products.find(p => p.id === parseInt(selectedProductId)).imageUrl} 
                        alt="p" 
                        className="w-8 h-8 rounded object-cover border border-white/10" 
                      />
                    )}
                    <select 
                      value={selectedProductId} onChange={e => setSelectedProductId(e.target.value)}
                      className="flex-1 bg-transparent text-white text-sm outline-none px-1 min-w-0"
                    >
                      <option value="" className="text-black">-- Chọn sản phẩm --</option>
                      {products.map(p => <option key={p.id} value={p.id} className="text-black">{p.name} - {formatMoney(p.price)}</option>)}
                    </select>
                  </div>
                  <input 
                    type="number" min="1" value={orderQuantity} onChange={e => setOrderQuantity(e.target.value)}
                    className="w-14 bg-white/5 border border-white/20 rounded px-1 py-1 text-white text-center shrink-0"
                  />
                  <button 
                    onClick={handleAddOrder}
                    className="shrink-0 px-4 py-1.5 bg-cyan-600 hover:bg-cyan-500 rounded text-white font-bold text-sm transition-colors"
                  >
                    Thêm
                  </button>
                </div>

                {/* Danh sách đã gọi */}
                <div className="flex-1 overflow-y-auto bg-navy-900/30 rounded-xl border border-white/5 p-2">
                  {orders.length === 0 ? (
                    <div className="h-full flex items-center justify-center text-slate-500 text-sm italic">Chưa gọi món nào</div>
                  ) : (
                    <ul className="space-y-2">
                      {orders.map(o => (
                        <li key={o.id} className="flex items-center justify-between p-2 rounded border border-white/5 bg-white/5 hover:bg-white/10 transition">
                          <div>
                            <div className="text-sm text-white font-medium">{o.productName}</div>
                            <div className="text-xs text-slate-400">{o.quantity} x {formatMoney(o.unitPrice)}</div>
                          </div>
                          <div className="flex items-center gap-3">
                            <span className="text-emerald-400 font-bold text-sm">{formatMoney(o.amount)}</span>
                            <button onClick={() => handleRemoveOrder(o.id)} className="text-red-400 hover:text-red-300 pl-2 border-l border-white/10">✕</button>
                          </div>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ─── MODAL TRANSFER ─── */}
      {modalType === 'TRANSFER' && activeTable && (
        <div className="fixed inset-0 z-[60] bg-black/80 flex items-start sm:items-center justify-center p-4 overflow-y-auto py-6 sm:py-10 animate-in scroll-smooth">
          <div className="glass-card w-full max-w-sm p-6 relative">
            <h3 className="text-xl font-bold text-white mb-4">🔁 Chuyển Bàn</h3>
            <div className="mb-2 text-slate-400 text-sm">Từ: <strong className="text-white">{activeTable.name}</strong></div>
            <label className="block text-sm text-slate-400 mb-1">Sang bàn trống:</label>
            <select 
              value={targetTableId} onChange={e => setTargetTableId(e.target.value)}
              className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white mb-6"
            >
              <option value="">-- Chọn bàn --</option>
              {tables.filter(t => t.status === 'AVAILABLE').map(t => (
                <option key={t.id} value={t.id}>{t.name} ({t.tableType})</option>
              ))}
            </select>
            <div className="flex gap-3">
              <button onClick={() => setModalType('MANAGE')} className="flex-1 py-2 rounded font-bold bg-white/10 hover:bg-white/20 text-white">Quay lại</button>
              <button 
                onClick={handleTransfer} disabled={!targetTableId} 
                className="flex-1 py-2 rounded font-bold bg-blue-600 hover:bg-blue-500 text-white disabled:opacity-50"
              >Chuyển ngay</button>
            </div>
          </div>
        </div>
      )}

      {/* ─── MODAL CHECKOUT & RECEIPT ─── */}
      {modalType === 'CHECKOUT' && activeTable && (
        <div className="fixed inset-0 z-[60] bg-black/80 flex items-start sm:items-center justify-center p-4 overflow-y-auto py-6 sm:py-10 animate-in scroll-smooth">
          <div className="bg-white w-full max-w-md rounded-lg shadow-2xl overflow-hidden print-area text-slate-800">
            {checkoutResult ? (
              /* Biên lai thanh toán thành công */
              <div className="p-8">
                <div className="text-center mb-6 border-b-2 border-dashed border-slate-300 pb-4">
                  <h2 className="text-2xl font-black text-slate-800 uppercase tracking-widest">BILLIARDS HALL</h2>
                  <p className="text-sm text-slate-500 mt-1">Hóa đơn thanh toán</p>
                </div>
                
                <div className="space-y-3 mb-6 font-mono text-sm">
                  <div className="flex justify-between"><span>Mã hóa đơn:</span> <strong className="select-all">{checkoutResult.invoiceNumber}</strong></div>
                  <div className="flex justify-between"><span>Bàn:</span> <strong>{activeTable.name}</strong></div>
                  <div className="flex justify-between text-[11px] text-slate-500 border-t border-slate-100 pt-1 mt-1"><span>Giờ vào:</span> <span>{formatDateTime(checkoutResult.startTime)}</span></div>
                  <div className="flex justify-between text-[11px] text-slate-500 pb-1"><span>Giờ ra:</span> <span>{formatDateTime(checkoutResult.endTime)}</span></div>
                  <div className="flex justify-between"><span>Tiền giờ:</span> <span>{formatMoney(checkoutResult.tableCharge)}</span></div>
                  <div className="flex justify-between"><span>Tiền đồ uống:</span> <span>{formatMoney(checkoutResult.serviceCharge)}</span></div>
                  {(checkoutResult.discount > 0 || checkoutResult.codeDiscountAmount > 0) && (
                    <div className="flex justify-between text-rose-600 font-bold border-t border-slate-200 mt-2 pt-2">
                       <span>Tổng giảm:</span> <span>- {formatMoney((checkoutResult.discount || 0) + (checkoutResult.codeDiscountAmount || 0))}</span>
                    </div>
                  )}
                  <div className="flex justify-between text-xl font-extrabold border-t-2 border-slate-800 pt-3 mt-3">
                    <span>TỔNG CỘNG:</span> <span className="text-emerald-600">{formatMoney(checkoutResult.invoiceTotal)}</span>
                  </div>
                </div>

                <button onClick={closeModal} className="w-full py-3 bg-slate-800 text-white font-bold rounded hover:bg-slate-700">Đóng Hóa Đơn</button>
              </div>
            ) : (
              /* Form xác nhận thanh toán */
              <div className="bg-navy-800 p-6 text-white h-full border border-white/10 rounded-lg">
                <h3 className="text-2xl font-bold mb-4 font-mono text-center pb-2 border-b border-white/10">💰 Tính Tiền {activeTable.name}</h3>
                <div className="bg-black/30 rounded-xl p-5 mb-5 space-y-3 font-mono text-lg">
                  <div className="flex justify-between text-slate-300"><span>Tiền giờ ({formatTime(activeTable.playingMinutes)}):</span> <span>{formatMoney(activeTable.currentAmount)}</span></div>
                  <div className="flex justify-between font-extrabold text-2xl text-emerald-400 border-t border-white/10 pt-3 mt-3">
                    <span>Tổng:</span> <span>{formatMoney(activeTable.currentAmount)}</span>
                  </div>
                </div>
                
                <div className="grid grid-cols-2 gap-4 mb-6">
                  <div>
                    <label className="block text-sm text-slate-400 mb-1">Mã giảm giá:</label>
                    <input 
                      type="text" value={discountCode} onChange={e => setDiscountCode(e.target.value.toUpperCase())}
                      className="w-full bg-black/20 border border-white/20 rounded p-2.5 text-white focus:border-cyan-400 outline-none uppercase font-mono"
                      placeholder="VD: VIP10"
                    />
                  </div>
                  <div>
                    <label className="block text-sm text-slate-400 mb-1">Giá bàn thủ công:</label>
                    <input 
                      type="number" value={manualTableCharge} onChange={e => setManualTableCharge(e.target.value)}
                      className="w-full bg-black/20 border border-white/20 rounded p-2.5 text-white focus:border-cyan-400 outline-none font-mono"
                      placeholder="VD: 50000"
                    />
                  </div>
                </div>

                <div className="flex gap-3 mt-4">
                  <button onClick={() => setModalType('MANAGE')} className="flex-1 py-3 rounded-lg font-bold bg-white/10 hover:bg-white/20">Quay lại</button>
                  <button onClick={handleCheckout} className="flex-[2] py-3 rounded-lg font-extrabold bg-emerald-600 hover:bg-emerald-500 shadow-xl shadow-emerald-900/50">Xác Nhận Thu Tiền</button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
      {/* ─── MODAL ADD/EDIT TABLE (Admin Only) ─── */}
      {(modalType === 'ADD_TABLE' || modalType === 'EDIT_TABLE') && (
        <div className="fixed inset-0 z-50 bg-black/70 flex items-center justify-center p-4 animate-in">
          <div className="glass-card w-full max-w-md p-6 relative">
            <button onClick={closeModal} className="absolute top-4 right-4 text-slate-400 hover:text-white text-xl">✕</button>
            <h3 className="text-xl font-bold text-white mb-6">
              {modalType === 'ADD_TABLE' ? '🎱 Thêm Bàn Bida Mới' : '✏️ Sửa Thông Tin Bàn'}
            </h3>
            
            <form onSubmit={handleSaveTable} className="space-y-4">
              <div>
                <label className="block text-sm text-slate-400 mb-1">Tên bàn *</label>
                <input 
                  required type="text" 
                  value={tableFormData.name} 
                  onChange={e => setTableFormData({...tableFormData, name: e.target.value})} 
                  placeholder="VD: Bàn 01"
                  className="w-full bg-navy-900 border border-white/10 rounded-lg p-3 text-white focus:border-cyan-500 outline-none" 
                />
              </div>
              <div>
                <label className="block text-sm text-slate-400 mb-1">Loại bàn *</label>
                <select 
                  value={tableFormData.tableType} 
                  onChange={e => setTableFormData({...tableFormData, tableType: e.target.value})} 
                  className="w-full bg-navy-900 border border-white/10 rounded-lg p-3 text-white focus:border-cyan-500 outline-none"
                >
                  <option value="POOL">Bàn Lỗ (POOL)</option>
                  <option value="CAROM">Bàn Carom (3 Băng / Phăng)</option>
                  <option value="VIP">Bàn VIP</option>
                </select>
              </div>
              
              <div className="pt-4 flex gap-3">
                {modalType === 'EDIT_TABLE' && (
                  <button 
                    type="button" 
                    onClick={handleDeleteTable} 
                    className="px-4 py-3 rounded-lg font-bold bg-red-500/10 text-red-500 border border-red-500/20 hover:bg-red-500 hover:text-white transition-all"
                  >
                    🗑️
                  </button>
                )}
                <button 
                  type="button" 
                  onClick={closeModal} 
                  className="flex-1 py-3 rounded-lg font-bold bg-white/5 text-slate-400 hover:bg-white/10"
                >
                  Hủy
                </button>
                <button 
                  type="submit" 
                  className="flex-[2] py-3 rounded-lg font-bold bg-cyan-600 hover:bg-cyan-500 text-white shadow-lg shadow-cyan-900/40"
                >
                  Lưu Lại
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
