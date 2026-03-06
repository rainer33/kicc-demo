<template>
  <main class="page">
    <section class="header-card">
      <div>
        <h1>테스트 이력 상세</h1>
        <p>실행 조건, 기대결과, 실제결과를 근거 데이터와 함께 확인합니다.</p>
      </div>
      <div class="actions">
        <label>
          Admin Token
          <input v-model="adminToken" />
        </label>
        <button @click="loadDetail" :disabled="loading">새로고침</button>
        <button @click="goList" class="outline">목록으로</button>
      </div>
    </section>

    <section class="detail-card" v-if="detail">
      <div class="title-row">
        <h2>{{ detail.scenarioName }}</h2>
        <div class="chips">
          <span class="chip category">{{ detail.category }}</span>
          <span class="chip" :class="statusClass(detail.status)">{{ detail.status }}</span>
        </div>
      </div>

      <div class="meta-grid">
        <div><strong>ID</strong><span>{{ detail.id }}</span></div>
        <div><strong>Run ID</strong><span>{{ detail.runId }}</span></div>
        <div><strong>환경</strong><span>{{ detail.environment }}</span></div>
        <div><strong>실행자</strong><span>{{ detail.executedBy }}</span></div>
        <div><strong>API</strong><span>{{ detail.httpMethod || '-' }} {{ detail.apiEndpoint || '-' }}</span></div>
        <div><strong>Idempotency-Key</strong><span>{{ detail.idempotencyKey || '-' }}</span></div>
        <div><strong>동시성 레벨</strong><span>{{ detail.concurrencyLevel ?? '-' }}</span></div>
        <div><strong>네트워크 조건</strong><span>{{ detail.networkCondition || '-' }}</span></div>
        <div><strong>시작 시각</strong><span>{{ formatDate(detail.startedAt) }}</span></div>
        <div><strong>종료 시각</strong><span>{{ formatDate(detail.finishedAt) }}</span></div>
        <div><strong>소요(ms)</strong><span>{{ detail.durationMs ?? '-' }}</span></div>
      </div>

      <div class="text-block">
        <h3>기대 결과</h3>
        <p>{{ detail.expectedResult }}</p>
      </div>

      <div class="text-block">
        <h3>실제 결과</h3>
        <p>{{ detail.actualResult }}</p>
      </div>

      <div class="split">
        <div class="text-block">
          <h3>요청 페이로드</h3>
          <pre>{{ detail.requestPayload || '-' }}</pre>
        </div>
        <div class="text-block">
          <h3>응답 페이로드</h3>
          <pre>{{ detail.responsePayload || '-' }}</pre>
        </div>
      </div>

      <div class="text-block" v-if="detail.notes">
        <h3>메모</h3>
        <p>{{ detail.notes }}</p>
      </div>
    </section>

    <section class="detail-card" v-if="error">
      <p class="error">{{ error }}</p>
    </section>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api'
import { useAdminToken } from '../useAdminToken'

const route = useRoute()
const router = useRouter()
const { adminToken } = useAdminToken()

const loading = ref(false)
const error = ref('')
const detail = ref(null)

const loadDetail = async () => {
  error.value = ''
  loading.value = true
  try {
    const { data } = await api.get(`/admin/test-histories/${route.params.id}`, {
      headers: {
        'X-Admin-Token': adminToken.value
      }
    })
    detail.value = data
  } catch (e) {
    error.value = e.response?.data?.message || '테스트 이력 상세 조회 실패'
    detail.value = null
  } finally {
    loading.value = false
  }
}

const goList = () => {
  router.push({ name: 'test-history-list' })
}

const formatDate = (value) => {
  if (!value) return '-'
  return new Date(value).toLocaleString('ko-KR')
}

const statusClass = (status) => {
  if (status === 'PASS') return 'pass'
  if (status === 'FAIL') return 'fail'
  if (status === 'BLOCKED') return 'blocked'
  return 'skipped'
}

onMounted(async () => {
  await loadDetail()
})
</script>

<style scoped>
.page {
  padding: 18px;
  display: grid;
  gap: 16px;
}

.header-card,
.detail-card {
  width: min(1080px, 100%);
  margin: 0 auto;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
}

.header-card {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: end;
  background: linear-gradient(130deg, #7c2d12, #9a3412);
  color: #fff7ed;
}

.header-card h1 { margin: 0 0 8px; }
.header-card p { margin: 0; opacity: 0.9; }

.actions {
  display: flex;
  gap: 8px;
  align-items: end;
}

label {
  display: grid;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
}

input {
  border: 1px solid #fed7aa;
  border-radius: 10px;
  padding: 9px 11px;
  min-width: 280px;
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

button.outline {
  background: #fff;
  color: #9a3412;
}

button[disabled] { opacity: 0.5; cursor: not-allowed; }

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.title-row h2 {
  margin: 0;
}

.chips {
  display: flex;
  gap: 8px;
}

.chip {
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 12px;
  font-weight: 800;
  background: #e2e8f0;
  color: #334155;
}

.chip.category {
  background: #fef3c7;
  color: #92400e;
}

.chip.pass { background: #d1fae5; color: #065f46; }
.chip.fail { background: #fee2e2; color: #991b1b; }
.chip.blocked { background: #fef3c7; color: #92400e; }
.chip.skipped { background: #e2e8f0; color: #334155; }

.meta-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.meta-grid div {
  display: grid;
  gap: 4px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px;
  background: #f8fafc;
}

.meta-grid strong {
  font-size: 12px;
  color: #334155;
}

.meta-grid span {
  font-size: 14px;
  color: #0f172a;
  word-break: break-word;
}

.text-block {
  margin-top: 14px;
}

.text-block h3 {
  margin: 0 0 6px;
}

.text-block p {
  margin: 0;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px;
  line-height: 1.5;
}

.split {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

pre {
  margin: 0;
  background: #0f172a;
  color: #e2e8f0;
  border-radius: 10px;
  padding: 12px;
  overflow: auto;
  min-height: 160px;
}

.error {
  color: #b42318;
  font-weight: 700;
}

@media (max-width: 1100px) {
  .header-card {
    flex-direction: column;
    align-items: stretch;
  }

  .actions {
    flex-direction: column;
    align-items: stretch;
  }

  input {
    min-width: unset;
  }

  .meta-grid,
  .split {
    grid-template-columns: 1fr;
  }
}
</style>
