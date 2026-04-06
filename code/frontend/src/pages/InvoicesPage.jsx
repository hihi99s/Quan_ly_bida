import { useState, useEffect } from 'react';
import { invoiceApi, formatMoney } from '../api';

export default function InvoicesPage() {
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selectedInvoice, setSelectedInvoice] = useState(null);

  useEffect(() => {
    loadInvoices();
  }, []);

  async function loadInvoices() {
    try {
      const data = await invoiceApi.getAll();
      setInvoices(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  async function handleViewDetail(id) {
    try {
      const detail = await invoiceApi.getById(id);
      setSelectedInvoice(detail);
    } catch (e) {
      alert("Lỗi tải chi tiết: " + e.message);
    }
  }

  const filtered = invoices.filter(inv => 
    search === '' || 
    inv.invoiceNumber?.toLowerCase().includes(search.toLowerCase()) ||
    inv.tableName?.toLowerCase().includes(search.toLowerCase())
  );

  if (loading) return <div className="flex justify-center h-40 items-center"><div className="w-10 h-10 border-4 animate-spin border-t-cyan border-cyan/30 rounded-full" /></div>;

  return (
    <>
      <div className="space-y-6 animate-in">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <h2 className="text-2xl font-bold text-white flex items-center gap-2">🧾 Lịch Sử Hóa Đơn</h2>
          <div className="relative w-full sm:w-72">
            <input 
              type="text" 
              placeholder="Tìm mã hóa đơn, tên bàn..." 
              value={search}
              onChange={e => setSearch(e.target.value)}
              className="w-full bg-white/5 border border-white/10 rounded-lg pl-10 pr-4 py-2.5 text-white outline-none focus:border-cyan-500"
            />
            <span className="absolute left-3 top-2.5 text-slate-400">🔍</span>
          </div>
        </div>

        <div className="glass-card overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-white/5 border-b border-white/10 text-xs uppercase text-slate-400 font-bold">
                  <th className="p-4 rounded-tl-xl text-center">STT</th>
                  <th className="p-4">Mã Hóa Đơn</th>
                  <th className="p-4">Thời gian</th>
                  <th className="p-4">Bàn chơi</th>
                  <th className="p-4 text-right">Giảm giá</th>
                  <th className="p-4 text-right">Tổng Tiền</th>
                  <th className="p-4 rounded-tr-xl text-center">Thao tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5 whitespace-nowrap">
                {filtered.map((inv, idx) => (
                  <tr key={inv.id} className="hover:bg-white/5 transition-colors">
                    <td className="p-4 text-center text-slate-500">{idx + 1}</td>
                    <td className="p-4 font-mono font-bold text-cyan-400">{inv.invoiceNumber}</td>
                    <td className="p-4 text-slate-300">{new Date(inv.createdAt).toLocaleString('vi-VN')}</td>
                    <td className="p-4">
                      <span className="px-2 py-1 bg-white/10 rounded text-xs font-bold text-slate-200">
                        {inv.tableName || '---'}
                      </span>
                    </td>
                    <td className="p-4 text-right text-rose-400">
                      {inv.discount > 0 ? `-${formatMoney(inv.discount)}` : '0đ'}
                    </td>
                    <td className="p-4 text-right font-black text-emerald-400 text-lg">
                      {formatMoney(inv.totalAmount)}
                    </td>
                    <td className="p-4 text-center">
                      <button onClick={() => handleViewDetail(inv.id)} className="text-sm px-3 py-1.5 bg-blue-500/20 text-blue-400 hover:bg-blue-500 hover:text-white rounded transition">Xem Chi Tiết</button>
                    </td>
                  </tr>
                ))}
                {filtered.length === 0 && (
                  <tr><td colSpan="7" className="p-8 text-center text-slate-500 italic">Không tìm thấy hóa đơn nào phù hợp.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {selectedInvoice && (
        <div className="fixed inset-0 z-[60] bg-black/80 flex items-start sm:items-center justify-center p-4 overflow-y-auto backdrop-blur-sm">
          <div className="bg-white w-full max-w-md rounded-lg shadow-2xl overflow-hidden text-slate-800 my-auto animate-in fade-in zoom-in duration-200">
            <div className="p-8">
              <div className="text-center mb-6 border-b-2 border-dashed border-slate-300 pb-4 relative">
                <button onClick={() => setSelectedInvoice(null)} className="absolute -top-4 right-0 text-slate-400 hover:text-red-500 text-2xl leading-none">✕</button>
                <h2 className="text-2xl font-black text-slate-800 uppercase tracking-widest">BILLIARDS HALL</h2>
                <p className="text-sm text-slate-500 mt-1">Hóa đơn thanh toán (Bản Nháp)</p>
              </div>
              
              <div className="space-y-3 mb-6 font-mono text-sm leading-relaxed">
                <div className="flex justify-between"><span>Hóa đơn:</span> <strong className="select-all">{selectedInvoice.invoiceNumber}</strong></div>
                <div className="flex justify-between"><span>Thời gian:</span> <span>{new Date(selectedInvoice.createdAt).toLocaleString('vi-VN')}</span></div>
                <div className="flex justify-between"><span>Bàn:</span> <strong>{selectedInvoice.tableName || '---'}</strong></div>
                <div className="flex justify-between"><span>Nhân viên:</span> <span>{selectedInvoice.staffName || 'Admin'}</span></div>
                
                <div className="border-t border-slate-200 mt-3 pt-3">
                  <div className="flex justify-between"><span>Tiền giờ:</span> <span>{formatMoney(selectedInvoice.tableCharge)}</span></div>
                  <div className="flex justify-between"><span>Tiền đồ ăn/uống:</span> <span>{formatMoney(selectedInvoice.serviceCharge)}</span></div>
                </div>

                {(selectedInvoice.discount > 0 || selectedInvoice.codeDiscountAmount > 0) && (
                  <div className="flex justify-between text-rose-600 font-bold border-t border-slate-200 pt-3 mt-3">
                      <span>Giảm giá:</span> <span>- {formatMoney((selectedInvoice.discount || 0) + (selectedInvoice.codeDiscountAmount || 0))}</span>
                  </div>
                )}
                
                <div className="flex justify-between text-xl font-extrabold border-t-2 border-slate-800 pt-3 mt-3">
                  <span>TỔNG CỘNG:</span> <span className="text-emerald-600">{formatMoney(selectedInvoice.totalAmount)}</span>
                </div>
              </div>

              <div className="flex gap-2">
                <button onClick={() => setSelectedInvoice(null)} className="flex-1 py-3 bg-slate-200 text-slate-700 font-bold rounded hover:bg-slate-300 transition-colors">Đóng</button>
                <button onClick={() => window.print()} className="flex-1 py-3 bg-blue-600 text-white font-bold rounded hover:bg-blue-700 flex justify-center items-center gap-2 transition-colors">
                  🖨 In Bill
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
