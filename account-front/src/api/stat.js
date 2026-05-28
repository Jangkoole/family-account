import request from '@/utils/request'

export function getSummary(type, date) {
  return request.get('/stat/summary', { params: { type, date } })
}

export function getCategoryStats(type, startDate, endDate) {
  return request.get('/stat/category', { params: { type, startDate, endDate } })
}

export function getTrend(granularity, startDate, endDate) {
  return request.get('/stat/trend', { params: { granularity, startDate, endDate } })
}

export function getFamilyMemberStats(startDate, endDate) {
  return request.get('/stat/family/members', { params: { startDate, endDate } })
}
