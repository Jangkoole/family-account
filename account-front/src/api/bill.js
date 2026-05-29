// src/api/bill.js
import request from '@/utils/request'  // 假设你已封装 axios 实例，自动携带 token

// 新增收支记录
export function addBill(data) {
  return request({
    url: '/bill/add',
    method: 'post',
    data
  })
}

// 修改收支记录
export function updateBill(data) {
  return request({
    url: '/bill/update',
    method: 'put',
    data
  })
}

// 删除收支记录
export function deleteBill(id) {
  return request({
    url: `/bill/delete/${id}`,
    method: 'delete'
  })
}

// 查询收支记录列表（分页 + 筛选）
export function listBills(params) {
  return request({
    url: '/bill/list',
    method: 'get',
    params
  })
}

// 获取收支记录详情
export function getBillDetail(id) {
  return request({
    url: `/bill/detail/${id}`,
    method: 'get'
  })
}

// 批量修改可见范围
export function updateVisible(data) {
  return request({
    url: '/bill/visible',
    method: 'put',
    data
  })
}

// 预览导入（上传文件预览）
export function previewImport(file, source) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('source', source)
  return request({
    url: '/bill/import/preview',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 正式导入
export function importBills(file, source) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('source', source)
  return request({
    url: '/bill/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 家庭管理员查询成员明细
export function listFamilyBills(params) {
  return request({
    url: '/bill/family/list',
    method: 'get',
    params
  })
}