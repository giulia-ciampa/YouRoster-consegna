import type { ApiErrorResponse } from "../types/auth"
import type {
  AbsenceCertificationRequestDTO,
  AbsenceCertificationResponse,
  AbsenceCertificationReviewResponse,
  ChangeHolidayRequestDTO,
  ChangeHolidayRequestResponseDTO,
  ChangeLeaveHoursRequestDTO,
  ChangeLeaveHoursRequestResponseDTO,
  HolidayRequestDTO,
  HolidayRequestResponse,
  HrCertificationRequest,
  LeaveHoursRequestDTO,
  LeaveHoursRequestResponse,
  RequestResponse,
  RequestResponseDTO,
  ReviewerNotesDTO,
  UpdateCertificationRequestDTO,
  UpdateHolidayRequestDTO,
  UpdateLeaveHoursRequestDTO,
} from "../types/requests"
import type { PageResponse, UserSummaryResponse } from "../types/users"
import { authFetch } from "./apiClient"

//NUOVA RICHIESTA FERIE
export const createHolidayRequest = async (payload: HolidayRequestDTO) => {
  const response = await authFetch("/holidays", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    const errorData = await response.json()
    throw new Error(
      errorData.message || "Errore nella creazione della richiesta ferie.",
    )
  }

  return response.json()
}

//NUOVA RICHIESTA DI PERMESSO
export const createLeaveHoursRequest = async (
  payload: LeaveHoursRequestDTO,
) => {
  const response = await authFetch("/leave-hours", {
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
        "Errore nella creazione della richiesta di permesso.",
    )
  }

  return response.json()
}

//NUOVA RICHIESTA CON CERTIFICATO
export const createAbsenceCertificationRequest = async (
  payload: AbsenceCertificationRequestDTO,
) => {
  const formData = new FormData()

  formData.append("protocolCode", payload.protocolCode)
  formData.append("startDate", payload.startDate)
  formData.append("endDate", payload.endDate)
  formData.append("issueDate", payload.issueDate)
  formData.append("certificateFile", payload.certificateFile)
  formData.append("certificateType", payload.certificateType)

  if (payload.employeeNotes) {
    formData.append("employeeNotes", payload.employeeNotes)
  }

  const response = await authFetch("/certifications", {
    method: "POST",
    body: formData,
  })

  if (!response.ok) {
    const errorData = await response.json()

    const error = new Error(errorData.message) as ApiErrorResponse
    error.errorsList = errorData.errorsList

    throw error
  }

  return response.json()
}

//GET MIE RICHIESTE -----------------------------------------------------------------

//GET FERIE
export const getMyHolidayRequests = async (
  page = 0,
  size = 15,
): Promise<PageResponse<HolidayRequestResponse>> => {
  const response = await authFetch(
    `/holidays/me?page=${page}&size=${size}&sortBy=createdAt`,
  )

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message || "Errore nel recupero delle richieste ferie.",
    )
  }

  return response.json()
}

//GET PERMESSI
export const getMyLeaveHoursRequests = async (
  page = 0,
  size = 15,
): Promise<PageResponse<LeaveHoursRequestResponse>> => {
  const response = await authFetch(
    `/leave-hours/me?page=${page}&size=${size}&sortBy=createdAt`,
  )

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message || "Errore nel recupero delle richieste di permesso.",
    )
  }

  return response.json()
}

//GET RICHIESTE CON CERTIFICATO
export const getMyAbsenceCertifications = async (
  page = 0,
  size = 15,
): Promise<PageResponse<AbsenceCertificationResponse>> => {
  const response = await authFetch(
    `/certifications/myRequests?page=${page}&size=${size}&sortBy=createdAt`,
  )

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message || "Errore nel recupero delle richieste di malattia.",
    )
  }

  return response.json()
}

//GET RIEPILOGO
export const getUserLeaveSummary = async (): Promise<UserSummaryResponse> => {
  const response = await authFetch("/leaves/summary")

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message ||
        "Errore nel recupero del riepilogo ferie e permessi.",
    )
  }

  return response.json()
}

//-----MODIFICA MIE RICHIESTE

//MODIFICA MIE FERIE

export const updateHolidayRequest = async (
  requestId: string,
  payload: UpdateHolidayRequestDTO,
) => {
  const response = await authFetch(`/holidays/${requestId}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message || "Errore nella modifica della richiesta ferie.",
    )
  }

  return response.json()
}

//MODIFICA MIE ORE DI PERMESSO
export const updateLeaveHoursRequest = async (
  requestId: string,
  payload: UpdateLeaveHoursRequestDTO,
) => {
  const response = await authFetch(`/leave-hours/${requestId}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message || "Errore nella modifica della richiesta di permesso.",
    )
  }

  return response.json()
}

//MODIFICA MIE ASSENZE CERTIFICATE
export const updateAbsenceCertificationRequest = async (
  requestId: string,
  payload: UpdateCertificationRequestDTO,
) => {
  const formData = new FormData()

  if (payload.protocolCode) {
    formData.append("protocolCode", payload.protocolCode)
  }

  if (payload.startDate) {
    formData.append("startDate", payload.startDate)
  }

  if (payload.endDate) {
    formData.append("endDate", payload.endDate)
  }

  if (payload.issueDate) {
    formData.append("issueDate", payload.issueDate)
  }

  if (payload.certificateFile) {
    formData.append("certificateFile", payload.certificateFile)
  }

  if (payload.certificateType) {
    formData.append("certificateType", payload.certificateType)
  }

  if (payload.employeeNotes) {
    formData.append("employeeNotes", payload.employeeNotes)
  }

  const response = await authFetch(`/certifications/${requestId}`, {
    method: "PATCH",
    body: formData,
  })

  if (!response.ok) {
    const errorData = await response.json()

    const errorMessage = errorData.errorslist?.length
      ? errorData.errorslist.join("\n")
      : errorData.message ||
        "Errore nella modifica della richiesta certificata."

    throw new Error(errorMessage)
  }

  return response.json()
}

//----ELIMINA(PROPRIE)

//ELIMINA MIE FERIE
export const deleteHolidayRequest = async (requestId: string) => {
  const response = await authFetch(`/holidays/${requestId}`, {
    method: "DELETE",
  })

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message || "Errore nell'eliminazione della richiesta ferie.",
    )
  }
}

//ELIMINA MIE ORE PERMESSO
export const deleteLeaveHoursRequest = async (requestId: string) => {
  const response = await authFetch(`/leave-hours/${requestId}`, {
    method: "DELETE",
  })

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message ||
        "Errore nell'eliminazione della richiesta di permesso.",
    )
  }
}

//ELIMINA MIE RICHIESTE CERTIFICATE
export const deleteAbsenceCertificationRequest = async (requestId: string) => {
  const response = await authFetch(`/certifications/${requestId}`, {
    method: "DELETE",
  })

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message ||
        "Errore nell'eliminazione della richiesta certificata.",
    )
  }
}

//GET PER SHIFT MANAGER E ADMIN ----------------------------------------------------------------

// GET RICHIESTE FERIE DA APPROVARE - SHIFT MANAGER
export const getPendingHolidayRequests = async (): Promise<
  PageResponse<HolidayRequestResponse>
> => {
  const response = await authFetch("/holidays/pending", {
    method: "GET",
  })

  if (!response.ok) {
    throw new Error("Errore nel recupero delle richieste ferie")
  }

  const pageData: PageResponse<HolidayRequestResponse> = await response.json()

  return pageData
}

// GET RICHIESTE PERMESSO DA APPROVARE - SHIFT MANAGER
export const getPendingLeaveHoursRequests = async (): Promise<
  PageResponse<LeaveHoursRequestResponse>
> => {
  const response = await authFetch("/leave-hours/pending", {
    method: "GET",
  })

  if (!response.ok) {
    throw new Error("Errore nel recupero delle richieste di permesso.")
  }

  const pageData: PageResponse<LeaveHoursRequestResponse> =
    await response.json()

  return pageData
}

//-------------------------------APPROVA RICHIESTE
// APPROVA RICHIESTA FERIE - SHIFT MANAGER / ADMIN

export const approveHolidayRequest = async (
  requestId: string,
  notes?: string,
): Promise<HolidayRequestResponse> => {
  const response = await authFetch(`/holidays/${requestId}/approve`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: notes ? JSON.stringify({ notes }) : undefined,
  })

  if (!response.ok) {
    throw new Error("Errore nell'approvazione della richiesta ferie")
  }

  return await response.json()
}

// APPROVA RICHIESTA PERMESSO - SHIFT MANAGER / ADMIN

export const approveLeaveHoursRequest = async (
  requestId: string,
  notes?: string,
): Promise<LeaveHoursRequestResponse> => {
  const response = await authFetch(`/leave-hours/${requestId}/approve`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: notes ? JSON.stringify({ notes }) : undefined,
  })

  if (!response.ok) {
    throw new Error("Errore nell'approvazione della richiesta di permesso")
  }

  return await response.json()
}

//_-------------------------------RIFIUTA RICHIESTE
//RIFIUTA RICHIESTA FERIE
export const rejectHolidayRequest = async (
  requestId: string,
  notes?: string,
): Promise<HolidayRequestResponse> => {
  const response = await authFetch(`/holidays/${requestId}/reject`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: notes ? JSON.stringify({ notes }) : undefined,
  })

  if (!response.ok) {
    throw new Error("Errore nel rifiuto della richiesta ferie")
  }

  return await response.json()
}

//RIFIUTA RICHIESTA DI PERMESSO
export const rejectLeaveHoursRequest = async (
  requestId: string,
  notes?: string,
): Promise<LeaveHoursRequestResponse> => {
  const response = await authFetch(`/leave-hours/${requestId}/reject`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: notes ? JSON.stringify({ notes }) : undefined,
  })

  if (!response.ok) {
    throw new Error("Errore nel rifiuto della richiesta di permesso")
  }

  return await response.json()
}

//GET TUTTE LE RICHIESTE -----------------------------HR E ADMIN---------------
export const getRequests = async (
  page = 0,
  size = 15,
): Promise<PageResponse<RequestResponse>> => {
  const response = await authFetch(
    `/requests?page=${page}&size=${size}&sortBy=createdAt`,
  )

  return response.json()
}

//MODIFICA RICHIESTA ORIGINALE
export const createChangeHolidayRequest = async (
  originalRequestId: string,
  payload: ChangeHolidayRequestDTO,
): Promise<ChangeHolidayRequestResponseDTO> => {
  const response = await authFetch(
    `/change-holiday-requests/${originalRequestId}`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    },
  )

  return response.json()
}

//MODIFICA RICHIESTA ORIGINALE
export const createChangeLeaveHoursRequest = async (
  originalRequestId: string,
  payload: ChangeLeaveHoursRequestDTO,
): Promise<ChangeLeaveHoursRequestResponseDTO> => {
  const response = await authFetch(
    `/change-leave-hours-requests/${originalRequestId}`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    },
  )

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message || "Errore nell'invio della richiesta di modifica.",
    )
  }

  return response.json()
}

// GET SINGOLA RICHIESTA DI MODIFICA FERIE
export const getMyChangeHolidayRequest = async (
  originalRequestId: string,
): Promise<ChangeHolidayRequestResponseDTO> => {
  const response = await authFetch(
    `/change-holiday-requests/${originalRequestId}`,
  )

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message ||
        "Errore nel recupero della richiesta di modifica delle ferie.",
    )
  }

  return response.json()
}

// GET SINGOLA RICHIESTA DI MODIFICA PERMESSO ORE
export const getMyChangeLeaveHoursRequest = async (
  originalRequestId: string,
): Promise<ChangeLeaveHoursRequestResponseDTO> => {
  const response = await authFetch(
    `/change-leave-hours-requests/${originalRequestId}`,
  )

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message ||
        "Errore nel recupero della richiesta di modifica del permesso.",
    )
  }

  return response.json()
}

//GET TUTTE LE MIE RICHIESTE DI MODIFICA FERIE
export const getMyChangeHolidayRequests = async (
  page = 0,
  size = 15,
): Promise<PageResponse<ChangeHolidayRequestResponseDTO>> => {
  const response = await authFetch(
    `/change-holiday-requests/me?page=${page}&size=${size}&sortBy=createdAt`,
  )

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message ||
        "Errore nel recupero delle richieste di modifica ferie.",
    )
  }

  return response.json()
}

// GET TUTTE LE MIE RICHIESTE DI MODIFICA PERMESSO ORE
export const getMyChangeLeaveHoursRequests = async (
  page = 0,
  size = 15,
): Promise<PageResponse<ChangeLeaveHoursRequestResponseDTO>> => {
  const response = await authFetch(
    `/change-leave-hours-requests/me?page=${page}&size=${size}&sortBy=createdAt`,
  )

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message ||
        "Errore nel recupero delle richieste di modifica permesso.",
    )
  }

  return response.json()
}

// ANNULLA RICHIESTA DI MODIFICA FERIE
export const cancelMyChangeHolidayRequest = async (
  requestId: string,
): Promise<ChangeHolidayRequestResponseDTO> => {
  const response = await authFetch(
    `/change-holiday-requests/${requestId}/cancel`,
    {
      method: "PATCH",
    },
  )

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message ||
        "Errore nell'annullamento della richiesta di modifica.",
    )
  }

  return response.json()
}

// ANNULLA RICHIESTA DI MODIFICA PERMESSO ORE
export const cancelMyChangeLeaveHoursRequest = async (
  requestId: string,
): Promise<ChangeLeaveHoursRequestResponseDTO> => {
  const response = await authFetch(
    `/change-leave-hours-requests/${requestId}/cancel`,
    {
      method: "PATCH",
    },
  )

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message ||
        "Errore nell'annullamento della richiesta di modifica.",
    )
  }

  return response.json()
}

//GET RICHIESTE DI MODIFICA DA LAVORARE

// GET RICHIESTE DI MODIFICA FERIE DA LAVORARE (SENT)

export const getPendingChangeHolidayRequests = async (
  name?: string,
  startDate?: string,
  endDate?: string,
  page = 0,
  size = 15,
  sortBy = "createdAt",
): Promise<PageResponse<ChangeHolidayRequestResponseDTO>> => {
  const params = new URLSearchParams()

  if (name) params.append("name", name)
  if (startDate) params.append("startDate", startDate)
  if (endDate) params.append("endDate", endDate)

  params.append("page", page.toString())
  params.append("size", size.toString())
  params.append("sortBy", sortBy)

  const response = await authFetch(
    `/change-holiday-requests/pending?${params.toString()}`,
  )

  if (!response.ok) {
    const errorData = await response.json()
    throw new Error(
      errorData.message ||
        "Errore nel recupero delle richieste di modifica ferie da lavorare.",
    )
  }

  return response.json()
}

// GET RICHIESTE DI MODIFICA PERMESSI ORE DA LAVORARE (SENT)

export const getPendingChangeLeaveHoursRequests = async (
  name?: string,
  date?: string,
  startTime?: string,
  endTime?: string,
  page = 0,
  size = 15,
  sortBy = "createdAt",
): Promise<PageResponse<ChangeLeaveHoursRequestResponseDTO>> => {
  const params = new URLSearchParams()

  if (name) params.append("name", name)
  if (date) params.append("date", date)
  if (startTime) params.append("startTime", startTime)
  if (endTime) params.append("endTime", endTime)

  params.append("page", page.toString())
  params.append("size", size.toString())
  params.append("sortBy", sortBy)

  const response = await authFetch(
    `/change-leave-hours-requests/pending?${params.toString()}`,
  )

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message ||
        "Errore nel recupero delle richieste di modifica dei permessi da lavorare.",
    )
  }

  return response.json()
}

// GET SINGOLA RICHIESTA DI MODIFICA FERIE - REVIEWER
export const getChangeHolidayRequestForReviewer = async (
  requestId: string,
): Promise<ChangeHolidayRequestResponseDTO> => {
  const response = await authFetch(
    `/change-holiday-requests/${requestId}/reviewer`,
  )

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message ||
        "Errore nel recupero della richiesta di modifica delle ferie.",
    )
  }

  return response.json()
}

// GET SINGOLA RICHIESTA DI MODIFICA PERMESSO ORE - REVIEWER
export const getChangeLeaveHoursRequestForReviewer = async (
  requestId: string,
): Promise<ChangeLeaveHoursRequestResponseDTO> => {
  const response = await authFetch(
    `/change-leave-hours-requests/${requestId}/reviewer`,
  )

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message ||
        "Errore nel recupero della richiesta di modifica del permesso.",
    )
  }

  return response.json()
}

//APPROVA RICHIESTA DI MODIFICA FERIE
export const approveChangeHolidayRequest = async (
  requestId: string,
  notes?: string,
): Promise<ChangeHolidayRequestResponseDTO> => {
  const response = await authFetch(
    `/change-holiday-requests/${requestId}/approve`,
    {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        notes: notes || null,
      }),
    },
  )

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message ||
        "Errore nell'approvazione della richiesta di modifica ferie.",
    )
  }

  return response.json()
}

// APPROVA RICHIESTA DI MODIFICA PERMESSO ORE - REVIEWER

export const approveChangeLeaveHoursRequest = async (
  requestId: string,
  notes?: string,
): Promise<ChangeLeaveHoursRequestResponseDTO> => {
  const response = await authFetch(
    `/change-leave-hours-requests/${requestId}/approve`,
    {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        notes: notes || null,
      }),
    },
  )

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message ||
        "Errore nell'approvazione della richiesta di modifica del permesso.",
    )
  }

  return response.json()
}

// RIFIUTA RICHIESTA DI MODIFICA FERIE - REVIEWER

export const rejectChangeHolidayRequest = async (
  requestId: string,
  notes?: string,
): Promise<ChangeHolidayRequestResponseDTO> => {
  const response = await authFetch(
    `/change-holiday-requests/${requestId}/reject`,
    {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        notes: notes || null,
      }),
    },
  )

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message ||
        "Errore nel rifiuto della richiesta di modifica ferie.",
    )
  }

  return response.json()
}

// RIFIUTA RICHIESTA DI MODIFICA PERMESSO ORE - REVIEWER

export const rejectChangeLeaveHoursRequest = async (
  requestId: string,
  notes?: string,
): Promise<ChangeLeaveHoursRequestResponseDTO> => {
  const response = await authFetch(
    `/change-leave-hours-requests/${requestId}/reject`,
    {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        notes: notes || null,
      }),
    },
  )

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message ||
        "Errore nel rifiuto della richiesta di modifica del permesso.",
    )
  }

  return response.json()
}

// GET TUTTE LE RICHIESTE - ADMIN E SHIFT MANAGER
export const getAllRequests = async (
  page = 0,
  size = 20,
): Promise<PageResponse<RequestResponseDTO>> => {
  const response = await authFetch(`/requests/all?page=${page}&size=${size}`)

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message || "Errore nel caricamento delle richieste.",
    )
  }

  return response.json()
}

//RICHIESTE CON CERTIFICATO DA LAVORARE
export const getPendingCertificationRequests = async (
  page = 0,
  size = 15,
): Promise<PageResponse<HrCertificationRequest>> => {
  const token = localStorage.getItem("accessToken")

  const response = await authFetch(
    `/certifications/pending?page=${page}&size=${size}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        ...(token && { Authorization: `Bearer ${token}` }),
      },
    },
  )

  if (!response.ok) {
    throw new Error("Errore nel recupero delle certificazioni.")
  }

  return response.json()
}

//APPROVA RICHIESTA CON CERTIFICATO
export const approveCertificationRequest = async (
  requestId: string,
  payload?: ReviewerNotesDTO,
): Promise<AbsenceCertificationReviewResponse> => {
  const token = localStorage.getItem("accessToken")

  const response = await authFetch(`/certifications/${requestId}/approve`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      ...(token && { Authorization: `Bearer ${token}` }),
    },
    body: payload ? JSON.stringify(payload) : undefined,
  })

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message || "Errore durante l'approvazione della richiesta.",
    )
  }

  return response.json()
}

//RIFIUTA RICHIESTA CON CERTIFICATO
export const rejectCertificationRequest = async (
  requestId: string,
  payload?: ReviewerNotesDTO,
): Promise<AbsenceCertificationReviewResponse> => {
  const token = localStorage.getItem("accessToken")

  const response = await authFetch(`/certifications/${requestId}/reject`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      ...(token && { Authorization: `Bearer ${token}` }),
    },
    body: payload ? JSON.stringify(payload) : undefined,
  })

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message || "Errore durante il rifiuto della richiesta.",
    )
  }

  return response.json()
}

// GET TUTTE LE RICHIESTE CERTIFICATE CON PAGINAZIONE E FILTRI
export const getAllCertificationRequests = async (
  page = 0,
  size = 15,
  name?: string,
  status?: string,
  startDate?: string,
  endDate?: string,
) => {
  const token = localStorage.getItem("accessToken")

  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
    sortBy: "createdAt",
  })

  if (name) params.append("name", name)
  if (status) params.append("status", status)
  if (startDate) params.append("startDate", startDate)
  if (endDate) params.append("endDate", endDate)

  const response = await authFetch(
    `/certifications/search?${params.toString()}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        ...(token && {
          Authorization: `Bearer ${token}`,
        }),
      },
    },
  )

  if (!response.ok) {
    throw new Error("Errore nel recupero delle richieste certificate.")
  }

  return response.json()
}
