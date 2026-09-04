import { useEffect, useState } from "react"
import type { OfficeResponseDTO } from "../../types/office"
import {
  fetchActiveOffices,
  fetchAllOffices,
} from "../../services/officeService"

import { Col, Container, Row, Form, Button } from "react-bootstrap"
import { ShiftManagerNavbar } from "../shift manager/ShiftManagerNavbar"

export const ShiftManagerOffices = () => {
  const [activeOfficesList, setActiveOfficesList] = useState<
    OfficeResponseDTO[]
  >([])
  const [allOfficesList, setAllOfficesList] = useState<OfficeResponseDTO[]>([])
  const [searchTerm, setSearchTerm] = useState("")

  useEffect(() => {
    const loadData = async () => {
      try {
        // Carica sempre gli uffici attivi
        const activeData = await fetchActiveOffices()
        setActiveOfficesList(activeData)

        const allData = await fetchAllOffices()
        setAllOfficesList(allData)
      } catch (err) {
        console.error("Errore nel caricamento delle sedi", err)
      }
    }

    loadData()
  }, [])

  const filteredActiveOffices = activeOfficesList.filter((office) =>
    office.name.toLowerCase().includes(searchTerm.toLowerCase()),
  )

  const filteredAllOffices = allOfficesList.filter((office) =>
    office.name.toLowerCase().includes(searchTerm.toLowerCase()),
  )

  const getStatusVariant = (status: string) => {
    switch (status) {
      case "ACTIVE":
        return "text-success fw-bold"
      case "TEMPORARILY_CLOSED":
      case "PERMANENTLY_CLOSED":
        return "text-danger fw-bold"
      default:
        return "text-dark"
    }
  }

  return (
    <>
      <ShiftManagerNavbar />
      <Container fluid className="px-3 min-vh-100">
        {/* Barra di ricerca */}
        <Row className="d-flex justify-content-center mt-4">
          <Col xs={12} lg={10}>
            <div className="d-flex align-items-center">
              <Form.Control
                className="w-50 search-input-desktop mt-2 border border-1 border-secondary smaller-text"
                type="text"
                placeholder="Cerca ufficio"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />

              <Button className="btn-custom2 ms-1 mt-2">Cerca</Button>
            </div>
          </Col>
        </Row>

        {/* TABELLA UFFICI ATTIVI */}
        <Row className="my-5 d-flex justify-content-center align-items-center">
          <Col xs={12} lg={10}>
            <div className="card shadow-sm border border-1 border-secondary">
              <div className="p-3">
                <h4 className="text-dark mb-3 small-title">Uffici attivi</h4>
                <div className="table-responsive">
                  <table className="table table-hover align-middle">
                    <thead className="table-primary">
                      <tr>
                        <th className="text-dark smaller-text">Nome</th>
                        <th className="text-dark smaller-text">Indirizzo</th>
                        <th className="text-dark smaller-text">Apertura</th>
                        <th className="text-dark smaller-text">Chiusura</th>
                        <th className="text-dark smaller-text">Stato</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredActiveOffices.length > 0 ? (
                        filteredActiveOffices.map((office) => (
                          <tr key={office.id}>
                            <td className="smaller-text text-nowrap">
                              {office.name}
                            </td>
                            <td className="smaller-text text-nowrap">
                              {office.street} {office.houseNumber} -{" "}
                              {office.zipCode}
                            </td>
                            <td className="ps-3 smaller-text">
                              {office.openingTime}
                            </td>
                            <td className="ps-3 smaller-text">
                              {office.closingTime}
                            </td>
                            <td className={getStatusVariant(office.status)}>
                              {office.status}
                            </td>
                          </tr>
                        ))
                      ) : (
                        <tr>
                          <td colSpan={5} className="text-center">
                            Nessun ufficio trovato
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </Col>
        </Row>

        {/* TABELLA TUTTI GLI UFFICI (Visibile sia a User che a Admin in sola lettura) */}
        <Row className="my-5 d-flex justify-content-center align-items-center">
          <Col xs={12} lg={10}>
            <div className="card shadow-sm border border-1 border-secondary">
              <div className="p-3">
                <h4 className="text-dark mb-3 small-title">Tutti gli uffici</h4>
                <div className="table-responsive">
                  <table className="table table-hover align-middle">
                    <thead className="table-primary">
                      <tr>
                        <th className="text-dark smaller-text">Nome</th>
                        <th className="text-dark smaller-text">Indirizzo</th>
                        <th className="text-dark smaller-text">Apertura</th>
                        <th className="text-dark smaller-text">Chiusura</th>
                        <th className="text-dark smaller-text">Stato</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredAllOffices.length > 0 ? (
                        filteredAllOffices.map((office) => (
                          <tr key={office.id}>
                            <td className="smaller-text text-nowrap">
                              {office.name}
                            </td>
                            <td className="smaller-text text-nowrap">
                              {office.street} {office.houseNumber} -{" "}
                              {office.zipCode}
                            </td>
                            <td className="ps-3 smaller-text">
                              {office.openingTime}
                            </td>
                            <td className="smaller-text">
                              {office.closingTime}
                            </td>
                            <td className="smaller-text">
                              <td className={getStatusVariant(office.status)}>
                                {office.status}
                              </td>
                            </td>
                          </tr>
                        ))
                      ) : (
                        <tr>
                          <td
                            colSpan={5}
                            className="text-center text-muted py-3"
                          >
                            Nessun ufficio trovato
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </Col>
        </Row>
      </Container>
    </>
  )
}
