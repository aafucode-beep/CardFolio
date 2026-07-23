import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000, // 60s for OCR processing
})

// ── Types ──
export interface Card {
  id: string
  company: string | null
  person_name: string | null
  title: string | null
  phone: string[] | null
  email: string[] | null
  address: string | null
  website: string | null
  country: string | null
  category: 'supplier' | 'competitor' | 'domestic' | 'international' | 'other'
  tags: string[] | null
  notes: string | null
  front_image_path: string | null
  back_image_path: string | null
  front_thumbnail_url: string | null
  back_thumbnail_url: string | null
  needs_review: boolean
  created_at: string
  updated_at: string
}

export interface CardListResponse {
  total: number
  page: number
  page_size: number
  items: Card[]
}

export interface UploadResponse {
  card: Card
  ocr_status: 'success' | 'needs_review' | 'failed'
  message: string
}

export interface CategoryStats {
  category: string
  count: number
}

// ── API Functions ──
export const cardApi = {
  list: (params?: { page?: number; page_size?: number; category?: string; country?: string; needs_review?: boolean }) =>
    api.get<CardListResponse>('/cards', { params }).then(r => r.data),

  get: (id: string) =>
    api.get<Card>(`/cards/${id}`).then(r => r.data),

  update: (id: string, data: Partial<Card>) =>
    api.put<Card>(`/cards/${id}`, data).then(r => r.data),

  delete: (id: string) =>
    api.delete(`/cards/${id}`).then(r => r.data),

  search: (q: string, page = 1) =>
    api.get<CardListResponse>('/cards/search/q', { params: { q, page } }).then(r => r.data),

  upload: (front: File | null, back: File | null) => {
    const form = new FormData()
    if (front) form.append('front', front)
    if (back) form.append('back', back)
    return api.post<UploadResponse>('/cards/upload', form).then(r => r.data)
  },

  stats: () =>
    api.get<CategoryStats[]>('/cards/categories/stats').then(r => r.data),

  exportCsv: () => `${axios.defaults.baseURL}/cards/export/csv`,
  exportVcard: () => `${axios.defaults.baseURL}/cards/export/vcard`,
}
