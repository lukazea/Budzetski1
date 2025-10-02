<template>
  <div class="tx-page">
    <header class="head">
      <h1>Transakcije</h1>
      <div class="controls">
        <label>
          Novčanik
          <select v-model.number="state.walletId" @change="onWalletChange">
            <option :value="undefined">— izaberi —</option>
            <option v-for="w in wallets" :key="w.id" :value="w.id">
              {{ w.name }} ({{ (w.currencies ?? []).join(', ') || 'RSD' }})
            </option>
          </select>
        </label>

        <label>
          Period
          <select v-model="state.periodType" @change="fetchPeriod">
            <option value="DAY">Dan</option>
            <option value="WEEK">Nedelja</option>
            <option value="MONTH">Mesec</option>
            <option value="QUARTER">Kvartal</option>
            <option value="RANGE">Opseg datuma</option>
          </select>
        </label>

        <template v-if="state.periodType === 'DAY'">
          <label>
            Datum
            <input v-model="state.day" type="date" @change="fetchPeriod">
          </label>
        </template>
        <template v-else-if="state.periodType === 'WEEK'">
          <label>
            Bilo koji datum u nedelji
            <input v-model="state.weekDate" type="date" @change="fetchPeriod">
          </label>
        </template>
        <template v-else-if="state.periodType === 'MONTH'">
          <label>
            Godina
            <input
              v-model.number="state.year"
              type="number"
              min="1970"
              @change="fetchPeriod"
            >
          </label>
          <label>
            Mesec
            <input
              v-model.number="state.month"
              type="number"
              min="1"
              max="12"
              @change="fetchPeriod"
            >
          </label>
        </template>
        <template v-else-if="state.periodType === 'QUARTER'">
          <label>
            Godina
            <input
              v-model.number="state.year"
              type="number"
              min="1970"
              @change="fetchPeriod"
            >
          </label>
          <label>
            Kvartal (1-4)
            <input
              v-model.number="state.quarter"
              type="number"
              min="1"
              max="4"
              @change="fetchPeriod"
            >
          </label>
        </template>
        <template v-else-if="state.periodType === 'RANGE'">
          <label>
            Od
            <input v-model="state.startDate" type="date">
          </label>
          <label>
            Do
            <input v-model="state.endDate" type="date">
          </label>
          <button class="btn" @click="fetchRange(0)">
            Prikaži
          </button>
        </template>
      </div>

      <div v-if="selectedWallet" class="summary">
        <div>
          <div class="muted">
            Novčanik
          </div>
          <div class="val">
            {{ selectedWallet.name }}
          </div>
        </div>
        <div>
          <div class="muted">
            Stanje
          </div>
          <div class="val">
            {{ fmtMoney(walletBalances[selectedWallet.id] ?? selectedWallet.currentBalance ?? 0) }}
          </div>
        </div>
        <div>
          <div class="muted">
            Transakcija
          </div>
          <div class="val">
            {{ page.totalElements ?? periodTx.length }}
          </div>
        </div>
      </div>
    </header>

    <section v-if="selectedWallet" class="panels">
      <details open>
        <summary>Dodaj transakciju</summary>
        <form class="form-grid" @submit.prevent="createTx">
          <label>Naziv<input v-model.trim="createForm.name" required placeholder="npr. Plata"></label>
          <label>Iznos<input
            v-model.number="createForm.amount"
            type="number"
            step="0.01"
            required
          ></label>
          <label>Tip
            <select v-model="createForm.type" @change="loadCategories">
              <option value="PRIHOD">Prihod</option>
              <option value="TROSAK">Trošak</option>
            </select>
          </label>
          <label>Datum<input v-model="createForm.transactionDate" type="date" required></label>
          <label>Kategorija
            <select v-model.number="createForm.categoryId">
              <option :value="undefined">—</option>
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </label>
          <div class="row-actions">
            <button class="btn" type="submit" :disabled="creating">
              {{ creating ? 'Čuvam…' : 'Dodaj' }}
            </button>
            <span v-if="error" class="err">{{ error }}</span>
            <span v-if="okMsg" class="ok">{{ okMsg }}</span>
          </div>
        </form>
      </details>

      <details>
        <summary>Transfer između novčanika</summary>
        <form class="form-grid" @submit.prevent="createTransfer">
          <label>Sa novčanika
            <select v-model.number="transferForm.fromWalletId" required>
              <option v-for="w in wallets" :key="'f-'+w.id" :value="w.id">{{ w.name }}</option>
            </select>
          </label>
          <label>Na novčanik
            <select v-model.number="transferForm.toWalletId" required>
              <option v-for="w in wallets" :key="'t-'+w.id" :value="w.id">{{ w.name }}</option>
            </select>
          </label>
          <label>Iznos (izlazna valuta)
            <input
              v-model.number="transferForm.fromAmount"
              type="number"
              step="0.01"
              required
            >
          </label>
          <label>Datum
            <input v-model="transferForm.transactionDate" type="date" required>
          </label>
          <div class="row-actions">
            <button class="btn" type="submit" :disabled="creatingXfer">
              {{ creatingXfer ? 'Prenosim…' : 'Prebaci' }}
            </button>
            <span class="muted">Konverzija valuta se računa na serveru.</span>
          </div>
        </form>
      </details>
    </section>

    <section class="list">
      <div class="list-head">
        <h2>Lista transakcija</h2>
        <div class="pager">
          <label>Veličina strane
            <input
              v-model.number="state.size"
              type="number"
              min="1"
              @change="refetchPage(0)"
            >
          </label>
          <button class="btn" :disabled="state.page<=0" @click="refetchPage(state.page-1)">
            ◀
          </button>
          <span>{{ state.page+1 }} / {{ page.totalPages ?? 1 }}</span>
          <button class="btn" :disabled="page.last" @click="refetchPage(state.page+1)">
            ▶
          </button>
        </div>
      </div>

      <table v-if="rows.length">
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
          <tr v-for="t in rows" :key="t.id">
            <td>{{ t.transactionDate }}</td>
            <td>{{ t.name }}</td>
            <td>{{ t.transfer ? 'TRANSFER' : t.type }}</td>
            <td>{{ t.formattedAmount ?? fmtMoney(t.amount) }}</td>
            <td>{{ t.categoryName ?? '-' }}</td>
          </tr>
        </tbody>
      </table>
      <p v-else class="muted">
        Nema transakcija za izabrane filtere.
      </p>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, computed, watch } from "vue";
import { useAuth } from "@/composables/useAuth";
import { getUserWallets, getWalletBalance } from "@/services/wallets";
import * as txApi from "@/services/transactions";
import { getCategoriesByType } from "@/services/categories";

const { userId } = useAuth();

// --- State ---
const wallets = ref([]);
const walletBalances = reactive({});
const state = reactive({
  walletId: undefined,
  periodType: "DAY", // DAY | WEEK | MONTH | QUARTER | RANGE
  day: new Date().toISOString().slice(0,10),
  weekDate: new Date().toISOString().slice(0,10),
  year: new Date().getFullYear(),
  month: new Date().getMonth() + 1,
  quarter: Math.floor((new Date().getMonth())/3)+1,
  startDate: new Date().toISOString().slice(0,10),
  endDate: new Date().toISOString().slice(0,10),
  page: 0,
  size: 10,
});

const page = reactive({ content: [], totalPages: 1, totalElements: 0, last: true });
const periodTx = ref([]); // for non-paginated period endpoints
const rows = computed(() => (page.content?.length ? page.content : periodTx.value) || []);

const selectedWallet = computed(() => wallets.value.find(w => w.id === state.walletId));

// Create TX form
const createForm = reactive({
  name: "", amount: 0, type: "PRIHOD", categoryId: undefined,
  transactionDate: new Date().toISOString().slice(0,10),
});
const categories = ref([]);
const creating = ref(false);
const error = ref(null);
const okMsg = ref(null);

// Transfer form
const transferForm = reactive({ fromWalletId: undefined, toWalletId: undefined, fromAmount: 0, transactionDate: new Date().toISOString().slice(0,10) });
const creatingXfer = ref(false);

// --- Methods ---
function fmtMoney(n){ return (typeof n === "number" ? n : 0).toFixed(2); }

async function loadWallets(){
  wallets.value = await getUserWallets(userId.value);
  // preselect first active
  if (!state.walletId && wallets.value.length) state.walletId = wallets.value[0].id;
  // balances
  await Promise.all(wallets.value.map(async w => { walletBalances[w.id] = await getWalletBalance(w.id); }));
  // init transfer selects
  if (wallets.value.length){
    transferForm.fromWalletId = wallets.value[0].id;
    transferForm.toWalletId = wallets.value.at(1)?.id ?? wallets.value[0].id;
  }
}

function onWalletChange(){
  refetch();
}

async function loadCategories(){
  categories.value = await getCategoriesByType(userId.value, createForm.type);
}

async function createTx(){
  creating.value = true; error.value = null; okMsg.value = null;
  try{
    const payload = {
      name: createForm.name,
      amount: createForm.amount,
      type: createForm.type,
      categoryId: createForm.categoryId,
      transactionDate: createForm.transactionDate,
      walletId: state.walletId,
      userId: userId.value,
    };
    await txApi.createTransaction(payload);
    okMsg.value = "Transakcija dodata.";
    await refetch();
    // reset minimal
    createForm.name = "";
    createForm.amount = 0;
  }catch(e){
    error.value = e?.response?.data?.message ?? e?.message ?? "Greška.";
  }finally{ creating.value = false; }
}

async function createTransfer(){
  creatingXfer.value = true;
  try{
    const payload = {
      transfer: true,
      fromWalletId: transferForm.fromWalletId,
      toWalletId: transferForm.toWalletId,
      fromAmount: transferForm.fromAmount,
      transactionDate: transferForm.transactionDate,
      userId: userId.value,
      name: "Transfer sredstava",
      type: "PRIHOD", // backend ignoriše za transfer
    };
    await txApi.createTransfer(payload);
    await refetch();
  } finally { creatingXfer.value = false; }
}

async function fetchPeriod(){
  if(!state.walletId) return;
  periodTx.value = [];
  page.content = []; page.totalPages = 1; page.totalElements = 0; page.last = true;
  if(state.periodType === "DAY"){
    periodTx.value = await txApi.getDailyTransactions(userId.value, state.day);
  } else if(state.periodType === "WEEK"){
    periodTx.value = await txApi.getWeeklyTransactions(userId.value, state.weekDate);
  } else if(state.periodType === "MONTH"){
    periodTx.value = await txApi.getMonthlyTransactions(userId.value, state.year, state.month);
  } else if(state.periodType === "QUARTER"){
    periodTx.value = await txApi.getQuarterlyTransactions(userId.value, state.year, state.quarter);
  } else {
    await fetchRange(0);
  }
}

async function fetchRange(pageIndex){
  if(!state.startDate || !state.endDate) return;
  state.page = pageIndex;
  const p = await txApi.getTransactionsPaginatedByDate(userId.value, state.startDate, state.endDate, state.page, state.size);
  Object.assign(page, p);
}

async function refetchPage(p){
  if(state.periodType === "RANGE") return fetchRange(p);
  // General paginated list (no date filter) for chosen wallet & user
  state.page = p;
  const resp = await txApi.getTransactionsPaginated(userId.value, state.page, state.size);
  Object.assign(page, resp);
}

async function refetch(){
  if(state.periodType === "RANGE") return fetchRange(state.page);
  await fetchPeriod();
}

watch(() => state.size, () => {
  if(state.periodType === "RANGE") fetchRange(0);
  else refetchPage(0);
});

onMounted(async () => {
  await loadWallets();
  await loadCategories();
  await refetch();
});
</script>

  <style scoped>
  .tx-page { max-width: 1100px; margin: 0 auto; padding: 20px; display: grid; gap: 18px; font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial; }
  .head h1 { margin: 0 0 10px; }
  .controls { display: flex; flex-wrap: wrap; gap: 12px; align-items: end; }
  .controls label { display: grid; gap: 6px; font-size: 13px; color: #374151; }
  .controls select, .controls input { padding: 6px 8px; border: 1px solid #d1d5db; border-radius: 8px; }
  .btn { border: 1px solid #cbd5e1; border-radius: 8px; background: white; padding: 6px 10px; cursor: pointer; }
  .btn:hover { background: #f3f4f6; }
  .summary { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; background: #f8fafc; border: 1px solid #e5e7eb; border-radius: 12px; padding: 12px; }
  .summary .muted { color: #6b7280; font-size: 12px; }
  .summary .val { font-weight: 700; font-size: 18px; }
  .panels details { border: 1px solid #e5e7eb; border-radius: 12px; padding: 10px 12px; background: #ffffff; }
  .panels summary { font-weight: 600; cursor: pointer; }
  .form-grid { margin-top: 8px; display: grid; grid-template-columns: repeat(5, minmax(160px, 1fr)); gap: 10px; align-items: end; }
  .form-grid input, .form-grid select { border: 1px solid #d1d5db; border-radius: 8px; padding: 6px 8px; }
  .row-actions { display: flex; align-items: center; gap: 10px; }
  .err { color: #b91c1c; }
  .ok { color: #065f46; }
  .list { border-top: 1px solid #eef2f7; padding-top: 8px; }
  .list-head { display: flex; align-items: center; justify-content: space-between; }
  .pager { display: flex; gap: 8px; align-items: center; }
   table { width: 100%; border-collapse: collapse; }
   th, td { border-bottom: 1px solid #eef2f7; text-align: left; padding: 8px 6px; font-size: 14px; }
  </style>
