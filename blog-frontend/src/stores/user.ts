import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '@/api'
import type { User, LoginRequest, RegisterRequest } from '@/types'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))
  const loading = ref(false)

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.roles?.includes('ADMIN') ?? false)

  async function login(data: LoginRequest) {
    loading.value = true
    try {
      const response = await userApi.login(data)
      token.value = response.accessToken
      user.value = response.user
      localStorage.setItem('token', response.accessToken)
      if (response.user?.id) {
        localStorage.setItem('userId', response.user.id)
      }
      return response
    } finally {
      loading.value = false
    }
  }

  async function register(data: RegisterRequest) {
    loading.value = true
    try {
      return await userApi.register(data)
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    try {
      await userApi.logout()
    } finally {
      user.value = null
      token.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
    }
  }

  async function fetchCurrentUser() {
    if (!token.value) return null
    loading.value = true
    try {
      user.value = await userApi.getCurrentUser()
      if (user.value?.id) {
        localStorage.setItem('userId', user.value.id)
      }
      return user.value
    } catch {
      user.value = null
      token.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      return null
    } finally {
      loading.value = false
    }
  }

  async function updateProfile(data: Partial<User>) {
    if (!user.value) return
    loading.value = true
    try {
      user.value = await userApi.updateProfile(data)
    } finally {
      loading.value = false
    }
  }

  return {
    user,
    token,
    loading,
    isLoggedIn,
    isAdmin,
    login,
    register,
    logout,
    fetchCurrentUser,
    updateProfile,
  }
})
