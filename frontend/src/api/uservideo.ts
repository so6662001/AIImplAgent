import request from '@/utils/request'
import type { R } from '@/types'
export function listAllVideos(approvalStatus?: string) { return request.get<R<any[]>>('/user-videos', { params: approvalStatus ? { approvalStatus } : {} }) }
export function approveVideo(id: number, data: { approved: boolean; rejectionReason?: string }) { return request.put<R<any>>(`/user-videos/${id}/approve`, data) }
export function getPendingCount() { return request.get<R<any>>('/user-videos/pending-count') }
