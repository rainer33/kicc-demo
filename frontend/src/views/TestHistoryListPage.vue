<template>
  <main class="page">
    <section class="hero">
      <div>
        <h1>테스트 이력 관리</h1>
        <p>동시성, 네트워크 장애, 보안 테스트까지 누적 추적하는 운영 화면입니다.</p>
      </div>
      <div class="token-box">
        <label>
          Admin Token
          <input v-model="adminToken" placeholder="관리자 토큰 입력" />
        </label>
        <button @click="loadHistories" :disabled="loading">조회</button>
      </div>
    </section>

    <section class="panel">
      <div class="filter-row">
        <label>
          카테고리
          <select v-model="filters.category">
            <option value="">전체</option>
            <option v-for="option in categoryOptions" :key="option" :value="option">{{ option }}</option>
          </select>
        </label>

        <label>
          결과
          <select v-model="filters.status">
            <option value="">전체</option>
            <option v-for="option in statusOptions" :key="option" :value="option">{{ option }}</option>
          </select>
        </label>

        <label class="keyword">
          키워드
          <input v-model="filters.keyword" placeholder="시나리오, runId, endpoint 검색" />
        </label>

        <button @click="applyFilters" :disabled="loading">필터 적용</button>
      </div>

      <details class="create-box">
        <summary>새 테스트 이력 등록</summary>
        <div class="create-grid">
          <label>Run ID<input v-model="createForm.runId" placeholder="RUN-20260307-009" /></label>
          <label>시나리오<input v-model="createForm.scenarioName" placeholder="네트워크 장애 재시도" /></label>
          <label>카테고리
            <select v-model="createForm.category">
              <option v-for="option in categoryOptions" :key="option" :value="option">{{ option }}</option>
            </select>
          </label>
          <label>결과
            <select v-model="createForm.status">
              <option v-for="option in statusOptions" :key="option" :value="option">{{ option }}</option>
            </select>
          </label>
          <label>환경<input v-model="createForm.environment" placeholder="staging" /></label>
          <label>실행자<input v-model="createForm.executedBy" placeholder="qa-user" /></label>
          <label>API<input v-model="createForm.apiEndpoint" placeholder="/api/payments/ready" /></label>
          <label>HTTP 메서드<input v-model="createForm.httpMethod" placeholder="POST" /></label>
          <label>동시성<input v-model.number="createForm.concurrencyLevel" type="number" min="1" /></label>
          <label>네트워크<input v-model="createForm.networkCondition" placeholder="latency-200ms" /></label>
          <label class="span2">기대 결과<input v-model="createForm.expectedResult" /></label>
          <label class="span2">실제 결과<input v-model="createForm.actualResult" /></label>
        </div>
        <button @click="createHistory" :disabled="loading">이력 저장</button>
      </details>

      <div v-if="error" class="error">{{ error }}</div>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Run ID</th>
              <th>시나리오</th>
              <th>카테고리</th>
              <th>결과</th>
              <th>환경</th>
              <th>동시성</th>
              <th>네트워크</th>
              <th>실행자</th>
              <th>시작시각</th>
              <th>소요(ms)</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="item in pageData.content"
              :key="item.id"
              class="clickable"
              @click="goDetail(item.id)"
            >
              <td>{{ item.id }}</td>
              <td>{{ item.runId }}</td>
              <td>{{ item.scenarioName }}</td>
              <td><span class="badge category">{{ item.category }}</span></td>
              <td><span class="badge" :class="statusClass(item.status)">{{ item.status }}</span></td>
              <td>{{ item.environment }}</td>
              <td>{{ item.concurrencyLevel ?? '-' }}</td>
              <td>{{ item.networkCondition || '-' }}</td>
              <td>{{ item.executedBy }}</td>
              <td>{{ formatDate(item.startedAt) }}</td>
              <td>{{ item.durationMs ?? '-' }}</td>
            </tr>
            <tr v-if="!loading && pageData.content.length === 0">
              <td colspan="11" class="empty">조회 결과가 없습니다.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pager">
        <button @click="prevPage" :disabled="loading || pageData.page <= 0">이전</button>
        <span>{{ pageData.page + 1 }} / {{ Math.max(pageData.totalPages, 1) }} 페이지</span>
        <button @click="nextPage" :disabled="loading || pageData.page + 1 >= pageData.totalPages">다음</button>
      </div>
    </section>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { useAdminToken } from '../useAdminToken'

const router = useRouter()
const { adminToken } = useAdminToken()

const loading = ref(false)
const error = ref('')
const categoryOptions = [
  'PAYMENT_FLOW',
  'IDEMPOTENCY',
  'CONCURRENCY',
  'NETWORK_FAILURE',
  'CALLBACK_SECURITY',
  'REFUND',
  'CANCEL',
  'RECONCILIATION',
  'INFRA'
]
const statusOptions = ['PASS', 'FAIL', 'BLOCKED', 'SKIPPED']

const filters = reactive({
  category: '',
  status: '',
  keyword: ''
})

const createForm = reactive({
  runId: '',
  scenarioName: '',
  category: 'CONCURRENCY',
  status: 'PASS',
  environment: 'local-dev',
  apiEndpoint: '/api/payments/ready',
  httpMethod: 'POST',
  concurrencyLevel: 1,
  networkCondition: 'normal',
  expectedResult: '',
  actualResult: '',
  executedBy: 'tester'
})

const pageData = reactive({
  content: [],
  page: 0,
  size: 12,
  totalElements: 0,
  totalPages: 0
})

const loadHistories = async (targetPage = 0) => {
  error.value = ''
  loading.value = true
  try {
    const params = {
      page: targetPage,
      size: pageData.size
    }
    if (filters.category) params.category = filters.category
    if (filters.status) params.status = filters.status
    if (filters.keyword?.trim()) params.keyword = filters.keyword.trim()

    const { data } = await api.get('/admin/test-histories', {
      params,
      headers: {
        'X-Admin-Token': adminToken.value
      }
    })

    pageData.content = data.content
    pageData.page = data.page
    pageData.size = data.size
    pageData.totalElements = data.totalElements
    pageData.totalPages = data.totalPages
  } catch (e) {
    error.value = e.response?.data?.message || '테스트 이력 조회 실패'
    pageData.content = []
    pageData.page = 0
    pageData.totalPages = 0
  } finally {
    loading.value = false
  }
}

const applyFilters = async () => {
  await loadHistories(0)
}

const prevPage = async () => {
  if (pageData.page <= 0) return
  await loadHistories(pageData.page - 1)
}

const nextPage = async () => {
  if (pageData.page + 1 >= pageData.totalPages) return
  await loadHistories(pageData.page + 1)
}

const goDetail = (id) => {
  router.push({ name: 'test-history-detail', params: { id } })
}

const createHistory = async () => {
  error.value = ''
  loading.value = true
  try {
    const payload = {
      runId: createForm.runId.trim(),
      scenarioName: createForm.scenarioName.trim(),
      category: createForm.category,
      status: createForm.status,
      environment: createForm.environment.trim(),
      apiEndpoint: createForm.apiEndpoint.trim() || null,
      httpMethod: createForm.httpMethod.trim() || null,
      concurrencyLevel: Number.isFinite(createForm.concurrencyLevel) ? createForm.concurrencyLevel : null,
      networkCondition: createForm.networkCondition.trim() || null,
      expectedResult: createForm.expectedResult.trim(),
      actualResult: createForm.actualResult.trim(),
      executedBy: createForm.executedBy.trim()
    }
    await api.post('/admin/test-histories', payload, {
      headers: { 'X-Admin-Token': adminToken.value }
    })
    createForm.runId = ''
    createForm.scenarioName = ''
    createForm.expectedResult = ''
    createForm.actualResult = ''
    await loadHistories(0)
  } catch (e) {
    error.value = e.response?.data?.message || '테스트 이력 저장 실패'
  } finally {
    loading.value = false
  }
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
  await loadHistories(0)
})
</script>

<style scoped>
.page {
  padding: 18px;
  display: grid;
  gap: 16px;
}

.hero {
  width: min(1180px, 100%);
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: end;
  background: linear-gradient(135deg, #0f766e, #0b3d2e);
  color: #f0fdfa;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 16px 36px rgba(15, 118, 110, 0.3);
}

.hero h1 { margin: 0 0 8px; }
.hero p { margin: 0; opacity: 0.9; }

.token-box {
  display: grid;
  gap: 8px;
  min-width: 340px;
}

label {
  display: grid;
  gap: 6px;
  font-weight: 700;
  font-size: 13px;
}

input,
select {
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 14px;
  background: #ffffff;
}

button {
  border: 0;
  border-radius: 10px;
  padding: 10px 14px;
  background: #14532d;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}
button[disabled] { opacity: 0.5; cursor: not-allowed; }

.panel {
  width: min(1180px, 100%);
  margin: 0 auto;
  background: #ffffff;
  border: 1px solid #d1fae5;
  border-radius: 20px;
  padding: 18px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
}

.create-box {
  margin-bottom: 12px;
  border: 1px solid #d1fae5;
  background: #f0fdfa;
  border-radius: 12px;
  padding: 10px;
}

.create-box summary {
  cursor: pointer;
  font-weight: 800;
  margin-bottom: 10px;
}

.create-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 10px;
}

.span2 {
  grid-column: span 2;
}

.filter-row {
  display: grid;
  grid-template-columns: 180px 160px 1fr auto;
  gap: 10px;
  align-items: end;
  margin-bottom: 14px;
}

.keyword {
  width: 100%;
}

.error {
  margin-bottom: 10px;
  color: #b42318;
  font-weight: 700;
}

.table-wrap {
  overflow: auto;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}

.table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1080px;
}

.table th,
.table td {
  text-align: left;
  padding: 10px;
  border-bottom: 1px solid #e5e7eb;
  font-size: 13px;
}

.table th {
  background: #f8fafc;
  position: sticky;
  top: 0;
}

.clickable {
  cursor: pointer;
}

.clickable:hover {
  background: #f0fdfa;
}

.badge {
  display: inline-block;
  border-radius: 999px;
  padding: 3px 8px;
  font-size: 11px;
  font-weight: 800;
}

.badge.category {
  background: #dcfce7;
  color: #166534;
}

.badge.pass { background: #d1fae5; color: #065f46; }
.badge.fail { background: #fee2e2; color: #991b1b; }
.badge.blocked { background: #fef3c7; color: #92400e; }
.badge.skipped { background: #e2e8f0; color: #334155; }

.empty {
  text-align: center !important;
  color: #64748b;
}

.pager {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
}

@media (max-width: 1100px) {
  .hero {
    flex-direction: column;
    align-items: stretch;
  }

  .token-box {
    min-width: unset;
  }

  .filter-row {
    grid-template-columns: 1fr;
  }

  .create-grid {
    grid-template-columns: 1fr;
  }

  .span2 {
    grid-column: auto;
  }
}
</style>
