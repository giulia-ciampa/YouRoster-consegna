import type { RoleResponseDTO } from "../types/role"
import { authFetch } from "./apiClient"

export const fetchAllRoles = async (): Promise<RoleResponseDTO[]> => {
  const response = await authFetch("/roles", {
    method: "GET",
  })

  if (!response.ok) {
    const errorData = await response.json().catch(() => null)

    throw new Error(
      errorData?.message || "Errore durante il recupero dei ruoli.",
    )
  }

  return await response.json()
}
