import type {
  ActiveUser,
  UserProfileResponse,
  UserProfileResponseDTO,
} from "../types/users"
import { authFetch } from "./apiClient"

//GET MY PROFILE
export const getMyProfile = async (): Promise<UserProfileResponse> => {
  const token = localStorage.getItem("accessToken")

  const response = await authFetch(`/users/me`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  })

  if (!response.ok) {
    throw new Error("Errore nel recupero del profilo utente.")
  }

  const data: UserProfileResponse = await response.json()
  return data
}

//GET MY PROFILE CON TUTT I DETTAGLI
export const getMyProfileDetails =
  async (): Promise<UserProfileResponseDTO> => {
    const token = localStorage.getItem("accessToken")

    const response = await authFetch(`/users/me`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    })

    if (!response.ok) {
      throw new Error("Errore nel recupero del profilo utente.")
    }

    const data: UserProfileResponseDTO = await response.json()

    return data
  }

//UPLOAD FOTO
export const updateMyAvatar = async (
  file: File,
): Promise<UserProfileResponse> => {
  const token = localStorage.getItem("accessToken")

  const formData = new FormData()

  formData.append("photoUrl", file)

  const response = await authFetch(`/users/me/avatar`, {
    method: "PATCH",
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: formData,
  })

  if (!response.ok) {
    throw new Error("Errore durante l'aggiornamento dell'avatar")
  }

  return await response.json()
}

//GET PER LISTA UTENTI PER NOME
export const getActiveUsers = async (): Promise<ActiveUser[]> => {
  try {
    const response = await authFetch(`/users/active`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    })

    if (!response.ok) {
      throw new Error(`Errore HTTP: ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    console.error("Errore durante il recupero degli utenti attivi:", error)
    throw error
  }
}

//AGGIORNA DATI PROFILO
export const updateMyProfile = async (
  formData: FormData,
): Promise<UserProfileResponseDTO> => {
  const token = localStorage.getItem("accessToken")

  const response = await authFetch(`/users/me`, {
    method: "PATCH",
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: formData,
  })

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message || "Errore durante la modifica del profilo.",
    )
  }

  const data: UserProfileResponseDTO = await response.json()

  return data
}
