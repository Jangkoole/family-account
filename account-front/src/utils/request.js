import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 10000
})

request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = token
        }
        return config
    },
    error => Promise.reject(error)
)

request.interceptors.response.use(
    response => {
        const data = response.data
        if (data.code !== 200) {
            ElMessage.error(data.message || '请求失败')
            return Promise.reject(new Error(data.message))
        }
        return data
    },
    error => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token')
            window.location.href = '/login'
        }
        ElMessage.error(error.message || '网络错误')
        return Promise.reject(error)
    }
)

export default request
