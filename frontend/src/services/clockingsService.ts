import type { Clocking } from "../types/shift"
import { authFetch } from "./apiClient"

//TIMBRATURA ENTRATA
export const handleClockIn = async (note: string): Promise<boolean> => {
  if (!navigator.geolocation) {
    alert("La geolocalizzazione non è supportata dal tuo browser")
    return false
  }

  return new Promise((resolve) => {
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const latitude = position.coords.latitude.toFixed(6)
        const longitude = position.coords.longitude.toFixed(6)

        try {
          const response = await authFetch("/clockings/in", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
            },
            body: JSON.stringify({ latitude, longitude, note }),
          })

          if (response.ok) {
            alert("Timbratura di entrata effettuata con successo!")
            resolve(true) // Successo reale!
          } else {
            const errData = await response.json()
            alert(errData.message || "Errore durante la timbratura")
            resolve(false) // Fallito (es. distanza eccessiva)
          }
        } catch (error) {
          console.error("Errore di rete:", error)
          alert("Errore di connessione al server.")
          resolve(false)
        }
      },
      (error) => {
        console.error(error)
        alert(
          "Impossibile recuperare la posizione. Concedi i permessi di localizzazione.",
        )
        resolve(false)
      },
    )
  })
}

//VERIFICA TIMBRATURA IN UNA CERTA DATA
export const getMyClockingForDate = async (
  date: string,
): Promise<Clocking | null> => {
  const response = await authFetch(`/clockings/me/date?date=${date}`)

  if (!response.ok) {
    throw new Error("Errore nel recupero della timbratura")
  }

  const text = await response.text()

  if (!text) {
    return null
  }

  return JSON.parse(text)
}

//TIMBRATURA USCITA
export const handleClockOut = async (
  clockOutNote: string,
): Promise<boolean> => {
  if (!navigator.geolocation) {
    alert("La geolocalizzazione non è supportata dal tuo browser")
    return false
  }

  return new Promise((resolve) => {
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const latitude = position.coords.latitude.toFixed(6)
        const longitude = position.coords.longitude.toFixed(6)

        try {
          const response = await authFetch("/clockings/out", {
            method: "PATCH",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
            },
            body: JSON.stringify({ latitude, longitude, clockOutNote }),
          })

          if (response.ok) {
            alert("Timbratura di uscita effettuata con successo!")
            resolve(true) // Successo reale!
          } else {
            const errData = await response.json()
            alert(errData.message || "Errore durante la timbratura di uscita")
            resolve(false)
          }
        } catch (error) {
          console.error("Errore di rete:", error)
          alert("Errore di connessione al server.")
          resolve(false)
        }
      },
      (error) => {
        console.error(error)
        alert(
          "Impossibile recuperare la posizione. Concedi i permessi di localizzazione.",
        )
        resolve(false)
      },
    )
  })
}
