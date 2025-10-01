<template>
  <div class="admin-page">
    <h1>Admin panel</h1>

    <section class="users">
      <h2>Korisnici</h2>
      <div class="toolbar">
        <input
          v-model.trim="userSearch"
          placeholder="Pretraga po imenu/emailu..."
        >
      </div>

      <table v-if="filteredUsers.length">
        <thead>
          <tr>
            <th>ID</th>
            <th>Ime</th>
            <th>Korisničko ime</th>
            <th>Email</th>
            <th>Uloga</th>
            <th>Blokiran</th>
            <th>Akcije</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="u in filteredUsers"
            :key="u.id"
            :class="{ selected: u.id === selectedUserId }"
          >
            <td>{{ u.id }}</td>
            <td>
              {{ [u.firstName, u.lastName].filter(Boolean).join(" ") || "-" }}
            </td>
            <td>{{ u.userName }}</td>
            <td>{{ u.email }}</td>
            <td>{{ u.role }}</td>
            <td>{{ u.blocked ? "DA" : "NE" }}</td>
            <td class="actions">
              <button @click="selectUser(u)">
                Otvori
              </button>
              <button v-if="!u.blocked" class="danger" @click="onBlock(u)">
                Blokiraj
              </button>
              <button v-else @click="onUnblock(u)">
                Odblokiraj
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else>
        Nema korisnika.
      </p>
    </section>

    <section v-if="selectedUser" class="details">
      <div class="details-header">
        <h2>
          Detalji — {{ selectedUser.userName }}
          <small>(ID: {{ selectedUser.id }})</small>
        </h2>
      </div>

      <div class="columns">
        <!-- TRANSAKCIJE -->
        <div class="card">
          <h3>Transakcije</h3>

          <div class="row">
            <label>Sortiraj po</label>
            <select v-model="txSortBy" @change="loadTransactions()">
              <option value="date">
                Datum
              </option>
              <option value="amount">
                Iznos
              </option>
              <option value="name">
                Naziv
              </option>
            </select>

            <label>Pravac</label>
            <select v-model="txSortDirection" @change="loadTransactions()">
              <option value="desc">
                Opadajuće
              </option>
              <option value="asc">
                Rastuće
              </option>
            </select>
          </div>

          <table v-if="txPage?.content?.length">
            <thead>
              <tr>
                <th>Datum</th>
                <th>Naziv</th>
                <th>Tip</th>
                <th>Iznos</th>
                <th>Kategorija</th>
                <th>Novčanik</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="t in txPage.content" :key="t.id">
                <td>{{ t.transactionDate }}</td>
                <td>{{ t.name }}</td>
                <td>{{ t.type }}</td>
                <td>{{ t.formattedAmountWithCurrency ?? money(t.amount) }}</td>
                <td>{{ t.categoryName ?? "-" }}</td>
                <td>{{ t.walletName ?? t.walletId }}</td>
              </tr>
            </tbody>
          </table>
          <p v-else class="muted">
            Nema transakcija.
          </p>

          <div class="pager">
            <button :disabled="txPage?.first" @click="goTx(0)">
              « Prva
            </button>
            <button :disabled="txPage?.first" @click="goTx(txPage.number - 1)">
              ‹
            </button>
            <span>Strana {{ (txPage?.number ?? 0) + 1 }} /
              {{ txPage?.totalPages ?? 1 }}</span>
            <button :disabled="txPage?.last" @click="goTx(txPage.number + 1)">
              ›
            </button>
            <button
              :disabled="txPage?.last"
              @click="goTx((txPage?.totalPages ?? 1) - 1)"
            >
              Poslednja »
            </button>

            <select v-model.number="txSize" @change="loadTransactions()">
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
        </div>

        <!-- BELEŠKE -->
        <div class="card">
          <h3>Interne beleške</h3>

          <form class="note-form" @submit.prevent="saveNote">
            <textarea
              v-model.trim="noteText"
              rows="3"
              placeholder="Dodaj belešku..."
            />
            <div class="note-actions">
              <button type="submit" :disabled="savingNote">
                {{
                  savingNote
                    ? "Čuvam..."
                    : editingNoteId
                      ? "Sačuvaj izmene"
                      : "Dodaj belešku"
                }}
              </button>
              <button v-if="editingNoteId" type="button" @click="cancelEdit">
                Otkaži
              </button>
            </div>
          </form>

          <ul v-if="notesPage?.content?.length" class="notes">
            <li v-for="n in notesPage.content" :key="n.id">
              <div class="note-head">
                <strong>#{{ n.id }}</strong>
                <span class="meta">
                  kreirao: {{ n.createdBy ?? "-" }},
                  {{ fmtDateTime(n.createdDate) }}
                  <template v-if="n.updatedDate">
                    • izmenjeno: {{ fmtDateTime(n.updatedDate) }}</template>
                </span>
              </div>
              <p class="note-text">
                {{ n.note }}
              </p>
              <div class="note-row-actions">
                <button @click="startEdit(n)">
                  Uredi
                </button>
                <button class="danger" @click="removeNote(n)">
                  Obriši
                </button>
              </div>
            </li>
          </ul>
          <p v-else class="muted">
            Nema beleški.
          </p>

          <div class="pager">
            <button :disabled="notesPage?.first" @click="goNotes(0)">
              «
            </button>
            <button
              :disabled="notesPage?.first"
              @click="goNotes(notesPage.number - 1)"
            >
              ‹
            </button>
            <span>Strana {{ (notesPage?.number ?? 0) + 1 }} /
              {{ notesPage?.totalPages ?? 1 }}</span>
            <button
              :disabled="notesPage?.last"
              @click="goNotes(notesPage.number + 1)"
            >
              ›
            </button>
            <button
              :disabled="notesPage?.last"
              @click="goNotes((notesPage?.totalPages ?? 1) - 1)"
            >
              »
            </button>

            <select v-model.number="notesSize" @change="loadNotes()">
              <option :value="5">
                5
              </option>
              <option :value="10">
                10
              </option>
              <option :value="20">
                20
              </option>
            </select>
          </div>
        </div>
      </div>

      <p v-if="error" class="error">
        {{ error }}
      </p>
      <p v-if="success" class="success">
        {{ success }}
      </p>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import {
  getAllUsers,
  blockUser,
  unblockUser,
  getAdminTransactionsByUser,
  getUserNotes,
  createUserNote,
  updateUserNote,
  deleteUserNote,
} from "@/services/admin";

const users = ref([]);
const userSearch = ref("");
const filteredUsers = computed(() => {
  const q = userSearch.value.toLowerCase();
  if (!q) return users.value;
  return users.value.filter((u) =>
    [u.firstName, u.lastName, u.userName, u.email]
      .filter(Boolean)
      .join(" ")
      .toLowerCase()
      .includes(q)
  );
});
const selectedUserId = ref(null);
const selectedUser = computed(
  () => users.value.find((u) => u.id === selectedUserId.value) || null
);
function selectUser(u) {
  selectedUserId.value = u.id;
}

const txPage = ref(null);
const txPageIndex = ref(0);
const txSize = ref(20);
const txSortBy = ref("createdAt");
const txSortDirection = ref("desc");

const notesPage = ref(null);
const notesIndex = ref(0);
const notesSize = ref(10);
const noteText = ref("");
const editingNoteId = ref(null);

const error = ref(null);
const success = ref(null);
const savingNote = ref(false);

function money(n) {
  const v = typeof n === "number" ? n : 0;
  return v.toFixed(2);
}
function fmtDateTime(s) {
  if (!s) return "-";
  try {
    const d = new Date(s);
    return d.toLocaleString();
  } catch {
    return s;
  }
}

async function loadUsers() {
  users.value = await getAllUsers();
}

async function loadTransactions() {
  if (!selectedUser.value) return;
  txPage.value = await getAdminTransactionsByUser(selectedUser.value.id, {
    page: txPageIndex.value,
    size: txSize.value,
    sortBy: txSortBy.value,
    sortDirection: txSortDirection.value,
  });
}

async function goTx(p) {
  txPageIndex.value = Math.max(0, p);
  await loadTransactions();
}

async function loadNotes() {
  if (!selectedUser.value) return;
  notesPage.value = await getUserNotes(selectedUser.value.id, {
    page: notesIndex.value,
    size: notesSize.value,
  });
}

async function goNotes(p) {
  notesIndex.value = Math.max(0, p);
  await loadNotes();
}

async function onBlock(u) {
  error.value = success.value = null;
  await blockUser(u.id);
  u.blocked = true;
  success.value = `Korisnik #${u.id} blokiran.`;
}

async function onUnblock(u) {
  error.value = success.value = null;
  await unblockUser(u.id);
  u.blocked = false;
  success.value = `Korisnik #${u.id} odblokiran.`;
}

function startEdit(n) {
  editingNoteId.value = n.id;
  noteText.value = n.note || "";
}
function cancelEdit() {
  editingNoteId.value = null;
  noteText.value = "";
}

async function saveNote() {
  if (!selectedUser.value) return;
  savingNote.value = true;
  error.value = success.value = null;
  try {
    if (editingNoteId.value) {
      await updateUserNote(editingNoteId.value, noteText.value);
      success.value = "Beleška izmenjena.";
      editingNoteId.value = null;
    } else {
      await createUserNote({
        userId: selectedUser.value.id,
        note: noteText.value,
      });
      success.value = "Beleška dodata.";
    }
    noteText.value = "";
    await loadNotes();
  } catch (e) {
    error.value =
      e?.response?.data?.message ?? e?.message ?? "Greška pri čuvanju beleške.";
  } finally {
    savingNote.value = false;
  }
}

async function removeNote(n) {
  if (!confirm("Obriši belešku?")) return;
  error.value = success.value = null;
  await deleteUserNote(n.id);
  success.value = "Beleška obrisana.";
  await loadNotes();
}

watch(selectedUserId, async () => {
  txPageIndex.value = 0;
  notesIndex.value = 0;
  await Promise.all([loadTransactions(), loadNotes()]);
});

onMounted(async () => {
  await loadUsers();
});
</script>

<style scoped>
.admin-page {
  display: grid;
  gap: 24px;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial;
}
h1,
h2,
h3 {
  margin: 0 0 10px;
}
.users .toolbar {
  margin-bottom: 10px;
}
.users input {
  width: 320px;
  padding: 8px 10px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
}

table {
  width: 100%;
  border-collapse: collapse;
}
th,
td {
  border-bottom: 1px solid #eef2f7;
  text-align: left;
  padding: 8px;
  font-size: 14px;
}
tr.selected {
  background: #f8fafc;
}
.actions button {
  margin-right: 6px;
}
button {
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 7px 10px;
  background: white;
  cursor: pointer;
}
button:hover {
  background: #f3f4f6;
}
button.danger {
  color: #b91c1c;
  border-color: #fecaca;
}

.details .columns {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 16px;
}
.card {
  border: 1px solid #e6e8ee;
  border-radius: 12px;
  padding: 14px;
  background: #fff;
}
.row {
  display: grid;
  grid-template-columns: 120px 1fr 80px 1fr;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.pager {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}
.pager select {
  padding: 4px 6px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
}

.note-form textarea {
  width: 100%;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 8px;
}
.note-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.notes {
  list-style: none;
  padding: 0;
  margin: 12px 0 0;
  display: grid;
  gap: 10px;
}
.note-head {
  display: flex;
  gap: 8px;
  align-items: baseline;
}
.note-head .meta {
  color: #6b7280;
  font-size: 12px;
}
.note-text {
  margin: 6px 0 0;
  white-space: pre-wrap;
}
.note-row-actions {
  display: flex;
  gap: 8px;
  margin-top: 6px;
}

.muted {
  color: #6b7280;
}
.error {
  color: #b91c1c;
}
.success {
  color: #065f46;
}
</style>
