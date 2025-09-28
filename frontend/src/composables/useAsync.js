import { ref } from "vue";

export function useAsync(asyncFn) {
  const loading = ref(false);
  const error = ref(null);

  const run = async (...args) => {
    loading.value = true;
    error.value = null;
    try {
      return await asyncFn(...args);
    } catch (e) {
      error.value = e.message || "Something went wrong";
      return null;
    } finally {
      loading.value = false;
    }
  };

  return { run, loading, error };
}
