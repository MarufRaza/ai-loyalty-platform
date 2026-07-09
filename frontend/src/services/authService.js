import api from './api'

const authService = {
  login: async (email, password) => {
    const { data } = await api.post('/auth/login', { email, password })
    return data.data
  },

  register: async (name, email, password, role) => {
    const { data } = await api.post('/auth/register', { name, email, password, role })
    return data.data
  },

  logout: async () => {
    await api.post('/auth/logout')
  },

  refreshToken: async (refreshToken) => {
    const { data } = await api.post('/auth/refresh', { refreshToken })
    return data.data
  },

  changePassword: async (payload) => {
    const { data } = await api.put('/auth/change-password', payload)
    return data.data
  }
}

export default authService
