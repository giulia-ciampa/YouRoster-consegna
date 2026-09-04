const API_URL = import.meta.env.VITE_API_URL

// Funzione helper per le chiamate autenticate con rinnovo automatico
export const authFetch = async (
  endpoint: string,
  options: RequestInit = {},
): Promise<Response> => {
  const token = localStorage.getItem("accessToken")

  // Assicuriamoci che headers esista e sia un oggetto
  const headers: Record<string, string> = {
    ...(options.headers as Record<string, string>),
    Authorization: `Bearer ${token}`,
  }

  if (!(options.body instanceof FormData)) {
    headers["Content-Type"] = "application/json"
  }

  const updatedOptions: RequestInit = {
    ...options,
    headers,
  }

  let response = await fetch(`${API_URL}${endpoint}`, updatedOptions)

  // Se riceviamo 401 (Token scaduto)
  if (response.status === 401) {
    const refreshToken = localStorage.getItem("refreshToken")

    if (!refreshToken) {
      localStorage.clear()
      window.location.href = "/login"
      throw new Error("Sessione scaduta. Effettua nuovamente il login.")
    }

    try {
      const refreshResponse = await fetch(`${API_URL}/auth/refresh`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken }),
      })

      if (!refreshResponse.ok) {
        throw new Error("Refresh token non valido o scaduto")
      }

      const data = await refreshResponse.json()

      localStorage.setItem("accessToken", data.accessToken)
      if (data.refreshToken) {
        localStorage.setItem("refreshToken", data.refreshToken)
      }

      // Aggiorniamo l'header con il nuovo token e ripeteremo la richiesta originale
      headers["Authorization"] = `Bearer ${data.accessToken}`
      response = await fetch(`${API_URL}${endpoint}`, updatedOptions)
    } catch (refreshError) {
      localStorage.clear()
      window.location.href = "/login"
      throw refreshError
    }
  }

  return response
}
