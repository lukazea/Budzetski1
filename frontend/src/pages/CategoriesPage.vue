<template>
  <div class="cats-page">
    <h1>Kategorije</h1>

    <section class="add">
      <h2>Dodaj sopstvenu kategoriju</h2>
      <form @submit.prevent="onCreate">
        <label>Naziv
          <input v-model.trim="form.name" required placeholder="npr. Štednja za more">
        </label>
        <label>Tip
          <select v-model="form.type">
            <option value="PRIHOD">Prihod</option>
            <option value="TROSAK">Trošak</option>
          </select>
        </label>
        <button :disabled="saving">
          {{ saving ? "Čuvam..." : "Dodaj" }}
        </button>
        <span v-if="okMsg" class="ok">{{ okMsg }}</span>
        <span v-if="error" class="err">{{ error }}</span>
      </form>
    </section>

    <section class="lists">
      <div class="toolbar">
        <select v-model="filterType" @change="refresh">
          <option value="ALL">
            Sve
          </option>
          <option value="PRIHOD">
            Prihodi
          </option>
          <option value="TROSAK">
            Troškovi
          </option>
        </select>
        <input v-model.trim="q" placeholder="Pretraga naziva..." @keyup.enter="doSearch">
        <button @click="doSearch">
          Traži
        </button>
        <button v-if="q" @click="resetSearch">
          Reset
        </button>
      </div>

      <h2>Vidljive kategorije</h2>
      <table v-if="visible.length">
        <thead>
          <tr>
            <th>Naziv</th>
            <th>Tip</th>
            <th>Vrsta</th>
            <th>Aktivna</th>
            <th style="width:180px">
              Akcije
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in visible" :key="c.id">
            <td>
              <template v-if="editingId === c.id">
                <input v-model.trim="edit.name">
              </template>
              <template v-else>
                {{ c.name }}
              </template>
            </td>
            <td>
              <template v-if="editingId === c.id">
                <select v-model="edit.type">
                  <option value="PRIHOD">
                    PRIHOD
                  </option>
                  <option value="TROSAK">
                    TROSAK
                  </option>
                </select>
              </template>
              <template v-else>
                {{ c.type }}
              </template>
            </td>
            <td>
              <span :class="c.predefined ? 'tag pre' : 'tag priv'">
                {{ c.predefined ? 'predefinisana' : 'privatna' }}
              </span>
            </td>
            <td>
              <template v-if="editingId === c.id">
                <input v-model="edit.active" type="checkbox">
              </template>
              <template v-else>
                {{ c.active ? 'DA' : 'NE' }}
              </template>
            </td>
            <td class="actions">
              <template v-if="!c.predefined">
                <button v-if="editingId !== c.id" @click="startEdit(c)">
                  Uredi
                </button>
                <button v-else @click="saveEdit(c.id)">
                  Sačuvaj
                </button>
                <button v-if="editingId === c.id" @click="cancelEdit">
                  Otkaži
                </button>
              </template>
              <template v-else>
                <em class="muted">Globalna (read-only)</em>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="muted">
        Nema kategorija.
      </p>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useAuth } from "@/composables/useAuth";
import {
  getAvailableCategories,
  getCategoriesByType,
  searchCategories,
  createUserCategory,
  updateCategory,
} from "@/services/categories";

const { userId } = useAuth();

const all = ref([]);
const filterType = ref("ALL");
const q = ref("");

const form = ref({ name: "", type: "TROSAK" });
const saving = ref(false);
const okMsg = ref("");
const error = ref("");

const editingId = ref(null);
const edit = ref({ name: "", type: "TROSAK", active: true });

const visible = computed(() => {
  let list = [...all.value];
  if (filterType.value !== "ALL") {
    list = list.filter((c) => c.type === filterType.value);
  }
  if (q.value) {
    const s = q.value.toLowerCase();
    list = list.filter((c) => c.name?.toLowerCase().includes(s));
  }
  return list.sort((a,b) => Number(!!b.predefined) - Number(!!a.predefined) || a.name.localeCompare(b.name));
});

async function refresh() {
  if (filterType.value === "ALL") {
    all.value = await getAvailableCategories(userId.value);
  } else {
    all.value = await getCategoriesByType(userId.value, filterType.value);
  }
}

async function doSearch() {
  if (!q.value) return refresh();
  all.value = await searchCategories(userId.value, q.value);
}

function resetSearch() {
  q.value = "";
  refresh();
}

async function onCreate() {
  saving.value = true; okMsg.value = ""; error.value = "";
  try {
    const payload = { name: form.value.name, type: form.value.type, predefined: false, active: true };
    await createUserCategory(userId.value, payload);
    okMsg.value = "Kategorija dodata.";
    form.value.name = "";
    await refresh();
  } catch (e) {
    error.value = e?.response?.data?.message ?? e?.message ?? "Greška.";
  } finally {
    saving.value = false;
  }
}

function startEdit(c) {
  editingId.value = c.id;
  edit.value = { name: c.name, type: c.type, active: c.active };
}
function cancelEdit() {
  editingId.value = null;
}
async function saveEdit(id) {
  try {
    await updateCategory(id, { ...edit.value, predefined: false }); // backend svakako štiti
    editingId.value = null;
    await refresh();
  } catch (e) {
    alert(e?.response?.data?.message ?? e?.message ?? "Greška pri čuvanju.");
  }
}

onMounted(refresh);
</script>

  <style scoped>
  .cats-page { max-width: 960px; margin: 0 auto; padding: 20px; display: grid; gap: 20px; font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial; }
  .add form { display: flex; gap: 10px; align-items: end; }
  .add input, .add select { border: 1px solid #d1d5db; border-radius: 8px; padding: 6px 8px; }
  .ok { color: #065f46; margin-left: 8px; }
  .err { color: #b91c1c; margin-left: 8px; }
  .lists .toolbar { display: flex; gap: 8px; align-items: center; }
  table { width: 100%; border-collapse: collapse; }
  th, td { border-bottom: 1px solid #eef2f7; text-align: left; padding: 8px 6px; font-size: 14px; }
  .tag { border: 1px solid #e5e7eb; border-radius: 999px; font-size: 12px; padding: 2px 8px; }
  .tag.pre { background: #f8fafc; }
  .tag.priv { background: #fff7ed; }
  .muted { color: #6b7280; font-size: 12px; }
  button { border: 1px solid #cbd5e1; border-radius: 8px; background: white; padding: 6px 10px; cursor: pointer; }
  button:hover { background: #f3f4f6; }
  </style>
