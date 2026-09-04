import type {
  ContractDTO,
  ContractResponseDTO,
  UpdateContractDTO,
} from "../types/contract"
import type { PageResponse } from "../types/users"
import { authFetch } from "./apiClient"

//CREA NUOVO CONTRATTO
export const createContract = async (
  payload: ContractDTO,
): Promise<ContractResponseDTO> => {
  const response = await authFetch("/contracts", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(
      errorData.message || "Errore nella creazione del contratto.",
    )
  }

  return response.json()
}

//GET CONTRATTI ATTIVI
export const getActiveContracts = async (
  page = 0,
  size = 15,
): Promise<PageResponse<ContractResponseDTO>> => {
  const response = await authFetch(
    `/contracts?contractStatus=ACTIVE&page=${page}&size=${size}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    },
  )

  if (!response.ok) {
    throw new Error("Errore nel recupero dei contratti attivi.")
  }

  return response.json()
}

//GET TUTTI I CONTRATTI
export const getAllContracts = async (
  name = "",
  page = 0,
  size = 15,
): Promise<PageResponse<ContractResponseDTO>> => {
  const response = await authFetch(
    `/contracts?name=${encodeURIComponent(name)}&page=${page}&size=${size}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    },
  )

  if (!response.ok) {
    throw new Error("Errore nel recupero dei contratti.")
  }

  return response.json()
}

//MODIFICA CONTRATTO
export const updateContract = async (
  contractId: string,
  payload: UpdateContractDTO,
): Promise<ContractResponseDTO> => {
  const response = await authFetch(`/contracts/${contractId}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    const errorData = await response.json()

    throw new Error(errorData.message || "Errore nella modifica del contratto.")
  }

  return response.json()
}
