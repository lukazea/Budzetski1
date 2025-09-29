import { ref, computed } from "vue";
import {
  login as apiLogin,
  register as apiRegister,
  logout as apiLogout,
  isAuthenticated,
} from "@/services/auth";

const authed = ref(isAuthenticated());

export function useAuth() {
  const isAuthed = computed(() => authed.value);

  async function login(username, password) {
    await apiLogin({ username, password });
    authed.value = true;
  }

  async function register(input) {
    await apiRegister(input);
  }

  function logout() {
    apiLogout();
    authed.value = false;
  }

  return { isAuthed, login, register, logout };
}
