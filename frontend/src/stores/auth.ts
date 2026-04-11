import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const STORAGE_KEY = 'ai_delivery_auth'

interface AuthState {
  token: string
  username: string
  realName: string
  role: string
}

function loadFromLocalStorage(): AuthState | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) return JSON.parse(raw) as AuthState
  } catch { /* ignore */ }
  return null
}

function saveToLocalStorage(state: AuthState) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
}

function clearLocalStorage() {
  localStorage.removeItem(STORAGE_KEY)
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const username = ref('')
  const realName = ref('')
  const role = ref('')

  const isLoggedIn = computed(() => !!token.value)

  function loadFromStorage() {
    const saved = loadFromLocalStorage()
    if (saved) {
      token.value = saved.token
      username.value = saved.username
      realName.value = saved.realName
      role.value = saved.role
    }
  }

  function setAuth(data: { token: string; username: string; realName: string; role: string }) {
    token.value = data.token
    username.value = data.username
    realName.value = data.realName
    role.value = data.role
    saveToLocalStorage({ token: data.token, username: data.username, realName: data.realName, role: data.role })
  }

  function logout() {
    token.value = ''
    username.value = ''
    realName.value = ''
    role.value = ''
    clearLocalStorage()
  }

  return { token, username, realName, role, isLoggedIn, loadFromStorage, setAuth, logout }
})
