import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { cardApi, type Card } from '../api/client'
import { Download, FileText, AlertCircle } from 'lucide-react'

const categoryLabels: Record<string, string> = {
  supplier: '供应商',
  competitor: '竞争对手',
  domestic: '国内客户',
  international: '国外客户',
  other: '其他',
}

const categoryColors: Record<string, string> = {
  supplier: 'bg-blue-100 text-blue-700',
  competitor: 'bg-red-100 text-red-700',
  domestic: 'bg-green-100 text-green-700',
  international: 'bg-purple-100 text-purple-700',
  other: 'bg-gray-100 text-gray-700',
}

export default function CardListPage() {
  const [page, setPage] = useState(1)
  const [category, setCategory] = useState<string | undefined>(undefined)

  const { data, isLoading } = useQuery({
    queryKey: ['cards', page, category],
    queryFn: () => cardApi.list({ page, page_size: 20, category }),
  })

  return (
    <div>
      {/* ── Filter Bar ── */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex gap-2">
          <button
            onClick={() => setCategory(undefined)}
            className={`px-3 py-1.5 rounded-lg text-sm font-medium ${
              !category ? 'bg-gray-900 text-white' : 'bg-white text-gray-600 border'
            }`}
          >
            全部
          </button>
          {Object.entries(categoryLabels).map(([key, label]) => (
            <button
              key={key}
              onClick={() => setCategory(key)}
              className={`px-3 py-1.5 rounded-lg text-sm font-medium ${
                category === key ? 'bg-gray-900 text-white' : 'bg-white text-gray-600 border'
              }`}
            >
              {label}
            </button>
          ))}
        </div>
        <div className="flex gap-2">
          <a href="/api/cards/export/csv" className="flex items-center gap-1 px-3 py-1.5 bg-white border rounded-lg text-sm text-gray-600 hover:bg-gray-50">
            <Download size={14} /> CSV
          </a>
          <a href="/api/cards/export/vcard" className="flex items-center gap-1 px-3 py-1.5 bg-white border rounded-lg text-sm text-gray-600 hover:bg-gray-50">
            <FileText size={14} /> vCard
          </a>
        </div>
      </div>

      {/* ── Card Grid ── */}
      {isLoading ? (
        <p className="text-gray-400">加载中...</p>
      ) : data && data.items.length > 0 ? (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {data.items.map((card) => (
              <CardCard key={card.id} card={card} />
            ))}
          </div>
          {/* Pagination */}
          <div className="flex items-center justify-center gap-2 mt-8">
            <button
              onClick={() => setPage(p => Math.max(1, p - 1))}
              disabled={page === 1}
              className="px-3 py-1.5 bg-white border rounded-lg text-sm disabled:opacity-40"
            >
              上一页
            </button>
            <span className="text-sm text-gray-500">
              第 {page} 页 / 共 {Math.ceil(data.total / 20)} 页（{data.total} 张）
            </span>
            <button
              onClick={() => setPage(p => p + 1)}
              disabled={data.items.length < 20}
              className="px-3 py-1.5 bg-white border rounded-lg text-sm disabled:opacity-40"
            >
              下一页
            </button>
          </div>
        </>
      ) : (
        <div className="text-center py-20 text-gray-400">
          <p className="text-lg">还没有名片</p>
          <p className="text-sm mt-2">点击「上传名片」开始添加</p>
        </div>
      )}
    </div>
  )
}

function CardCard({ card }: { card: Card }) {
  return (
    <Link to={`/cards/${card.id}`} className="block">
      <div className="bg-white rounded-xl border border-gray-200 p-5 hover:shadow-md transition cursor-pointer">
        <div className="flex items-start justify-between mb-3">
          <div>
            <h3 className="font-semibold text-gray-900">{card.person_name || '未知'}</h3>
            <p className="text-sm text-gray-500">{card.title || ''}</p>
          </div>
          <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${categoryColors[card.category]}`}>
            {categoryLabels[card.category]}
          </span>
        </div>
        <p className="text-sm font-medium text-gray-700 mb-2">{card.company || ''}</p>
        {card.phone && card.phone.length > 0 && (
          <p className="text-sm text-gray-500">📞 {card.phone.join(', ')}</p>
        )}
        {card.email && card.email.length > 0 && (
          <p className="text-sm text-gray-500">✉️ {card.email.join(', ')}</p>
        )}
        {card.country && (
          <p className="text-sm text-gray-500 mt-1">📍 {card.country}</p>
        )}
        {card.needs_review && (
          <p className="text-xs text-amber-600 mt-2 flex items-center gap-1">
            <AlertCircle size={12} /> 需人工核对
          </p>
        )}
      </div>
    </Link>
  )
}
