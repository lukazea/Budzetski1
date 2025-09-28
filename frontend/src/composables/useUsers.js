import { ref } from "vue";
import { createUser, getPublicUserCount } from "@/services/users";
import { useAsync } from "@/composables/useAsync";

export function useUsers() {
  const users = ref(null);

  const fetchUserCount = useAsync(async () => {
    const data = await getPublicUserCount();
    users.value = data;
  });

  const addUser = useAsync(async (input) => {
    const created = await createUser(input);
    users.value = [...(users.value || []), created];
  });

  return {
    users,
    fetchUserCount: fetchUserCount,
    addUser: addUser,
  };
}
