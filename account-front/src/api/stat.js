import request from '@/utils/request'

export function getSummary(type, date, scope) {
  return request.get('/stat/summary', { params: { type, date, scope } })
}

export function getCategoryStats(type, startDate, endDate, scope) {
  return request.get('/stat/category', { params: { type, startDate, endDate, scope } })
}

export function getTrend(granularity, startDate, endDate, scope) {
  return request.get('/stat/trend', { params: { granularity, startDate, endDate, scope } })
}

export function getFamilyMemberStats(startDate, endDate) {
  return request.get('/stat/family/members', { params: { startDate, endDate } })
}
