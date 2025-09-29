import { http } from "@/lib/http";

export async function register(body) {
  const { data } = await http.post("/auth/register", body);
  return data;
}

export async function login(body) {
  const { data } = await http.post("/auth/login", body);

  const token = data.token;
  if (!token) throw new Error("Token not present in response.");

  localStorage.setItem("jwt", token);
  localStorage.setItem("userId", data.id);

  return data;
}

export function logout() {
  localStorage.removeItem("jwt");
  localStorage.removeItem("userId");
}

export function isAuthenticated() {
  return !!localStorage.getItem("jwt");
}
