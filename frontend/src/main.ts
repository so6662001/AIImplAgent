import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
import App from './App.vue'
import './styles/global.css'
import { useAuthStore } from './stores/auth'
import { useClientAuthStore } from './stores/clientAuth'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)

const authStore = useAuthStore()
authStore.loadFromStorage()

const clientAuthStore = useClientAuthStore()
clientAuthStore.loadFromStorage()

app.use(router)
app.use(ElementPlus, { size: 'default' })
app.mount('#app')
