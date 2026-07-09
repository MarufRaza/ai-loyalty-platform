import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Sparkles, Loader2, Copy, Check, ArrowRight, Wand2 } from 'lucide-react'
import campaignService from '../services/campaignService'
import toast from 'react-hot-toast'

const CAMPAIGN_TYPES = [
  { value: 'EMAIL', label: '📧 Email', desc: 'Newsletter and promotional emails' },
  { value: 'WHATSAPP', label: '💬 WhatsApp', desc: 'Short conversational messages' },
  { value: 'SMS', label: '📱 SMS', desc: 'Brief text notifications' },
  { value: 'PUSH_NOTIFICATION', label: '🔔 Push', desc: 'In-app push alerts' },
]

const TONES = ['Professional', 'Friendly', 'Urgent', 'Casual', 'Enthusiastic', 'Formal']

export default function AICampaignPage() {
  const navigate = useNavigate()
  const [segments, setSegments] = useState([])
  const [form, setForm] = useState({
    campaignType: 'EMAIL', objective: '', segmentId: '',
    targetAudience: '', tone: 'Professional', brandName: 'LoyaltyPro'
  })
  const [generated, setGenerated] = useState(null)
  const [loading, setLoading] = useState(false)
  const [copied, setCopied] = useState(false)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    campaignService.getSegments().then(setSegments).catch(() => {})
  }, [])

  const handleGenerate = async (e) => {
    e.preventDefault()
    if (!form.objective.trim()) {
      toast.error('Please describe your campaign objective')
      return
    }
    setLoading(true)
    setGenerated(null)
    try {
      const selectedSegment = segments.find(s => s.id === parseInt(form.segmentId))
      const result = await campaignService.generateAI({
        ...form,
        segmentId: form.segmentId ? parseInt(form.segmentId) : null,
        segmentName: selectedSegment?.name,
      })
      if (result?.success) {
        setGenerated(result)
        toast.success('Campaign generated! 🎉')
      } else {
        toast.error(result?.errorMessage || 'Generation failed')
      }
    } catch (err) {
      toast.error(err?.response?.data?.message || 'AI generation failed. Is Ollama running?')
    } finally {
      setLoading(false)
    }
  }

  const handleCopy = () => {
    if (!generated) return
    const text = `Subject: ${generated.subjectLine}\n\n${generated.content}\n\nCTA: ${generated.callToAction}`
    navigator.clipboard.writeText(text)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  const handleSaveAsCampaign = async () => {
    if (!generated) return
    setSaving(true)
    try {
      await campaignService.create({
        name: generated.subjectLine || `AI Campaign - ${form.campaignType}`,
        objective: form.objective,
        campaignType: form.campaignType,
        subjectLine: generated.subjectLine,
        content: generated.content,
        callToAction: generated.callToAction,
        segmentId: form.segmentId || null,
        aiGenerated: true,
        status: 'DRAFT',
      })
      toast.success('Saved as draft campaign!')
      navigate('/campaigns')
    } catch {
      toast.error('Failed to save campaign')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="max-w-6xl mx-auto space-y-6 animate-fade-in">
      <div>
        <div className="flex items-center gap-2 mb-1">
          <div className="w-8 h-8 rounded-lg bg-indigo-100 flex items-center justify-center">
            <Sparkles className="w-5 h-5 text-indigo-600" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900">AI Campaign Generator</h1>
        </div>
        <p className="text-gray-500 text-sm ml-10">
          Powered by <strong>Groq</strong> + <strong>Llama 3.3</strong> — free, ultra-fast inference
        </p>
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        {/* Input Form */}
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
          <h2 className="text-base font-semibold text-gray-900 mb-5 flex items-center gap-2">
            <Wand2 className="w-4 h-4 text-indigo-600" />
            Campaign Configuration
          </h2>
          <form onSubmit={handleGenerate} className="space-y-5">
            {/* Channel */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Campaign Channel</label>
              <div className="grid grid-cols-2 gap-2">
                {CAMPAIGN_TYPES.map(ct => (
                  <button
                    key={ct.value}
                    type="button"
                    onClick={() => setForm(p => ({ ...p, campaignType: ct.value }))}
                    className={`p-3 rounded-lg border text-left transition-all ${
                      form.campaignType === ct.value
                        ? 'border-indigo-500 bg-indigo-50 text-indigo-700'
                        : 'border-gray-200 hover:border-gray-300 text-gray-700'
                    }`}
                  >
                    <div className="font-medium text-sm">{ct.label}</div>
                    <div className="text-xs text-gray-400 mt-0.5">{ct.desc}</div>
                  </button>
                ))}
              </div>
            </div>

            {/* Objective */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                Campaign Objective <span className="text-red-500">*</span>
              </label>
              <textarea
                rows={3}
                value={form.objective}
                onChange={e => setForm(p => ({ ...p, objective: e.target.value }))}
                className="input-field resize-none"
                placeholder="e.g. Re-engage inactive Gold tier customers with an exclusive discount and encourage them to make a purchase..."
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Target Segment</label>
                <select value={form.segmentId}
                  onChange={e => setForm(p => ({ ...p, segmentId: e.target.value }))}
                  className="input-field">
                  <option value="">All Customers</option>
                  {segments.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Tone</label>
                <select value={form.tone}
                  onChange={e => setForm(p => ({ ...p, tone: e.target.value }))}
                  className="input-field">
                  {TONES.map(t => <option key={t} value={t}>{t}</option>)}
                </select>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Brand Name</label>
              <input type="text" value={form.brandName}
                onChange={e => setForm(p => ({ ...p, brandName: e.target.value }))}
                className="input-field" placeholder="Your brand name" />
            </div>

            <button type="submit" disabled={loading} className="btn-primary w-full justify-center py-3">
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  Generating with Groq...
                </>
              ) : (
                <>
                  <Sparkles className="w-4 h-4" />
                  Generate Campaign
                </>
              )}
            </button>
          </form>
        </div>

        {/* Generated Output */}
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
          <div className="flex items-center justify-between mb-5">
            <h2 className="text-base font-semibold text-gray-900 flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-indigo-600" />
              Generated Campaign
            </h2>
            {generated && (
              <div className="flex items-center gap-2">
                <button onClick={handleCopy} className="btn-secondary text-xs py-1.5 px-3">
                  {copied ? <Check className="w-3.5 h-3.5 text-green-600" /> : <Copy className="w-3.5 h-3.5" />}
                  {copied ? 'Copied!' : 'Copy'}
                </button>
              </div>
            )}
          </div>

          {loading ? (
            <div className="h-64 flex flex-col items-center justify-center gap-3 text-gray-400">
              <div className="relative">
                <Sparkles className="w-10 h-10 text-indigo-300 animate-pulse-slow" />
              </div>
              <p className="text-sm font-medium text-gray-600">AI is crafting your campaign...</p>
              <p className="text-xs text-gray-400">This may take 10-30 seconds with Groq API</p>
            </div>
          ) : generated ? (
            <div className="space-y-4 animate-slide-up">
              <div className="p-4 bg-indigo-50 rounded-lg border border-indigo-100">
                <p className="text-xs font-semibold text-indigo-600 uppercase tracking-wide mb-1">Subject Line</p>
                <p className="text-gray-900 font-medium">{generated.subjectLine}</p>
              </div>
              <div className="p-4 bg-gray-50 rounded-lg border border-gray-100">
                <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">Message Content</p>
                <p className="text-gray-700 text-sm leading-relaxed whitespace-pre-wrap">{generated.content}</p>
              </div>
              <div className="p-4 bg-green-50 rounded-lg border border-green-100">
                <p className="text-xs font-semibold text-green-600 uppercase tracking-wide mb-1">Call to Action</p>
                <p className="text-gray-900 font-medium">{generated.callToAction}</p>
              </div>
              <div className="text-xs text-gray-400 text-center">
                Generated by {generated.generatedBy} • Groq AI
              </div>
              <button
                onClick={handleSaveAsCampaign}
                disabled={saving}
                className="btn-primary w-full justify-center py-2.5"
              >
                {saving ? <Loader2 className="w-4 h-4 animate-spin" /> : <ArrowRight className="w-4 h-4" />}
                Save as Draft Campaign
              </button>
            </div>
          ) : (
            <div className="h-64 flex flex-col items-center justify-center gap-2 text-gray-400">
              <Sparkles className="w-12 h-12 text-gray-200" />
              <p className="text-sm">Configure and generate your campaign above</p>
              <p className="text-xs text-gray-300">AI output will appear here</p>
            </div>
          )}
        </div>
      </div>

      {/* Info Box */}
      <div className="bg-blue-50 border border-blue-100 rounded-xl p-4 flex items-start gap-3">
        <div className="w-8 h-8 rounded-lg bg-blue-100 flex items-center justify-center flex-shrink-0 mt-0.5">
          <Sparkles className="w-4 h-4 text-blue-600" />
        </div>
        <div>
          <p className="text-sm font-semibold text-blue-900">How it works</p>
          <p className="text-sm text-blue-700 mt-0.5">
            This uses the <strong>Groq API</strong> with <strong>Llama 3.3 70B</strong> — completely free (14,400 requests/day),
            ultra-fast inference (~500 tokens/second), and no data stored.
            Get your free API key at <strong>console.groq.com</strong>.
          </p>
        </div>
      </div>
    </div>
  )
}
