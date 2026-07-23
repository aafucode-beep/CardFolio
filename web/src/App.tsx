import { Routes, Route, NavLink } from 'react-router-dom'
import { LayoutGrid, Upload, BarChart3, Search } from 'lucide-react'
import CardListPage from './pages/CardListPage'
import CardDetailPage from './pages/CardDetailPage'
import UploadPage from './pages/UploadPage'
import StatsPage from './pages/StatsPage'
import SearchPage from './pages/SearchPage'

const navItems = [
  { to: '/', label: '名片列表', icon: LayoutGrid },
  { to: '/upload', label: '上传名片', icon: Upload },
  { to: '/search', label: '搜索', icon: Search },
  { to: '/stats', label: '统计', icon: BarChart3 },
]

export default function App() {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* ── Nav Bar ── */}
      <nav className="bg-white border-b border-gray-200 px-6 py-3 flex items-center gap-6">
        <h1 className="text-xl font-bold text-gray-900">📇 CardFolio</h1>
        <div className="flex gap-4">
          {navItems.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              className={({ isActive }) =>
                `flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium transition ${
                  isActive
                    ? 'bg-blue-50 text-blue-700'
                    : 'text-gray-600 hover:bg-gray-100'
                }`
              }
            >
              <Icon size={16} />
              {label}
            </NavLink>
          ))}
        </div>
      </nav>

      {/* ── Page Content ── */}
      <main className="max-w-7xl mx-auto px-6 py-8">
        <Routes>
          <Route path="/" element={<CardListPage />} />
          <Route path="/cards/:id" element={<CardDetailPage />} />
          <Route path="/upload" element={<UploadPage />} />
          <Route path="/search" element={<SearchPage />} />
          <Route path="/stats" element={<StatsPage />} />
        </Routes>
      </main>
    </div>
  )
}
