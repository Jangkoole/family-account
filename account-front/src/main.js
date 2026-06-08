import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import '../public/font_nqn4r3r527r/iconfont.css'
import { useThemeStore } from './stores/theme'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')

// 动态加载已保存的主题 CSS（必须在 mount 之后，确保 Pinia 已初始化）
const themeStore = useThemeStore()
themeStore.init()
