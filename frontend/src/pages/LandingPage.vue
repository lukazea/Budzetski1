<template>
  <div class="landing-page">
    <div class="hero">
      <h1 class="title">
        Budget.ly
      </h1>
      <p class="subtitle">
        Track your income, expenses, and savings goals
      </p>

      <div class="user-count">
        <p>Total Users</p>
        <span class="count">
          {{ userCount ?? 0 }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>

import { onMounted } from "vue";
import { useUsers } from "@/composables/useUsers";

const { userCount, fetchUserCount } = useUsers();

onMounted(fetchUserCount);
</script>

<style scoped>
.landing-page {
  text-align: center;
  font-family: Arial, sans-serif;
  padding: 40px 20px;
}

.hero {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(to right, #42b983, #2c3e50);
  color: white;
  padding: 60px 20px;
  border-radius: 10px;
}

.title {
  font-size: 3rem;
  font-weight: bold;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 1.2rem;
  margin-bottom: 30px;
}

.user-count {
  background: rgba(255, 255, 255, 0.2);
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 1.2rem;
}

.count {
  display: block;
  font-size: 2rem;
  font-weight: bold;
}
</style>
