import { http } from "@/lib/http";

// CREATE
export async function createRecurringTemplate(payload) {
  const { data } = await http.post("/recurring-templates", payload);
  return data;
}

// UPDATE
export async function updateRecurringTemplate(templateId, userId, payload) {
  const { data } = await http.put(`/recurring-templates/${templateId}/user/${userId}`, payload);
  return data;
}

// DELETE
export async function deleteRecurringTemplate(templateId, userId) {
  await http.delete(`/recurring-templates/${templateId}/user/${userId}`);
}

// LIST BY USER
export async function getRecurringTemplatesByUser(userId) {
  const { data } = await http.get(`/recurring-templates/user/${userId}`);
  return data;
}

// ACTIVE COUNT
export async function getActiveCount(userId) {
  const { data } = await http.get(`/recurring-templates/user/${userId}/count`);
  return data;
}

// TOGGLE ACTIVE
export async function toggleRecurringTemplate(templateId, userId) {
  const { data } = await http.patch(`/recurring-templates/${templateId}/user/${userId}/toggle`);
  return data;
}

// STOP (SET END DATE)
export async function stopRecurringTemplate(templateId, userId, endDate) {
  const { data } = await http.patch(
    `/recurring-templates/${templateId}/user/${userId}/stop?endDate=${encodeURIComponent(endDate)}`
  );
  return data;
}

// MANUAL GENERATION (for testing)
export async function generateTransactionFromTemplate(templateId, userId) {
  const { data } = await http.post(`/recurring-templates/${templateId}/user/${userId}/generate`);
  return data; // returns a message string
}

// HISTORY (transactions produced from this template)
export async function getTransactionHistory(templateId, userId) {
  const { data } = await http.get(`/recurring-templates/${templateId}/user/${userId}/history`);
  return data; // Transaction[]
}
