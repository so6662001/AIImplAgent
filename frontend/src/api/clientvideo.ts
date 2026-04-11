import clientRequest from '@/utils/clientRequest'
import type { R } from '@/types'
export function publishVideo(data: Record<string, unknown>) { return clientRequest.post<R<any>>('/videos/publish', data) }
export function listApprovedVideos() { return clientRequest.get<R<any[]>>('/videos/approved') }
export function listMyPublished() { return clientRequest.get<R<any[]>>('/videos/my-published') }
export function startLearning(videoId: number) { return clientRequest.post<R<any>>(`/videos/${videoId}/start-learning`) }
export function updateVideoProgress(data: Record<string, unknown>) { return clientRequest.post<R<any>>('/videos/learning-progress', data) }
export function listMyLearning() { return clientRequest.get<R<any[]>>('/videos/my-learning') }
export function getPointsBalance() { return clientRequest.get<R<any>>('/videos/points/balance') }
export function getPointsHistory() { return clientRequest.get<R<any[]>>('/videos/points/history') }
