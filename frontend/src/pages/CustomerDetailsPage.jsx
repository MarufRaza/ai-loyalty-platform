import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import {
  ArrowLeft, Mail, Phone, MapPin, Award, DollarSign,
  Calendar, TrendingUp, Plus, Clock
} from 'lucide-react'
import customerService from '../services/customerService'
import Badge from '../components/UI/Badge'
import Card from '../components/UI/Card'
import LoadingSpinner from '../components/UI/LoadingSpinner'
import Modal from '../components/UI/Modal'
import toast from 'react-hot-toast'

export default function CustomerDetailsPage() {
  const { id } = useParams()
  const [customer, setCustomer] = useState(null)
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(true)
  const [showPointsModal, setShowPointsModal] = useState(false)
  const [pointsForm, setPointsForm] = useState({ type: 'EARN', points: '', description: '' })
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [cust, txData] = await Promise.all([
          customerService.getById(id),
          customerService.getLoyaltyTransactions(id, { page: 0, size: 10 })
        ])
        setCustomer(cust)
        setTransactions(txData.content || [])
      } catch {
        toast.error('Failed to load customer')
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [id])

  const handleAddPoints = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      const updated = await customerService.addLoyaltyTransaction({
        customerId: parseInt(id),
        transactionType: pointsForm.type,
        points: parseInt(pointsForm.points),
        description: pointsForm.description,
      })
      toast.success(`Points ${pointsForm.type === 'EARN' ? 'added' : 'redeemed'} successfully!`)
      setShowPointsModal(false)
      const [cust, txData] = await Promise.all([
        customerService.getById(id),
        customerService.getLoyaltyTransactions(id, { page: 0, size: 10 })
      ])
      setCustomer(cust)
      setTransactions(txData.content || [])
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Transaction failed')
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <LoadingSpinner />
  if (!customer) return <div className="text-center py-16 text-gray-500">Customer not found</div>

  const tierProgress = customer.nextTierProgress || 0

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Back */}
      <Link to="/customers" className="inline-flex items-center gap-2 text-sm text-gray-500 hover:text-gray-900 transition-colors">
        <ArrowLeft className="w-4 h-4" /> Back to Customers
      </Link>

      {/* Profile Header */}
      <div className="bg-gradient-to-r from-gray-900 to-gray-800 rounded-2xl p-8 text-white">
        <div className="flex items-start gap-6">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-primary-500 to-indigo-500 flex items-center justify-center text-2xl font-bold flex-shrink-0 shadow-xl">
            {customer.name.charAt(0).toUpperCase()}
          </div>
          <div className="flex-1">
            <div className="flex items-start justify-between">
              <div>
                <h1 className="text-2xl font-bold">{customer.name}</h1>
                <div className="flex items-center gap-4 mt-2 text-gray-300 text-sm">
                  <span className="flex items-center gap-1"><Mail className="w-3.5 h-3.5" />{customer.email}</span>
                  {customer.phone && <span className="flex items-center gap-1"><Phone className="w-3.5 h-3.5" />{customer.phone}</span>}
                  {customer.city && <span className="flex items-center gap-1"><MapPin className="w-3.5 h-3.5" />{customer.city}</span>}
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Badge tier={customer.loyaltyTier} label={customer.loyaltyTierDisplayName} />
                <button onClick={() => setShowPointsModal(true)} className="btn-primary text-sm">
                  <Plus className="w-4 h-4" /> Add Points
                </button>
              </div>
            </div>

            {/* Tier Progress */}
            <div className="mt-4">
              <div className="flex items-center justify-between text-sm text-gray-300 mb-1">
                <span>Tier Progress</span>
                <span>{customer.pointsToNextTier > 0 ? `${customer.pointsToNextTier} pts to next tier` : 'Max tier reached'}</span>
              </div>
              <div className="h-2 bg-gray-700 rounded-full overflow-hidden">
                <div
                  className="h-full bg-gradient-to-r from-primary-500 to-indigo-500 rounded-full transition-all duration-500"
                  style={{ width: `${tierProgress}%` }}
                />
              </div>
              <div className="text-xs text-gray-400 mt-1">{tierProgress}% to next tier</div>
            </div>
          </div>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-white rounded-xl p-5 border border-gray-200 shadow-sm text-center">
          <Award className="w-6 h-6 text-amber-500 mx-auto mb-2" />
          <p className="text-2xl font-bold text-gray-900">{customer.loyaltyPoints?.toLocaleString()}</p>
          <p className="text-xs text-gray-500 mt-0.5">Loyalty Points</p>
        </div>
        <div className="bg-white rounded-xl p-5 border border-gray-200 shadow-sm text-center">
          <DollarSign className="w-6 h-6 text-green-500 mx-auto mb-2" />
          <p className="text-2xl font-bold text-gray-900">${customer.lifetimeValue?.toLocaleString()}</p>
          <p className="text-xs text-gray-500 mt-0.5">Lifetime Value</p>
        </div>
        <div className="bg-white rounded-xl p-5 border border-gray-200 shadow-sm text-center">
          <TrendingUp className="w-6 h-6 text-indigo-500 mx-auto mb-2" />
          <p className="text-2xl font-bold text-gray-900">{customer.loyaltyTierDisplayName}</p>
          <p className="text-xs text-gray-500 mt-0.5">Current Tier</p>
        </div>
        <div className="bg-white rounded-xl p-5 border border-gray-200 shadow-sm text-center">
          <Calendar className="w-6 h-6 text-blue-500 mx-auto mb-2" />
          <p className="text-sm font-bold text-gray-900">{customer.lastPurchaseDate || 'N/A'}</p>
          <p className="text-xs text-gray-500 mt-0.5">Last Purchase</p>
        </div>
      </div>

      {/* Transaction History */}
      <Card>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-base font-semibold text-gray-900">Transaction History</h2>
          <Clock className="w-4 h-4 text-gray-400" />
        </div>
        {transactions.length === 0 ? (
          <p className="text-gray-400 text-sm text-center py-8">No transactions yet</p>
        ) : (
          <div className="space-y-3">
            {transactions.map((tx) => (
              <div key={tx.id} className="flex items-center gap-3 py-2 border-b border-gray-50 last:border-0">
                <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold ${
                  tx.transactionType === 'EARN' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                }`}>
                  {tx.transactionType === 'EARN' ? '+' : '-'}
                </div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900">{tx.description || tx.transactionType}</p>
                  <p className="text-xs text-gray-400">{tx.createdAt ? new Date(tx.createdAt).toLocaleDateString() : ''}</p>
                </div>
                <div className="text-right">
                  <p className={`text-sm font-bold ${tx.transactionType === 'EARN' ? 'text-green-600' : 'text-red-600'}`}>
                    {tx.transactionType === 'EARN' ? '+' : '-'}{tx.points} pts
                  </p>
                  <p className="text-xs text-gray-400">Balance: {tx.balanceAfter}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>

      {/* Add Points Modal */}
      <Modal
        isOpen={showPointsModal}
        onClose={() => setShowPointsModal(false)}
        title="Loyalty Transaction"
        footer={
          <>
            <button onClick={() => setShowPointsModal(false)} className="btn-secondary">Cancel</button>
            <button onClick={handleAddPoints} disabled={submitting} className="btn-primary">
              {submitting ? 'Processing...' : 'Submit'}
            </button>
          </>
        }
      >
        <form className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Transaction Type</label>
            <select
              value={pointsForm.type}
              onChange={e => setPointsForm(p => ({ ...p, type: e.target.value }))}
              className="input-field"
            >
              <option value="EARN">Earn Points</option>
              <option value="REDEEM">Redeem Points</option>
              <option value="BONUS">Bonus Points</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Points</label>
            <input
              type="number"
              min="1"
              value={pointsForm.points}
              onChange={e => setPointsForm(p => ({ ...p, points: e.target.value }))}
              className="input-field"
              placeholder="Enter points amount"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Description</label>
            <input
              type="text"
              value={pointsForm.description}
              onChange={e => setPointsForm(p => ({ ...p, description: e.target.value }))}
              className="input-field"
              placeholder="e.g. Purchase reward, Birthday bonus"
            />
          </div>
        </form>
      </Modal>
    </div>
  )
}
