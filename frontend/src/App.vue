<template>
  <div class="app-shell">
    <header class="topbar">
      <div class="brand">
        <strong>KICC Demo Lab</strong>
        <span>결제 + 테스트 이력 운영 콘솔</span>
      </div>
      <nav class="nav">
        <RouterLink to="/" class="nav-link">결제 데모</RouterLink>
        <RouterLink to="/kiosk-demo" class="nav-link">키오스크 데모</RouterLink>
        <RouterLink to="/test-histories" class="nav-link">테스트 이력</RouterLink>
      </nav>
    </header>

    <RouterView />
  </div>
</template>

<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<style scoped>
:global(*) { box-sizing: border-box; }
:global(body) { margin: 0; font-family: "Pretendard Variable", "Noto Sans KR", sans-serif; }

.app-shell {
  min-height: 100vh;
  background:
    radial-gradient(circle at 8% 8%, rgba(15, 118, 110, 0.2), transparent 28%),
    radial-gradient(circle at 92% 12%, rgba(30, 64, 175, 0.2), transparent 25%),
    linear-gradient(180deg, #f8fafc 0%, #ecfeff 100%);
}

.topbar {
  width: min(1180px, calc(100% - 24px));
  margin: 0 auto;
  padding: 16px 0 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.brand {
  display: grid;
  gap: 2px;
}

.brand strong {
  font-size: 18px;
  color: #0f172a;
}

.brand span {
  font-size: 12px;
  color: #475467;
}

.nav {
  display: flex;
  gap: 8px;
}

.nav-link {
  text-decoration: none;
  color: #0f172a;
  font-weight: 700;
  font-size: 13px;
  border: 1px solid #cbd5e1;
  border-radius: 999px;
  padding: 8px 12px;
  background: #ffffff;
}

.nav-link.router-link-exact-active {
  background: #0f766e;
  color: #ffffff;
  border-color: #0f766e;
}

@media (max-width: 740px) {
  .topbar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
