<template>
  <div class="auth-card">
    <h1>Registracija</h1>
    <form @submit.prevent="onSubmit">
      <div class="grid">
        <div>
          <label>Ime</label>
          <input v-model.trim="firstName" required>
        </div>
        <div>
          <label>Prezime</label>
          <input v-model.trim="lastName" required>
        </div>
      </div>

      <label>Korisničko ime</label>
      <input v-model.trim="userName" required autocomplete="username">

      <label>Email</label>
      <input
        v-model.trim="email"
        required
        type="email"
        autocomplete="email"
      >

      <label>Lozinka</label>
      <input
        v-model="password"
        required
        type="password"
        autocomplete="new-password"
      >

      <div class="grid">
        <div>
          <label>Valuta</label>
          <select v-model="currency">
            <option value="">
              —
            </option>
            <option>RSD</option>
            <option>EUR</option>
            <option>USD</option>
          </select>
        </div>
        <div>
          <label>Datum rođenja</label>
          <input v-model="birthdate" type="date">
        </div>
      </div>

      <button :disabled="loading">
        {{ loading ? "Kreiram nalog..." : "Registruj se" }}
      </button>
      <p class="hint">
        Već imaš nalog? <router-link to="/login">
          Prijava
        </router-link>
      </p>
      <p v-if="error" class="error">
        {{ error }}
      </p>
      <p v-if="success" class="success">
        Uspešna registracija. Možeš da se prijaviš.
      </p>
    </form>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuth } from "@/composables/useAuth";

const router = useRouter();
const { register } = useAuth();

const firstName = ref("");
const lastName = ref("");
const userName = ref("");
const email = ref("");
const password = ref("");
const currency = ref("");
const birthdate = ref("");

const loading = ref(false);
const error = ref(null);
const success = ref(false);

async function onSubmit() {
  loading.value = true; error.value = null; success.value = false;
  try {
    await register({
      firstName: firstName.value,
      lastName: lastName.value,
      userName: userName.value,
      email: email.value,
      password: password.value,
      currency: currency.value || undefined,
      birthdate: birthdate.value || undefined,
    });
    success.value = true;
    setTimeout(() => router.push({ name: "Login" }), 800);
  } catch (e) {
    error.value = e?.response?.data?.message ?? e?.message ?? "Registracija neuspešna.";
  } finally {
    loading.value = false;
  }
}
</script>

  <style scoped>
  .auth-card {
    max-width: 520px; margin: 40px auto; padding: 24px;
    border: 1px solid #e5e7eb; border-radius: 12px; background: #fff;
  }
  .grid { display:grid; grid-template-columns: 1fr 1fr; gap:12px; }
  label { display:block; margin-top:10px; font-size:14px; color:#374151; }
  input, select { width:100%; padding:10px; border:1px solid #d1d5db; border-radius:8px; margin-top:6px; }
  button { margin-top:16px; width:100%; padding:10px; border-radius:10px; border:1px solid #cbd5e1; background:white; cursor:pointer; }
  .error { color:#b91c1c; margin-top:10px; }
  .success { color:#065f46; margin-top:10px; }
  .hint { margin-top:10px; font-size:14px; }
  </style>
