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
    path: "/admin-users",
    name: "AdminUsers",
    component: () => import("@/pages/AdminUsersPage.vue"),
    meta: { requiresAdmin: true, requiresAuth: true }
  },
  {
    path: "/admin-categories",
    name: "AdminCategories",
    component: () => import("@/pages/AdminCategoriesPage.vue"),
    meta: { requiresAdmin: true, requiresAuth: true },
  },
  {
    path: "/admin-currencies",
    name: "AdminCurrencies",
    component: () => import("@/pages/AdminCurrenciesPage.vue"),
    meta: { requiresAdmin: true, requiresAuth: true },
  },
  {
    path: "/admin-monitoring",
    name: "AdminMonitoring",
    component: () => import("@/pages/AdminMonitoringPage.vue"),
    meta: { requiresAdmin: true, requiresAuth: true },
  },
  {
    path: "/admin-dashboard",
    name: "AdminDashboard",
    component: () => import("@/pages/AdminDashboardPage.vue"),
    meta: { requiresAdmin: true, requiresAuth: true },
  },
  {
    path: "/wallets/:walletId/transactions",
    name: "Transactions",
    component: () => import("@/pages/TransactionsPage.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/categories",
    name: "UserCategories",
    component: () => import("@/pages/CategoriesPage.vue"),
    meta: { requiresAuth: true }
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
