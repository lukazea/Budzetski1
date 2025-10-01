<template>
  <div class="page">
    <h1>Upravljanje kategorijama</h1>
    <p class="muted">
      Dodavanje predefinisanih kategorija za nove korisnike. Brisanje ovde
      uklanja kategoriju iz <em>predefinisane liste</em> – postojeći korisnici
      zadržavaju svoje već dodeljene kategorije.
    </p>

    <section class="card">
      <h2>Dodaj predefinisanu kategoriju</h2>
      <form class="row" @submit.prevent="onCreate">
        <input
          v-model.trim="form.name"
          placeholder="Naziv kategorije (npr. Hrana)"
          required
        >
        <select v-model="form.type" required>
          <option disabled value="">
            Tip
          </option>
          <option value="PRIHOD">
            PRIHOD
          </option>
          <option value="TROSAK">
            TROSAK
          </option>
        </select>
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

    <section class="card">
      <div class="head">
        <h2>Predefinisane kategorije</h2>
        <input v-model.trim="q" placeholder="Pretraga po nazivu...">
      </div>

      <table v-if="filtered.length">
        <thead>
          <tr>
            <th>ID</th>
            <th>Naziv</th>
            <th>Tip</th>
            <th>Predefinisana</th>
            <th>Akcije</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in filtered" :key="c.id">
            <td>{{ c.id }}</td>
            <td>{{ c.name }}</td>
            <td>{{ c.type }}</td>
            <td>{{ c.predefined ? "DA" : "NE" }}</td>
            <td>{{ c.active ? "DA" : "NE" }}</td>
            <td>
              <button v-if="c.active" class="danger" @click="deactivate(c)">
                Deaktiviraj
              </button>
              <button v-else @click="activate(c)">
                Aktiviraj
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="muted">
        Nema kategorija.
      </p>

      <p v-if="listError" class="error">
        {{ listError }}
      </p>
      <p v-if="listSuccess" class="success">
        {{ listSuccess }}
      </p>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import {
  getPredefinedCategoriesAdmin,
  createPredefinedCategory,
  activatePredefined,
  deactivatePredefined
} from "@/services/admin";

const items = ref([]);
const q = ref("");

const form = ref({ name: "", type: "" });
const creating = ref(false);
const createError = ref(null);
const createSuccess = ref(null);

const listError = ref(null);
const listSuccess = ref(null);

const filtered = computed(() => {
  const s = q.value.toLowerCase();
  if (!s) return items.value;
  return items.value.filter((c) => (c.name ?? "").toLowerCase().includes(s));
});

async function load() {
  listError.value = null;
  try {
    items.value = await getPredefinedCategoriesAdmin();
  } catch (e) {
    listError.value =
        e?.response?.data?.message ?? e?.message ?? "Greška pri učitavanju.";
  }
}

async function onCreate() {
  if (!form.value.name || !form.value.type) return;
  creating.value = true;
  createError.value = null;
  createSuccess.value = null;
  try {
    await createPredefinedCategory({
      name: form.value.name,
      type: form.value.type,
    });
    createSuccess.value = "Kategorija dodata.";
    form.value = { name: "", type: "" };
    await load();
  } catch (e) {
    createError.value =
        e?.response?.data?.message ?? e?.message ?? "Greška pri dodavanju.";
  } finally {
    creating.value = false;
  }
}
async function activate(c) { await activatePredefined(c.id); await load(); }
async function deactivate(c) { await deactivatePredefined(c.id); await load(); }


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
    grid-template-columns: 1fr 180px 120px;
    gap: 8px;
  }
  .row input, .row select {
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
  .head input {
    width: 280px;
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
  </style>
