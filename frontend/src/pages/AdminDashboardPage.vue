<template>
  <div class="page">
    <h1>Admin Dashboard</h1>

    <!-- KPI kartice -->
    <section class="kpis">
      <div class="kpi">
        <div class="label">
          Ukupno korisnika
        </div>
        <div class="value">
          {{ stats?.totalUsers ?? "—" }}
        </div>
      </div>
      <div class="kpi">
        <div class="label">
          Aktivni (30 dana)
        </div>
        <div class="value">
          {{ stats?.activeUsers ?? "—" }}
        </div>
      </div>
      <div class="kpi">
        <div class="label">
          Prosečan novac (aktivni)
        </div>
        <div class="value money">
          {{ fmtNumber(stats?.averageActiveUserBalance) }}
        </div>
      </div>
      <div class="kpi">
        <div class="label">
          Ukupan novac (sistem)
        </div>
        <div class="value money">
          {{ fmtNumber(stats?.totalSystemBalance) }}
        </div>
      </div>
    </section>

    <div class="grid">
      <!-- Top 10 u poslednjih 30 dana -->
      <section class="card">
        <div class="head">
          <h2>Top 10 transakcija — 30 dana</h2>
          <button class="ghost" :disabled="loading30" @click="reloadLast30d">
            Osveži
          </button>
        </div>

        <table v-if="top30?.length">
          <thead>
            <tr>
              <th>Datum</th>
              <th>Naziv</th>
              <th>Tip</th>
              <th>Iznos</th>
              <th>Kategorija</th>
              <th>Korisnik</th>
              <th>Novčanik</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in top30" :key="t.id">
              <td>{{ t.transactionDate }}</td>
              <td>{{ t.name }}</td>
              <td>{{ t.type }}</td>
              <td>{{ t.formattedAmountWithCurrency ?? money(t.amount) }}</td>
              <td>{{ t.categoryName ?? "-" }}</td>
              <td>{{ t.userName ?? t.userId }}</td>
              <td>{{ t.walletName ?? t.walletId }}</td>
            </tr>
          </tbody>
        </table>
        <p v-else class="muted">
          {{ loading30 ? "Učitavam..." : "Nema podataka." }}
        </p>
      </section>

      <!-- Top 10 u poslednja 2 minuta (auto-refresh) -->
      <section class="card">
        <div class="head">
          <h2>Top 10 transakcija — 2 min (test)</h2>
          <div class="row-actions">
            <label><input v-model="live" type="checkbox"> Live refresh</label>
            <button class="ghost" :disabled="loading2" @click="reload2min">
              Osveži
            </button>
          </div>
        </div>

        <table v-if="top2?.length">
          <thead>
            <tr>
              <th>Vreme</th>
              <th>Naziv</th>
              <th>Tip</th>
              <th>Iznos</th>
              <th>Kategorija</th>
              <th>Korisnik</th>
              <th>Novčanik</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in top2" :key="t.id">
              <td>{{ t.createdAt ?? (t.transactionDate + " 00:00") }}</td>
              <td>{{ t.name }}</td>
              <td>{{ t.type }}</td>
              <td>{{ t.formattedAmountWithCurrency ?? money(t.amount) }}</td>
              <td>{{ t.categoryName ?? "-" }}</td>
              <td>{{ t.userName ?? t.userId }}</td>
              <td>{{ t.walletName ?? t.walletId }}</td>
            </tr>
          </tbody>
        </table>
        <p v-else class="muted">
          {{ loading2 ? "Učitavam..." : "Nema podataka (poslednja 2 min)." }}
        </p>
      </section>
    </div>

    <p v-if="error" class="error">
      {{ error }}
    </p>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from "vue";
import {
  getDashboardStats,
  getTopTransactionsSince,
  isoDateDaysAgo,
  isoDateMinutesAgo,
} from "@/services/admin";

const stats = ref(null);

const top30 = ref([]);
const top2 = ref([]);

const loading30 = ref(false);
const loading2 = ref(false);
const error = ref(null);

const live = ref(true);     // auto-refresh za 2min tabelu
let timer = null;

function money(n) {
  const v = typeof n === "number" ? n : 0;
  return v.toFixed(2);
}
function fmtNumber(n) {
  if (n == null || Number.isNaN(n)) return "—";
  return Number(n).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

async function reloadStats() {
  try {
    stats.value = await getDashboardStats();
  } catch (e) {
    error.value = e?.response?.data?.message ?? e?.message ?? "Greška pri učitavanju statistika.";
  }
}

async function reloadLast30d() {
  loading30.value = true;
  try {
    const since = isoDateDaysAgo(30); // YYYY-MM-DD
    top30.value = await getTopTransactionsSince(since, 10);
  } catch (e) {
    error.value = e?.response?.data?.message ?? e?.message ?? "Greška pri učitavanju top (30 dana).";
  } finally {
    loading30.value = false;
  }
}

async function reload2min() {
  loading2.value = true;
  try {
    const since = isoDateMinutesAgo(2); // ISO datetime
    top2.value = await getTopTransactionsSince(since, 10);
  } catch (e) {
    error.value = e?.response?.data?.message ?? e?.message ?? "Greška pri učitavanju top (2 min).";
  } finally {
    loading2.value = false;
  }
}

function startLive() {
  stopLive();
  timer = setInterval(reload2min, 10_000); // osvežavaj na 10s radi testiranja
}
function stopLive() {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
}

watch(live, (v) => (v ? startLive() : stopLive()));

onMounted(async () => {
  await reloadStats();
  await reloadLast30d();
  await reload2min();
  if (live.value) startLive();
});
onBeforeUnmount(() => stopLive());
</script>

  <style scoped>
  .page {
    display: grid;
    gap: 18px;
    padding: 20px;
    max-width: 1200px;
    margin: 0 auto;
    font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial;
  }
  .muted { color: #6b7280; }
  .error { color: #b91c1c; }

  .kpis {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
  }
  .kpi {
    border: 1px solid #e6e8ee;
    border-radius: 12px;
    padding: 12px;
    background: #fff;
  }
  .kpi .label { color: #6b7280; font-size: 12px; }
  .kpi .value { font-size: 22px; font-weight: 600; margin-top: 4px; }
  .kpi .value.money { font-variant-numeric: tabular-nums; }

  .grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }

  .card {
    border: 1px solid #e6e8ee;
    border-radius: 12px;
    padding: 14px;
    background: #fff;
  }
  .head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    margin-bottom: 6px;
  }
  .row-actions { display: flex; gap: 10px; align-items: center; }

  table { width: 100%; border-collapse: collapse; }
  th, td {
    border-bottom: 1px solid #eef2f7;
    text-align: left;
    padding: 8px;
    font-size: 14px;
  }
  button {
    border: 1px solid #cbd5e1;
    border-radius: 10px;
    padding: 7px 10px;
    background: white;
    cursor: pointer;
  }
  button:hover { background: #f3f4f6; }
  button.ghost { background: #f8fafc; }
  </style>
