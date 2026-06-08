import request from '../utils/request'

// 创建家庭组
export function createFamily(data) {
    return request.post('/family/create', data)
}

// 获取家庭组信息
export function getFamilyInfo() {
    return request.get('/family/info')
}

// 申请加入家庭组
export function joinFamily(data) {
    return request.post('/family/join', data)
}

// 获取申请列表
export function getApplyList() {
    return request.get('/family/apply/list')
}

// 审核申请
export function reviewApply(data) {
    return request.put('/family/apply/review', data)
}

// 获取成员列表
export function getMemberList() {
    return request.get('/family/member/list')
}

// 移除成员
export function removeMember(userId) {
    return request.delete(`/family/member/remove/${userId}`)
}

// 转让管理员
export function transferAdmin(data) {
    return request.put('/family/admin/transfer', data)
}

// 刷新邀请码
export function refreshInviteCode() {
    return request.put('/family/invite/refresh')
}

// 退出家庭组
export function quitFamily() {
    return request.post('/family/quit')
}

// 解散家庭组
export function dissolveFamily() {
    return request.delete('/family/dissolve')
}
