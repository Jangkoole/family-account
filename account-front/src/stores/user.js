//说明：token和userInfo初始化时从localStorage读取，页面刷新后不会丢失登录状态
// setToken和setUserInfo同时更新内存和localStorage
// clear用于登出时清除所有登录状态

import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo')) || {})

    // 设置token
    function setToken(newToken) {
        token.value = newToken
        localStorage.setItem('token', newToken)
    }

    // 设置用户信息
    function setUserInfo(info) {
        userInfo.value = info
        localStorage.setItem('userInfo', JSON.stringify(info))
    }

    // 清除登录状态
    function clear() {
        token.value = ''
        userInfo.value = {}
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
    }

    return { token, userInfo, setToken, setUserInfo, clear }
})