import { useQuery } from '@tanstack/react-query'
import { cardApi } from '../api/client'

const categoryLabels: Record<string, string> = {
  supplier: '供应商',
  competitor: '竞争对手',
  domestic: '国内客户',
  international: '国外客户',
  other: '其他',
}

const barColors: Record<string, string> = {
  supplier: 'bg-blue-500',
  competitor: 'bg-red-500',
  domestic: 'bg-green-500',
  international: 'bg-purple-500',
  other: 'bg-gray-500',
}

export default function StatsPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['stats'],
    queryFn: () => cardApi.stats(),
  })

  if (isLoading) return <p className="text-gray-400">加载中...</p>
  if (!data || data.length === 0) return <p className="text-gray-400">暂无数据</p>

  const total = data.reduce((sum, d) => sum + d.count, 0)
  const maxCount = Math.max(...data.map(d => d.count))

  return (
    <div className="max-w-2xl">
      <h2 className="text-xl font-bold text-gray-900 mb-6">分类统计</h2>
      <p className="text-sm text-gray-500 mb-6">共 {total} 张名片</p>

      <div className="space-y-4">
        {data.map((stat) => (
          <div key={stat.category}>
            <div className="flex items-center justify-between mb-1">
              <span className="text-sm font-medium text-gray-700">
                {categoryLabels[stat.category] || stat.category}
              </span>
              <span className="text-sm text-gray-500">{stat.count} 张</span>
            </div>
            <div className="h-3 bg-gray-100 rounded-full overflow-hidden">
              <div
                className={`h-full ${barColors[stat.category] || 'bg-gray-500'} rounded-full`}
                style={{ width: `${(stat.count / maxCount) * 100}%` }}
              />
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
