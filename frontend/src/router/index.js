import { createRouter, createWebHistory } from "vue-router";
import LandingPage from "@/pages/LandingPage.vue";
import { isAuthenticated } from "@/services/auth";

const routes = [
  {
    path: "/",
    name: "Home",
    component: LandingPage,
  },
  {
    path: "/login",
    name: "Login",
    component: () => import("@/pages/LoginPage.vue"),
  },
  {
    path: "/register",
    name: "Register",
    component: () => import("@/pages/RegisterPage.vue"),
  },
  {
    path: "/wallets",
    name: "Wallets",
    component: () => import("@/pages/WalletsPage.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("@/pages/NotFound.vue"),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !isAuthenticated()) {
    return { name: "Login", query: { redirect: to.fullPath } };
  }
});

export default router;
