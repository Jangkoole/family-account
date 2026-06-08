import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/theme-editorial.css'       // 📰 杂志编辑 - 大标题 · 留白 · 衬线体
import App from './App.vue'
import router from './router'
import '../public/font_nqn4r3r527r/iconfont.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')
