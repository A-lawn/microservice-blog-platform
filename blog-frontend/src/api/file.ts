import request from '@/utils/request'

export interface UploadResponse {
  id: string
  originalName: string
  contentType: string
  size: number
  url: string
  thumbnailUrl: string
}

export const fileApi = {
  uploadFile(file: File): Promise<UploadResponse> {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  uploadFiles(files: File[]): Promise<UploadResponse[]> {
    const formData = new FormData()
    files.forEach((file) => {
      formData.append('files', file)
    })
    return request.post('/files/upload/batch', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  getFileInfo(fileId: string): Promise<UploadResponse> {
    return request.get(`/files/${fileId}/info`)
  },

  deleteFile(fileId: string): Promise<void> {
    return request.delete(`/files/${fileId}`)
  },

  getPresignedUrl(fileId: string, expiresInSeconds = 3600): Promise<string> {
    return request.get(`/files/${fileId}/presigned-url`, { params: { expiresInSeconds } })
  },
}
