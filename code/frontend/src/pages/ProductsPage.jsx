import { useState, useEffect } from 'react';
import { productApi, formatMoney } from '../api';

export default function ProductsPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');
  const [search, setSearch] = useState('');

  // Form states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    name: '', category: 'DRINK', price: 0, stockQuantity: 0, imageUrl: ''
  });

  useEffect(() => {
    loadProducts();
  }, []);

  async function loadProducts() {
    try {
      const data = await productApi.getAll();
      setProducts(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  const filteredProducts = products.filter(p => 
    (filter === 'ALL' || p.category === filter) &&
    (search === '' || p.name.toLowerCase().includes(search.toLowerCase()))
  );

  const categories = ['ALL', 'DRINK', 'FOOD', 'SNACK', 'CARD', 'OTHER'];

  function openCreateModal() {
    setEditingId(null);
    setFormData({ name: '', category: 'DRINK', price: 0, stockQuantity: 0, imageUrl: '' });
    setIsModalOpen(true);
  }

  function openEditModal(p) {
    setEditingId(p.id);
    setFormData({ name: p.name, category: p.category, price: p.price, stockQuantity: p.stockQuantity, imageUrl: p.imageUrl || '' });
    setIsModalOpen(true);
  }

  async function handleDelete(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) return;
    try {
      await productApi.delete(id);
      loadProducts();
    } catch (e) {
      alert(e.message);
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      if (editingId) {
        await productApi.update(editingId, formData);
      } else {
        await productApi.create(formData);
      }
      setIsModalOpen(false);
      loadProducts();
    } catch (e) {
      alert(e.message);
    }
  }

  if (loading) {
    return <div className="flex justify-center h-40 items-center"><div className="w-10 h-10 border-4 animate-spin border-t-cyan border-cyan/30 rounded-full" /></div>;
  }

  return (
    <div className="space-y-6 animate-in">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <h2 className="text-2xl font-bold text-white flex items-center gap-2">🍺 Quản Lý Kho Sản Phẩm</h2>
        
        <div className="flex flex-wrap gap-2 w-full sm:w-auto">
          <input 
            type="text" 
            placeholder="Tìm kiếm..." 
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="flex-1 sm:flex-none bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white outline-none focus:border-cyan-500 w-full md:w-64"
          />
          <button 
            onClick={openCreateModal}
            className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white font-bold rounded-lg whitespace-nowrap"
          >
            + Thêm Mới
          </button>
        </div>
      </div>

      <div className="flex gap-2 overflow-x-auto pb-2 scrollbar-none">
        {categories.map(cat => (
          <button
            key={cat}
            onClick={() => setFilter(cat)}
            className={`px-4 py-2 rounded-full text-sm font-bold whitespace-nowrap transition-colors ${
              filter === cat ? 'bg-cyan-600 text-white shadow-lg shadow-cyan-900/50' : 'bg-white/5 text-slate-400 hover:bg-white/10 hover:text-white'
            }`}
          >
            {cat === 'ALL' ? 'Tất cả' : cat}
          </button>
        ))}
      </div>

      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 xl:grid-cols-6 gap-4">
        {filteredProducts.map(p => (
          <div key={p.id} className="glass-card overflow-hidden hover:scale-105 transition-transform duration-300 relative group flex flex-col">
            <div className="h-32 bg-navy-900 flex items-center justify-center p-4 relative">
              <span className="text-5xl opacity-80 group-hover:scale-110 transition-transform">
                {p.category === 'DRINK' ? '🥤' : p.category === 'FOOD' ? '🍜' : p.category === 'CARD' ? '🎴' : '📦'}
              </span>
              <div className={`absolute top-2 right-2 px-2 py-0.5 rounded-md text-xs font-bold ${
                p.stockQuantity <= 5 ? 'bg-red-500 text-white animate-pulse' : 'bg-navy-800/80 text-emerald-400 border border-emerald-500/30'
              }`}>
                Còn {p.stockQuantity}
              </div>
              
              {/* Thao tác hover */}
              <div className="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2">
                <button onClick={() => openEditModal(p)} className="p-2 bg-blue-500 hover:bg-blue-400 text-white rounded-full"><span className="text-sm">✏️</span></button>
                <button onClick={() => handleDelete(p.id)} className="p-2 bg-red-500 hover:bg-red-400 text-white rounded-full"><span className="text-sm">🗑</span></button>
              </div>
            </div>
            
            <div className="p-4 border-t border-white/5 flex-1 flex flex-col justify-between">
              <div>
                <h3 className="text-white font-bold text-sm truncate" title={p.name}>{p.name}</h3>
                <p className="text-xs text-slate-400 mt-1 mb-2">{p.category}</p>
              </div>
              <div className="text-lg font-black text-cyan-400">{formatMoney(p.price)}</div>
            </div>
          </div>
        ))}
        {filteredProducts.length === 0 && (
          <div className="col-span-full py-12 text-center text-slate-500 bg-white/5 rounded-xl border border-white/5">
            Không tìm thấy sản phẩm nào.
          </div>
        )}
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-black/70 flex items-center justify-center p-4 animate-in">
          <div className="glass-card w-full max-w-md p-6 relative">
            <button onClick={() => setIsModalOpen(false)} className="absolute top-4 right-4 text-slate-400 hover:text-white text-xl">✕</button>
            <h3 className="text-xl font-bold text-white mb-6">
              {editingId ? '✏ Cập nhật sản phẩm' : '+ Thêm sản phẩm mới'}
            </h3>
            
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm text-slate-400 mb-1">Tên sản phẩm *</label>
                <input required type="text" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-slate-400 mb-1">Danh mục *</label>
                  <select value={formData.category} onChange={e => setFormData({...formData, category: e.target.value})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white">
                    <option value="DRINK">Đồ uống (DRINK)</option>
                    <option value="FOOD">Đồ ăn (FOOD)</option>
                    <option value="SNACK">Ăn vặt (SNACK)</option>
                    <option value="CARD">Thẻ (CARD)</option>
                    <option value="OTHER">Khác (OTHER)</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm text-slate-400 mb-1">Tồn kho *</label>
                  <input required type="number" min="0" value={formData.stockQuantity} onChange={e => setFormData({...formData, stockQuantity: parseInt(e.target.value)})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white" />
                </div>
              </div>
              <div>
                <label className="block text-sm text-slate-400 mb-1">Giá bán (VNĐ) *</label>
                <input required type="number" min="0" value={formData.price} onChange={e => setFormData({...formData, price: parseFloat(e.target.value)})} className="w-full bg-navy-900 border border-white/10 rounded-lg p-2.5 text-white text-xl font-mono text-cyan-400" />
              </div>
              
              <div className="pt-4 flex gap-3">
                <button type="button" onClick={() => setIsModalOpen(false)} className="flex-1 py-2.5 rounded-lg font-bold bg-white/10 text-white hover:bg-white/20">Hủy</button>
                <button type="submit" className="flex-1 py-2.5 rounded-lg font-bold bg-cyan-600 hover:bg-cyan-500 text-white">Lưu Lại</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
