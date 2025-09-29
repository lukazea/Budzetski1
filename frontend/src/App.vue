<template>
  <div id="app">
    <nav>
      <router-link to="/">Home</router-link>
      <router-link v-if="!isAuthed" to="/login">Login</router-link>
      <router-link v-if="!isAuthed" to="/register">Register</router-link>
      <router-link v-if="isAuthed" to="/wallets">Wallets</router-link>
      <button v-if="isAuthed" class="logout" @click="doLogout">Logout</button>
    </nav>
    <router-view></router-view>
  </div>
</template>

<script setup>
import { useAuth } from "@/composables/useAuth";
import { useRouter, useRoute } from "vue-router";

const { isAuthed, logout } = useAuth();
const router = useRouter();
const route = useRoute();

function doLogout() {
  logout();
  if (route.name === "Wallets") router.push({ name: "Home" });
}
</script>

<style scoped>
nav {
  display: flex;
  gap: 1rem;
  background: #f5f5f5;
  padding: 10px;
  align-items: center;
}
router-link {
  text-decoration: none;
  font-weight: bold;
  color: #333;
}
router-link.active {
  color: #42b983;
}
.logout {
  margin-left: auto;
  border: 1px solid #cbd5e1;
  background: white;
  border-radius: 8px;
  padding: 6px 10px;
  cursor: pointer;
}
</style>
