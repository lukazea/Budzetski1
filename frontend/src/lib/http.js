import axios from "axios";

export const http = axios.create({
  baseURL: process.env.VITE_API_URL ?? "http://localhost:8080/api",
  timeout: 10_000,
});

http.interceptors.response.use(
  (r) => r,
  (err) => {
    err.message ||= "Network error";
    return Promise.reject(err);
  }
);
