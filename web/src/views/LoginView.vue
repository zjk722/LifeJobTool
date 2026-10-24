<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { mockEnabled } from '@/api/mock'
import type { UserRole } from '@/types/auth'
import { getLastPhone, setLastPhone } from '@/utils/auth-storage'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const formRef = ref<FormInstance>()
const errorRef = ref<HTMLElement | null>(null)

const form = reactive({ phone: '', code: '' })
const role = ref<UserRole>('CUSTOMER')
const agreed = ref(false)
const sending = ref(false)
const submitting = ref(false)
const countdown = ref(0)
const codeSent = ref(false)
const errorMessage = ref('')

let timer: number | undefined

const phonePattern = /^1[3-9]\d{9}$/
const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: phonePattern, message: '请输入 11 位手机号', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为 6 位数字', trigger: 'blur' },
  ],
}

const cooldownKey = computed(() => `hs_sms_cooldown_${form.phone}`)
const codeButtonText = computed(() => {
  if (countdown.value > 0) return `${countdown.value}s 后重试`
  return codeSent.value ? '重新获取' : '获取验证码'
})

function startCountdown(seconds: number) {
  window.clearInterval(timer)
  countdown.value = seconds
  const deadline = Date.now() + seconds * 1000

  timer = window.setInterval(() => {
    const left = Math.max(0, Math.ceil((deadline - Date.now()) / 1000))
    countdown.value = left
    if (left <= 0) {
      window.clearInterval(timer)
      localStorage.removeItem(cooldownKey.value)
    }
  }, 250)

  localStorage.setItem(cooldownKey.value, String(deadline))
}

function resumeCountdown() {
  window.clearInterval(timer)
  countdown.value = 0
  if (!form.phone) return

  const raw = localStorage.getItem(cooldownKey.value)
  if (!raw) return

  const left = Math.ceil((Number(raw) - Date.now()) / 1000)
  if (left > 0) {
    startCountdown(left)
  } else {
    localStorage.removeItem(cooldownKey.value)
  }
}

async function focusError() {
  await nextTick()
  errorRef.value?.focus()
}

async function sendCode() {
  errorMessage.value = ''

  if (!phonePattern.test(form.phone)) {
    errorMessage.value = '请先填写正确的手机号'
    await focusError()
    return
  }

  sending.value = true
  try {
    await auth.sendCode(form.phone)
    setLastPhone(form.phone)
    codeSent.value = true
    startCountdown(60)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '验证码发送失败，请重试'
    await focusError()
  } finally {
    sending.value = false
  }
}

async function submit() {
  errorMessage.value = ''

  if (!agreed.value) {
    errorMessage.value = '请先阅读并同意服务协议与隐私政策'
    await focusError()
    return
  }

  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    errorMessage.value = '请检查手机号和验证码'
    await focusError()
    return
  }

  submitting.value = true
  try {
    await auth.login({ phone: form.phone, code: form.code, role: role.value })
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.replace(redirect)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败，请重试'
    await focusError()
  } finally {
    submitting.value = false
  }
}

function switchRole(next: UserRole) {
  role.value = next
  errorMessage.value = ''
}

onMounted(() => {
  form.phone = getLastPhone()
  resumeCountdown()
})

onBeforeUnmount(() => {
  window.clearInterval(timer)
})
</script>

<template>
  <main class="login">
    <section class="brand" aria-labelledby="brand-title">
      <div class="brand__arch" aria-hidden="true"></div>

      <header class="brand__head">
        <span class="brand__logo">到家</span>
        <span class="brand__descriptor">家政上门服务</span>
      </header>

      <div class="brand__body">
        <h1 id="brand-title" class="brand__title">把家交给<br />对的人</h1>
        <p class="brand__lead">保洁、清洗、维修，按你的时间上门。</p>

        <ul class="brand__list">
          <li><span>日常保洁</span><span>2 小时起</span></li>
          <li><span>家电清洗</span><span>深度除菌</span></li>
          <li><span>上门维修</span><span>30 分钟响应</span></li>
        </ul>
      </div>

      <footer class="brand__foot">上海，每天 8:00–20:00 可预约</footer>
    </section>

    <section class="panel" aria-labelledby="login-title">
      <div class="panel__inner">
        <h2 id="login-title" class="panel__title">登录</h2>
        <p class="panel__lead">未注册的手机号将自动创建账号</p>

        <div class="role" role="group" aria-label="选择登录身份">
          <button
            type="button"
            class="role__item"
            :class="{ 'is-active': role === 'CUSTOMER' }"
            :aria-pressed="role === 'CUSTOMER'"
            @click="switchRole('CUSTOMER')"
          >
            我是客户
          </button>
          <button
            type="button"
            class="role__item"
            :class="{ 'is-active': role === 'WORKER' }"
            :aria-pressed="role === 'WORKER'"
            @click="switchRole('WORKER')"
          >
            我是服务人员
          </button>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="form"
          @submit.prevent="submit"
        >
          <div class="field">
            <label class="field__label" for="phone">手机号</label>
            <el-form-item prop="phone">
              <el-input
                id="phone"
                v-model="form.phone"
                inputmode="tel"
                autocomplete="tel"
                maxlength="11"
                placeholder="请输入 11 位手机号"
                size="large"
                @blur="resumeCountdown"
              />
            </el-form-item>
          </div>

          <div class="field">
            <label class="field__label" for="code">验证码</label>
            <el-form-item prop="code">
              <div class="code-row">
                <el-input
                  id="code"
                  v-model="form.code"
                  inputmode="numeric"
                  autocomplete="one-time-code"
                  maxlength="6"
                  placeholder="6 位数字"
                  size="large"
                />
                <button
                  type="button"
                  class="code-btn"
                  :disabled="sending || countdown > 0"
                  @click="sendCode"
                >
                  {{ sending ? '发送中' : codeButtonText }}
                </button>
              </div>
            </el-form-item>
          </div>

          <label class="agree">
            <input v-model="agreed" class="agree__input" type="checkbox" />
            <span class="agree__box" aria-hidden="true"></span>
            <span class="agree__text">
              我已阅读并同意
              <a href="#" @click.prevent.stop>《服务协议》</a>
              与
              <a href="#" @click.prevent.stop>《隐私政策》</a>
            </span>
          </label>

          <p v-if="errorMessage" ref="errorRef" class="error" role="alert" tabindex="-1">
            {{ errorMessage }}
          </p>

          <button type="submit" class="submit" :disabled="submitting">
            {{ submitting ? '正在登录' : '登录' }}
          </button>
        </el-form>

        <p v-if="mockEnabled" class="mock">开发模式验证码：123456</p>
      </div>
    </section>
  </main>
</template>

<style scoped lang="scss">
.login {
  display: grid;
  grid-template-columns: minmax(0, 1.18fr) minmax(420px, 0.82fr);
  min-height: 100dvh;
  background: var(--paper);
}

.brand {
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 100dvh;
  padding: clamp(28px, 3.4vw, 52px) clamp(28px, 4.4vw, 64px) clamp(24px, 3vw, 44px);
  overflow: hidden;
  color: var(--paper);
  background: var(--spruce);
}

.brand__arch {
  position: absolute;
  top: 8%;
  right: -7%;
  width: 64%;
  height: 90%;
  border: 1px solid rgba(231, 194, 125, 0.3);
  border-bottom: 0;
  border-radius: 50% 50% 0 0 / 44% 44% 0 0;
  background:
    radial-gradient(120% 92% at 50% 100%, rgba(231, 194, 125, 0.34), rgba(231, 194, 125, 0) 62%),
    linear-gradient(180deg, rgba(231, 194, 125, 0.1), rgba(11, 36, 24, 0.06));
  pointer-events: none;
  transform-origin: 50% 100%;
  animation: arch-in 900ms cubic-bezier(0.16, 1, 0.3, 1) both;
}

.brand__arch::after {
  content: "";
  position: absolute;
  inset: 12% 15% 0;
  border: 1px solid rgba(231, 194, 125, 0.16);
  border-bottom: 0;
  border-radius: 50% 50% 0 0 / 42% 42% 0 0;
}

.brand__head {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: baseline;
  gap: 14px;
}

.brand__logo {
  font-family: var(--font-display);
  font-size: 28px;
  letter-spacing: 0.12em;
  color: var(--paper);
}

.brand__descriptor {
  font-size: 12px;
  letter-spacing: 0.08em;
  color: rgba(247, 244, 236, 0.68);
}

.brand__body {
  position: relative;
  z-index: 1;
  max-width: 580px;
  margin-top: auto;
  padding-top: 72px;
}

.brand__title {
  margin: 0 0 24px;
  font-family: var(--font-display);
  font-size: clamp(48px, 5.2vw, 78px);
  font-weight: 600;
  line-height: 1.08;
  letter-spacing: 0.01em;
  color: var(--paper);
  text-wrap: balance;
}

.brand__lead {
  max-width: 22em;
  margin: 0;
  font-size: 16px;
  line-height: 1.8;
  color: rgba(247, 244, 236, 0.78);
}

.brand__list {
  max-width: 26em;
  margin: 48px 0 0;
  padding: 0;
  list-style: none;
  border-top: 1px solid rgba(231, 194, 125, 0.26);
}

.brand__list li {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 24px;
  padding: 14px 0;
  border-bottom: 1px solid rgba(231, 194, 125, 0.16);
  font-size: 13px;
  color: rgba(247, 244, 236, 0.72);
}

.brand__list li span:last-child {
  color: var(--brass-light);
  font-variant-numeric: tabular-nums;
}

.brand__foot {
  position: relative;
  z-index: 1;
  margin-top: 48px;
  font-size: 12px;
  letter-spacing: 0.04em;
  color: rgba(247, 244, 236, 0.6);
}

.panel {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100dvh;
  padding: 48px clamp(28px, 5.6vw, 88px);
  background: var(--paper);
}

.panel::before {
  content: "";
  position: absolute;
  top: 12%;
  left: 0;
  width: 1px;
  height: 76%;
  background: linear-gradient(180deg, transparent, rgba(201, 151, 63, 0.5), transparent);
}

.panel__inner {
  width: min(100%, 380px);
  animation: panel-in 700ms 120ms cubic-bezier(0.16, 1, 0.3, 1) both;
}

.panel__title {
  margin: 0 0 10px;
  font-size: 34px;
  font-weight: 600;
  line-height: 1.1;
  letter-spacing: -0.02em;
  color: var(--ink);
}

.panel__lead {
  margin: 0 0 32px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--muted);
}

.role {
  display: flex;
  gap: 28px;
  margin-bottom: 30px;
  border-bottom: 1px solid var(--line);
}

.role__item {
  position: relative;
  padding: 0 0 12px;
  border: 0;
  background: transparent;
  font-size: 14px;
  color: #7a877d;
  transition: color 180ms ease;
}

.role__item::after {
  content: "";
  position: absolute;
  right: 0;
  bottom: -1px;
  left: 0;
  height: 2px;
  background: var(--brass);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 220ms ease;
}

.role__item.is-active {
  color: var(--ink);
  font-weight: 600;
}

.role__item.is-active::after {
  transform: scaleX(1);
}

.field {
  margin-bottom: 20px;
}

.field__label {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  line-height: 1.2;
  letter-spacing: 0.04em;
  color: var(--muted);
}

.form :deep(.el-form-item) {
  margin-bottom: 0;
}

.form :deep(.el-form-item__content) {
  line-height: normal;
}

.form :deep(.el-input__wrapper) {
  height: 48px;
  padding: 0 14px;
  border-radius: 2px;
  background: transparent;
  box-shadow: 0 0 0 1px var(--line) inset;
  transition: box-shadow 180ms ease;
}

.form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #b9c4b7 inset;
}

.form :deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 1px var(--spruce) inset,
    0 0 0 3px rgba(22, 59, 43, 0.08);
}

.form :deep(.el-input__inner) {
  font-size: 15px;
  color: var(--ink);
}

.form :deep(.el-input__inner::placeholder) {
  color: #7c897f;
}

.form :deep(.el-form-item.is-error) .el-input__wrapper {
  box-shadow: 0 0 0 1px var(--danger) inset;
}

.form :deep(.el-form-item__error) {
  position: static;
  padding-top: 6px;
  font-size: 12px;
  line-height: 1.4;
  color: var(--danger);
}

.code-row {
  display: flex;
  align-items: stretch;
  gap: 12px;
  width: 100%;
}

.code-row :deep(.el-input) {
  flex: 1;
  min-width: 0;
}

.code-btn {
  flex: 0 0 118px;
  height: 48px;
  padding: 0;
  border: 0;
  border-bottom: 1px solid var(--line);
  background: transparent;
  font-size: 13px;
  color: var(--spruce);
  transition:
    color 180ms ease,
    border-color 180ms ease;
}

.code-btn:hover:not(:disabled) {
  color: var(--brass);
  border-color: var(--brass);
}

.code-btn:disabled {
  color: #9aa69c;
  cursor: not-allowed;
}

.agree {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin: 4px 0 20px;
  font-size: 12px;
  line-height: 1.65;
  color: var(--muted);
  cursor: pointer;
}

.agree__input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
}

.agree__box {
  position: relative;
  flex: 0 0 16px;
  width: 16px;
  height: 16px;
  margin-top: 2px;
  border: 1px solid #b9c4b7;
  border-radius: 2px;
  background: transparent;
  transition:
    background 160ms ease,
    border-color 160ms ease;
}

.agree__input:checked + .agree__box {
  border-color: var(--brass);
  background: var(--brass);
}

.agree__input:checked + .agree__box::after {
  content: "";
  position: absolute;
  top: 1px;
  left: 5px;
  width: 4px;
  height: 8px;
  border-right: 1.5px solid #1b160c;
  border-bottom: 1.5px solid #1b160c;
  transform: rotate(45deg);
}

.agree__input:focus-visible + .agree__box {
  outline: 2px solid var(--brass);
  outline-offset: 2px;
}

.agree__text a {
  color: var(--ink);
  text-underline-offset: 2px;
}

.agree__text a:hover {
  color: var(--brass);
}

.error {
  margin: 0 0 14px;
  padding-left: 10px;
  border-left: 2px solid var(--danger);
  font-size: 12px;
  line-height: 1.5;
  color: var(--danger);
  outline: none;
}

.submit {
  width: 100%;
  height: 52px;
  border: 0;
  border-radius: 2px;
  background: var(--brass);
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.12em;
  color: #1b160c;
  transition:
    background 180ms ease,
    transform 100ms ease,
    opacity 180ms ease;
}

.submit:hover:not(:disabled) {
  background: #d6a54a;
}

.submit:active:not(:disabled) {
  transform: scale(0.995);
}

.submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.mock {
  margin: 20px 0 0;
  font-size: 11px;
  letter-spacing: 0.02em;
  color: #8a968c;
}

@keyframes arch-in {
  from {
    opacity: 0;
    transform: scale(0.96) translateY(12px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

@keyframes panel-in {
  from {
    opacity: 0;
    transform: translateY(14px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1080px) {
  .login {
    grid-template-columns: 1fr 1fr;
  }

  .brand {
    padding-right: 36px;
    padding-left: 36px;
  }

  .brand__title {
    font-size: clamp(44px, 5vw, 58px);
  }

  .panel {
    padding-right: 36px;
    padding-left: 36px;
  }
}

@media (max-width: 820px) {
  .login {
    grid-template-columns: 1fr;
  }

  .brand {
    min-height: 250px;
    padding: 26px 24px 30px;
  }

  .brand__arch {
    top: -22%;
    right: -16%;
    width: 72%;
    height: 155%;
    opacity: 0.85;
  }

  .brand__body {
    padding-top: 42px;
  }

  .brand__title {
    margin-bottom: 14px;
    font-size: 42px;
  }

  .brand__lead {
    font-size: 14px;
    max-width: 20em;
  }

  .brand__list,
  .brand__foot {
    display: none;
  }

  .panel {
    min-height: auto;
    padding: 34px 24px 46px;
  }

  .panel::before {
    display: none;
  }

  .panel__inner {
    width: 100%;
    max-width: 420px;
    margin: 0 auto;
  }
}

@media (max-width: 480px) {
  .brand {
    min-height: 218px;
  }

  .brand__logo {
    font-size: 24px;
  }

  .brand__body {
    padding-top: 34px;
  }

  .brand__title {
    font-size: 36px;
  }

  .brand__lead {
    font-size: 13px;
  }

  .panel {
    padding: 28px 20px 40px;
  }

  .panel__title {
    font-size: 30px;
  }

  .role {
    gap: 22px;
  }

  .code-btn {
    flex-basis: 104px;
    font-size: 12px;
  }
}
</style>
