export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

export interface PageParams {
  page?: number
  size?: number
  sort?: string
}

export interface User {
  id: string
  username: string
  email: string
  nickname: string
  avatarUrl: string
  bio: string
  status: string
  createdAt: string
  statistics: UserStatistics
}

export interface UserStatistics {
  articleCount: number
  commentCount: number
  likeCount: number
  followerCount: number
  followingCount: number
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface LoginResponse {
  token: string
  expiresIn: number
  user: User
}

export interface Article {
  id: string
  authorId: string
  authorName: string
  authorAvatar: string
  title: string
  slug: string
  content: string
  summary: string
  coverImage: string
  status: string
  publishTime: string
  createdAt: string
  tags: string[]
  category: Category
  statistics: ArticleStatistics
}

export interface ArticleStatistics {
  viewCount: number
  likeCount: number
  commentCount: number
  shareCount: number
  bookmarkCount: number
}

export interface CreateArticleRequest {
  title: string
  content: string
  summary: string
  coverImage: string
  categoryId: number
  tags: string[]
}

export interface Category {
  id: number
  name: string
  description: string
  articleCount: number
}

export interface Tag {
  id: number
  name: string
  slug: string
  articleCount: number
}

export interface Comment {
  id: string
  articleId: string
  authorId: string
  authorName: string
  authorAvatar: string
  parentId: string
  content: string
  status: string
  createdAt: string
  likeCount: number
  replyCount: number
  replies: Comment[]
}

export interface CreateCommentRequest {
  articleId: string
  parentId?: string
  content: string
}

export interface Notification {
  id: number
  type: string
  title: string
  content: string
  referenceId: string
  referenceType: string
  isRead: boolean
  createdAt: string
}
