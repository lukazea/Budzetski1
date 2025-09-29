<template>
    <div class="auth-card">
      <h1>Prijava</h1>
      <form @submit.prevent="onSubmit">
        <label>Korisničko ime</label>
        <input v-model.trim="username" required autocomplete="username" />
  
        <label>Lozinka</label>
        <input v-model="password" type="password" required autocomplete="current-password" />
  
        <button :disabled="loading">{{ loading ? "Prijavljivanje..." : "Prijavi se" }}</button>
        <p class="hint">Nemaš nalog? <router-link to="/register">Registruj se</router-link></p>
        <p v-if="error" class="error">{{ error }}</p>
      </form>
    </div>
  </template>
  
  <script setup>
  import { ref } from "vue";
  import { useRouter } from "vue-router";
  import { useAuth } from "@/composables/useAuth";
  
  const router = useRouter();
  const { login } = useAuth();
  
  const username = ref("");
  const password = ref("");
  const loading = ref(false);
  const error = ref(null);
  
  async function onSubmit() {
    loading.value = true; error.value = null;
    try {
      await login(username.value, password.value);
      router.push({ name: "Home" });
    } catch (e) {
      error.value = e?.response?.data?.message ?? e?.message ?? "Neuspešna prijava.";
    } finally {
      loading.value = false;
    }
  }
  </script>
  
  <style scoped>
  .auth-card {
    max-width: 380px; margin: 40px auto; padding: 24px;
    border: 1px solid #e5e7eb; border-radius: 12px; background: #fff;
  }
  label { display:block; margin-top: 10px; font-size: 14px; color:#374151; }
  input { width:100%; padding:10px; border:1px solid #d1d5db; border-radius:8px; margin-top:6px; }
  button { margin-top:16px; width:100%; padding:10px; border-radius:10px; border:1px solid #cbd5e1; background:white; cursor:pointer; }
  button:disabled { opacity: .7; cursor:not-allowed; }
  .error { color:#b91c1c; margin-top:10px; }
  .hint { margin-top:10px; font-size: 14px; }
  </style>
  