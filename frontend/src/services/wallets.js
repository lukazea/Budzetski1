import { http } from "@/lib/http";

export async function getUserWallets(userId) {
  const { data } = await http.get(`/http/wallets/user/${userId}`);
  return data;
}

export async function getArchivedWallets(userId) {
  const { data } = await http.get(`/http/wallets/user/${userId}/archived`);
  return data;
}

export async function getWalletById(userId, walletId) {
  const { data } = await http.get(`/http/wallets/user/${userId}/${walletId}`);
  return data;
}

export async function getWalletBalance(walletId) {
  const { data } = await http.get(`/http/wallets/${walletId}/balance`);
  return data;
}

export async function getTotalUserBalance(userId) {
  const { data } = await http.get(`/http/wallets/user/${userId}/total-balance`);
  return data;
}

export async function createWallet(
  userId,
  currencyCode,
  payload
) {
  const { data } = await http.post(
    `/http/wallets/user/${userId}?currencyCode=${encodeURIComponent(currencyCode)}`,
    payload
  );
  return data;
}

export async function updateWallet(walletId, payload) {
  const { data } = await http.put(`/http/wallets/${walletId}`, payload);
  return data;
}

export async function deleteWallet(walletId) {
  await http.delete(`/http/wallets/${walletId}`);
}

export async function archiveWallet(walletId) {
  await http.put(`/http/wallets/${walletId}/archive`);
}

export async function activateWallet(walletId) {
  await http.put(`/http/wallets/${walletId}/activate`);
}

export async function getWalletTransactions(walletId, userId) {
  const { data } = await http.get(
    `/http/transactions/wallet/${walletId}?userId=${userId}`
  );
  return data;
}
