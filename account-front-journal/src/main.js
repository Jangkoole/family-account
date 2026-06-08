import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/theme-journal.css'         // 📒 手账本 - 点阵 · 纸片 · 手写感
import App from './App.vue'
import router from './router'
import '../public/font_nqn4r3r527r/iconfont.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')
