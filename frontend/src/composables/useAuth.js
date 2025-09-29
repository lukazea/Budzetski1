import { ref, computed, readonly, watch } from "vue";
import {
  login as apiLogin,
  register as apiRegister,
  logout as apiLogout,
  isAuthenticated,
} from "@/services/auth";

const authed = ref(isAuthenticated());
const uid = ref(localStorage.getItem("userId"));

watch(uid, (v) => {
  if (v === null || v === undefined) localStorage.removeItem("userId");
  else localStorage.setItem("userId", v);
});

export function useAuth() {
  const isAuthed = computed(() => authed.value);
  const userId = readonly(uid);

  async function login(username, password) {
    const data = await apiLogin({ username, password });
    uid.value = data?.id ?? null;
    authed.value = true;
  }

  async function register(input) {
    await apiRegister(input);
  }

  function logout() {
    apiLogout();
    authed.value = false;
  }

  return { userId: readonly(userId), isAuthed, login, register, logout };
}
