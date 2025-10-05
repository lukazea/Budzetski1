<template>
  <div class="recurring-page">
    <header class="head">
      <h1>Ponavljajuće transakcije</h1>
      <p class="muted">
        Definiši šablon (npr. Plata, Kirija). Sistem automatski generiše transakcije po zadatoj učestalosti. Šablon možeš pauzirati ili zaustaviti.
      </p>
    </header>

    <section class="card">
      <h2>{{ editId ? 'Izmena šablona' : 'Novi šablon' }}</h2>
      <form class="form" @submit.prevent="onSubmit">
        <label>Naziv
          <input v-model.trim="form.name" required placeholder="npr. Plata / Kirija">
        </label>

        <div class="row">
          <label>Iznos
            <input
              v-model.number="form.amount"
              type="number"
              min="0"
              step="0.01"
              required
            >
          </label>
          <label>Tip
            <select v-model="form.type" required>
              <option value="PRIHOD">PRIHOD</option>
              <option value="TROSAK">TROSAK</option>
            </select>
          </label>
        </div>

        <div class="row">
          <label>Kategorija
            <select v-model.number="form.categoryId" required>
              <option :value="undefined">— izaberi —</option>
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </label>
          <label>Novčanik
            <select v-model.number="form.walletId" required>
              <option :value="undefined">— izaberi —</option>
              <option v-for="w in wallets" :key="w.id" :value="w.id">{{ w.name }}</option>
            </select>
          </label>
        </div>

        <div class="row">
          <label>Učestalost
            <select v-model="form.frequency" required>
              <option v-for="f in FREQUENCIES" :key="f" :value="f">{{ f }}</option>
            </select>
          </label>
          <label>Početak
            <input v-model="form.startDate" type="date" required>
          </label>
        </div>

        <div class="row">
          <label>Kraj (opciono)
            <input v-model="form.endDate" type="date">
          </label>
          <div />
        </div>

        <div class="actions">
          <button class="btn" type="submit" :disabled="saving">
            {{ saving ? 'Čuvam…' : (editId ? 'Sačuvaj izmene' : 'Kreiraj') }}
          </button>
          <button
            v-if="editId"
            class="btn"
            type="button"
            @click="resetForm"
          >
            Otkaži
          </button>
          <span v-if="okMsg" class="ok">{{ okMsg }}</span>
          <span v-if="error" class="err">{{ error }}</span>
        </div>
      </form>
    </section>

    <section class="card">
      <div class="list-head">
        <h2>Moji šabloni</h2>
        <div class="spacer" />
        <span class="chip">Aktivnih: {{ activeCount }}</span>
        <button class="btn" @click="refresh">
          Osveži
        </button>
      </div>

      <table v-if="templates.length">
        <thead>
          <tr>
            <th>Naziv</th>
            <th>Iznos</th>
            <th>Tip</th>
            <th>Novčanik</th>
            <th>Kategorija</th>
            <th>Učestalost</th>
            <th>Period</th>
            <th>Akcije</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in templates" :key="t.id">
            <td>{{ t.name }}</td>
            <td>{{ fmtMoney(t.amount) }}</td>
            <td>{{ t.type }}</td>
            <td>{{ t.walletName ?? walletName(t.walletId) }}</td>
            <td>{{ t.categoryName ?? categoryName(t.categoryId) }}</td>
            <td>{{ t.frequency }}</td>
            <td>
              <span>{{ t.startDate }}</span>
              <span v-if="t.endDate"> → {{ t.endDate }}</span>
            </td>
            <td class="actions">
              <button class="btn" title="Uredi" @click="selectForEdit(t)">
                Uredi
              </button>
              <button
                class="btn"
                title="Pauziraj/Nastavi"
                :disabled="acting[t.id]"
                @click="toggle(t)"
              >
                {{ t.active ? 'Pauziraj' : 'Aktiviraj' }}
              </button>
              <button class="btn" title="Zaustavi (postavi kraj)" @click="promptStop(t)">
                Zaustavi
              </button>
              <button
                class="btn"
                title="Generiši sada"
                :disabled="acting[t.id]"
                @click="generateNow(t)"
              >
                Generiši
              </button>
              <button
                class="btn danger"
                title="Obriši"
                :disabled="acting[t.id]"
                @click="remove(t)"
              >
                Obriši
              </button>
            </td>
          </tr>
          <tr v-for="t in templates" :key="'h-'+t.id" class="history-row">
            <td colspan="8">
              <details>
                <summary>Istorija generisanja</summary>
                <div v-if="!history[t.id]?.loaded" class="mini">
                  <button class="btn" @click="loadHistory(t)">
                    Učitaj istoriju
                  </button>
                </div>
                <table v-else-if="(history[t.id]?.items?.length ?? 0) > 0" class="mini-table">
                  <thead>
                    <tr>
                      <th>Datum</th>
                      <th>Naziv</th>
                      <th>Iznos</th>
                      <th>Tip</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="h in history[t.id].items" :key="h.id">
                      <td>{{ h.transactionDate }}</td>
                      <td>{{ h.name }}</td>
                      <td>{{ fmtMoney(h.amount) }}</td>
                      <td>{{ h.type }}</td>
                    </tr>
                  </tbody>
                </table>
                <p v-else class="muted">
                  Nema stavki.
                </p>
              </details>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="muted">
        Još nema šablona.
      </p>
    </section>

    <dialog ref="stopDlg">
      <form class="form" @submit.prevent="doStop">
        <h3>Zaustavi šablon: {{ stopTarget?.name }}</h3>
        <label>Krajnji datum
          <input v-model="stopEndDate" type="date" required>
        </label>
        <div class="actions">
          <button class="btn" type="submit">
            Postavi kraj
          </button>
          <button class="btn" type="button" @click="closeStop">
            Otkaži
          </button>
        </div>
      </form>
    </dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { useAuth } from "@/composables/useAuth";
import { getUserWallets } from "@/services/wallets";
import * as catApi from "@/services/categories";
import * as rapi from "@/services/recurring";

const { userId } = useAuth();
const FREQUENCIES = ["DAILY","WEEKLY","MONTHLY"];

// form state
const form = reactive({
  name: "", amount: 0, type: "PRIHOD", categoryId: undefined, walletId: undefined,
  frequency: "MONTHLY", startDate: new Date().toISOString().slice(0,10), endDate: ""
});
const editId = ref(null);
const saving = ref(false);
const okMsg = ref("");
const error = ref("");

// lists
const wallets = ref([]);
const categories = ref([]);
const templates = ref([]);
const activeCount = ref(0);
const acting = reactive({});
const history = reactive({}); // { [templateId]: { loaded: boolean, items: [] } }

function resetForm(){
  editId.value = null;
  form.name = "";
  form.amount = 0;
  form.type = "PRIHOD";
  form.categoryId = undefined;
  form.walletId = undefined;
  form.frequency = "MONTHLY";
  form.startDate = new Date().toISOString().slice(0,10);
  form.endDate = "";
  okMsg.value = "";
  error.value = "";
}

function selectForEdit(t){
  editId.value = t.id;
  form.name = t.name;
  form.amount = t.amount;
  form.type = t.type;
  form.categoryId = t.categoryId;
  form.walletId = t.walletId;
  form.frequency = t.frequency;
  form.startDate = t.startDate;
  form.endDate = t.endDate ?? "";
}

async function onSubmit(){
  saving.value = true; okMsg.value=""; error.value="";
  try{
    const payload = { ...form, userId: userId.value };
    if(!form.endDate) delete payload.endDate;
    if(editId.value){
      await rapi.updateRecurringTemplate(editId.value, userId.value, payload);
      okMsg.value = "Šablon izmenjen.";
    } else {
      await rapi.createRecurringTemplate(payload);
      okMsg.value = "Šablon kreiran.";
    }
    resetForm();
    await refresh();
  } catch(e){ error.value = e?.response?.data?.message ?? e?.message ?? "Greška."; }
  finally{ saving.value = false; }
}

async function refresh(){
  const [tpl, cnt] = await Promise.all([
    rapi.getRecurringTemplatesByUser(userId.value),
    rapi.getActiveCount(userId.value)
  ]);
  templates.value = tpl;
  activeCount.value = Number(cnt ?? 0);
}

function walletName(id){ return wallets.value.find(w => w.id === id)?.name ?? "-"; }
function categoryName(id){ return categories.value.find(c => c.id === id)?.name ?? "-"; }
function fmtMoney(n){ return (typeof n === "number" ? n : 0).toFixed(2); }

async function toggle(t){
  acting[t.id] = true;
  try{ await rapi.toggleRecurringTemplate(t.id, userId.value); await refresh(); } finally { acting[t.id] = false; }
}

// stop (set endDate)
const stopDlg = ref(null);
const stopTarget = ref(null);
const stopEndDate = ref(new Date().toISOString().slice(0,10));
function promptStop(t){ stopTarget.value = t; stopEndDate.value = new Date().toISOString().slice(0,10); stopDlg.value?.showModal?.(); }
function closeStop(){ stopDlg.value?.close?.(); }
async function doStop(){
  if(!stopTarget.value) return;
  await rapi.stopRecurringTemplate(stopTarget.value.id, userId.value, stopEndDate.value);
  closeStop();
  await refresh();
}

async function generateNow(t){
  acting[t.id] = true;
  try{ await rapi.generateTransactionFromTemplate(t.id, userId.value); await loadHistory(t); await refresh(); } finally { acting[t.id] = false; }
}

async function remove(t){
  if(!confirm(`Obriši šablon "${t.name}"?`)) return;
  acting[t.id] = true;
  try{ await rapi.deleteRecurringTemplate(t.id, userId.value); await refresh(); } finally { acting[t.id] = false; }
}

async function loadHistory(t){
  if(!history[t.id]?.loaded){
    const items = await rapi.getTransactionHistory(t.id, userId.value);
    history[t.id] = { loaded: true, items };
  }
}

onMounted(async () => {
  wallets.value = await getUserWallets(userId.value);
  categories.value = await catApi.getAvailableCategories(userId.value);
  await refresh();
});
</script>

  <style scoped>
  .recurring-page { max-width: 1100px; margin: 0 auto; padding: 20px; display: grid; gap: 18px; font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial; }
  .muted { color: #6b7280; }
  .card { background: #fff; border: 1px solid #e6e8ee; border-radius: 12px; padding: 14px; }
  .form { display: grid; gap: 12px; }
  .row { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
  input, select { border: 1px solid #d1d5db; border-radius: 8px; padding: 8px 10px; }
  .actions { display: flex; gap: 8px; align-items: center; }
  .btn { border: 1px solid #cbd5e1; border-radius: 8px; background: white; padding: 6px 10px; cursor: pointer; }
  .btn:hover { background: #f3f4f6; }
  .btn.danger { color: #b91c1c; border-color: #fecaca; }
  .ok { color: #065f46; }
  .err { color: #b91c1c; }
  .list-head { display: flex; align-items: center; gap: 8px; }
  .list-head .spacer { flex: 1; }
  .chip { border: 1px solid #e5e7eb; padding: 4px 8px; border-radius: 999px; font-size: 12px; }
  .table { width: 100%; }
  .actions button { margin-right: 6px; }
  .history-row details { margin-top: 8px; }
  .mini { color: #64748b; font-size: 13px; }
  .mini-table { width: 100%; border-collapse: collapse; margin-top: 8px; }
  .mini-table th, .mini-table td { border-bottom: 1px solid #eef2f7; padding: 6px 8px; text-align: left; font-size: 13px; }
  </style>
