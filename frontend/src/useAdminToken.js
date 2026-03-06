import { ref, watch } from 'vue'

const STORAGE_KEY = 'kicc_admin_token'

// 관리자 토큰을 페이지 간 공유하기 위한 전역 ref입니다.
const adminToken = ref(localStorage.getItem(STORAGE_KEY) || '')

// 값이 바뀔 때마다 localStorage에 동기화해 새로고침 후에도 유지합니다.
watch(adminToken, (value) => {
  localStorage.setItem(STORAGE_KEY, value || '')
})

export const useAdminToken = () => ({
  adminToken
})
