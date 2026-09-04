import type {
  ShiftAssignment,
  ShiftAssignmentDTO,
  UpdateShiftAssignmentDTO,
} from "../types/shift"
import type { PageResponse } from "../types/users"
import { authFetch } from "./apiClient"

//GET IL TURNO DEL GIORNO
export const getMyAssignmentsByDate = async (
  dateString: string,
): Promise<ShiftAssignment[]> => {
  const token = localStorage.getItem("accessToken")

  // startDate ed endDate uguali alla data di oggi per isolare il turno
  const response = await authFetch(
    `/shift-assignments/me?startDate=${dateString}&endDate=${dateString}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    },
  )

  if (!response.ok) {
    throw new Error("Errore nel recupero delle assegnazioni.")
  }

  const pageData: PageResponse<ShiftAssignment> = await response.json()
  return pageData.content
}

// GET UTENTI IN TURNO CON ME
export const getColleaguesWithMyShift = async (
  dateString: string,
): Promise<ShiftAssignment[]> => {
  const token = localStorage.getItem("accessToken")

  const response = await authFetch(
    `/shift-assignments/colleagues-onshift?shiftDate=${dateString}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    },
  )

  if (!response.ok) {
    throw new Error("Errore nel recupero delle assegnazioni dei colleghi.")
  }

  return await response.json()
}

//CREA NUOVA ASSEGNAZIONE TURNO
export const createShiftAssignment = async (
  shiftAssignmentDTO: ShiftAssignmentDTO,
) => {
  const token = localStorage.getItem("accessToken")

  const response = await authFetch("/shift-assignments", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token && { Authorization: `Bearer ${token}` }),
    },
    body: JSON.stringify(shiftAssignmentDTO),
  })

  if (!response.ok) {
    const errorData = await response.json()
    throw new Error(
      errorData.message || "Errore durante il salvataggio dell'assegnazione",
    )
  }

  return response.json()
}

//GET ASSEGNAZIONE TRA DUE DATE
export const getAssignmentsBetweenDates = async (
  startDate: string,
  endDate: string,
): Promise<PageResponse<ShiftAssignment>> => {
  const token = localStorage.getItem("accessToken")

  const response = await authFetch(
    `/shift-assignments/between-dates?startDate=${startDate}&endDate=${endDate}&page=0&size=1000`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        ...(token && { Authorization: `Bearer ${token}` }),
      },
    },
  )

  if (!response.ok) {
    throw new Error("Errore nel recupero delle assegnazioni.")
  }

  return response.json()
}

//MODIFICA ASSEGNAZIONE
export const updateShiftAssignment = async (
  id: string,
  payload: UpdateShiftAssignmentDTO,
): Promise<ShiftAssignment> => {
  const response = await authFetch(`/shift-assignments/${id}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    const errorData = await response.json()
    throw new Error(
      errorData.message || "Errore nella modifica dell'assegnazione.",
    )
  }

  return response.json()
}

//3. CANCELLA UN'ASSEGNAZIONE
export const deleteShiftAssignment = async (id: string): Promise<void> => {
  const response = await authFetch(`/shift-assignments/${id}`, {
    method: "DELETE",
  })

  if (!response.ok) {
    const errorData = await response.json()
    throw new Error(
      errorData.message || "Errore durante la cancellazione dell'assegnazione.",
    )
  }
}

// GET LE PROPRIE ASSEGNAZIONI TRA DUE DATE
export const getMyAssignmentsBetweenDates = async (
  startDate: string,
  endDate: string,
): Promise<ShiftAssignment[]> => {
  const token = localStorage.getItem("accessToken")

  const response = await authFetch(
    `/shift-assignments/me?startDate=${startDate}&endDate=${endDate}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        ...(token && { Authorization: `Bearer ${token}` }),
      },
    },
  )

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message || "Errore nel recupero delle proprie assegnazioni.",
    )
  }

  const pageData: PageResponse<ShiftAssignment> = await response.json()

  return pageData.content
}
