import { useState, useEffect } from 'react'
import {
  AreaChart, Area, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend
} from 'recharts'
import { BarChart3, TrendingUp, Users, DollarSign, Megaphone } from 'lucide-react'
import analyticsService from '../services/analyticsService'
import { StatCard } from '../components/UI/Card'
import LoadingSpinner from '../components/UI/LoadingSpinner'

const COLORS = ['#3b82f6', '#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6']

export default function AnalyticsPage() {
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
  const statusData = data ? Object.entries(data.campaignsByStatus || {})
    .filter(([, v]) => v > 0)
    .map(([name, value]) => ({ name, value })) : []

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Analytics</h1>
        <p className="text-gray-500 text-sm mt-0.5">Platform performance overview</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard title="Total Customers" value={data?.totalCustomers?.toLocaleString() || '0'} icon={Users} color="blue" />
        <StatCard title="Total Revenue" value={`$${(data?.totalRevenue || 0).toLocaleString()}`} icon={DollarSign} color="green" />
        <StatCard title="Campaigns" value={data?.totalCampaigns?.toLocaleString() || '0'} icon={Megaphone} color="purple" />
        <StatCard title="Active Campaigns" value={data?.activeCampaigns?.toLocaleString() || '0'} icon={TrendingUp} color="amber" />
      </div>

      {/* Monthly Growth Chart */}
      <div className="bg-white rounded-xl p-6 border border-gray-200 shadow-sm">
        <h2 className="text-base font-semibold text-gray-900 mb-6">Customer Growth (6 months)</h2>
        <ResponsiveContainer width="100%" height={300}>
          <AreaChart data={data?.monthlyGrowth || []}>
            <defs>
              <linearGradient id="growthGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.2} />
                <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
              </linearGradient>
              <linearGradient id="revenueGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#10b981" stopOpacity={0.2} />
                <stop offset="95%" stopColor="#10b981" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis dataKey="month" tick={{ fontSize: 12 }} tickLine={false} axisLine={false} />
            <YAxis tick={{ fontSize: 12 }} tickLine={false} axisLine={false} />
            <Tooltip contentStyle={{ borderRadius: '8px', border: '1px solid #e5e7eb', fontSize: '12px' }} />
            <Legend />
            <Area type="monotone" dataKey="newCustomers" name="New Customers" stroke="#3b82f6" fill="url(#growthGrad)" strokeWidth={2} />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      {/* Two Charts */}
      <div className="grid lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl p-6 border border-gray-200 shadow-sm">
          <h2 className="text-base font-semibold text-gray-900 mb-6">Campaign Status Distribution</h2>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={statusData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} tickLine={false} axisLine={false} />
              <YAxis tick={{ fontSize: 11 }} tickLine={false} axisLine={false} />
              <Tooltip contentStyle={{ borderRadius: '8px', border: '1px solid #e5e7eb', fontSize: '12px' }} />
              <Bar dataKey="value" name="Campaigns" radius={[4, 4, 0, 0]}>
                {statusData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-white rounded-xl p-6 border border-gray-200 shadow-sm">
          <h2 className="text-base font-semibold text-gray-900 mb-6">Loyalty Tier Distribution</h2>
          {tierData.length > 0 ? (
            <>
              <ResponsiveContainer width="100%" height={200}>
                <PieChart>
                  <Pie data={tierData} cx="50%" cy="50%" outerRadius={80} dataKey="value" label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}>
                    {tierData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                  </Pie>
                  <Tooltip formatter={(v) => [v.toLocaleString(), 'Customers']} />
                </PieChart>
              </ResponsiveContainer>
              <div className="flex justify-center gap-6 mt-2">
                {tierData.map(({ name, value }, i) => (
                  <div key={name} className="flex items-center gap-1.5 text-sm">
                    <div className="w-3 h-3 rounded-full" style={{ backgroundColor: COLORS[i % COLORS.length] }} />
                    <span className="text-gray-600">{name}</span>
                    <span className="font-semibold">{value}</span>
                  </div>
                ))}
              </div>
            </>
          ) : (
            <div className="h-48 flex items-center justify-center text-gray-400 text-sm">No tier data</div>
          )}
        </div>
      </div>

      {/* Top Segments */}
      {data?.topSegments?.length > 0 && (
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
          <h2 className="text-base font-semibold text-gray-900 mb-4">Top Customer Segments</h2>
          <div className="space-y-3">
            {data.topSegments.map((seg, i) => {
              const max = Math.max(...data.topSegments.map(s => s.customerCount), 1)
              return (
                <div key={i} className="flex items-center gap-3">
                  <span className="w-32 text-sm text-gray-700 truncate">{seg.name}</span>
                  <div className="flex-1 h-2 bg-gray-100 rounded-full overflow-hidden">
                    <div
                      className="h-full rounded-full transition-all duration-500"
                      style={{ width: `${(seg.customerCount / max) * 100}%`, backgroundColor: COLORS[i % COLORS.length] }}
                    />
                  </div>
                  <span className="text-sm font-semibold text-gray-900 w-12 text-right">
                    {seg.customerCount.toLocaleString()}
                  </span>
                </div>
              )
            })}
          </div>
        </div>
      )}
    </div>
  )
}
