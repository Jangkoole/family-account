import request from '../utils/request'

// 注册
export function register(data) {
    return request.post('/user/register', data)
}

// 登录
export function login(data) {
    return request.post('/user/login', data)
}

// 登出
export function logout() {
    return request.post('/user/logout')
}

// 获取个人信息
export function getUserInfo() {
    return request.get('/user/info')
}

// 修改昵称
export function updateNickname(data) {
    return request.put('/user/nickname', data)
}

// 修改密码
export function updatePassword(data) {
    return request.put('/user/password', data)
}

// 修改默认可见范围
export function updateDefaultVisible(data) {
    return request.put('/user/default-visible', data)
}