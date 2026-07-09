import api from './api'

const analyticsService = {
  getDashboard: async () => {
    const { data } = await api.get('/analytics/dashboard')
    return data.data
  },

  getAuditLogs: async (params = {}) => {
    const { data } = await api.get('/audit-logs', { params })
    return data.data
  }
}

export default analyticsService
