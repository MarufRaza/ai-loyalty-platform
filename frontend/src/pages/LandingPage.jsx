import { Link } from 'react-router-dom'
import {
  Zap, Users, Megaphone, BarChart3, Shield, Sparkles,
  ArrowRight, CheckCircle, Star
} from 'lucide-react'

const features = [
  { icon: Users, title: 'Customer Management', desc: 'Manage 10K+ customers with smart segmentation and loyalty tiers.', color: 'text-blue-600 bg-blue-50' },
  { icon: Sparkles, title: 'AI Campaign Generator', desc: 'Generate email, SMS, WhatsApp campaigns using Groq API-powered AI.', color: 'text-indigo-600 bg-indigo-50' },
  { icon: Megaphone, title: 'Campaign Automation', desc: 'Schedule, publish and track marketing campaigns with analytics.', color: 'text-purple-600 bg-purple-50' },
  { icon: BarChart3, title: 'Real-time Analytics', desc: 'Monitor revenue, conversions, and customer growth in real-time.', color: 'text-green-600 bg-green-50' },
  { icon: Shield, title: 'Loyalty Engine', desc: 'Silver, Gold, Platinum tiers with points earn and redemption.', color: 'text-amber-600 bg-amber-50' },
  { icon: Zap, title: 'Coupon & Promotions', desc: 'Create flat/percentage discounts with tier-based requirements.', color: 'text-rose-600 bg-rose-50' },
]

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-white">
      {/* Header */}
      <header className="border-b border-gray-100 bg-white sticky top-0 z-50 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-gradient-to-br from-primary-600 to-indigo-600 rounded-lg flex items-center justify-center">
                <Zap className="w-5 h-5 text-white" />
              </div>
              <span className="text-xl font-bold text-gray-900">LoyaltyPro</span>
            </div>
            <div className="flex items-center gap-3">
              <Link to="/login" className="text-sm font-medium text-gray-600 hover:text-gray-900 transition-colors px-3 py-2">
                Sign In
              </Link>
              <Link to="/register" className="btn-primary">
                Get Started Free
                <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
          </div>
        </div>
      </header>

      {/* Hero */}
      <section className="pt-20 pb-28 bg-gradient-to-b from-gray-50 to-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <div className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-50 text-indigo-700 rounded-full text-sm font-medium mb-8 border border-indigo-100">
            <Sparkles className="w-4 h-4" />
            AI-Powered • Powered by Groq + Llama 3.3
          </div>
          <h1 className="text-5xl md:text-6xl font-extrabold text-gray-900 leading-tight mb-6">
            Turn Customers into{' '}
            <span className="text-gradient">Brand Advocates</span>
          </h1>
          <p className="text-xl text-gray-600 max-w-3xl mx-auto mb-10 leading-relaxed">
            A production-grade loyalty and marketing automation platform. Manage customers, run AI-generated campaigns, and drive revenue — all from one dashboard.
          </p>
          <div className="flex items-center justify-center gap-4 flex-wrap">
            <Link to="/register" className="btn-primary px-8 py-3 text-base">
              Start Free Trial
              <ArrowRight className="w-5 h-5" />
            </Link>
            <Link to="/login" className="btn-secondary px-8 py-3 text-base">
              View Demo
            </Link>
          </div>
          <div className="mt-8 flex items-center justify-center gap-6 text-sm text-gray-500">
            {['10,000+ customers supported', 'No paid AI APIs', 'Production-ready'].map(item => (
              <div key={item} className="flex items-center gap-1.5">
                <CheckCircle className="w-4 h-4 text-green-500" />
                {item}
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="py-24 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 mb-4">Everything you need to grow</h2>
            <p className="text-xl text-gray-600 max-w-2xl mx-auto">Built for marketing teams at scale. From startup to enterprise.</p>
          </div>
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
            {features.map(({ icon: Icon, title, desc, color }) => (
              <div key={title} className="p-6 border border-gray-100 rounded-2xl hover:border-gray-200 hover:shadow-md transition-all duration-200 group">
                <div className={`w-12 h-12 rounded-xl flex items-center justify-center mb-4 ${color}`}>
                  <Icon className="w-6 h-6" />
                </div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">{title}</h3>
                <p className="text-gray-600 text-sm leading-relaxed">{desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Stats */}
      <section className="py-20 bg-gray-900 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid md:grid-cols-4 gap-8 text-center">
            {[
              { value: '10K+', label: 'Customers Supported' },
              { value: '1000+', label: 'Campaigns Managed' },
              { value: '100%', label: 'Local AI (No API Keys)' },
              { value: '99.9%', label: 'Uptime SLA' },
            ].map(({ value, label }) => (
              <div key={label}>
                <div className="text-4xl font-extrabold text-primary-400 mb-2">{value}</div>
                <div className="text-gray-400 text-sm">{label}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-24 bg-gradient-to-r from-primary-600 to-indigo-700">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center text-white">
          <h2 className="text-4xl font-bold mb-4">Ready to supercharge loyalty?</h2>
          <p className="text-lg text-primary-100 mb-8">Join companies using LoyaltyPro to retain customers and grow revenue.</p>
          <Link to="/register" className="inline-flex items-center gap-2 px-8 py-4 bg-white text-primary-700 font-semibold rounded-xl hover:bg-primary-50 transition-colors shadow-lg text-base">
            Get Started Today
            <ArrowRight className="w-5 h-5" />
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-gray-400 py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <div className="flex items-center justify-center gap-2 mb-4">
            <div className="w-6 h-6 bg-primary-600 rounded-md flex items-center justify-center">
              <Zap className="w-4 h-4 text-white" />
            </div>
            <span className="text-white font-semibold">LoyaltyPro</span>
          </div>
          <p className="text-sm">Built with Spring Boot 3, React 18, Ollama AI • Production-ready SaaS Platform</p>
        </div>
      </footer>
    </div>
  )
}
