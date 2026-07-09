import { createContext, useState, useEffect, useCallback } from 'react'
import authService from '../services/authService'
import api from '../services/api'

export const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const storedUser = localStorage.getItem('user')
    const token = localStorage.getItem('accessToken')
    if (storedUser && token) {
      setUser(JSON.parse(storedUser))
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`
    }
    setLoading(false)
  }, [])

  const login = useCallback(async (email, password) => {
    const data = await authService.login(email, password)
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('user', JSON.stringify({
      id: data.userId,
      name: data.name,
      email: data.email,
      role: data.role,
    }))
    api.defaults.headers.common['Authorization'] = `Bearer ${data.accessToken}`
    setUser({ id: data.userId, name: data.name, email: data.email, role: data.role })
    return data
  }, [])

  const register = useCallback(async (name, email, password, role) => {
    const data = await authService.register(name, email, password, role)
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('user', JSON.stringify({
      id: data.userId,
      name: data.name,
      email: data.email,
      role: data.role,
    }))
    api.defaults.headers.common['Authorization'] = `Bearer ${data.accessToken}`
    setUser({ id: data.userId, name: data.name, email: data.email, role: data.role })
    return data
  }, [])

  const logout = useCallback(async () => {
    try {
      await authService.logout()
    } catch {
      // Silent fail
    } finally {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
      delete api.defaults.headers.common['Authorization']
      setUser(null)
    }
  }, [])

  const hasRole = useCallback((role) => {
    return user?.role === role
  }, [user])

  const hasAnyRole = useCallback((...roles) => {
    return roles.some(r => user?.role === r)
  }, [user])

  return (
    <AuthContext.Provider value={{
      user,
      loading,
      isAuthenticated: !!user,
      login,
      register,
      logout,
      hasRole,
      hasAnyRole,
    }}>
      {children}
    </AuthContext.Provider>
  )
}
