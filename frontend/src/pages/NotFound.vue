<template>
    <div class="not-found">
      <h1>404</h1>
      <p>Oops! The page you are looking for doesn't exist.</p>
  
      <router-link to="/" class="home-link">Go Back Home</router-link>
    </div>
  </template>
  
  <script setup>
  </script>
  
  <style scoped>
  .not-found {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: 80vh;
    text-align: center;
    padding: 20px;
  }
  
  .not-found h1 {
    font-size: 6rem;
    font-weight: bold;
    color: #42b983;
    margin-bottom: 10px;
  }
  
  .not-found p {
    font-size: 1.2rem;
    margin-bottom: 20px;
    color: #555;
  }
  
  .home-link {
    background-color: #42b983;
    color: white;
    padding: 10px 20px;
    border-radius: 6px;
    text-decoration: none;
    font-weight: bold;
    transition: background 0.3s;
  }
  
  .home-link:hover {
    background-color: #369e6f;
  }
  </style>
  