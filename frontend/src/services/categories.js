import { http } from "@/lib/http";

export async function getUserCategories(userId) {
  const { data } = await http.get(`/categories/user/${userId}/custom`);
  return data;
}

export async function getCategoriesByType(userId, type) {
  const { data } = await http.get(`/categories/user/${userId}/type/${type}`);
  return data;
}

export async function getAvailableCategories(userId) {
  const { data } = await http.get(`/categories/user/${userId}`);
  return data;
}

// Pretraga po nazivu (spaja globalne + privatne vidljive)
export async function searchCategories(userId, searchTerm) {
  const { data } = await http.get(`/categories/user/${userId}/search`, { params: { searchTerm } });
  return data;
}

// Kreiraj PRIVATNU kategoriju korisniku
export async function createUserCategory(userId, payload) {
  const { data } = await http.post(`/categories/user/${userId}`, payload);
  return data;
}

// Update (backend štiti ownership i predefined)
export async function updateCategory(categoryId, payload) {
  const { data } = await http.put(`/categories/${categoryId}`, payload);
  return data;
}
