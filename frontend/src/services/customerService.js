import api from './api'

const customerService = {
  getAll: async (params = {}) => {
    const { data } = await api.get('/customers', { params })
    return data.data
  },

  getById: async (id) => {
    const { data } = await api.get(`/customers/${id}`)
    return data.data
  },

  create: async (customerData) => {
    const { data } = await api.post('/customers', customerData)
    return data.data
  },

  update: async (id, customerData) => {
    const { data } = await api.put(`/customers/${id}`, customerData)
    return data.data
  },

  delete: async (id) => {
    const { data } = await api.delete(`/customers/${id}`)
    return data
  },

  getLoyaltyTransactions: async (customerId, params = {}) => {
    const { data } = await api.get(`/loyalty/customers/${customerId}/transactions`, { params })
    return data.data
  },

  addLoyaltyTransaction: async (transactionData) => {
    const { data } = await api.post('/loyalty/transactions', transactionData)
    return data.data
  }
}

export default customerService
