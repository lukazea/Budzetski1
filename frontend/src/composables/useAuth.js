import { ref, computed, readonly, watch } from "vue";
import {
  login as apiLogin,
  register as apiRegister,
  logout as apiLogout,
  isAuthenticated,
} from "@/services/auth";

const authed = ref(isAuthenticated());
const uid = ref(localStorage.getItem("userId"));
const role = ref(localStorage.getItem("role"));

watch(uid, (v) => {
  if (v === null || v === undefined) localStorage.removeItem("userId");
  else localStorage.setItem("userId", v);
});

watch(role, (v) => {
  if (v === null || v === undefined) localStorage.removeItem("role");
  else localStorage.setItem("role", v);
});

export function useAuth() {
  const isAuthed = computed(() => authed.value);
  const userId = readonly(uid);
  const isAdmin = computed(() => role.value === "ADMIN");

  async function login(username, password) {
    const data = await apiLogin({ username, password });
    uid.value = data?.id ?? null;
    authed.value = true;
    role.value = data.role ?? null;
  }

  async function register(input) {
    await apiRegister(input);
  }

  function logout() {
    apiLogout();
    authed.value = false;
  }

  return { userId: readonly(userId), isAuthed, isAdmin, login, register, logout };
}
