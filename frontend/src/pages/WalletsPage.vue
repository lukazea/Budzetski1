<template>
  <div class="wallets-page">
    <section class="dash">
      <h1>Virtuelni novčanik</h1>

      <div class="stats">
        <div class="stat">
          <div class="label">
            Ukupno stanje
          </div>
          <div class="value">
            <template v-if="totalBalanceLoading">
              ...
            </template>
            <template v-else>
              {{ fmtMoney(totalBalance) }}
            </template>
          </div>
        </div>

        <div class="stat">
          <div class="label">
            Aktivnih novčanika
          </div>
          <div class="value">
            {{ activeWallets.length }}
          </div>
        </div>

        <div class="stat">
          <div class="label">
            Arhiviranih
          </div>
          <div class="value">
            {{ archivedWallets.length }}
          </div>
        </div>
      </div>
    </section>

    <section class="editor">
      <h2>{{ editWallet?.id ? "Uredi novčanik" : "Novi novčanik" }}</h2>
      <form @submit.prevent="onSubmit">
        <div class="row">
          <label>Naziv</label>
          <input v-model.trim="form.name" required placeholder="npr. Tekući račun">
        </div>

        <div class="row">
          <label>Početno stanje</label>
          <input v-model.number="form.initialBalance" type="number" step="0.01">
        </div>

        <div class="row">
          <label>Valuta</label>
          <select v-model="formCurrency">
            <option v-for="c in currencies" :key="c" :value="c">
              {{ c }}
            </option>
          </select>
        </div>

        <div class="row">
          <label>Štednja?</label>
          <input v-model="form.savings" type="checkbox">
        </div>

        <div class="actions">
          <button type="submit" :disabled="saving">
            {{ saving ? "Čuvam..." : (editWallet?.id ? "Sačuvaj izmene" : "Kreiraj") }}
          </button>
          <button v-if="editWallet" type="button" @click="resetForm">
            Otkaži
          </button>
        </div>

        <p v-if="error" class="error">
          {{ error }}
        </p>
        <p v-if="successMsg" class="success">
          {{ successMsg }}
        </p>
      </form>
    </section>

    <section class="list">
      <h2>Aktivni novčanici</h2>
      <table v-if="activeWallets.length">
        <thead>
          <tr>
            <th>Naziv</th>
            <th>Stanje</th>
            <th>Valute</th>
            <th>Akcije</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="w in activeWallets" :key="w.id" :class="{ selected: w.id === selectedWalletId }">
            <td>{{ w.name }}</td>
            <td>
              <template v-if="balancesLoading[w.id]">
                ...
              </template>
              <template v-else>
                {{ fmtMoney(walletBalances[w.id] ?? w.currentBalance ?? 0) }}
              </template>
            </td>
            <td>{{ (w.currencies ?? []).join(", ") }}</td>
            <td class="actions">
              <button @click="selectForEdit(w)">
                Uredi
              </button>
              <button @click="$router.push({ name: 'Transactions', params: { walletId: w.id } })">
                Transakcije
              </button>
              <button title="Arhiviraj" @click="onArchive(w)">
                Arhiviraj
              </button>
              <button class="danger" @click="onDelete(w)">
                Obriši
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else>
        Nema aktivnih novčanika.
      </p>
    </section>

    <section class="list secondary">
      <h2>Arhivirani novčanici</h2>
      <table v-if="archivedWallets.length">
        <thead>
          <tr>
            <th>Naziv</th>
            <th>Stanje</th>
            <th>Valute</th>
            <th>Akcije</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="w in archivedWallets" :key="w.id">
            <td>{{ w.name }}</td>
            <td>
              <template v-if="balancesLoading[w.id]">
                ...
              </template>
              <template v-else>
                {{ fmtMoney(walletBalances[w.id] ?? w.currentBalance ?? 0) }}
              </template>
            </td>
            <td>{{ (w.currencies ?? []).join(", ") }}</td>
            <td class="actions">
              <button title="Aktiviraj" @click="onActivate(w)">
                Aktiviraj
              </button>
              <button class="danger" @click="onDelete(w)">
                Obriši
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else>
        Nema arhiviranih novčanika.
      </p>
    </section>

    <section v-if="selectedWallet" class="tx">
      <h2>Transakcije — {{ selectedWallet.name }}</h2>
      <p class="muted">
        Ukupno: {{ transactions.length }}
      </p>
      <table v-if="transactions.length">
        <thead>
          <tr>
            <th>Datum</th>
            <th>Naziv</th>
            <th>Tip</th>
            <th>Iznos</th>
            <th>Kategorija</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in transactions" :key="t.id">
            <td>{{ t.transactionDate }}</td>
            <td>{{ t.name }}</td>
            <td>{{ t.type }}</td>
            <td>{{ t.formattedAmountWithCurrency ?? fmtMoney(t.amount) }}</td>
            <td>{{ t.categoryName ?? "-" }}</td>
          </tr>
        </tbody>
      </table>
      <p v-else>
        Nema transakcija za ovaj novčanik.
      </p>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, computed } from "vue";
import {
  getUserWallets,
  getArchivedWallets,
  getWalletBalance,
  getTotalUserBalance,
  createWallet,
  updateWallet,
  deleteWallet,
  archiveWallet,
  activateWallet,
} from "@/services/wallets";
import {useAuth} from "@/composables/useAuth";

const {userId} = useAuth();
console.log("userId",userId.value);
const currencies = ["RSD", "EUR", "USD"];

const activeWallets = ref([]);
const archivedWallets = ref([]);
const walletBalances = reactive({});
const balancesLoading = reactive({});
const totalBalance = ref(0);
const totalBalanceLoading = ref(false);

const transactions = ref([]);
const selectedWalletId = ref(null);

const selectedWallet = computed(() =>
  activeWallets.value.find((w) => w.id === selectedWalletId.value) ??
    archivedWallets.value.find((w) => w.id === selectedWalletId.value),
);

const editWallet = ref(null);
const form = reactive({
  name: "",
  initialBalance: 0,
  savings: false,
  currencies: ["RSD"],
});
const formCurrency = ref("RSD");
const saving = ref(false);
const error = ref(null);
const successMsg = ref(null);

function resetForm() {
  editWallet.value = null;
  form.name = "";
  form.initialBalance = 0;
  form.savings = false;
  form.currencies = ["RSD"];
  formCurrency.value = "RSD";
  error.value = null;
  successMsg.value = null;
}

function selectForEdit(w) {
  editWallet.value = { ...w };
  form.name = w.name;
  form.initialBalance = w.initialBalance ?? 0;
  form.savings = !!w.savings;
  form.currencies = w.currencies?.length ? w.currencies : ["RSD"];
  formCurrency.value = form.currencies[0];
}

async function onSubmit() {
  saving.value = true;
  error.value = null;
  successMsg.value = null;
  try {
    const payload = {
      name: form.name,
      initialBalance: form.initialBalance ?? 0,
      savings: form.savings,
    };

    if (editWallet.value?.id) {
      const updated = await updateWallet(editWallet.value.id, payload);
      const ix = activeWallets.value.findIndex((w) => w.id === updated.id);
      if (ix >= 0) activeWallets.value[ix] = updated;
      successMsg.value = "Izmene sačuvane.";
    } else {
      const created = await createWallet(userId.value, formCurrency.value, payload);
      if (created.archived) archivedWallets.value.unshift(created);
      else activeWallets.value.unshift(created);
      successMsg.value = "Novčanik kreiran.";
    }

    await refreshTotals();
    resetForm();
  } catch (e) {
    error.value = e?.response?.data?.message ?? e?.message ?? "Greška pri čuvanju.";
  } finally {
    saving.value = false;
  }
}

async function onArchive(w) {
  if (!w.id) return;
  await archiveWallet(w.id);
  moveBetweenLists(w.id, activeWallets.value, archivedWallets.value);
  await refreshTotals();
}

async function onActivate(w) {
  if (!w.id) return;
  await activateWallet(w.id);
  moveBetweenLists(w.id, archivedWallets.value, activeWallets.value);
  await refreshTotals();
}

async function onDelete(w) {
  if (!w.id) return;
  if (!confirm(`Obriši novčanik "${w.name}"?`)) return;
  await deleteWallet(w.id);
  removeFromLists(w.id);
  if (selectedWalletId.value === w.id) {
    selectedWalletId.value = null;
    transactions.value = [];
  }
  await refreshTotals();
}

function moveBetweenLists(id, from, to) {
  const idx = from.findIndex((x) => x.id === id);
  if (idx >= 0) {
    const [item] = from.splice(idx, 1);
    to.unshift({ ...item, archived: to === archivedWallets.value });
  }
}

function removeFromLists(id) {
  const rm = (arr) => {
    const ix = arr.findIndex((x) => x.id === id);
    if (ix >= 0) arr.splice(ix, 1);
  };
  rm(activeWallets.value);
  rm(archivedWallets.value);
}

async function loadWallets() {
  const [active, archived] = await Promise.all([
    getUserWallets(userId.value),
    getArchivedWallets(userId.value),
  ]);
  activeWallets.value = active;
  archivedWallets.value = archived;
}

async function loadBalancesFor(list) {
  await Promise.all(
    list
      .filter((w) => !!w.id)
      .map(async (w) => {
        const id = w.id;
        balancesLoading[id] = true;
        try {
          walletBalances[id] = await getWalletBalance(id);
        } finally {
          balancesLoading[id] = false;
        }
      }),
  );
}

async function refreshTotals() {
  totalBalanceLoading.value = true;
  try {
    totalBalance.value = await getTotalUserBalance(userId.value);
  } finally {
    totalBalanceLoading.value = false;
  }
}

function fmtMoney(n) {
  const v = typeof n === "number" ? n : 0;
  return v.toFixed(2);
}

onMounted(async () => {
  await loadWallets();
  await Promise.all([
    loadBalancesFor(activeWallets.value),
    loadBalancesFor(archivedWallets.value),
    refreshTotals(),
  ]);
});
</script>

<style scoped>
  .wallets-page {
    display: grid; gap: 28px; padding: 20px; max-width: 1100px; margin: 0 auto; font-family: system-ui, -apple-system, Segoe UI, Roboto, "Helvetica Neue", Arial; } h1, h2 { margin: 0 0 12px; }
    .dash .stats { display: grid; grid-template-columns: repeat(3, minmax(160px, 1fr)); gap: 12px; }
    .stat { background: #f6f7f9; border: 1px solid #e6e8ee; border-radius: 12px; padding: 14px 16px; }
    .stat .label { color: #6b7280; font-size: 13px; } .stat .value { font-weight: 700; font-size: 22px; }
    .editor form .row { display: grid; grid-template-columns: 160px 1fr; align-items: center; gap: 12px; margin-bottom: 10px; }
    .editor input, .editor select { border: 1px solid #d1d5db; border-radius: 8px; padding: 8px 10px; }
    .actions { display: flex; gap: 8px; margin-top: 8px; }
    button { border: 1px solid #cbd5e1; border-radius: 10px; padding: 8px 12px; background: white; cursor: pointer; }
    button:hover { background: #f3f4f6; }
    button.danger { color: #b91c1c; border-color: #fecaca; }
    .error { color: #b91c1c; margin-top: 8px; }
    .success { color: #065f46; margin-top: 8px; }
    .list table, .tx table { width: 100%; border-collapse: collapse; }
    .list th, .list td, .tx th, .tx td { border-bottom: 1px solid #eef2f7; text-align: left; padding: 10px 8px; font-size: 14px; }
    .list tr.selected { background: #f8fafc; }
    .list.secondary { opacity: 0.95; }
    .tx .muted { color: #6b7280; font-size: 13px; margin-top: -8px; }
</style>
