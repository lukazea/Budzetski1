<template>
  <div class="page">
    <h1>Upravljanje valutama</h1>
    <p class="muted">
      Dodavanje novih valuta i ažuriranje kursne liste (polje <em>valueToEur</em> je kurs prema EUR).
    </p>

    <!-- Dodavanje valute -->
    <section class="card">
      <h2>Dodaj novu valutu</h2>
      <form class="row" @submit.prevent="onCreate">
        <input
          v-model.trim="form.currency"
          placeholder="Šifra (npr. RSD, USD)"
          maxlength="10"
          required
        >
        <input
          v-model.number="form.valueToEur"
          type="number"
          step="0.0001"
          min="0"
          placeholder="valueToEur (npr. 117.5 za RSD)"
          required
        >
        <button :disabled="creating">
          {{ creating ? "Dodajem..." : "Dodaj" }}
        </button>
      </form>
      <p v-if="createError" class="error">
        {{ createError }}
      </p>
      <p v-if="createSuccess" class="success">
        {{ createSuccess }}
      </p>
    </section>

    <!-- Lista + pretraga + bulk refresh -->
    <section class="card">
      <div class="head">
        <h2>Valute</h2>
        <div class="toolbar">
          <input v-model.trim="q" placeholder="Pretraga po šifri...">
          <button :disabled="refreshing" @click="onRefreshFromAPI">
            {{ refreshing ? "Osvežavam..." : "Osveži kurseve iz API-ja" }}
          </button>
        </div>
      </div>

      <table v-if="filtered.length">
        <thead>
          <tr>
            <th>ID</th>
            <th>Šifra</th>
            <th>valueToEur</th>
            <th>Akcije</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in filtered" :key="c.id">
            <td>{{ c.id }}</td>
            <td>{{ c.currency }}</td>
            <td class="rate-cell">
              <input
                v-model.number="editRates[c.currency]"
                type="number"
                step="0.0001"
                min="0"
                :placeholder="String(c.valueToEur)"
                @focus="ensureEditValue(c)"
              >
              <button @click="saveRate(c)">
                Sačuvaj
              </button>
            </td>
            <td>
              <button class="danger" @click="removeCurrency(c)">
                Obriši
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="muted">
        Nema valuta.
      </p>

      <p v-if="listError" class="error">
        {{ listError }}
      </p>
      <p v-if="listSuccess" class="success">
        {{ listSuccess }}
      </p>
    </section>

    <!-- Brza provera konverzije -->
    <section class="card">
      <h2>Test konverzije</h2>
      <form class="row" @submit.prevent="onConvert">
        <input
          v-model.number="conv.amount"
          type="number"
          step="0.01"
          min="0"
          placeholder="Iznos"
          required
        >
        <input v-model.trim="conv.from" placeholder="Iz (npr. RSD)" required>
        <input v-model.trim="conv.to" placeholder="U (npr. EUR)" required>
        <button :disabled="converting">
          {{ converting ? "Računam..." : "Konvertuj" }}
        </button>
      </form>
      <p v-if="convError" class="error">
        {{ convError }}
      </p>
      <p v-if="convResult !== null" class="success">
        Rezultat: {{ convResult }}
      </p>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import {
  getAllCurrencies,
  createCurrency,
  updateExchangeRate,
  deleteCurrency,
  updateRatesFromAPI,
  convertBetweenCurrencies,
} from "@/services/admin";

const items = ref([]);
const q = ref("");

// Forma za dodavanje
const form = ref({ currency: "", valueToEur: null });
const creating = ref(false);
const createError = ref(null);
const createSuccess = ref(null);

// Lista status
const listError = ref(null);
const listSuccess = ref(null);
const refreshing = ref(false);

// Inline edit rates { [code]: number }
const editRates = ref({});

// Konverzija
const conv = ref({ amount: null, from: "", to: "" });
const converting = ref(false);
const convResult = ref(null);
const convError = ref(null);

const filtered = computed(() => {
  const s = q.value.toLowerCase();
  if (!s) return items.value;
  return items.value.filter((x) => (x.currency ?? "").toLowerCase().includes(s));
});

function ensureEditValue(c) {
  if (editRates.value[c.currency] == null) {
    editRates.value[c.currency] = c.valueToEur;
  }
}

async function load() {
  listError.value = null;
  try {
    items.value = await getAllCurrencies();
  } catch (e) {
    listError.value = e?.response?.data?.message ?? e?.message ?? "Greška pri učitavanju.";
  }
}

async function onCreate() {
  if (!form.value.currency || form.value.valueToEur == null) return;
  creating.value = true;
  createError.value = null;
  createSuccess.value = null;
  try {
    // normalizuj šifru npr. "rsd" -> "RSD"
    const code = form.value.currency.trim().toUpperCase();
    await createCurrency({ currency: code, valueToEur: Number(form.value.valueToEur) });
    createSuccess.value = "Valuta dodata.";
    form.value = { currency: "", valueToEur: null };
    await load();
  } catch (e) {
    createError.value = e?.response?.data?.message ?? e?.message ?? "Greška pri dodavanju.";
  } finally {
    creating.value = false;
  }
}

async function saveRate(c) {
  listError.value = null;
  listSuccess.value = null;
  try {
    const newRate = Number(editRates.value[c.currency]);
    if (!isFinite(newRate) || newRate < 0) throw new Error("Neispravan kurs.");
    await updateExchangeRate(c.currency, newRate);
    listSuccess.value = `Kurs za ${c.currency} ažuriran.`;
    await load();
  } catch (e) {
    listError.value = e?.response?.data?.message ?? e?.message ?? "Greška pri ažuriranju kursa.";
  }
}

async function removeCurrency(c) {
  if (!confirm(`Obrisati valutu ${c.currency} (ID ${c.id})?`)) return;
  listError.value = null;
  listSuccess.value = null;
  try {
    await deleteCurrency(c.id);
    listSuccess.value = "Valuta obrisana.";
    await load();
  } catch (e) {
    listError.value = e?.response?.data?.message ?? e?.message ?? "Greška pri brisanju.";
  }
}

async function onRefreshFromAPI() {
  refreshing.value = true;
  listError.value = null;
  listSuccess.value = null;
  try {
    await updateRatesFromAPI();
    listSuccess.value = "Kursevi osveženi iz API-ja.";
    await load();
  } catch (e) {
    listError.value = e?.response?.data?.message ?? e?.message ?? "Greška pri osvežavanju.";
  } finally {
    refreshing.value = false;
  }
}

async function onConvert() {
  converting.value = true;
  convError.value = null;
  convResult.value = null;
  try {
    const amount = Number(conv.value.amount);
    const from = conv.value.from.trim().toUpperCase();
    const to = conv.value.to.trim().toUpperCase();
    const result = await convertBetweenCurrencies(amount, from, to);
    convResult.value = result;
  } catch (e) {
    convError.value = e?.response?.data?.message ?? e?.message ?? "Greška pri konverziji.";
  } finally {
    converting.value = false;
  }
}

onMounted(load);
</script>

  <style scoped>
  .page {
    display: grid;
    gap: 18px;
    padding: 20px;
    max-width: 900px;
    margin: 0 auto;
    font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial;
  }
  .muted { color: #6b7280; }
  .error { color: #b91c1c; }
  .success { color: #065f46; }

  .card {
    border: 1px solid #e6e8ee;
    border-radius: 12px;
    padding: 14px;
    background: #fff;
  }
  .row {
    display: grid;
    grid-template-columns: 1fr 1fr 140px;
    gap: 8px;
  }
  .row input {
    padding: 8px 10px;
    border: 1px solid #d1d5db;
    border-radius: 8px;
  }
  button {
    border: 1px solid #cbd5e1;
    border-radius: 10px;
    padding: 7px 10px;
    background: white;
    cursor: pointer;
  }
  button:hover { background: #f3f4f6; }
  button.danger { color: #b91c1c; border-color: #fecaca; }

  .head {
    display: flex;
    gap: 12px;
    align-items: center;
    justify-content: space-between;
  }
  .toolbar { display: flex; gap: 8px; }
  .toolbar input {
    width: 220px;
    padding: 8px 10px;
    border: 1px solid #d1d5db;
    border-radius: 8px;
  }

  table { width: 100%; border-collapse: collapse; }
  th, td {
    border-bottom: 1px solid #eef2f7;
    text-align: left;
    padding: 8px;
    font-size: 14px;
  }
  .rate-cell {
    display: flex;
    gap: 8px;
    align-items: center;
  }
  .rate-cell input {
    width: 160px;
    padding: 6px 8px;
    border: 1px solid #d1d5db;
    border-radius: 8px;
  }
  </style>
