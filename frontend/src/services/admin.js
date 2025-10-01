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
