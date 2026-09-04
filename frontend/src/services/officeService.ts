import type { OfficeResponseDTO } from "../types/office"
import type { UserBasicInformationResponseDTO } from "../types/users"
import { authFetch } from "./apiClient"

//1. GET ACTIVE OFFICES
export const fetchActiveOffices = async (): Promise<OfficeResponseDTO[]> => {
  const response = await authFetch("/offices/active", {
    method: "GET",
  })

  if (!response.ok) {
    const errorData = await response.json().catch(() => null)
    throw new Error(
      errorData?.message || "Errore durante il recupero delle sedi attive",
    )
  }

  return await response.json()
}

//2. CREAZIONE NUOVO UFFICIO
export const fetchCreateOffice = async (
  officeData: Omit<OfficeResponseDTO, "id">,
): Promise<OfficeResponseDTO> => {
  const response = await authFetch("/offices", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(officeData),
  })

  if (!response.ok) {
    throw new Error("Errore durante la creazione dell'ufficio")
  }

  return response.json()
}

//3. MODIFICA UFFICIO
export const fetchUpdateOffice = async (
  id: string,
  updatedData: OfficeResponseDTO,
) => {
  const token = localStorage.getItem("accessToken")

  const response = await authFetch(`/offices/${id}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(updatedData),
  })

  if (!response.ok) {
    // Leggiamo direttamente il JSON di errore restituito dal backend
    const errorData = await response.json()
    const errorMessage = errorData.message || JSON.stringify(errorData)
    throw new Error(errorMessage)
  }

  return await response.json()
}

// GET TUTTI GLI UFFICI
export const fetchAllOffices = async (
  status?: string,
): Promise<OfficeResponseDTO[]> => {
  const token = localStorage.getItem("accessToken")

  // Se viene passato uno status (es. "ACTIVE"), lo aggiunge come query param
  const url = status ? `/offices?status=${status}` : `/offices`

  const response = await authFetch(url, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })

  if (!response.ok) {
    const errorData = await response.json()
    const errorMessage = errorData.message || JSON.stringify(errorData)
    throw new Error(errorMessage)
  }

  return await response.json()
}

//SALVA ASSEGNAZIONE UFFICIO ALL'UTENTE
export const updateUserOffice = async (
  userId: string,
  officeId: string,
): Promise<UserBasicInformationResponseDTO> => {
  const response = await authFetch(`/users/${userId}/office/${officeId}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
  })

  if (!response.ok) {
    const errorData = await response.json().catch(() => null)

    throw new Error(
      errorData?.message || "Errore durante l'aggiornamento della sede.",
    )
  }

  return await response.json()
}
