import { http } from "@/lib/http";

export async function getAllUsers() {
  const { data } = await http.get("/users/admin/all");
  return data;
}

export async function blockUser(userId) {
  await http.put(`/users/admin/${userId}/block`);
}

export async function unblockUser(userId) {
  await http.put(`/users/admin/${userId}/unblock`);
}

export async function getAdminTransactionsByUser(userId, {
  sortBy = "createdAt",
  sortDirection = "desc",
  page = 0,
  size = 20
} = {}) {
  console.log("sort by,", sortBy);
  const { data } = await http.get(`/users/admin/${userId}/transactions`, {
    params: { sortBy, sortDirection, page, size }
  });
  return data;
}

export async function getUserNotes(userId, { page = 0, size = 10 } = {}) {
  const { data } = await http.get(`/users/admin/${userId}/notes`, {
    params: { "pageable.page": page, "pageable.size": size, page, size }
  });
  return data;
}

export async function getUserNotesCount(userId) {
  const { data } = await http.get(`/users/admin/${userId}/notes/count`);
  return data;
}

export async function createUserNote({ userId, note }) {
  const { data } = await http.post("/users/admin/notes", { userId, note });
  return data;
}

export async function updateUserNote(noteId, note) {
  const { data } = await http.put(`/users/admin/notes/${noteId}`, note, {
    headers: { "Content-Type": "application/json" }
  });
  return data;
}

export async function deleteUserNote(noteId) {
  await http.delete(`/users/admin/notes/${noteId}`);
}

export async function getPredefinedCategoriesAdmin() {
  const { data } = await http.get("/categories/admin/predefined");
  return data ?? [];
}

export async function createPredefinedCategory({ name, type }) {
  const payload = { name, type, predefined: true };
  const { data } = await http.post("/categories/admin/predefined", payload, {
    headers: { "Content-Type": "application/json" },
  });
  return data;
}

export async function deleteCategoryById(categoryId) {
  await http.delete(`/categories/${categoryId}`);
}

export async function activatePredefined(categoryId) {
  const { data } = await http.patch(`/categories/admin/${categoryId}/activate`);
  return data;
}
export async function deactivatePredefined(categoryId) {
  const { data } = await http.patch(`/categories/admin/${categoryId}/deactivate`);
  return data;
}

export async function getAllCurrencies() {
  const { data } = await http.get("/currencies");
  return data ?? [];
}

export async function getCurrency(currencyCode) {
  const { data } = await http.get(`/currencies/${currencyCode}`);
  return data;
}

export async function createCurrency({ currency, valueToEur }) {
  const payload = { currency, valueToEur };
  const { data } = await http.post("/currencies", payload, {
    headers: { "Content-Type": "application/json" },
  });
  return data;
}

export async function updateExchangeRate(currencyCode, newRate) {
  const { data } = await http.put(
    `/currencies/${currencyCode}`,
    null,
    { params: { newRate } }
  );
  return data;
}

export async function deleteCurrency(currencyId) {
  await http.delete(`/currencies/${currencyId}`);
}

export async function updateRatesFromAPI() {
  await http.post("/currencies/update-rates");
}

export async function convertBetweenCurrencies(amount, fromCode, toCode) {
  const { data } = await http.get("/currencies/convert", {
    params: { amount, fromCode, toCode }
  });
  return data;
}
