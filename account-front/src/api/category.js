import request from '@/utils/request'
import { getMockCategoryList } from './mock/bill'

const USE_MOCK = true

// 2.1 获取分类列表
export const getCategoryList = () => {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 200,
      message: 'success',
      data: getMockCategoryList()
    })
  }
  return request.get('/category/list')
}