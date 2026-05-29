// src/api/mock/bill.js
// 模拟后端返回的数据

// 获取模拟的账单列表
export const getMockBillList = (params) => {
  // 模拟数据
  const mockList = [
    {
      id: 1001,
      type: 'EXPENSE',
      categoryId: 1,
      categoryName: '餐饮',
      amount: 45.50,
      date: '2026-05-28',
      note: '午餐哈哈哈哈哈哈哈',
      visible: 'PRIVATE',
      tags: ['午餐']
    },
    {
      id: 1002,
      type: 'EXPENSE',
      categoryId: 2,
      categoryName: '交通',
      amount: 28.00,
      date: '2026-05-27',
      note: '打车上班',
      visible: 'FAMILY',
      tags: ['通勤']
    },
    {
      id: 1003,
      type: 'INCOME',
      categoryId: 9,
      categoryName: '工资',
      amount: 8000.00,
      date: '2026-05-25',
      note: '5月工资',
      visible: 'PRIVATE',
      tags: []
    },
    {
      id: 1004,
      type: 'EXPENSE',
      categoryId: 3,
      categoryName: '购物',
      amount: 199.00,
      date: '2026-05-26',
      note: '买衣服',
      visible: 'FAMILY',
      tags: ['购物']
    },
    {
      id: 1005,
      type: 'EXPENSE',
      categoryId: 7,
      categoryName: '娱乐',
      amount: 88.00,
      date: '2026-05-24',
      note: '看电影',
      visible: 'PRIVATE',
      tags: ['电影']
    }
  ]

  // 模拟筛选逻辑
  let filteredList = [...mockList]

  if (params.type) {
    filteredList = filteredList.filter(item => item.type === params.type)
  }
  if (params.categoryId) {
    filteredList = filteredList.filter(item => item.categoryId === params.categoryId)
  }
  if (params.startDate) {
    filteredList = filteredList.filter(item => item.date >= params.startDate)
  }
  if (params.endDate) {
    filteredList = filteredList.filter(item => item.date <= params.endDate)
  }

  // 分页
  const page = params.page || 1
  const pageSize = params.pageSize || 20
  const start = (page - 1) * pageSize
  const end = start + pageSize
  const pagedList = filteredList.slice(start, end)

  // 返回统一格式
  return Promise.resolve({
    code: 200,
    message: 'success',
    data: {
      total: filteredList.length,
      page: page,
      pageSize: pageSize,
      list: pagedList
    }
  })
}

// 模拟家人账单列表（管理员接口）
export const getMockFamilyBillList = (params) => {
  const mockList = [
    {
      id: 2001,
      userId: 2,
      nickname: '张三',
      type: 'EXPENSE',
      categoryName: '餐饮',
      amount: 120.00,
      date: '2026-05-28',
      note: '请客吃饭',
      visible: 'FAMILY'
    },
    {
      id: 2002,
      userId: 3,
      nickname: '李四',
      type: 'INCOME',
      categoryName: '工资',
      amount: 6000.00,
      date: '2026-05-25',
      note: '5月工资',
      visible: 'FAMILY'
    }
  ]

  let filteredList = [...mockList]
  if (params.userId) {
    filteredList = filteredList.filter(item => item.userId === params.userId)
  }

  return Promise.resolve({
    code: 200,
    message: 'success',
    data: {
      total: filteredList.length,
      page: params.page || 1,
      pageSize: params.pageSize || 20,
      list: filteredList
    }
  })
}

// 模拟分类列表
export const getMockCategoryList = () => {
  return [
    { id: 1, name: '餐饮', type: 'EXPENSE', isSystem: true },
    { id: 2, name: '交通', type: 'EXPENSE', isSystem: true },
    { id: 3, name: '购物', type: 'EXPENSE', isSystem: true },
    { id: 4, name: '住房', type: 'EXPENSE', isSystem: true },
    { id: 5, name: '医疗', type: 'EXPENSE', isSystem: true },
    { id: 6, name: '教育', type: 'EXPENSE', isSystem: true },
    { id: 7, name: '娱乐', type: 'EXPENSE', isSystem: true },
    { id: 8, name: '其他支出', type: 'EXPENSE', isSystem: true },
    { id: 9, name: '工资', type: 'INCOME', isSystem: true },
    { id: 10, name: '奖金', type: 'INCOME', isSystem: true },
    { id: 11, name: '理财收益', type: 'INCOME', isSystem: true },
    { id: 12, name: '其他收入', type: 'INCOME', isSystem: true }
  ]
}

// 模拟标签列表
export const getMockTagList = () => {
  return [
    { id: 1, name: '午餐' },
    { id: 2, name: '通勤' },
    { id: 3, name: '购物' },
    { id: 4, name: '电影' }
  ]
}

// 模拟家庭成员列表
export const getMockMemberList = () => {
  return [
    { userId: 1, nickname: '谢依', role: 'ADMIN', joinTime: '2026-01-01' },
    { userId: 2, nickname: '张三', role: 'MEMBER', joinTime: '2026-01-02' },
    { userId: 3, nickname: '李四', role: 'MEMBER', joinTime: '2026-01-03' }
  ]
}