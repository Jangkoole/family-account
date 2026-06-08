import { defineStore } from 'pinia'
import { ref } from 'vue'

const THEME_KEY = 'app-theme'

/** 可用主题列表 */
export const themes = [
    { value: 'chinese', label: '新中式', icon: '🏮' },
    { value: 'editorial', label: '杂志风', icon: '📰' },
    { value: 'journal', label: '手账风', icon: '📒' },
]

/** 主题 → CSS 文件映射 */
const themeCssMap = {
    chinese: '/theme.css',
    editorial: '/theme-editorial.css',
    journal: '/theme-journal.css',
}

// 全局 link 元素引用，用于动态切换 CSS
let themeLinkEl = null

function loadThemeCss(href) {
    if (!themeLinkEl) {
        themeLinkEl = document.createElement('link')
        themeLinkEl.rel = 'stylesheet'
        document.head.appendChild(themeLinkEl)
    }
    themeLinkEl.href = href
}

export const useThemeStore = defineStore('theme', () => {
    const current = ref(localStorage.getItem(THEME_KEY) || 'chinese')

    function setTheme(value) {
        if (!themeCssMap[value]) return
        current.value = value
        localStorage.setItem(THEME_KEY, value)
        loadThemeCss(themeCssMap[value])
    }

    // 初始化时加载已保存的主题
    function init() {
        const saved = localStorage.getItem(THEME_KEY) || 'chinese'
        if (themeCssMap[saved]) {
            loadThemeCss(themeCssMap[saved])
        }
    }

    return { current, setTheme, init }
})
