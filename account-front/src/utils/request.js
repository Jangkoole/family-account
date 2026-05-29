import axios from 'axios'

const request = axios.create({
  baseURL: 'http://localhost:8090',
  timeout: 10000
})

// 请求拦截器：自动携带 token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    // 同时设置两种常见格式，确保后端能识别
    config.headers.Authorization = token
    config.headers.satoken = token   // Sa-Token 默认也支持此 header
    console.log('请求URL:', config.url, '携带token:', token)
  } else {
    console.warn('未找到token')
  }
  return config
}, error => Promise.reject(error))

// 响应拦截器
request.interceptors.response.use(
  response => {
    console.log('响应:', response.config.url, response.data)
    return response.data
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

export default request