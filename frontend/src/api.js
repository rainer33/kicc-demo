import axios from 'axios'

// 모든 화면에서 공통으로 사용하는 백엔드 API 클라이언트입니다.
export const api = axios.create({
  baseURL: 'http://localhost:8080/api'
})
