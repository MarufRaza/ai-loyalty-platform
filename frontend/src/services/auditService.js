import api from './api'

const auditService = {
  getLogs: async (params = {}) => {
    const { data } = await api.get('/audit-logs', { params })
    return data.data
  }
}

export default auditService
