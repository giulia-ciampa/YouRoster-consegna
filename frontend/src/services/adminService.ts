import type {
  AdminApprovalRequestDTO,
  MessageResponseDTO,
} from "../types/approveRequest"
import type { PageResponse, UserProfileResponseDTO } from "../types/users"
import { authFetch } from "./apiClient"

// 1. SHIFT ASSIGNMENT BY DATE
export const fetchAssignmentsByDate = async (
  shiftDate: string,
  officeName?: string,
  assignmentType?: string,
) => {
  const params = new URLSearchParams({ shiftDate })
  if (officeName) params.append("officeName", officeName)
  if (assignmentType) params.append("assignmentType", assignmentType)

  // Passiamo solo il percorso relativo, authFetch aggiungerà API_URL in automatico
  const response = await authFetch(
    `/shift-assignments/by-date?${params.toString()}`,
    {
      method: "GET",
    },
  )

  if (!response.ok) throw new Error("Errore nel recupero dei turni")
  return response.json()
}

// 2. SHIFT TASKS
export const fetchDailyTasks = async (date?: string) => {
  const params = new URLSearchParams()
  if (date) params.append("date", date)

  const response = await authFetch(`/shift-tasks?${params.toString()}`, {
    method: "GET",
  })

  if (!response.ok) throw new Error("Errore nel recupero dei task")
  return response.json()
}

// 3. CLOCKINGS BY USER
export const fetchClockingsByUser = async (userId: string, date?: string) => {
  const params = new URLSearchParams()
  if (date) params.append("date", date)

  const response = await authFetch(
    `/clockings/user/${userId}?${params.toString()}`,
    {
      method: "GET",
    },
  )

  if (!response.ok) throw new Error("Errore nel recupero delle timbrature")
  return response.json()
}

//4. ACCOUNT DA ATTIVARE
export const fetchPendingAccounts = async (page = 0, size = 20) => {
  const response = await authFetch(
    `/accounts/pending?page=${page}&size=${size}`,
  )

  if (!response.ok) {
    const errorData = await response.json()
    throw new Error(
      errorData.message || "Errore nel caricamento delle richieste in sospeso",
    )
  }

  return await response.json()
}

//5. ACCETTA ACCOUNT
export const fetchAcceptAccount = async (
  accountId: string,
  role: string,
  officeId: string | null,
): Promise<MessageResponseDTO> => {
  const payload: AdminApprovalRequestDTO = {
    roles: role ? [role] : [], // Se vuoto, il backend assegnerà STAFF
    officeId: officeId || null,
  }

  const response = await authFetch(`/accounts/${accountId}/accept`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    const errorData = await response.json()
    throw new Error(
      errorData.message || "Errore durante l'approvazione dell'account",
    )
  }

  return await response.json()
}

// RIFIUTA / ELIMINA RICHIESTA ACCOUNT
export const fetchRejectAccount = async (
  accountId: string,
): Promise<MessageResponseDTO> => {
  const response = await authFetch(`/accounts/${accountId}/reject`, {
    method: "DELETE",
  })

  if (!response.ok) {
    const errorData = await response.json()
    throw new Error(
      errorData.message || "Errore durante il rifiuto dell'account",
    )
  }

  return await response.json()
}

//GET TUTTI GLI UTENTI
export const getAllUsers = async (
  page = 0,
  size = 15,
): Promise<PageResponse<UserProfileResponseDTO>> => {
  const response = await authFetch(`/users?page=${page}&size=${size}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  })

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(errorData.message || "Errore nel recupero degli utenti.")
  }

  return response.json()
}

// AGGIORNA I RUOLI DELL'ACCOUNT

export const updateUserRoles = async (
  accountId: string,
  roles: string[],
): Promise<void> => {
  const response = await authFetch(`/accounts/${accountId}/roles`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      roles,
    }),
  })

  if (!response.ok) {
    const errorData = await response.json().catch(() => null)

    throw new Error(
      errorData?.message || "Errore durante l'aggiornamento dei ruoli.",
    )
  }
}
