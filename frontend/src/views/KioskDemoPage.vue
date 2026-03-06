<template>
  <main class="page">
    <section class="kiosk-shell">
      <div class="left-pane">
        <header class="kiosk-header">
          <h1>무인 키오스크 결제 데모</h1>
          <p>주문 생성부터 단말 승인 이벤트까지 키오스크 연동 흐름을 재현합니다.</p>
        </header>

        <section class="step-card">
          <h2>1. 주문 생성</h2>
          <div class="grid3">
            <label>키오스크 ID<input v-model="createForm.kioskId" /></label>
            <label>주문명<input v-model="createForm.orderName" /></label>
            <label>금액<input v-model.number="createForm.amount" type="number" min="100" /></label>
          </div>
          <button @click="createSession" :disabled="loading">세션 시작</button>
        </section>

        <section class="step-card" v-if="session">
          <h2>2. 결제 요청</h2>
          <div class="method-row">
            <button
              v-for="method in methods"
              :key="method.value"
              @click="paymentMethod = method.value"
              :class="['method', { active: paymentMethod === method.value }]"
              :disabled="loading"
            >
              {{ method.label }}
            </button>
          </div>
          <button @click="requestPayment" :disabled="loading">결제 요청 전송</button>
        </section>

        <section class="step-card" v-if="session && session.status === 'PAYMENT_REQUESTED'">
          <h2>3. 단말 이벤트</h2>
          <div class="grid2">
            <label>단말 거래번호<input v-model="terminalTxId" placeholder="TERM-TX-001" /></label>
            <label>메시지<input v-model="terminalMessage" placeholder="카드사 승인" /></label>
          </div>
          <div class="action-row">
            <button @click="terminalApprove" :disabled="loading">단말 승인</button>
            <button class="danger" @click="terminalFail" :disabled="loading">단말 실패</button>
          </div>
        </section>

        <p v-if="error" class="error">{{ error }}</p>
      </div>

      <aside class="right-pane">
        <section class="receipt" v-if="session">
          <h3>현재 세션</h3>
          <ul>
            <li><strong>Session:</strong> {{ session.sessionId }}</li>
            <li><strong>Kiosk:</strong> {{ session.kioskId }}</li>
            <li><strong>주문:</strong> {{ session.orderName }}</li>
            <li><strong>금액:</strong> {{ session.amount }}</li>
            <li><strong>상태:</strong> <span class="status">{{ session.status }}</span></li>
            <li><strong>수단:</strong> {{ session.paymentMethod || '-' }}</li>
            <li><strong>Order ID:</strong> {{ session.orderId || '-' }}</li>
            <li><strong>단말TX:</strong> {{ session.terminalTransactionId || '-' }}</li>
            <li><strong>메시지:</strong> {{ session.lastMessage || '-' }}</li>
          </ul>
          <button @click="refreshSession" :disabled="loading">상태 새로고침</button>
        </section>

        <section class="history">
          <div class="history-head">
            <h3>최근 세션 50건</h3>
            <button @click="loadSessions" :disabled="loading">새로고침</button>
          </div>
          <div class="list">
            <button
              v-for="item in sessions"
              :key="item.sessionId"
              class="row"
              @click="selectSession(item.sessionId)"
            >
              <span>{{ item.sessionId }}</span>
              <span>{{ item.status }}</span>
              <span>{{ item.amount }}</span>
            </button>
            <p v-if="sessions.length === 0" class="empty">세션이 없습니다.</p>
          </div>
        </section>
      </aside>
    </section>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../api'

const loading = ref(false)
const error = ref('')

const createForm = reactive({
  kioskId: 'KIOSK-01',
  orderName: '아메리카노',
  amount: 4500
})

const methods = [
  { value: 'CARD', label: '카드' },
  { value: 'EASY_PAY', label: '간편결제' },
  { value: 'CASH', label: '현금' }
]
const paymentMethod = ref('CARD')

const session = ref(null)
const sessions = ref([])
const terminalTxId = ref('TERM-TX-001')
const terminalMessage = ref('카드사 승인')

const createSession = async () => {
  await runAction(async () => {
    const { data } = await api.post('/kiosk/sessions', createForm)
    session.value = data
    await loadSessions()
  }, '세션 생성 실패')
}

const requestPayment = async () => {
  if (!session.value) return
  await runAction(async () => {
    const { data } = await api.post(`/kiosk/sessions/${session.value.sessionId}/request-payment`, {
      paymentMethod: paymentMethod.value
    })
    session.value = data
    await loadSessions()
  }, '결제 요청 실패')
}

const terminalApprove = async () => {
  if (!session.value) return
  await runAction(async () => {
    const { data } = await api.post(`/kiosk/sessions/${session.value.sessionId}/terminal-approve`, {
      terminalTransactionId: terminalTxId.value,
      message: terminalMessage.value
    })
    session.value = data
    await loadSessions()
  }, '단말 승인 실패')
}

const terminalFail = async () => {
  if (!session.value) return
  await runAction(async () => {
    const { data } = await api.post(`/kiosk/sessions/${session.value.sessionId}/terminal-fail`, {
      terminalTransactionId: terminalTxId.value,
      message: terminalMessage.value || '카드사 거절'
    })
    session.value = data
    await loadSessions()
  }, '단말 실패 처리 실패')
}

const refreshSession = async () => {
  if (!session.value) return
  await runAction(async () => {
    const { data } = await api.get(`/kiosk/sessions/${session.value.sessionId}`)
    session.value = data
  }, '세션 조회 실패')
}

const loadSessions = async () => {
  const { data } = await api.get('/kiosk/sessions')
  sessions.value = data
}

const selectSession = async (sessionId) => {
  await runAction(async () => {
    const { data } = await api.get(`/kiosk/sessions/${sessionId}`)
    session.value = data
  }, '세션 선택 실패')
}

const runAction = async (fn, defaultMessage) => {
  loading.value = true
  error.value = ''
  try {
    await fn()
  } catch (e) {
    error.value = e.response?.data?.message || defaultMessage
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await runAction(loadSessions, '세션 목록 조회 실패')
})
</script>

<style scoped>
.page { padding: 18px; }

.kiosk-shell {
  width: min(1180px, 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 14px;
}

.left-pane,
.right-pane {
  display: grid;
  gap: 12px;
}

.kiosk-header {
  background: linear-gradient(130deg, #111827, #1f2937);
  color: #f9fafb;
  border-radius: 18px;
  padding: 18px;
}

.kiosk-header h1 { margin: 0 0 8px; }
.kiosk-header p { margin: 0; color: #d1d5db; }

.step-card,
.receipt,
.history {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 14px;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.06);
}

.step-card h2,
.receipt h3,
.history h3 { margin: 0 0 10px; }

label {
  display: grid;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
}

input {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  background: #f9fafb;
}

.grid3 {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 10px;
}

.grid2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 10px;
}

.method-row {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.method {
  background: #e5e7eb;
  color: #1f2937;
}

.method.active {
  background: #0f766e;
  color: #fff;
}

.action-row {
  display: flex;
  gap: 8px;
}

button {
  border: 0;
  border-radius: 10px;
  padding: 10px 14px;
  background: #1d4ed8;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}

button.danger { background: #b42318; }
button[disabled] { opacity: 0.5; cursor: not-allowed; }

.receipt ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 6px;
}

.status {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 999px;
  background: #dbeafe;
  color: #1e3a8a;
  font-weight: 800;
}

.history-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.list {
  display: grid;
  gap: 6px;
  max-height: 400px;
  overflow: auto;
}

.row {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 8px;
  align-items: center;
  background: #f8fafc;
  color: #111827;
  border: 1px solid #e2e8f0;
}

.empty { margin: 0; color: #6b7280; }
.error { color: #b42318; font-weight: 700; }

@media (max-width: 980px) {
  .kiosk-shell {
    grid-template-columns: 1fr;
  }

  .grid3,
  .grid2 {
    grid-template-columns: 1fr;
  }
}
</style>
