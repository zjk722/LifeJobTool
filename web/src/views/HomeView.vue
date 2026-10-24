<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()

const roleLabel = computed(() => {
  switch (auth.user?.role) {
    case 'WORKER':
      return '服务人员'
    case 'ADMIN':
      return '运营'
    default:
      return '客户'
  }
})

async function handleLogout() {
  await auth.logout()
  await router.replace('/login')
}
</script>

<template>
  <main class="home">
    <header class="home__bar">
      <div class="home__brand">
        <span class="home__logo">到家</span>
        <span class="home__descriptor">家政上门服务</span>
      </div>
      <button type="button" class="home__logout" @click="handleLogout">退出登录</button>
    </header>

    <section class="home__body">
      <p class="home__status">
        <span class="home__dot" aria-hidden="true"></span>
        登录成功
      </p>

      <h1>你好，{{ auth.user?.name || '朋友' }}</h1>
      <p class="home__lead">
        工作台还在搭建中。下一步会从这里进入下单、派单和履约流程。
      </p>

      <dl class="home__meta">
        <div>
          <dt>手机号</dt>
          <dd>{{ auth.user?.phone || '—' }}</dd>
        </div>
        <div>
          <dt>当前身份</dt>
          <dd>{{ roleLabel }}</dd>
        </div>
      </dl>

      <div class="home__next">
        <span>下一步</span>
        <p>手机号 + 验证码登录已经打通，接下来做客户下单与服务人员排班。</p>
      </div>
    </section>
  </main>
</template>

<style scoped lang="scss">
.home {
  min-height: 100dvh;
  padding: 0 clamp(20px, 5vw, 48px);
  background: var(--paper);
}

.home__bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1120px;
  height: 78px;
  margin: 0 auto;
  border-bottom: 1px solid var(--line);
}

.home__brand {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.home__logo {
  font-family: var(--font-display);
  font-size: 24px;
  letter-spacing: 0.12em;
  color: var(--ink);
}

.home__descriptor {
  font-size: 11px;
  letter-spacing: 0.08em;
  color: var(--muted);
}

.home__logout {
  padding: 4px 0;
  border: 0;
  border-bottom: 1px solid transparent;
  background: transparent;
  font-size: 13px;
  color: var(--muted);
  transition:
    color 180ms ease,
    border-color 180ms ease;
}

.home__logout:hover {
  color: var(--ink);
  border-color: var(--brass);
}

.home__body {
  max-width: 720px;
  margin: 0 auto;
  padding: clamp(56px, 10vw, 120px) 0 80px;
}

.home__status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 18px;
  font-size: 12px;
  letter-spacing: 0.04em;
  color: var(--muted);
}

.home__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--brass);
}

.home__body h1 {
  margin: 0 0 20px;
  font-size: clamp(40px, 6vw, 64px);
  font-weight: 600;
  line-height: 1.12;
  letter-spacing: -0.03em;
  color: var(--ink);
}

.home__lead {
  max-width: 34em;
  margin: 0;
  font-size: 16px;
  line-height: 1.8;
  color: var(--muted);
}

.home__meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin: 48px 0 0;
  border-top: 1px solid var(--line);
  border-bottom: 1px solid var(--line);
}

.home__meta div {
  padding: 20px 0;
}

.home__meta div + div {
  padding-left: 24px;
  border-left: 1px solid var(--line);
}

.home__meta dt {
  margin-bottom: 6px;
  font-size: 11px;
  letter-spacing: 0.06em;
  color: var(--muted);
}

.home__meta dd {
  margin: 0;
  font-size: 15px;
  color: var(--ink);
  font-variant-numeric: tabular-nums;
}

.home__next {
  display: grid;
  grid-template-columns: 90px minmax(0, 1fr);
  gap: 20px;
  margin-top: 48px;
  padding-top: 20px;
  border-top: 1px solid var(--line);
}

.home__next span {
  font-size: 11px;
  letter-spacing: 0.06em;
  color: var(--muted);
}

.home__next p {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--muted);
}

@media (max-width: 640px) {
  .home__bar {
    height: 68px;
  }

  .home__descriptor {
    display: none;
  }

  .home__body {
    padding: 48px 0 64px;
  }

  .home__meta {
    grid-template-columns: 1fr;
  }

  .home__meta div + div {
    padding-left: 0;
    border-top: 1px solid var(--line);
    border-left: 0;
  }

  .home__next {
    grid-template-columns: 1fr;
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .home__body h1 {
    font-size: 36px;
  }
}
</style>
