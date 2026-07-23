import { useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { cardApi } from '../api/client'
import { ArrowLeft, Pencil, Trash2, Save, X, AlertCircle } from 'lucide-react'

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

function imageUrl(path: string | null): string | null {
  if (!path) return null
  const filename = path.split('/').pop()
  return `/cardfolio/uploads/${filename}`
}

export default function CardDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [editing, setEditing] = useState(false)
  const [confirmDelete, setConfirmDelete] = useState(false)

  const { data: card, isLoading } = useQuery({
    queryKey: ['card', id],
    queryFn: () => cardApi.get(id!),
    enabled: !!id,
  })

  const [form, setForm] = useState<Record<string, string>>({})

  const startEdit = () => {
    if (!card) return
    setForm({
      company: card.company || '',
      person_name: card.person_name || '',
      title: card.title || '',
      phone: (card.phone || []).join(', '),
      email: (card.email || []).join(', '),
      address: card.address || '',
      website: card.website || '',
      country: card.country || '',
      category: card.category,
      tags: (card.tags || []).join(', '),
      notes: card.notes || '',
    })
    setEditing(true)
  }

  const updateMutation = useMutation({
    mutationFn: (data: Record<string, unknown>) => cardApi.update(id!, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['card', id] })
      queryClient.invalidateQueries({ queryKey: ['cards'] })
      setEditing(false)
    },
  })

  const deleteMutation = useMutation({
    mutationFn: () => cardApi.delete(id!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cards'] })
      navigate('/')
    },
  })

  const handleSave = () => {
    const data = {
      company: form.company || null,
      person_name: form.person_name || null,
      title: form.title || null,
      phone: form.phone ? form.phone.split(',').map(s => s.trim()).filter(Boolean) : [],
      email: form.email ? form.email.split(',').map(s => s.trim()).filter(Boolean) : [],
      address: form.address || null,
      website: form.website || null,
      country: form.country || null,
      category: form.category,
      tags: form.tags ? form.tags.split(',').map(s => s.trim()).filter(Boolean) : [],
      notes: form.notes || null,
    }
    updateMutation.mutate(data)
  }

  if (isLoading) return <p className="text-gray-400">加载中...</p>
  if (!card) return <p className="text-gray-400">名片不存在</p>

  const frontUrl = imageUrl(card.front_image_path)
  const backUrl = imageUrl(card.back_image_path)

  return (
    <div className="max-w-4xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <Link to="/" className="flex items-center gap-1.5 text-sm text-gray-600 hover:text-gray-900">
          <ArrowLeft size={16} /> 返回列表
        </Link>
        <div className="flex gap-2">
          {!editing && (
            <>
              <button
                onClick={startEdit}
                className="flex items-center gap-1.5 px-3 py-1.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700"
              >
                <Pencil size={14} /> 编辑
              </button>
              {confirmDelete ? (
                <button
                  onClick={() => deleteMutation.mutate()}
                  className="flex items-center gap-1.5 px-3 py-1.5 bg-red-600 text-white rounded-lg text-sm hover:bg-red-700"
                >
                  确认删除
                </button>
              ) : (
                <button
                  onClick={() => setConfirmDelete(true)}
                  className="flex items-center gap-1.5 px-3 py-1.5 bg-white border border-red-300 text-red-600 rounded-lg text-sm hover:bg-red-50"
                >
                  <Trash2 size={14} /> 删除
                </button>
              )}
            </>
          )}
          {editing && (
            <>
              <button
                onClick={handleSave}
                disabled={updateMutation.isPending}
                className="flex items-center gap-1.5 px-3 py-1.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 disabled:opacity-50"
              >
                <Save size={14} /> 保存
              </button>
              <button
                onClick={() => setEditing(false)}
                className="flex items-center gap-1.5 px-3 py-1.5 bg-white border rounded-lg text-sm text-gray-600 hover:bg-gray-50"
              >
                <X size={14} /> 取消
              </button>
            </>
          )}
        </div>
      </div>

      {/* Needs review banner */}
      {card.needs_review && (
        <div className="mb-4 px-4 py-3 bg-amber-50 border border-amber-200 rounded-xl text-sm text-amber-700 flex items-center gap-2">
          <AlertCircle size={16} /> 此名片 OCR 置信度较低，请核对信息是否准确
        </div>
      )}

      {/* Images */}
      {(frontUrl || backUrl) && (
        <div className="flex gap-4 mb-6">
          {frontUrl && (
            <div className="flex-1">
              <p className="text-xs text-gray-400 mb-1">正面</p>
              <img
                src={frontUrl}
                alt="名片正面"
                className="w-full rounded-xl border object-contain max-h-64 bg-gray-50"
                onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
              />
            </div>
          )}
          {backUrl && (
            <div className="flex-1">
              <p className="text-xs text-gray-400 mb-1">背面</p>
              <img
                src={backUrl}
                alt="名片背面"
                className="w-full rounded-xl border object-contain max-h-64 bg-gray-50"
                onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
              />
            </div>
          )}
        </div>
      )}

      {/* Card Info */}
      <div className="bg-white rounded-xl border p-6">
        {!editing ? (
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <h2 className="text-2xl font-bold text-gray-900">{card.person_name || '未知'}</h2>
              <span className={`px-2.5 py-0.5 rounded-full text-xs font-medium ${categoryColors[card.category]}`}>
                {categoryLabels[card.category]}
              </span>
            </div>
            {card.title && <p className="text-gray-600">{card.title}</p>}
            {card.company && <p className="text-gray-700 font-medium">{card.company}</p>}

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t">
              {card.phone && card.phone.length > 0 && (
                <div>
                  <p className="text-xs text-gray-400 mb-1">电话</p>
                  {card.phone.map(p => <p key={p} className="text-sm text-gray-700">{p}</p>)}
                </div>
              )}
              {card.email && card.email.length > 0 && (
                <div>
                  <p className="text-xs text-gray-400 mb-1">邮箱</p>
                  {card.email.map(e => <p key={e} className="text-sm text-gray-700">{e}</p>)}
                </div>
              )}
              {card.website && (
                <div>
                  <p className="text-xs text-gray-400 mb-1">网站</p>
                  <a href={card.website} target="_blank" rel="noreferrer" className="text-sm text-blue-600 hover:underline">{card.website}</a>
                </div>
              )}
              {card.country && (
                <div>
                  <p className="text-xs text-gray-400 mb-1">国家/地区</p>
                  <p className="text-sm text-gray-700">{card.country}</p>
                </div>
              )}
              {card.address && (
                <div>
                  <p className="text-xs text-gray-400 mb-1">地址</p>
                  <p className="text-sm text-gray-700">{card.address}</p>
                </div>
              )}
              {card.tags && card.tags.length > 0 && (
                <div>
                  <p className="text-xs text-gray-400 mb-1">标签</p>
                  <div className="flex flex-wrap gap-1">
                    {card.tags.map(t => (
                      <span key={t} className="px-2 py-0.5 bg-gray-100 rounded text-xs text-gray-600">{t}</span>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {card.notes && (
              <div className="pt-4 border-t">
                <p className="text-xs text-gray-400 mb-1">备注</p>
                <p className="text-sm text-gray-700 whitespace-pre-wrap">{card.notes}</p>
              </div>
            )}

            <div className="pt-4 border-t text-xs text-gray-400">
              创建时间：{new Date(card.created_at).toLocaleString('zh-CN')}
              {' · '}更新时间：{new Date(card.updated_at).toLocaleString('zh-CN')}
            </div>
          </div>
        ) : (
          /* Edit Form */
          <div className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Field label="姓名" value={form.person_name} onChange={v => setForm(f => ({ ...f, person_name: v }))} />
              <Field label="公司" value={form.company} onChange={v => setForm(f => ({ ...f, company: v }))} />
              <Field label="职位" value={form.title} onChange={v => setForm(f => ({ ...f, title: v }))} />
              <Field label="国家/地区" value={form.country} onChange={v => setForm(f => ({ ...f, country: v }))} />
              <Field label="电话（逗号分隔）" value={form.phone} onChange={v => setForm(f => ({ ...f, phone: v }))} />
              <Field label="邮箱（逗号分隔）" value={form.email} onChange={v => setForm(f => ({ ...f, email: v }))} />
              <Field label="网站" value={form.website} onChange={v => setForm(f => ({ ...f, website: v }))} />
              <Field label="标签（逗号分隔）" value={form.tags} onChange={v => setForm(f => ({ ...f, tags: v }))} />
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">分类</label>
                <select
                  value={form.category}
                  onChange={e => setForm(f => ({ ...f, category: e.target.value }))}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  {Object.entries(categoryLabels).map(([key, label]) => (
                    <option key={key} value={key}>{label}</option>
                  ))}
                </select>
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">地址</label>
              <input
                value={form.address}
                onChange={e => setForm(f => ({ ...f, address: e.target.value }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">备注</label>
              <textarea
                value={form.notes}
                onChange={e => setForm(f => ({ ...f, notes: e.target.value }))}
                rows={3}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            {updateMutation.isError && (
              <p className="text-sm text-red-600">保存失败，请重试</p>
            )}
          </div>
        )}
      </div>
    </div>
  )
}

function Field({ label, value, onChange }: { label: string; value: string; onChange: (v: string) => void }) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      <input
        value={value}
        onChange={e => onChange(e.target.value)}
        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>
  )
}
