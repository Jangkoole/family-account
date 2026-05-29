import request from '@/utils/request'
import { getMockBillList, getMockFamilyBillList } from './mock/bill'

const USE_MOCK = false

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

export const getBillList = (params) => {
  if (USE_MOCK) {
    return getMockBillList(params)
  }
  return request.get('/bill/list', { params })
}

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

export const getFamilyBillList = (params) => {
  if (USE_MOCK) {
    return getMockFamilyBillList(params)
  }
  return request.get('/bill/family/list', { params })
}

export const previewImport = (data) => {
  return request.post('/bill/import/preview', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const confirmImport = (previewId) => {
  return request.post('/bill/import', { previewId })
}