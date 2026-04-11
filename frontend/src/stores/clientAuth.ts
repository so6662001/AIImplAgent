import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const STORAGE_KEY = 'client_qa_auth'

interface ClientAuthState {
  accessToken: string
  projectId: number
  projectName: string
  employeeName: string
  role: string
}

function loadFromLocalStorage(): ClientAuthState | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) return JSON.parse(raw) as ClientAuthState
  } catch { /* ignore */ }
  return null
}

function saveToLocalStorage(state: ClientAuthState) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
}

function clearLocalStorage() {
  localStorage.removeItem(STORAGE_KEY)
}

export const useClientAuthStore = defineStore('clientAuth', () => {
  const accessToken = ref('')
  const projectId = ref(0)
  const projectName = ref('')
  const employeeName = ref('')
  const role = ref('')

  const isLoggedIn = computed(() => !!accessToken.value)

  function loadFromStorage() {
    const saved = loadFromLocalStorage()
    if (saved) {
      accessToken.value = saved.accessToken
      projectId.value = saved.projectId
      projectName.value = saved.projectName
      employeeName.value = saved.employeeName
      role.value = saved.role
    }
  }

  function setAuth(data: ClientAuthState) {
    accessToken.value = data.accessToken
    projectId.value = data.projectId
    projectName.value = data.projectName
    employeeName.value = data.employeeName
    role.value = data.role
    saveToLocalStorage(data)
  }

  function logout() {
    accessToken.value = ''
    projectId.value = 0
    projectName.value = ''
    employeeName.value = ''
    role.value = ''
    clearLocalStorage()
  }

  return { accessToken, projectId, projectName, employeeName, role, isLoggedIn, loadFromStorage, setAuth, logout }
})
