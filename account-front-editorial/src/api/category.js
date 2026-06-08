import request from '@/utils/request'

// 2.1 获取分类列表
export const getCategoryList = () => {
  return request.get('/category/list')
}

// 2.2 新增自定义分类
export const addCategory = (data) => {
  return request.post('/category/add', data)
}

// 2.3 修改自定义分类
export const updateCategory = (data) => {
  return request.put('/category/update', data)
}

// 2.4 删除自定义分类
export const deleteCategory = (id) => {
  return request.delete(`/category/delete/${id}`)
}

// 2.5 迁移分类下的记录
export const migrateCategory = (data) => {
  return request.put('/category/migrate', data)
}

// 2.6 获取待审核分类申请列表
export const getCategoryApplyList = () => {
  return request.get('/category/apply/list')
}

// 2.7 审核分类申请
export const reviewCategoryApply = (data) => {
  return request.put('/category/apply/review', data)
}
