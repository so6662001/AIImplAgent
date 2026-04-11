import clientRequest from '@/utils/clientRequest'
import type { R } from '@/types'

export function getMyPapers() { return clientRequest.get<R<any[]>>('/exams/my-papers') }
export function getPaper(paperId: number) { return clientRequest.get<R<any>>(`/exams/papers/${paperId}`) }
export function submitExamAnswers(data: Record<string, unknown>) { return clientRequest.post<R<any>>('/exams/submit', data) }
export function getMyResults() { return clientRequest.get<R<any[]>>('/exams/my-results') }
