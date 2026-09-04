import { useEffect, useState } from "react"
import { Container } from "react-bootstrap"
import { fetchAllOffices } from "../../services/officeService"
import type { OfficeResponseDTO } from "../../types/office"
import AdminActiveOffices from "./AdminActiveOffices"
import AdminNavbar from "./AdminNavbar"

const AdminOfficesContainer = () => {
  const [offices, setOffices] = useState<OfficeResponseDTO[]>([])
  const [loading, setLoading] = useState<boolean>(true)

  // Funzione per ricaricare gli uffici dal backend
  const loadOffices = () => {
    fetchAllOffices()
      .then((data) => {
        setOffices(data)
        setLoading(false)
      })
      .catch((err) => {
        console.error("Errore nel recupero degli uffici:", err)
        setLoading(false)
      })
  }

  useEffect(() => {
    loadOffices()
  }, [])

  // Filtriamo gli uffici attivi per la tabella in alto
  const activeOffices = offices.filter((office) => office.status === "ACTIVE")

  if (loading) {
    return <div className="text-center mt-5">Caricamento in corso...</div>
  }

  return (
    <>
      <AdminNavbar />
      <Container fluid className="px-4">
        {/* Unico componente che gestisce entrambe le tabelle e passa il refresh */}
        <AdminActiveOffices
          activeOffices={activeOffices}
          allOffices={offices}
          onOfficeUpdated={loadOffices}
        />
      </Container>
    </>
  )
}

export default AdminOfficesContainer
