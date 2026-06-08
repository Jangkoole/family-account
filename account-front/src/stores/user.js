import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfo } from '../api/user'
import { getFamilyInfo } from '../api/family'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo')) || {})
    const familyInfo = ref(JSON.parse(localStorage.getItem('familyInfo')) || null)
    const familyRole = ref(localStorage.getItem('familyRole') || null)

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

    // 设置家庭组信息
    function setFamilyInfo(info, role) {
        familyInfo.value = info
        familyRole.value = role
        localStorage.setItem('familyInfo', JSON.stringify(info))
        localStorage.setItem('familyRole', role)
    }

    // 清除家庭组信息
    function clearFamilyInfo() {
        familyInfo.value = null
        familyRole.value = null
        localStorage.removeItem('familyInfo')
        localStorage.removeItem('familyRole')
    }

    // 同步家庭组状态
    async function syncFamilyInfo() {
        try {
            const res = await getFamilyInfo()
            if (res.code === 200) {
                // 获取用户信息确认角色
                const userRes = await getUserInfo()
                if (userRes.code === 200) {
                    setFamilyInfo(res.data, userRes.data.familyRole)
                }
            } else {
                // 不在任何家庭组中（如被移除、退出、解散），清除家庭信息
                clearFamilyInfo()
            }
        } catch (e) {
            clearFamilyInfo()
        }
    }

    // 清除所有登录状态
    function clear() {
        token.value = ''
        userInfo.value = {}
        familyInfo.value = null
        familyRole.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('familyInfo')
        localStorage.removeItem('familyRole')
    }

    return {
        token,
        userInfo,
        familyInfo,
        familyRole,
        setToken,
        setUserInfo,
        setFamilyInfo,
        clearFamilyInfo,
        syncFamilyInfo,
        clear
    }
})