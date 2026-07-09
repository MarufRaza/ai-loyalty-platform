import api from './api'

const couponService = {
  getAll: async (params = {}) => {
    const { data } = await api.get('/coupons', { params })
    return data.data
  },

  getValid: async (params = {}) => {
    const { data } = await api.get('/coupons/valid', { params })
    return data.data
  },

  getByCode: async (code) => {
    const { data } = await api.get(`/coupons/${code}`)
    return data.data
  },

  create: async (couponData) => {
    const { data } = await api.post('/coupons', couponData)
    return data.data
  },

  redeem: async (code, customerId, orderAmount) => {
    const { data } = await api.post(
      `/coupons/${code}/redeem`,
      null,
      { params: { customerId, orderAmount } }
    )
    return data.data
  },

  deactivate: async (id) => {
    const { data } = await api.delete(`/coupons/${id}`)
    return data
  }
}

export default couponService
