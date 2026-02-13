import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import type { ApiResponse } from '@/types'

const instance: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

instance.interceptors.request.use(
  (config) => {
    NProgress.start()
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    const userId = localStorage.getItem('userId')
    if (userId) {
      config.headers['X-User-Id'] = userId
    }
    
    const csrfToken = getCsrfToken()
    if (csrfToken) {
      config.headers['X-XSRF-TOKEN'] = csrfToken
    }
    return config
  },
  (error) => {
    NProgress.done()
    return Promise.reject(error)
  }
)

instance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    NProgress.done()
    const { data } = response
    if (data.code === 200) {
      return data.data
    }
    ElMessage.error(data.message || '请求失败')
    return Promise.reject(new Error(data.message))
  },
  (error) => {
    NProgress.done()
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('userId')
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

function getCsrfToken(): string | null {
  const cookies = document.cookie.split(';')
  for (const cookie of cookies) {
    const [name, value] = cookie.trim().split('=')
    if (name === 'XSRF-TOKEN') {
      return value
    }
  }
  return null
}

const request = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return instance.get(url, config)
  },
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return instance.post(url, data, config)
  },
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return instance.put(url, data, config)
  },
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return instance.delete(url, config)
  },
}

export default request
