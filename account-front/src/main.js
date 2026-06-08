import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/theme.css'                 // 🏮 新中式 - 朱红 · 墨色 · 金色 · 宣纸
// import './styles/theme-warm.css'          // 🍵 温暖日式（备选）
import App from './App.vue'
import router from './router'
import '../public/font_nqn4r3r527r/iconfont.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')
