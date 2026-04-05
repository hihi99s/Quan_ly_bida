export default function PlaceholderPage({ title, icon, color = 'cyan' }) {
  const gradients = {
    cyan: 'from-cyan-500/20 to-blue-500/5',
    purple: 'from-purple-500/20 to-pink-500/5',
    emerald: 'from-emerald-500/20 to-teal-500/5',
    amber: 'from-amber-500/20 to-orange-500/5'
  };

  const borders = {
    cyan: 'border-cyan-500/30',
    purple: 'border-purple-500/30',
    emerald: 'border-emerald-500/30',
    amber: 'border-amber-500/30'
  };

  const texts = {
    cyan: 'text-cyan-400',
    purple: 'text-purple-400',
    emerald: 'text-emerald-400',
    amber: 'text-amber-400'
  };

  return (
    <div className={`flex flex-col items-center justify-center p-12 glass-card rounded-2xl animate-in bg-gradient-to-br ${gradients[color]} relative overflow-hidden h-[70vh]`}>
      
      {/* Back glow */}
      <div className={`absolute inset-0 bg-gradient-to-t ${gradients[color]} blur-3xl opacity-50`}></div>

      <div className="relative z-10 flex flex-col items-center text-center max-w-lg mx-auto">
        <div className={`w-28 h-28 flex items-center justify-center text-6xl rounded-3xl bg-black/40 border border-white/10 mb-6 shadow-2xl backdrop-blur-md`}>
          {icon}
        </div>
        
        <h2 className="text-4xl font-black text-white mb-4 tracking-tight drop-shadow-md">
          {title}
        </h2>
        
        <p className="text-slate-300 text-lg mb-8 leading-relaxed">
          Tính năng này hiện đang được quản lý tại <strong>Màn hình Web Quản Trị Cũ (Thymeleaf)</strong>. 
          Phiên bản Mobile SPA đang tập trung hoàn thiện Nghiệp vụ vận hành lõi.
        </p>

        <div className={`px-6 py-3 rounded-xl bg-black/40 border ${borders[color]} ${texts[color]} font-semibold text-sm backdrop-blur-sm flex items-center gap-2 shadow-lg`}>
          <span className="relative flex h-3 w-3">
            <span className={`animate-ping absolute inline-flex h-full w-full rounded-full ${texts[color].replace('text-', 'bg-')} opacity-75`}></span>
            <span className={`relative inline-flex rounded-full h-3 w-3 ${texts[color].replace('text-', 'bg-')}`}></span>
          </span>
          Đang nâng cấp Backend REST API
        </div>
      </div>
    </div>
  );
}
