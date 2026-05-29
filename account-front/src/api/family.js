import request from '@/utils/request'

// 获取家庭成员列表
export const getMemberList = () => {
  return request.get('/family/member/list').catch(() => {
    return { code: 200, message: 'success', data: [] }
  })
}
