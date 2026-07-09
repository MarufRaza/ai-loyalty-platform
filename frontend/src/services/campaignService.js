import api from './api'

const campaignService = {
  getAll: async (params = {}) => {
    const { data } = await api.get('/campaigns', { params })
    return data.data
  },

  getById: async (id) => {
    const { data } = await api.get(`/campaigns/${id}`)
    return data.data
  },

  create: async (campaignData) => {
    const { data } = await api.post('/campaigns', campaignData)
    return data.data
  },

  update: async (id, campaignData) => {
    const { data } = await api.put(`/campaigns/${id}`, campaignData)
    return data.data
  },

  publish: async (id) => {
    const { data } = await api.post(`/campaigns/${id}/publish`)
    return data.data
  },

  cancel: async (id) => {
    const { data } = await api.post(`/campaigns/${id}/cancel`)
    return data.data
  },

  generateAI: async (aiRequest) => {
    const { data } = await api.post('/ai/campaigns/generate', aiRequest)
    return data.data
  },

  getSegments: async () => {
    const { data } = await api.get('/segments')
    return data.data
  }
}

export default campaignService
