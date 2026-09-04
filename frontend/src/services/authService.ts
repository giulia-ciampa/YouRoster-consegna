import type {
  LoginPayload,
  LoginResponse,
  ApiErrorResponse,
  RegisterPayload,
  UpdateCredentialsPayload,
} from "../types/auth"
import { authFetch } from "./apiClient"

const API_URL = import.meta.env.VITE_API_URL

//============================
// LOGIN (Usa fetch normale)
//===============================
export const loginCall = async (
  payload: LoginPayload,
): Promise<LoginResponse> => {
  const response = await fetch(`${API_URL}/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    const errorData = await response.json()
    throw new Error(
      errorData.message ||
        errorData.error ||
        "Credenziali non valide o errore durante il login",
    )
  }

  const data: LoginResponse = await response.json()

  localStorage.setItem("accessToken", data.accessToken)
  localStorage.setItem("refreshToken", data.refreshToken)
  localStorage.setItem("photoUrl", data.photoUrl)

  return data
}

//==================================
// Forgot Password (Usa fetch normale)
//==================================
export async function forgotPassword(email: string) {
  const response = await fetch(`${API_URL}/auth/forgot-password`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ email }),
  })

  const data = (await response.json()) as ApiErrorResponse

  if (!response.ok) {
    const errorMessage =
      data.errorsList?.[0] || data.message || "Errore durante la richiesta"
    throw new Error(errorMessage)
  }

  return data.message
}

//==================================
// Reset Password (Usa fetch normale)
//==================================
export async function resetPassword(token: string, newPassword: string) {
  const response = await fetch(`${API_URL}/auth/reset-password`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ token, newPassword }),
  })

  const data = await response.json()

  if (!response.ok) {
    throw new Error(JSON.stringify(data) || "Token non valido o scaduto.")
  }

  return data.message
}

//==================================
// REGISTRAZIONE (Usa fetch normale)
//==================================
export async function registration(payload: RegisterPayload) {
  const formData = new FormData()

  formData.append("name", payload.name)
  formData.append("surname", payload.surname)
  formData.append("taxCode", payload.taxCode)
  formData.append("dateOfBirth", payload.dateOfBirth)
  formData.append("placeOfBirth", payload.placeOfBirth)
  formData.append("nationality", payload.nationality)
  formData.append("phoneNumber", payload.phoneNumber)
  formData.append("streetAddress", payload.streetAddress)
  formData.append("houseNumber", payload.houseNumber)
  formData.append("zipCode", payload.zipCode)
  formData.append("city", payload.city)
  formData.append("province", payload.province)
  if (payload.referenceOfficeId) {
    formData.append("referenceOfficeId", payload.referenceOfficeId)
  }
  formData.append("iban", payload.iban)
  formData.append("documentNumber", payload.documentNumber)
  formData.append("documentType", payload.documentType)
  formData.append("issueDate", payload.issueDate)
  formData.append("expirationDate", payload.expirationDate)
  formData.append("email", payload.email)
  formData.append("password", payload.password)
  formData.append("confirmPassword", payload.confirmPassword)

  // File (aggiungili solo se l'utente li ha selezionati)
  if (payload.documentFront)
    formData.append("documentFront", payload.documentFront)
  if (payload.documentBack)
    formData.append("documentBack", payload.documentBack)
  if (payload.taxCodeFront)
    formData.append("taxCodeFront", payload.taxCodeFront)
  if (payload.taxCodeBack) formData.append("taxCodeBack", payload.taxCodeBack)
  if (payload.avatar) {
    formData.append("avatar", payload.avatar)
  }

  const response = await fetch(`${API_URL}/auth/registration`, {
    method: "POST",
    body: formData,
  })

  const data = await response.json()

  if (!response.ok) {
    throw new Error(JSON.stringify(data))
  }

  return data
}

//MODIFICA EMAIL E PASSWORD
export const updateCredentials = async (
  credentialsData: UpdateCredentialsPayload,
) => {
  const token = localStorage.getItem("accessToken")

  const response = await authFetch(`/auth/credentials`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(credentialsData),
  })

  if (!response.ok) {
    const errorData = await response.json().catch(() => null)
    throw new Error(
      errorData?.message || "Errore durante l'aggiornamento delle credenziali",
    )
  }

  const contentType = response.headers.get("content-type")
  if (contentType && contentType.includes("application/json")) {
    return await response.json()
  }
  return { success: true }
}
