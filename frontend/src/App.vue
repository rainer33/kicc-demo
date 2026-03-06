<template>
  <main class="wrap">
    <section class="card">
      <h1>KICC 간편 결제 데모</h1>
      <p class="desc">승인, 취소, 부분환불, 관리자 조회(mock)를 테스트할 수 있습니다.</p>

      <form class="form" @submit.prevent="requestPayment">
        <label>
          주문명
          <input v-model="form.orderName" required />
        </label>

        <label>
          구매자명
          <input v-model="form.buyerName" required />
        </label>

        <label>
          결제금액(원)
          <input v-model.number="form.amount" type="number" min="100" required />
        </label>

        <label>
          Idempotency-Key (선택)
          <input v-model="idempotencyKey" placeholder="idem-001" />
        </label>

        <button :disabled="loading" type="submit">결제 요청</button>
      </form>

      <section v-if="readyData" class="result">
        <h2>결제 요청 완료</h2>
        <p><strong>Order ID:</strong> {{ readyData.orderId }}</p>
        <p><strong>PG URL:</strong> {{ readyData.payUrl }}</p>
        <p><strong>Mock 모드:</strong> {{ readyData.mockMode ? 'ON' : 'OFF' }}</p>

        <div v-if="readyData.mockMode" class="actions">
          <button @click="mockApprove" :disabled="loading">Mock 승인</button>
          <button @click="mockCancel" :disabled="loading || !canCancel" class="danger">전체취소</button>
          <button @click="refreshPayment" :disabled="loading">상태 새로고침</button>
        </div>

        <div class="refund-box">
          <label>
            환불금액(원)
            <input v-model.number="refundAmount" type="number" min="1" />
          </label>
          <label>
            사유
            <input v-model="refundReason" placeholder="고객 요청" />
          </label>
          <button @click="mockRefund" :disabled="loading || !canRefund">부분환불</button>
          <button @click="loadRefundHistory" :disabled="loading">환불이력 조회</button>
        </div>

        <details>
          <summary>전송 폼 필드 보기</summary>
          <pre>{{ readyData.formFields }}</pre>
        </details>
      </section>

      <section v-if="payment" class="result">
        <h2>결제 상태</h2>
        <p><strong>상태:</strong> {{ payment.status }}</p>
        <p><strong>PG 거래번호:</strong> {{ payment.pgTransactionId || '-' }}</p>
        <p><strong>누적 환불:</strong> {{ payment.refundedAmount ?? 0 }}</p>
        <p><strong>환불 가능:</strong> {{ payment.refundableAmount ?? 0 }}</p>
        <p><strong>실패사유:</strong> {{ payment.failReason || '-' }}</p>
      </section>

      <section v-if="refundHistory.length" class="result">
        <h2>환불 이력</h2>
        <table class="table">
          <thead>
            <tr>
              <th>환불금액</th>
              <th>환불거래ID</th>
              <th>사유</th>
              <th>시각</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in refundHistory" :key="item.refundTransactionId + item.createdAt">
              <td>{{ item.amount }}</td>
              <td>{{ item.refundTransactionId }}</td>
              <td>{{ item.reason || '-' }}</td>
              <td>{{ item.createdAt }}</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section class="result">
        <h2>관리자 도구</h2>
        <div class="admin-box">
          <label>
            Admin Token
            <input v-model="adminToken" />
          </label>
          <button @click="loadAdminPayments" :disabled="loading">결제 목록</button>
          <button @click="loadAdminOrders" :disabled="loading">주문 목록</button>
          <button @click="loadAuditLogs" :disabled="loading">감사 로그</button>
          <button @click="reconcileNow" :disabled="loading">보정 실행</button>
        </div>

        <details v-if="adminPayments.length">
          <summary>관리자 결제 목록</summary>
          <pre>{{ adminPayments }}</pre>
        </details>
        <details v-if="adminOrders.length">
          <summary>관리자 주문 목록</summary>
          <pre>{{ adminOrders }}</pre>
        </details>
        <details v-if="auditLogs.length">
          <summary>감사 로그</summary>
          <pre>{{ auditLogs }}</pre>
        </details>
      </section>

      <p v-if="error" class="error">{{ error }}</p>
    </section>
  </main>
</template>

<script setup>
import axios from 'axios'
import { computed, reactive, ref } from 'vue'

// 백엔드 API 기본 클라이언트
const api = axios.create({
  baseURL: 'http://localhost:8080/api'
})

// 결제 준비 요청 폼 상태
const form = reactive({
  orderName: '테스트 상품',
  buyerName: '홍길동',
  amount: 1000
})

// 멱등키: 같은 요청 중복 전송 시 동일 주문을 재사용하기 위한 선택 입력
const idempotencyKey = ref('')
// ready API 응답 데이터(주문번호, PG 폼 필드)
const readyData = ref(null)
// 현재 결제 상태 데이터
const payment = ref(null)
// 환불 이력 목록
const refundHistory = ref([])
// 환불 요청 입력 상태
const refundAmount = ref(100)
const refundReason = ref('고객 요청')
// 공통 로딩/에러 상태
const loading = ref(false)
const error = ref('')

// 관리자 기능 상태
const adminToken = ref('')
const adminPayments = ref([])
const adminOrders = ref([])
const auditLogs = ref([])

// 버튼 활성화 조건 계산
const canCancel = computed(() => payment.value?.status === 'APPROVED')
const canRefund = computed(() => ['APPROVED', 'PARTIALLY_REFUNDED'].includes(payment.value?.status))

// 결제 준비 요청: 필요 시 Idempotency-Key 헤더를 함께 전송
const requestPayment = async () => {
  error.value = ''
  payment.value = null
  refundHistory.value = []
  loading.value = true
  try {
    const headers = {}
    if (idempotencyKey.value?.trim()) {
      headers['Idempotency-Key'] = idempotencyKey.value.trim()
    }
    const { data } = await api.post('/payments/ready', form, { headers })
    readyData.value = data
  } catch (e) {
    error.value = e.response?.data?.message || '결제 요청 실패'
  } finally {
    loading.value = false
  }
}

// mock 승인 처리
const mockApprove = async () => {
  if (!readyData.value) return
  await runAction(async () => {
    const { data } = await api.post(`/payments/${readyData.value.orderId}/mock-approve`)
    payment.value = data
  }, 'Mock 승인 실패')
}

// mock 전체취소 처리
const mockCancel = async () => {
  if (!readyData.value) return
  await runAction(async () => {
    const { data } = await api.post(`/payments/${readyData.value.orderId}/mock-cancel`)
    payment.value = data
  }, '전체취소 실패')
}

// mock 부분환불 처리 후 환불이력 갱신
const mockRefund = async () => {
  if (!readyData.value) return
  await runAction(async () => {
    const { data } = await api.post(`/payments/${readyData.value.orderId}/mock-refund`, {
      amount: refundAmount.value,
      reason: refundReason.value
    })
    payment.value = data
    await loadRefundHistory()
  }, '부분환불 실패')
}

// 결제 상태 새로고침
const refreshPayment = async () => {
  if (!readyData.value) return
  await runAction(async () => {
    const { data } = await api.get(`/payments/${readyData.value.orderId}`)
    payment.value = data
  }, '상태 조회 실패')
}

// 환불이력 조회
const loadRefundHistory = async () => {
  if (!readyData.value) return
  await runAction(async () => {
    const { data } = await api.get(`/payments/${readyData.value.orderId}/refund-history`)
    refundHistory.value = data
  }, '환불이력 조회 실패')
}

// 관리자 결제 목록 조회
const loadAdminPayments = async () => {
  await runAction(async () => {
    const { data } = await api.get('/admin/payments', { headers: { 'X-Admin-Token': adminToken.value } })
    adminPayments.value = data
  }, '관리자 결제 목록 조회 실패')
}

// 관리자 주문 목록 조회
const loadAdminOrders = async () => {
  await runAction(async () => {
    const { data } = await api.get('/admin/orders', { headers: { 'X-Admin-Token': adminToken.value } })
    adminOrders.value = data
  }, '관리자 주문 목록 조회 실패')
}

// 관리자 감사로그 조회
const loadAuditLogs = async () => {
  await runAction(async () => {
    const { data } = await api.get('/admin/audit-logs', { headers: { 'X-Admin-Token': adminToken.value } })
    auditLogs.value = data
  }, '감사 로그 조회 실패')
}

// 수동 보정 실행 후 감사로그 새로고침
const reconcileNow = async () => {
  await runAction(async () => {
    await api.post('/admin/reconcile-now', {}, { headers: { 'X-Admin-Token': adminToken.value } })
    await loadAuditLogs()
  }, '보정 실행 실패')
}

// 모든 API 액션의 공통 에러/로딩 래퍼
const runAction = async (fn, defaultMessage) => {
  error.value = ''
  loading.value = true
  try {
    await fn()
  } catch (e) {
    error.value = e.response?.data?.message || defaultMessage
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
* { box-sizing: border-box; }
body { margin: 0; }

.wrap {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(120deg, #f7fbff, #eaf8f0);
  padding: 20px;
}

.card {
  width: min(860px, 100%);
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.08);
}

h1 { margin: 0 0 8px; }
.desc { margin: 0 0 20px; color: #555; }

.form { display: grid; gap: 12px; }
label { display: grid; gap: 6px; font-weight: 600; }
input {
  border: 1px solid #ccc;
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 14px;
}

button {
  border: 0;
  background: #12643b;
  color: white;
  border-radius: 10px;
  padding: 10px 14px;
  font-size: 14px;
  cursor: pointer;
}
button[disabled] { opacity: 0.5; cursor: not-allowed; }
button.danger { background: #b42318; }

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.refund-box {
  display: grid;
  grid-template-columns: 1fr 1fr auto auto;
  gap: 8px;
  align-items: end;
  margin-bottom: 12px;
}

.admin-box {
  display: grid;
  grid-template-columns: 1.5fr auto auto auto auto;
  gap: 8px;
  align-items: end;
}

.result {
  margin-top: 20px;
  background: #f9fafb;
  border-radius: 12px;
  padding: 14px;
}

.error {
  margin-top: 16px;
  color: #b42318;
  font-weight: 700;
}

pre {
  overflow: auto;
  background: #101828;
  color: #e4e7ec;
  border-radius: 8px;
  padding: 12px;
  font-size: 12px;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th,
.table td {
  padding: 8px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
  font-size: 13px;
}

@media (max-width: 900px) {
  .refund-box,
  .admin-box {
    grid-template-columns: 1fr;
  }
}
</style>
