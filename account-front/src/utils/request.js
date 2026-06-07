import axios from 'axios'
import { useUserStore } from '../stores/user'
import router from '../router'
import { ElMessage } from 'element-plus'

const request = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 10000
})

// 请求拦截器：自动携带token
request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = token
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器：统一处理401
request.interceptors.response.use(
    response => {
        // 后端返回的code为401时，清除登录状态并跳转登录页
        if (response.data && response.data.code === 401) {
            const userStore = useUserStore()
            userStore.clear()
            ElMessage.warning('登录已过期，请重新登录')
            setTimeout(() => {
                router.push('/login')
            }, 1000)
            return Promise.reject(new Error(response.data.message || '未登录或token已失效'))
        }
        return response.data
    },
    error => {
        if (error.response && error.response.status === 401) {
            const userStore = useUserStore()
            userStore.clear()
            ElMessage.warning('登录已过期，请重新登录')
            setTimeout(() => {
                router.push('/login')
            }, 1000)
        }
        return Promise.reject(error)
    }
)

export default request