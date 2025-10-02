import { http } from "@/lib/http";

export async function getUsers() {
  const { data } = await http.get("/users");
  return data;
}

export async function getPublicUserCount() {
  const { data } = await http.get("/users/public/count");
  return data;
}

export async function getUserProfile(userId) {
  const { data } = await http.get(`/users/${userId}/profile`);
  return data;
}

export async function updateUserProfile(userId, payload) {
  const { data } = await http.put(`/users/${userId}/profile`, payload);
  return data;
}

export async function partialUpdateUserProfile(userId, patch) {
  const { data } = await http.patch(`/users/${userId}/profile`, patch);
  return data;
}

export async function checkUsernameAvailability(userId, userName) {
  const { data } = await http.get(`/users/${userId}/check-username`, { params: { userName } });
  return data;
}

export async function checkEmailAvailability(userId, email) {
  const { data } = await http.get(`/users/${userId}/check-email`, { params: { email } });
  return data;
}
