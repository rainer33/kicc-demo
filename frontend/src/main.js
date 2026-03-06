import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// 라우터를 포함한 Vue 애플리케이션을 마운트합니다.
createApp(App).use(router).mount('#app')
