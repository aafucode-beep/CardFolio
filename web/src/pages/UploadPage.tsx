import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { cardApi } from '../api/client'
import { Upload, CheckCircle, AlertCircle, Loader2 } from 'lucide-react'

export default function UploadPage() {
  const [frontFile, setFrontFile] = useState<File | null>(null)
  const [backFile, setBackFile] = useState<File | null>(null)
  const [frontPreview, setFrontPreview] = useState<string | null>(null)
  const [backPreview, setBackPreview] = useState<string | null>(null)
  const [result, setResult] = useState<{ status: string; message: string; company?: string; name?: string } | null>(null)

  const mutation = useMutation({
    mutationFn: () => cardApi.upload(frontFile, backFile),
    onSuccess: (data) => {
      setResult({
        status: data.ocr_status,
        message: data.message,
        company: data.card.company || undefined,
        name: data.card.person_name || undefined,
      })
    },
    onError: (err: Error) => {
      setResult({ status: 'failed', message: err.message })
    },
  })

  const handleFile = (side: 'front' | 'back') => (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    if (side === 'front') {
      setFrontFile(file)
      setFrontPreview(URL.createObjectURL(file))
    } else {
      setBackFile(file)
      setBackPreview(URL.createObjectURL(file))
    }
  }

  const handleSubmit = () => {
    if (!frontFile && !backFile) return
    setResult(null)
    mutation.mutate()
  }

  const handleReset = () => {
    setFrontFile(null); setBackFile(null)
    setFrontPreview(null); setBackPreview(null)
    setResult(null)
  }

  return (
    <div className="max-w-2xl mx-auto">
      <h2 className="text-xl font-bold text-gray-900 mb-6">上传名片</h2>

      {/* ── Upload Areas ── */}
      <div className="grid grid-cols-2 gap-4 mb-6">
        {/* Front */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">正面</label>
          <label className="block border-2 border-dashed border-gray-300 rounded-xl p-6 text-center cursor-pointer hover:border-blue-400 transition">
            {frontPreview ? (
              <img src={frontPreview} alt="正面" className="max-h-48 mx-auto rounded" />
            ) : (
              <div className="text-gray-400">
                <Upload size={24} className="mx-auto mb-2" />
                <p className="text-sm">点击上传</p>
              </div>
            )}
            <input type="file" accept="image/*" className="hidden" onChange={handleFile('front')} />
          </label>
        </div>
        {/* Back */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">背面</label>
          <label className="block border-2 border-dashed border-gray-300 rounded-xl p-6 text-center cursor-pointer hover:border-blue-400 transition">
            {backPreview ? (
              <img src={backPreview} alt="背面" className="max-h-48 mx-auto rounded" />
            ) : (
              <div className="text-gray-400">
                <Upload size={24} className="mx-auto mb-2" />
                <p className="text-sm">点击上传（可选）</p>
              </div>
            )}
            <input type="file" accept="image/*" className="hidden" onChange={handleFile('back')} />
          </label>
        </div>
      </div>

      {/* ── Submit ── */}
      <button
        onClick={handleSubmit}
        disabled={!frontFile && !backFile || mutation.isPending}
        className="w-full py-3 bg-blue-600 text-white rounded-xl font-medium hover:bg-blue-700 disabled:opacity-40 disabled:cursor-not-allowed flex items-center justify-center gap-2"
      >
        {mutation.isPending ? (
          <><Loader2 size={18} className="animate-spin" /> OCR 识别中...</>
        ) : (
          <><Upload size={18} /> 上传并识别</>
        )}
      </button>

      {/* ── Result ── */}
      {result && (
        <div className={`mt-6 p-4 rounded-xl border ${
          result.status === 'success' ? 'bg-green-50 border-green-200' :
          result.status === 'needs_review' ? 'bg-amber-50 border-amber-200' :
          'bg-red-50 border-red-200'
        }`}>
          <div className="flex items-center gap-2 mb-2">
            {result.status === 'success' ? <CheckCircle className="text-green-600" size={18} /> :
             <AlertCircle className="text-amber-600" size={18} />}
            <span className="font-medium">
              {result.status === 'success' ? '识别成功' :
               result.status === 'needs_review' ? '需人工核对' : '识别失败'}
            </span>
          </div>
          {result.company && <p className="text-sm text-gray-700">公司：{result.company}</p>}
          {result.name && <p className="text-sm text-gray-700">姓名：{result.name}</p>}
          {result.message && <p className="text-sm text-gray-500 mt-1">{result.message}</p>}
          <button onClick={handleReset} className="mt-3 text-sm text-blue-600 hover:underline">
            继续上传下一张
          </button>
        </div>
      )}
    </div>
  )
}
