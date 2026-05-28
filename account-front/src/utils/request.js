import axios from 'axios'
import { useUserStore } from '../stores/user'
import router from '../router'

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
        return response.data
    },
    error => {
        if (error.response && error.response.status === 401) {
            const userStore = useUserStore()
            userStore.clear()
            router.push('/login')
        }
        return Promise.reject(error)
    }
)

export default request