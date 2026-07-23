import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { cardApi } from '../api/client'
import { Search } from 'lucide-react'

export default function SearchPage() {
  const [query, setQuery] = useState('')
  const [debounced, setDebounced] = useState('')

  const { data, isFetching } = useQuery({
    queryKey: ['search', debounced],
    queryFn: () => cardApi.search(debounced),
    enabled: debounced.length > 0,
  })

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setDebounced(query)
  }

  return (
    <div className="max-w-3xl mx-auto">
      <h2 className="text-xl font-bold text-gray-900 mb-6">搜索名片</h2>

      <form onSubmit={handleSearch} className="flex gap-2 mb-6">
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="搜索公司、姓名、电话、邮箱..."
          className="flex-1 px-4 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <button type="submit" className="px-5 py-2.5 bg-blue-600 text-white rounded-xl hover:bg-blue-700 flex items-center gap-2">
          <Search size={18} /> 搜索
        </button>
      </form>

      {isFetching && <p className="text-gray-400">搜索中...</p>}

      {data && data.items.length > 0 ? (
        <div className="space-y-3">
          <p className="text-sm text-gray-500">找到 {data.total} 条结果</p>
          {data.items.map((card) => (
            <Link key={card.id} to={`/cards/${card.id}`} className="block">
              <div className="bg-white rounded-xl border p-4 flex items-center justify-between hover:shadow-md transition cursor-pointer">
                <div>
                  <p className="font-medium text-gray-900">{card.person_name || '未知'}</p>
                  <p className="text-sm text-gray-500">{card.company} {card.title && `· ${card.title}`}</p>
                </div>
                <span className="text-xs text-gray-400">{card.country}</span>
              </div>
            </Link>
          ))}
        </div>
      ) : data && data.items.length === 0 ? (
        <p className="text-center py-10 text-gray-400">未找到匹配的名片</p>
      ) : null}
    </div>
  )
}
