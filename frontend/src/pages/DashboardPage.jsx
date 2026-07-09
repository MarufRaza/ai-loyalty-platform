import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import {
  Users, Megaphone, Tag, TrendingUp, Sparkles,
  ArrowRight, Award, DollarSign, Activity
} from 'lucide-react'
import {
  AreaChart, Area, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend
} from 'recharts'
import { StatCard } from '../components/UI/Card'
import LoadingSpinner from '../components/UI/LoadingSpinner'
import Badge from '../components/UI/Badge'
import analyticsService from '../services/analyticsService'
import { format } from 'date-fns'

const TIER_COLORS = { Silver: '#94a3b8', Gold: '#f59e0b', Platinum: '#6366f1' }

export default function DashboardPage() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    analyticsService.getDashboard()
      .then(setData)
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <LoadingSpinner />

  const tierData = data ? Object.entries(data.customersByTier || {}).map(([name, value]) => ({ name, value })) : []
  const statusData = data ? Object.entries(data.campaignsByStatus || {}).map(([name, value]) => ({ name, value })) : []

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
          <p className="text-gray-500 text-sm mt-0.5">Welcome back! Here's what's happening today.</p>
        </div>
        <div className="flex items-center gap-3">
          <Link to="/campaigns/ai" className="btn-primary">
            <Sparkles className="w-4 h-4" />
            AI Campaign
          </Link>
          <Link to="/customers" className="btn-secondary">
            <Users className="w-4 h-4" />
            Add Customer
          </Link>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Customers"
          value={data?.totalCustomers?.toLocaleString() || '0'}
          subtitle={`${data?.newCustomersThisMonth || 0} new this month`}
          icon={Users}
          color="blue"
          trend={{ value: '+12% vs last month', positive: true }}
        />
        <StatCard
          title="Active Campaigns"
          value={data?.activeCampaigns?.toLocaleString() || '0'}
          subtitle={`${data?.totalCampaigns || 0} total campaigns`}
          icon={Megaphone}
          color="purple"
        />
        <StatCard
          title="Total Revenue"
          value={`$${(data?.totalRevenue || 0).toLocaleString()}`}
          subtitle="From campaign conversions"
          icon={DollarSign}
          color="green"
          trend={{ value: '+8.3% this month', positive: true }}
        />
        <StatCard
          title="Active Coupons"
          value={data?.totalCoupons?.toLocaleString() || '0'}
          subtitle="Valid and unexpired"
          icon={Tag}
          color="amber"
        />
      </div>

      {/* Charts Row 1 */}
      <div className="grid lg:grid-cols-3 gap-6">
        {/* Monthly Growth */}
        <div className="lg:col-span-2 bg-white rounded-xl p-6 border border-gray-200 shadow-sm">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-base font-semibold text-gray-900">Monthly Growth</h2>
            <span className="text-xs text-gray-500">Last 6 months</span>
          </div>
          <ResponsiveContainer width="100%" height={240}>
            <AreaChart data={data?.monthlyGrowth || []}>
              <defs>
                <linearGradient id="colorCustomers" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.15} />
                  <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="month" tick={{ fontSize: 12 }} tickLine={false} axisLine={false} />
              <YAxis tick={{ fontSize: 12 }} tickLine={false} axisLine={false} />
              <Tooltip contentStyle={{ borderRadius: '8px', border: '1px solid #e5e7eb', fontSize: '12px' }} />
              <Area type="monotone" dataKey="newCustomers" name="New Customers" stroke="#3b82f6" fill="url(#colorCustomers)" strokeWidth={2} />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        {/* Loyalty Tiers */}
        <div className="bg-white rounded-xl p-6 border border-gray-200 shadow-sm">
          <h2 className="text-base font-semibold text-gray-900 mb-6">Loyalty Distribution</h2>
          {tierData.length > 0 ? (
            <>
              <ResponsiveContainer width="100%" height={180}>
                <PieChart>
                  <Pie data={tierData} cx="50%" cy="50%" innerRadius={50} outerRadius={80} paddingAngle={3} dataKey="value">
                    {tierData.map((entry) => (
                      <Cell key={entry.name} fill={TIER_COLORS[entry.name] || '#94a3b8'} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value) => [value.toLocaleString(), 'Customers']} />
                </PieChart>
              </ResponsiveContainer>
              <div className="space-y-2 mt-2">
                {tierData.map(({ name, value }) => (
                  <div key={name} className="flex items-center justify-between text-sm">
                    <div className="flex items-center gap-2">
                      <div className="w-3 h-3 rounded-full" style={{ backgroundColor: TIER_COLORS[name] }} />
                      <span className="text-gray-600">{name}</span>
                    </div>
                    <span className="font-semibold text-gray-900">{value?.toLocaleString()}</span>
                  </div>
                ))}
              </div>
            </>
          ) : (
            <div className="h-48 flex items-center justify-center text-gray-400 text-sm">No data yet</div>
          )}
        </div>
      </div>

      {/* Recent Campaigns & Segments */}
      <div className="grid lg:grid-cols-2 gap-6">
        {/* Recent Campaigns */}
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm">
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <h2 className="text-base font-semibold text-gray-900">Recent Campaigns</h2>
            <Link to="/campaigns" className="text-sm text-primary-600 hover:text-primary-700 flex items-center gap-1">
              View all <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
          <div className="divide-y divide-gray-50">
            {(data?.recentCampaigns || []).length === 0 ? (
              <div className="px-6 py-8 text-center text-gray-400 text-sm">No campaigns yet</div>
            ) : (
              data.recentCampaigns.map((c) => (
                <div key={c.id} className="flex items-center gap-3 px-6 py-3 hover:bg-gray-50 transition-colors">
                  <div className="w-8 h-8 rounded-lg bg-primary-50 flex items-center justify-center">
                    <Megaphone className="w-4 h-4 text-primary-600" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900 truncate">{c.name}</p>
                    <p className="text-xs text-gray-500">{c.type} • {c.totalSent} sent</p>
                  </div>
                  <Badge status={c.status} label={c.status} />
                </div>
              ))
            )}
          </div>
        </div>

        {/* Campaign Status */}
        <div className="bg-white rounded-xl p-6 border border-gray-200 shadow-sm">
          <h2 className="text-base font-semibold text-gray-900 mb-6">Campaign Status</h2>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={statusData.filter(d => d.value > 0)} barSize={32}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} tickLine={false} axisLine={false} />
              <YAxis tick={{ fontSize: 11 }} tickLine={false} axisLine={false} />
              <Tooltip contentStyle={{ borderRadius: '8px', border: '1px solid #e5e7eb', fontSize: '12px' }} />
              <Bar dataKey="value" name="Campaigns" fill="#6366f1" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  )
}
