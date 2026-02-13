import request from '@/utils/request'
import type { User, LoginRequest, RegisterRequest, LoginResponse } from '@/types'

export const userApi = {
  login(data: LoginRequest): Promise<LoginResponse> {
    return request.post('/users/login', data)
  },

  register(data: RegisterRequest): Promise<User> {
    return request.post('/users/register', data)
  },

  logout(): Promise<void> {
    return request.post('/users/logout')
  },

  getCurrentUser(): Promise<User> {
    return request.get('/users/profile')
  },

  getUserById(id: string): Promise<User> {
    return request.get(`/users/${id}`)
  },

  updateProfile(data: Partial<User>): Promise<User> {
    return request.put('/users/profile', data)
  },

  updateAvatar(avatarUrl: string): Promise<void> {
    return request.put('/users/avatar', { avatarUrl })
  },

  followUser(userId: string): Promise<void> {
    return request.post(`/users/${userId}/follow`)
  },

  unfollowUser(userId: string): Promise<void> {
    return request.delete(`/users/${userId}/follow`)
  },

  getFollowers(userId: string, page = 0, size = 20): Promise<any> {
    return request.get(`/users/${userId}/followers`, { params: { page, size } })
  },

  getFollowing(userId: string, page = 0, size = 20): Promise<any> {
    return request.get(`/users/${userId}/following`, { params: { page, size } })
  },
}
