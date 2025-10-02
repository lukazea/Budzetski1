// src/services/categories.js
import { http } from "@/lib/http";

export async function getCategoriesByType(userId, type) {
  const { data } = await http.get(`/categories/user/${userId}/type/${type}`);
  return data;
}
