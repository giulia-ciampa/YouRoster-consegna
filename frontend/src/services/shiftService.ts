import type {
  Shift,
  CreateShiftPayload,
  ShiftPage,
  ShiftToUpdatePayload,
} from "../types/shift"
import { authFetch } from "./apiClient"

//CREA NUOVO TURNO
export const createShift = async (
  payload: CreateShiftPayload,
): Promise<Shift> => {
  const token = localStorage.getItem("accessToken")

  const response = await authFetch("/shifts", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error("Errore nella creazione del turno")
  }

  return await response.json()
}

//GET TURNI ATTIVI
export const fetchShifts = async (
  isActive?: boolean,
  page: number = 0,
  size: number = 10,
): Promise<ShiftPage> => {
  const params = new URLSearchParams()

  params.append("page", page.toString())
  params.append("size", size.toString())

  if (isActive !== undefined) {
    params.append("isActive", isActive.toString())
  }

  const token = localStorage.getItem("accessToken")

  const response = await authFetch(`/shifts?${params.toString()}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  })

  if (!response.ok) {
    throw new Error("Errore nel recupero dei turni")
  }

  const data: ShiftPage = await response.json()

  return data
}

//MODIFICA TURNO
export const updateShift = async (
  shiftId: string,
  payload: ShiftToUpdatePayload,
) => {
  const token = localStorage.getItem("accessToken")

  const response = await authFetch(`/shifts/${shiftId}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    const errorText = await response.text()
    console.error("ERRORE UPDATE SHIFT:", errorText)

    throw new Error("Errore nella modifica del turno")
  }

  return await response.json()
}
