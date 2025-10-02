import { http } from "@/lib/http";

// CREATE
export async function createTransaction(payload) {
  const { data } = await http.post("/transactions", payload);
  return data;
}

export async function createTransfer(payload) {
  const { data } = await http.post("/transactions/transfer", payload);
  return data;
}

// LISTE — periodi
export async function getDailyTransactions(userId, date) {
  const { data } = await http.get("/transactions/daily", { params: { userId, date } });
  return data;
}

export async function getWeeklyTransactions(userId, date) {
  const { data } = await http.get("/transactions/weekly", { params: { userId, date } });
  return data;
}

export async function getMonthlyTransactions(userId, year, month) {
  const { data } = await http.get("/transactions/monthly", { params: { userId, year, month } });
  return data;
}

export async function getQuarterlyTransactions(userId, year, quarter) {
  const { data } = await http.get("/transactions/quarterly", { params: { userId, year, quarter } });
  return data;
}

// PAGINIRANO
export async function getTransactionsPaginated(userId, page = 0, size = 10) {
  const { data } = await http.get("/transactions/paginated", { params: { userId, page, size } });
  return data;
}

export async function getTransactionsPaginatedByDate(userId, startDate, endDate, page = 0, size = 10) {
  const { data } = await http.get("/transactions/paginated/date-range", {
    params: { userId, startDate, endDate, page, size },
  });
  return data;
}
