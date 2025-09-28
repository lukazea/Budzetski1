<template>
  <div id="app">
    <nav>
      <router-link to="/">Home</router-link>
    </nav>
    <router-view></router-view>
  </div>
</template>

<style scoped>
nav {
  display: flex;
  gap: 1rem;
  background: #f5f5f5;
  padding: 10px;
}
router-link {
  text-decoration: none;
  font-weight: bold;
  color: #333;
}
router-link.active {
  color: #42b983;
}
</style>
