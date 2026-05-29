import request from '../utils/request'

// 获取仪表盘数据
export function getDashboardSummary() {
    return request.get('/dashboard/summary')
}
