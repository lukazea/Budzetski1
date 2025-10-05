import { http } from "@/lib/http";

export async function createGoal(payload) {
  const { data } = await http.post("/goals", payload);
  return data;
}

export async function getUserGoals(userId) {
  const { data } = await http.get(`/goals/user/${userId}`);
  if (Array.isArray(data)) return data;
  if (data && typeof data === "object") return Object.values(data);
  return [];
}

export async function getGoalProgress(goalId) {
  const { data } = await http.get(`/goals/${goalId}/progress`);
  return data;
}
