import request from '@/utils/request'

// 是否使用 mock 数据
const USE_MOCK = true

// 模拟标签数据
const mockTags = [
  { id: 1, name: '午餐' },
  { id: 2, name: '通勤' },
  { id: 3, name: '购物' },
  { id: 4, name: '电影' }
]

// 获取标签列表
export const getTagList = () => {
  if (USE_MOCK) {
    return Promise.resolve({
      code: 200,
      message: 'success',   
      data: mockTags
    })
  }
  return request.get('/tag/list')
}

// 新增标签
export const addTag = (data) => {
  if (USE_MOCK) {
    const newTag = {
      id: Date.now(),
      name: data.name
    }
    mockTags.push(newTag)
    return Promise.resolve({
      code: 200,
      message: 'success',
      data: { id: newTag.id }
    })
  }
  return request.post('/tag/add', data)
}

// 删除标签
export const deleteTag = (id) => {
  if (USE_MOCK) {
    const index = mockTags.findIndex(t => t.id === id)
    if (index !== -1) mockTags.splice(index, 1)
    return Promise.resolve({
      code: 200,
      message: 'success',
      data: null
    })
  }
  return request.delete(`/tag/delete/${id}`)
}