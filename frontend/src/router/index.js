import { createRouter, createWebHistory } from "vue-router";
import LandingPage from "@/pages/LandingPage.vue";
import { isAuthenticated } from "@/services/auth";
import { useAuth } from "@/composables/useAuth";

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
    path: "/admin",
    name: "Admin",
    component: () => import("@/pages/AdminPage.vue"),
    meta: { requiresAdmin: true, requiresAuth: true } },
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
const { isAdmin } = useAuth();
router.beforeEach((to) => {
  if (to.meta.requiresAuth && !isAuthenticated()) {
    return { name: "Login", query: { redirect: to.fullPath } };
  }
  if(to.meta.requiresAdmin && !isAdmin.value){
    return { name: "Home", query: { redirect: to.fullPath } };
  }
});

export default router;
