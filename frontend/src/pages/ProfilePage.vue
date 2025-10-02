<template>
  <div class="profile-page">
    <h1>Podešavanja i profil</h1>

    <section class="card">
      <h2>Podaci o nalogu</h2>
      <div class="grid">
        <div>
          <div class="label">
            Korisničko ime
          </div>
          <div class="value">
            {{ original?.userName }}
          </div>
        </div>
        <div>
          <div class="label">
            Email
          </div>
          <div class="value">
            {{ original?.email }}
          </div>
        </div>
        <div>
          <div class="label">
            Uloga
          </div>
          <div class="value">
            {{ original?.role }}
          </div>
        </div>
        <div>
          <div class="label">
            Registrovan
          </div>
          <div class="value">
            {{ original?.registrationdate ?? '-' }}
          </div>
        </div>
      </div>
    </section>

    <section class="card">
      <h2>Izmena ličnih podataka</h2>
      <form class="form" @submit.prevent="onSave">
        <div class="row">
          <label>Ime
            <input v-model.trim="form.firstName" placeholder="Ime">
          </label>
          <label>Prezime
            <input v-model.trim="form.lastName" placeholder="Prezime">
          </label>
        </div>

        <div class="row">
          <label>Korisničko ime
            <input v-model.trim="form.userName" placeholder="npr. pera.peric" @input="debouncedCheckUsername">
            <small v-if="usernameStatus === 'checking'" class="muted">provera…</small>
            <small v-if="usernameStatus === 'taken'" class="err">zauzeto</small>
            <small v-if="usernameStatus === 'ok'" class="ok">slobodno</small>
          </label>
          <label>Email
            <input
              v-model.trim="form.email"
              type="email"
              placeholder="npr. pera@primer.com"
              @input="debouncedCheckEmail"
            >
            <small v-if="emailStatus === 'checking'" class="muted">provera…</small>
            <small v-if="emailStatus === 'taken'" class="err">zauzet</small>
            <small v-if="emailStatus === 'ok'" class="ok">slobodan</small>
          </label>
        </div>

        <div class="row">
          <label>Datum rođenja
            <input v-model="form.birthdate" type="date">
          </label>
          <label>Valuta
            <select v-model="form.currency">
              <option v-for="c in currencies" :key="c" :value="c">{{ c }}</option>
            </select>
          </label>
        </div>

        <div class="actions">
          <button type="submit" :disabled="saving || usernameStatus==='taken' || emailStatus==='taken'">
            {{ saving ? 'Čuvam…' : 'Sačuvaj izmene' }}
          </button>
          <button type="button" :disabled="saving" @click="reset">
            Poništi
          </button>
          <span v-if="okMsg" class="ok">{{ okMsg }}</span>
          <span v-if="error" class="err">{{ error }}</span>
        </div>
      </form>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from "vue";
import { useAuth } from "@/composables/useAuth";
import * as api from "@/services/users";

const { userId } = useAuth();
const currencies = ["RSD","EUR","USD"];

const original = ref(null);
const form = reactive({ firstName: "", lastName: "", userName: "", email: "", birthdate: "", currency: "RSD" });
const saving = ref(false);
const okMsg = ref("");
const error = ref("");

const usernameStatus = ref("idle"); // idle|checking|ok|taken
const emailStatus = ref("idle");
let uTimer = null;
let eTimer = null;

function reset(){
  if(!original.value) return;
  Object.assign(form, {
    firstName: original.value.firstName ?? "",
    lastName: original.value.lastName ?? "",
    userName: original.value.userName ?? "",
    email: original.value.email ?? "",
    birthdate: original.value.birthdate ?? "",
    currency: original.value.currency ?? "RSD",
  });
  okMsg.value = "";
  error.value = "";
  usernameStatus.value = "idle";
  emailStatus.value = "idle";
}

function parseAvail(resp){
  if (typeof resp === "boolean") return resp;
  if (resp && typeof resp === "object"){
    if ("available" in resp) return !!resp.available;
    const vals = Object.values(resp);
    if (vals.length) return !!vals[0];
  }
  return true;
}

function debouncedCheckUsername(){
  usernameStatus.value = "checking";
  clearTimeout(uTimer);
  uTimer = setTimeout(async () => {
    try{
      if(form.userName && original.value && form.userName !== original.value.userName){
        const res = await api.checkUsernameAvailability(userId.value, form.userName);
        usernameStatus.value = parseAvail(res) ? "ok" : "taken";
      } else {
        usernameStatus.value = "idle";
      }
    } catch { usernameStatus.value = "idle"; }
  }, 400);
}

function debouncedCheckEmail(){
  emailStatus.value = "checking";
  clearTimeout(eTimer);
  eTimer = setTimeout(async () => {
    try{
      if(form.email && original.value && form.email !== original.value.email){
        const res = await api.checkEmailAvailability(userId.value, form.email);
        emailStatus.value = parseAvail(res) ? "ok" : "taken";
      } else {
        emailStatus.value = "idle";
      }
    } catch { emailStatus.value = "idle"; }
  }, 400);
}

async function load(){
  const prof = await api.getUserProfile(userId.value);
  original.value = prof;
  reset();
}

async function onSave(){
  saving.value = true; okMsg.value=""; error.value="";
  try{
    const payload = { ...form };
    const updated = await api.updateUserProfile(userId.value, payload);
    original.value = updated.user;
    okMsg.value = "Izmene sačuvane.";
  }catch(e){
    error.value = e?.response?.data?.message ?? e?.message ?? "Greška pri čuvanju.";
  }finally{
    saving.value = false;
  }
}

onMounted(load);
</script>

  <style scoped>
  .profile-page { max-width: 900px; margin: 0 auto; padding: 20px; display: grid; gap: 20px; font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial; }
  .card { background: #fff; border: 1px solid #e6e8ee; border-radius: 12px; padding: 16px; }
  .card h2 { margin: 0 0 12px; }
  .grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
  .label { color: #6b7280; font-size: 12px; }
  .value { font-weight: 600; }
  .form { display: grid; gap: 12px; }
  .row { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .row label { display: grid; gap: 6px; }
  input, select { border: 1px solid #d1d5db; border-radius: 8px; padding: 8px 10px; }
  .actions { display: flex; gap: 8px; align-items: center; }
  button { border: 1px solid #cbd5e1; background: white; border-radius: 8px; padding: 8px 12px; cursor: pointer; }
  button:hover { background: #f3f4f6; }
  .ok { color: #065f46; }
  .err { color: #b91c1c; }
  .muted { color: #6b7280; }
  </style>
