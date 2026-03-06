import { createRouter, createWebHashHistory } from 'vue-router'
import PaymentDemoPage from './views/PaymentDemoPage.vue'
import TestHistoryListPage from './views/TestHistoryListPage.vue'
import TestHistoryDetailPage from './views/TestHistoryDetailPage.vue'

// 해시 라우팅을 사용해 별도 서버 라우팅 설정 없이 목록/상세 페이지를 제공합니다.
const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'payment-demo',
      component: PaymentDemoPage
    },
    {
      path: '/test-histories',
      name: 'test-history-list',
      component: TestHistoryListPage
    },
    {
      path: '/test-histories/:id',
      name: 'test-history-detail',
      component: TestHistoryDetailPage
    }
  ]
})

export default router
