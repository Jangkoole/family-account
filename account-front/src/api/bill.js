import request from '@/utils/request'
import { getMockBillList, getMockFamilyBillList } from './mock/bill'

// 是否使用 mock 数据（开发时用 true，联调时改 false）
const USE_MOCK = false

// 3.1 新增收支记录
export const addBill = (data) => {
  if (USE_MOCK) { 
    return Promise.resolve({
      code: 200,
      message: 'success',
      data: { id: Date.now() }
    })
  }
  return request.post('/bill/add', data)
} 

// 3.2 修改收支记录
export const updateBill = (data) => {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 200,
      message: 'success',
      data: null
    })
  }
  return request.put('/bill/update', data)
}

// 3.3 删除收支记录
export const deleteBill = (id) => {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 200,
      message: 'success',
      data: null
    })
  }
  return request.delete(`/bill/delete/${id}`)
}

// 3.4 查询收支记录列表
export const getBillList = (params) => {
  if (USE_MOCK) {
    return getMockBillList(params)
  }
  return request.get('/bill/list', { params })
}

// 3.5 获取收支记录详情
export const getBillDetail = (id) => {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 200,
      message: 'success',
      data: {
        id: id,
        type: 'EXPENSE',
        categoryId: 1,
        categoryName: '餐饮',
        amount: 45.50,
        date: '2026-05-28',
        note: '午餐',
        visible: 'PRIVATE'
      }
    })
  }
  return request.get(`/bill/detail/${id}`)
}

// 3.6 修改可见范围
export const updateBillVisible = (data) => {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 200,
      message: 'success',
      data: null
    })
  }
  return request.put('/bill/visible', data)
}

// 3.9 家庭管理员查询成员记录
export const getFamilyBillList = (params) => {
  if (USE_MOCK) {
    return getMockFamilyBillList(params)
  }
  return request.get('/bill/family/list', { params })
}