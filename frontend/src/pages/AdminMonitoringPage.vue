<template>
  <div class="page">
    <h1>Monitoring transakcija</h1>
    <p class="muted">
      Administrator vidi sve transakcije i može filtrirati po korisniku, kategoriji, sumi i datumu,
      uz sortiranje po datumu ili iznosu.
    </p>

    <section class="card">
      <h2>Filteri</h2>
      <div class="filters">
        <div class="field">
          <label>Korisnik</label>
          <select v-model.number="filters.userId">
            <option :value="undefined">
              Svi
            </option>
            <option v-for="u in users" :key="u.id" :value="u.id">
              {{ u.userName }} (ID: {{ u.id }})
            </option>
          </select>
        </div>

        <div class="field">
          <label>Kategorija</label>
          <select v-model.number="filters.categoryId">
            <option :value="undefined">
              Sve
            </option>
            <option v-for="c in categories" :key="c.id" :value="c.id">
              {{ c.name }} • {{ c.type }}
            </option>
          </select>
        </div>

        <div class="field">
          <label>Iznos (min)</label>
          <input
            v-model.number="filters.minAmount"
            type="number"
            step="0.01"
            min="0"
            placeholder="npr. 1000"
          >
        </div>
        <div class="field">
          <label>Iznos (max)</label>
          <input
            v-model.number="filters.maxAmount"
            type="number"
            step="0.01"
            min="0"
            placeholder="npr. 50000"
          >
        </div>

        <div class="field">
          <label>Datum od</label>
          <input v-model="filters.startDate" type="date">
        </div>
        <div class="field">
          <label>Datum do</label>
          <input v-model="filters.endDate" type="date">
        </div>

        <div class="field">
          <label>Sortiraj po</label>
          <select v-model="sortBy">
            <option value="createdAt">
              Datum
            </option>
            <option value="amount">
              Iznos
            </option>
          </select>
        </div>
        <div class="field">
          <label>Pravac</label>
          <select v-model="sortDirection">
            <option value="desc">
              Opadajuće
            </option>
            <option value="asc">
              Rastuće
            </option>
          </select>
        </div>

        <div class="actions">
          <button @click="applyFilters">
            Primeni filtere
          </button>
          <button class="ghost" @click="resetFilters">
            Reset
          </button>
        </div>
      </div>
    </section>

    <!-- LISTA -->
    <section class="card">
      <h2>Transakcije</h2>

      <table v-if="page?.content?.length">
        <thead>
          <tr>
            <th>ID</th>
            <th>Datum</th>
            <th>Naziv</th>
            <th>Tip</th>
            <th>Iznos</th>
            <th>Kategorija</th>
            <th>Novčanik</th>
            <th>Korisnik</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in page.content" :key="t.id">
            <td>{{ t.id }}</td>
            <td>{{ t.transactionDate }}</td>
            <td>{{ t.name }}</td>
            <td>{{ t.type }}</td>
            <td>{{ t.formattedAmountWithCurrency ?? money(t.amount) }}</td>
            <td>{{ t.categoryName ?? "-" }}</td>
            <td>{{ t.walletName ?? t.walletId }}</td>
            <td>{{ t.userName ?? t.userId }}</td>
          </tr>
        </tbody>
      </table>

      <p v-else class="muted">
        Nema podataka za izabrane filtere.
      </p>

      <div class="pager">
        <button :disabled="page?.first" @click="go(0)">
          « Prva
        </button>
        <button :disabled="page?.first" @click="go((page?.number ?? 0) - 1)">
          ‹
        </button>
        <span>Strana {{ (page?.number ?? 0) + 1 }} / {{ page?.totalPages ?? 1 }}</span>
        <button :disabled="page?.last" @click="go((page?.number ?? 0) + 1)">
          ›
        </button>
        <button :disabled="page?.last" @click="go((page?.totalPages ?? 1) - 1)">
          Poslednja »
        </button>

        <select v-model.number="size" @change="reload()">
          <option :value="10">
            10
          </option>
          <option :value="20">
            20
          </option>
          <option :value="50">
            50
          </option>
        </select>
      </div>

      <p v-if="error" class="error">
        {{ error }}
      </p>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import {
  getAllUsers,
  getAllCategories,
  getAllTransactions,
  getFilteredTransactions,
} from "@/services/admin";

const users = ref([]);
const categories = ref([]);

const filters = reactive({
  userId: undefined,
  categoryId: undefined,
  minAmount: undefined,
  maxAmount: undefined,
  startDate: undefined, // 'YYYY-MM-DD'
  endDate: undefined,   // 'YYYY-MM-DD'
});

const sortBy = ref("createdAt");
const sortDirection = ref("desc");

const page = ref(null);
const index = ref(0);
const size = ref(20);

const error = ref(null);

function money(n) {
  const v = typeof n === "number" ? n : 0;
  return v.toFixed(2);
}

async function loadUsersAndCategories() {
  users.value = await getAllUsers();
  categories.value = await getAllCategories();
}

async function reload() {
  error.value = null;
  try {
    const usingFilters =
        filters.userId != null ||
        filters.categoryId != null ||
        filters.minAmount != null ||
        filters.maxAmount != null ||
        filters.startDate != null ||
        filters.endDate != null ||
        sortBy.value !== "date" ||
        sortDirection.value !== "desc";

    if (usingFilters) {
      page.value = await getFilteredTransactions({
        ...filters,
        sortBy: sortBy.value,
        sortDirection: sortDirection.value,
        page: index.value,
        size: size.value,
      });
    } else {
      page.value = await getAllTransactions({ page: index.value, size: size.value });
    }
  } catch (e) {
    error.value = e?.response?.data?.message ?? e?.message ?? "Greška pri učitavanju.";
  }
}

function applyFilters() {
  index.value = 0;
  reload();
}
function resetFilters() {
  filters.userId = undefined;
  filters.categoryId = undefined;
  filters.minAmount = undefined;
  filters.maxAmount = undefined;
  filters.startDate = undefined;
  filters.endDate = undefined;
  sortBy.value = "createdAt";
  sortDirection.value = "desc";
  index.value = 0;
  reload();
}
async function go(p) {
  index.value = Math.max(0, p);
  await reload();
}

onMounted(async () => {
  await loadUsersAndCategories();
  await reload();
});
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

  .card {
    border: 1px solid #e6e8ee;
    border-radius: 12px;
    padding: 14px;
    background: #fff;
  }

  .filters {
    display: grid;
    gap: 10px;
    grid-template-columns: repeat(4, minmax(160px, 1fr));
    align-items: end;
  }
  .field { display: grid; gap: 6px; }
  .field input, .field select {
    padding: 8px 10px;
    border: 1px solid #d1d5db;
    border-radius: 8px;
  }

  .actions { display: flex; gap: 8px; align-items: center; }
  button {
    border: 1px solid #cbd5e1;
    border-radius: 10px;
    padding: 7px 10px;
    background: white;
    cursor: pointer;
  }
  button:hover { background: #f3f4f6; }
  button.ghost { background: #f8fafc; }

  table { width: 100%; border-collapse: collapse; margin-top: 8px; }
  th, td {
    border-bottom: 1px solid #eef2f7;
    text-align: left;
    padding: 8px;
    font-size: 14px;
  }

  .pager {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 10px;
  }
  .pager select {
    padding: 4px 6px;
    border: 1px solid #d1d5db;
    border-radius: 8px;
  }
  </style>
