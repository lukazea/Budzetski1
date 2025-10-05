<template>
  <div class="goals-page">
    <header class="head">
      <h1>Ciljevi štednje</h1>
      <p class="muted">
        Kreiraj cilj sa rokom i željenim iznosom. Uplati sredstava direktno ili transferom iz drugog novčanika.
      </p>
    </header>

    <section class="card">
      <h2>Novi cilj</h2>
      <form class="form" @submit.prevent="onCreate">
        <label>Naziv cilja
          <input v-model.trim="createForm.name" required placeholder="npr. Letovanje 2026">
        </label>

        <div class="row">
          <label>Ciljani iznos
            <input
              v-model.number="createForm.targetAmount"
              type="number"
              min="0"
              step="0.01"
              required
            >
          </label>
          <label>Rok (deadline)
            <input v-model="createForm.deadline" type="date" required>
          </label>
        </div>

        <div class="row">
          <label>
            Koristi postojeći štedni novčanik?
            <input v-model="useExistingSavings" type="checkbox">
          </label>
        </div>

        <div v-if="useExistingSavings" class="row">
          <label>Štedni novčanik
            <select v-model.number="createForm.walletId" required>
              <option :value="undefined">— izaberi —</option>
              <option v-for="w in savingsWallets" :key="w.id" :value="w.id">{{ w.name }}</option>
            </select>
          </label>
        </div>
        <div v-else class="row">
          <label>Naziv novog štednog novčanika
            <input v-model.trim="createForm.newWalletName" required placeholder="npr. Štednja — more">
          </label>
        </div>

        <div class="actions">
          <button class="btn" type="submit" :disabled="creating">
            {{ creating ? 'Kreiram…' : 'Kreiraj cilj' }}
          </button>
          <span v-if="okMsg" class="ok">{{ okMsg }}</span>
          <span v-if="error" class="err">{{ error }}</span>
        </div>
      </form>
    </section>

    <section class="card">
      <div class="list-head">
        <h2>Moji ciljevi</h2>
        <div class="spacer" />
        <button class="btn" @click="refresh">
          Osveži
        </button>
      </div>

      <table v-if="goals.length">
        <thead>
          <tr>
            <th>Naziv</th>
            <th>Novčanik</th>
            <th>Rok</th>
            <th style="width:320px">
              Napredak
            </th>
            <th style="width:220px">
              Uplata
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="g in goals" :key="g.id">
            <td>{{ g.name }}</td>
            <td>{{ walletName(g.walletId) }}</td>
            <td>{{ g.deadline ?? '-' }}</td>
            <td>
              <div class="bar">
                <div class="fill" :style="{ width: pct(g) + '%' }" />
              </div>
              <div class="mini">
                {{ fmtMoney(progress(g).current) }} / {{ fmtMoney(progress(g).target) }} ({{ pct(g).toFixed(0) }}%)
              </div>
            </td>
            <td>
              <div class="pay">
                <input
                  v-model.number="pay[g.id]"
                  type="number"
                  min="0"
                  step="0.01"
                  placeholder="Iznos"
                >
                <button class="btn" :disabled="!pay[g.id] || acting[g.id]" @click="deposit(g)">
                  Uplati
                </button>
                <!-- <button class="btn" :disabled="acting[g.id]" @click="openTransfer(g)">
                  Transfer
                </button> -->
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="muted">
        Još nema ciljeva.
      </p>
    </section>

    <dialog ref="transferDlg">
      <form class="form" @submit.prevent="doTransfer">
        <h3>Transfer na cilj: {{ activeGoal?.name }}</h3>
        <label>Sa novčanika
          <select v-model.number="xfer.fromWalletId" required>
            <option v-for="w in wallets" :key="'f-'+w.id" :value="w.id">{{ w.name }}</option>
          </select>
        </label>
        <label>Iznos (izlazna valuta)
          <input
            v-model.number="xfer.fromAmount"
            type="number"
            min="0"
            step="0.01"
            required
          >
        </label>
        <label>Datum
          <input v-model="xfer.transactionDate" type="date" required>
        </label>
        <div class="actions">
          <button class="btn" type="submit" :disabled="xfer.fromWalletId===activeGoal?.walletId || acting['xfer']">
            Prebaci
          </button>
          <button class="btn" type="button" @click="closeTransfer">
            Otkaži
          </button>
        </div>
      </form>
    </dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive, computed } from "vue";
import { useAuth } from "@/composables/useAuth";
import * as goalsApi from "@/services/goals";
import { getUserWallets } from "@/services/wallets";
import * as txApi from "@/services/transactions";

const { userId } = useAuth();
const wallets = ref([]);
const savingsWallets = computed(() => wallets.value.filter(w => !!w.savings && !w.archived));
function walletName(id){ return wallets.value.find(w => w.id === id)?.name ?? "-"; }

const useExistingSavings = ref(true);
const createForm = reactive({ name: "", targetAmount: 0, deadline: new Date().toISOString().slice(0,10), walletId: undefined, newWalletName: "" });
const creating = ref(false);
const okMsg = ref("");
const error = ref("");

const goals = ref([]);
const goalProgress = reactive({});
const pay = reactive({});
const acting = reactive({});

function fmtMoney(n){ return (typeof n === "number" ? n : 0).toFixed(2); }
function progress(g){ return goalProgress[g.id] ?? { current: 0, target: g.targetAmount ?? 0 }; }
function pct(g){ const p = progress(g); return Math.max(0, Math.min(100, p.target ? (p.current/p.target)*100 : 0)); }

async function loadWallets(){ wallets.value = await getUserWallets(userId.value); }

async function refresh(){
  const list = await goalsApi.getUserGoals(userId.value);
  goals.value = list;
  await Promise.all(goals.value.map(async g => {
    const p = await goalsApi.getGoalProgress(g.id);
    const current = Number(p.progress) || 0;
    const target = Number(g?.targetAmount) || 0;
    goalProgress[g.id] = { current, target };
  }));
}

async function onCreate(){
  creating.value = true; okMsg.value=""; error.value="";
  try{
    const payload = { name: createForm.name, userId: userId.value, targetAmount: createForm.targetAmount, deadline: createForm.deadline };
    if(useExistingSavings.value){ payload.walletId = createForm.walletId; } else { payload.newWalletName = createForm.newWalletName; }
    await goalsApi.createGoal(payload);
    okMsg.value = "Cilj kreiran.";
    createForm.name = "";
    createForm.targetAmount = 0;
    if(!useExistingSavings.value) createForm.newWalletName = "";
    await loadWallets();
    await refresh();
  } catch(e){ error.value = e?.response?.data?.message ?? e?.message ?? "Greška."; }
  finally { creating.value = false; }
}

async function deposit(g){
  if(!pay[g.id]) return;
  acting[g.id] = true;
  try{
    const payload = { name: `Uplata na cilj: ${g.name}`, amount: pay[g.id], type: "PRIHOD", categoryId: 1, transactionDate: new Date().toISOString().slice(0,10), walletId: g.walletId, userId: userId.value };
    await txApi.createTransaction(payload);
    pay[g.id] = 0;
    await refresh();
  } finally { acting[g.id] = false; }
}

const transferDlg = ref(null);
const activeGoal = ref(null);
const xfer = reactive({ fromWalletId: undefined, fromAmount: 0, transactionDate: new Date().toISOString().slice(0,10) });
// function openTransfer(g){
//   activeGoal.value = g;
//   xfer.fromWalletId = wallets.value.find(w => w.id !== g.walletId)?.id ?? wallets.value[0]?.id;
//   xfer.fromAmount = 0;
//   transferDlg.value?.showModal?.();
// }
function closeTransfer(){ transferDlg.value?.close?.(); }
async function doTransfer(){
  if(!activeGoal.value) return;
  acting["xfer"] = true;
  try{
    const payload = { transfer: true, fromWalletId: xfer.fromWalletId, toWalletId: activeGoal.value.walletId, fromAmount: xfer.fromAmount, transactionDate: xfer.transactionDate, userId: userId.value, name: `Transfer na cilj: ${activeGoal.value.name}`, type: "PRIHOD" };
    await txApi.createTransfer(payload);
    closeTransfer();
    await refresh();
  } finally { acting["xfer"] = false; }
}

onMounted(async () => {
  await loadWallets();
  if(savingsWallets.value.length) createForm.walletId = savingsWallets.value[0].id;
  await refresh();
});
</script>

<style scoped>
.goals-page { max-width: 1100px; margin: 0 auto; padding: 20px; display: grid; gap: 18px; font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial; }
.muted { color: #6b7280; }
.card { background: #fff; border: 1px solid #e6e8ee; border-radius: 12px; padding: 14px; }
.form { display: grid; gap: 12px; }
.row { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
input, select { border: 1px solid #d1d5db; border-radius: 8px; padding: 8px 10px; }
.actions { display: flex; gap: 8px; align-items: center; }
.btn { border: 1px solid #cbd5e1; border-radius: 8px; background: white; padding: 6px 10px; cursor: pointer; }
.btn:hover { background: #f3f4f6; }
.ok { color: #065f46; }
.err { color: #b91c1c; }

.list-head { display: flex; align-items: center; gap: 8px; }
.list-head .spacer { flex: 1; }

.bar { width: 100%; height: 10px; background: #f1f5f9; border-radius: 999px; overflow: hidden; border: 1px solid #e5e7eb; }
.fill { height: 100%; background: #10b981; }
.mini { color: #334155; font-size: 12px; margin-top: 4px; }
.pay { display: flex; gap: 6px; }

/* dialog */
dialog { border: 1px solid #e5e7eb; border-radius: 12px; padding: 14px; width: 420px; }
dialog::backdrop { background: rgba(0,0,0,.35); }
</style>
