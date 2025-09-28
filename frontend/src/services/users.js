import { http } from "@/lib/http";

export async function getUsers() {
  const { data } = await http.get("/users");
  return data;
}

export async function getPublicUserCount() {
  const { data } = await http.get("/users/public/count");
  return data;
}

export async function createUser(payload) {
  const { data } = await http.post("/users", payload);
  return data;
}
