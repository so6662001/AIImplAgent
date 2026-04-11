import request from '@/utils/request'
import type { R } from '@/types'
export function createVideo(data: Record<string, unknown>) { return request.post<R<any>>('/video-library/videos', data) }
export function listVideos(module?: string) { return request.get<R<any[]>>('/video-library/videos', { params: module ? { module } : {} }) }
export function createClip(videoId: number, data: Record<string, unknown>) { return request.post<R<any>>(`/video-library/videos/${videoId}/clips`, data) }
export function listClips(videoId: number) { return request.get<R<any[]>>(`/video-library/videos/${videoId}/clips`) }
export function matchClips(params: { page?: string; field?: string; question?: string }) { return request.get<R<any[]>>('/video-library/clips/match', { params }) }
