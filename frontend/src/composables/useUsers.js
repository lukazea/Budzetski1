import { ref } from "vue";
import { getPublicUserCount } from "@/services/users";

export function useUsers() {
  const userCount = ref(null);

  const fetchUserCount = async () => {
    const data = await getPublicUserCount();
    userCount.value = data;
  };

  return {
    userCount: userCount,
    fetchUserCount: fetchUserCount,
  };
}
