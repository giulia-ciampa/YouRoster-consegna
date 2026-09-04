import { Alert, Button, Col, Dropdown, Form, Modal, Row } from "react-bootstrap"
import {
  fetchCreateOffice,
  fetchUpdateOffice,
} from "../../services/officeService"
import { useState } from "react"
import type { OfficeResponseDTO } from "../../types/office"
import { IoMdAdd } from "react-icons/io"

interface AdminActiveOfficesProps {
  activeOffices: OfficeResponseDTO[]
  allOffices?: OfficeResponseDTO[]
  onOfficeUpdated: () => void
}

const AdminActiveOffices = ({
  activeOffices,
  allOffices = [],
  onOfficeUpdated,
}: AdminActiveOfficesProps) => {
  const [searchTerm, setSearchTerm] = useState("")
  const [selectedOffice, setSelectedOffice] =
    useState<OfficeResponseDTO | null>(null)

  const [showDetailsModal, setShowDetailsModal] = useState(false)
  const [isEditing, setIsEditing] = useState(false)

  const [editOffice, setEditOffice] = useState({
    id: "",
    name: "",
    street: "",
    houseNumber: "",
    zipCode: "",
    city: "",
    province: "",
    openingTime: "",
    closingTime: "",
    status: "",
    latitude: 0,
    longitude: 0,
  })

  const [showCreateModal, setShowCreateModal] = useState(false)
  const [newOffice, setNewOffice] = useState({
    id: "",
    name: "",
    street: "",
    houseNumber: "",
    zipCode: "",
    city: "",
    province: "",
    openingTime: "",
    closingTime: "",
    status: "ACTIVE",
    latitude: "",
    longitude: "",
  })

  const [feedbackMessage, setFeedbackMessage] = useState<string | null>(null)
  const [feedbackType, setFeedbackType] = useState<"primary" | "warning">(
    "primary",
  )
  const [isLoading, setIsLoading] = useState(false)

  const filteredOffices = activeOffices.filter((office) =>
    office.name.toLowerCase().includes(searchTerm.toLowerCase()),
  )

  const filteredAllOffices = allOffices.filter((office) =>
    office.name.toLowerCase().includes(searchTerm.toLowerCase()),
  )

  const handleShowDetails = (office: OfficeResponseDTO) => {
    setSelectedOffice(office)
    setShowDetailsModal(true)
    setIsEditing(false)
  }

  const handleCloseDetails = () => {
    setShowDetailsModal(false)
    setSelectedOffice(null)
    setIsEditing(false)
  }

  const handleStartEdit = (office: OfficeResponseDTO) => {
    setEditOffice({
      id: office.id,
      name: office.name,
      street: office.street,
      houseNumber: office.houseNumber,
      zipCode: office.zipCode,
      city: office.city,
      province: office.province,
      openingTime: office.openingTime,
      closingTime: office.closingTime,
      status: office.status,
      latitude: office.latitude,
      longitude: office.longitude,
    })
    setIsEditing(true)
  }

  const handleUpdateSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedOffice) return

    setIsLoading(true)

    try {
      const updatedOffice = await fetchUpdateOffice(
        selectedOffice.id,
        editOffice,
      )

      setFeedbackType("primary")
      setFeedbackMessage(
        updatedOffice?.message || "Ufficio aggiornato con successo!",
      )

      setTimeout(() => {
        setIsEditing(false)
        setShowDetailsModal(false)
        setFeedbackMessage(null)
        onOfficeUpdated()
      }, 1500)
    } catch (err: unknown) {
      setFeedbackType("warning")
      setFeedbackMessage(
        (err as Error).message || "Errore imprevisto durante il salvataggio",
      )
    } finally {
      setIsLoading(false)
    }
  }

  const handleCreateOffice = async (e: React.SubmitEvent) => {
    e.preventDefault()

    const payload = {
      ...newOffice,
      latitude: parseFloat(newOffice.latitude),
      longitude: parseFloat(newOffice.longitude),
    }

    try {
      await fetchCreateOffice(payload)
      setShowCreateModal(false)
      onOfficeUpdated()
    } catch (err: unknown) {
      console.error(err)
    }
  }

  const statusTranslations = (status: string) => {
    switch (status) {
      case "ACTIVE":
        return "ATTIVO"
      case "TEMPORARILY_CLOSED":
        return "CHIUSO TEMPORANEAMENTE"
      case "PERMANENTLY_CLOSED":
        return "CHIUSO TEMPORANEAMENTE"
    }
  }

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
      <Row className="d-flex justify-content-center align-items-center mt-4">
        <Col xs={12} lg={10}>
          <div className="d-flex align-items-center">
            <Form.Control
              className="input w-50 search-input-desktop mt-2 border border-1 border-secondary smaller-text"
              type="text"
              placeholder="Cerca ufficio"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
            <Button className="btn-custom2 ms-1 mt-2 small-text p-1">
              Cerca
            </Button>
            <div className="mt-2 ms-auto">
              <Button
                className="btn-custom1 rounded-circle d-flex align-items-center justify-content-center small-text"
                style={{ width: "30px", height: "30px", padding: "0" }}
                onClick={() => setShowCreateModal(true)}
              >
                <IoMdAdd />
              </Button>
            </div>
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
                      <th className="text-dark smaller-text">Dettagli</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredOffices.length > 0 ? (
                      filteredOffices.map((office) => (
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
                          <td>
                            <Button
                              size="sm"
                              variant="outline-primary"
                              onClick={() => handleShowDetails(office)}
                              className="smaller-text"
                            >
                              Dettagli
                            </Button>
                          </td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan={5} className="text-center text-muted py-3">
                          Nessun ufficio attivo trovato.
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

      {/* TABELLA TUTTI GLI UFFICI*/}
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
                      <th className="text-dark smaller-text">Dettagli</th>
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
                          <td className="smaller-text">{office.closingTime}</td>
                          <td className="smaller-text">
                            <span
                              className={`badge ${office.status === "ACTIVE" ? "custom4" : office.status === "TEMPORARILY_CLOSED" ? "custom2" : office.status === "PERMANENTLY_CLOSED" ? "custom3" : "Nessuno stato"}`}
                            >
                              {statusTranslations(office.status)}
                            </span>
                          </td>
                          <td>
                            <Button
                              size="sm"
                              variant="outline-primary"
                              onClick={() => handleShowDetails(office)}
                              className="smaller-text"
                            >
                              Dettagli
                            </Button>
                          </td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan={6} className="text-center text-muted py-3">
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

      {/* Modale Dettagli / Modifica Ufficio */}
      <Modal
        show={showDetailsModal}
        onHide={handleCloseDetails}
        centered
        size={isEditing ? "xl" : undefined}
        className="my-lg-4"
      >
        <Modal.Header closeButton>
          <Modal.Title className="text-dark small-title">
            {isEditing ? "Modifica Ufficio" : "Dettagli Ufficio"}
          </Modal.Title>
        </Modal.Header>

        <Form onSubmit={handleUpdateSubmit}>
          <Modal.Body className="px-lg-5 my-lg-1">
            {feedbackMessage && (
              <Alert
                variant={feedbackType}
                onClose={() => setFeedbackMessage(null)}
                dismissible
              >
                {feedbackMessage}
              </Alert>
            )}
            {!isEditing ? (
              <div>
                <p>
                  <strong>Nome:</strong> {selectedOffice?.name}
                </p>
                <p>
                  <strong>Indirizzo:</strong> {selectedOffice?.street},{" "}
                  {selectedOffice?.houseNumber} - {selectedOffice?.zipCode}{" "}
                  {selectedOffice?.city} ({selectedOffice?.province})
                </p>
                <p>
                  <strong>Orario Apertura:</strong>{" "}
                  {selectedOffice?.openingTime}
                </p>
                <p>
                  <strong>Orario Chiusura:</strong>{" "}
                  {selectedOffice?.closingTime}
                </p>
                <p>
                  <strong>Stato:</strong>{" "}
                  <span
                    className={getStatusVariant(selectedOffice?.status || "")}
                  >
                    {selectedOffice?.status === "ACTIVE"
                      ? "Attivo"
                      : selectedOffice?.status === "TEMPORARILY_CLOSED"
                        ? "Chiuso temporaneamente"
                        : selectedOffice?.status === "PERMANENTLY_CLOSED"
                          ? "Chiuso definitivamente"
                          : "Nessuno stato assegnato"}
                  </span>
                </p>
              </div>
            ) : (
              <Row className="d-flex flex-column">
                <Col xs={12} lg={4}>
                  <Form.Group className="mb-2">
                    <Form.Label className="small-text text-dark">
                      Nome Ufficio
                    </Form.Label>
                    <Form.Control
                      className="input"
                      type="text"
                      value={editOffice.name}
                      onChange={(e) =>
                        setEditOffice({ ...editOffice, name: e.target.value })
                      }
                      required
                    />
                  </Form.Group>
                </Col>

                <Col xs={12} className="d-flex flex-column flex-lg-row">
                  <Form.Group className="mb-2 me-lg-4 mt-lg-2">
                    <Form.Label className="small-text text-dark">
                      Via/Piazza
                    </Form.Label>
                    <Form.Control
                      className="input"
                      type="text"
                      value={editOffice.street}
                      onChange={(e) =>
                        setEditOffice({ ...editOffice, street: e.target.value })
                      }
                      required
                    />
                  </Form.Group>
                  <Form.Group className="mb-2 me-lg-4 mt-lg-2">
                    <Form.Label className="small-text text-dark">
                      Numero civico
                    </Form.Label>
                    <Form.Control
                      className="input"
                      type="text"
                      value={editOffice.houseNumber}
                      onChange={(e) =>
                        setEditOffice({
                          ...editOffice,
                          houseNumber: e.target.value,
                        })
                      }
                      required
                    />
                  </Form.Group>
                  <Form.Group className="mb-2 me-lg-4 mt-lg-2">
                    <Form.Label className="small-text text-dark">
                      Cap
                    </Form.Label>
                    <Form.Control
                      className="input"
                      type="text"
                      value={editOffice.zipCode}
                      onChange={(e) =>
                        setEditOffice({
                          ...editOffice,
                          zipCode: e.target.value,
                        })
                      }
                      required
                    />
                  </Form.Group>
                  <Form.Group className="mb-2 me-lg-4 mt-lg-2">
                    <Form.Label className="small-text text-dark">
                      Città
                    </Form.Label>
                    <Form.Control
                      className="input"
                      type="text"
                      value={editOffice.city}
                      onChange={(e) =>
                        setEditOffice({
                          ...editOffice,
                          city: e.target.value,
                        })
                      }
                      required
                    />
                  </Form.Group>
                  <Form.Group className="mb-2 me-lg-4 mt-lg-2">
                    <Form.Label className="small-text text-dark">
                      Provincia
                    </Form.Label>
                    <Form.Control
                      className="input"
                      type="text"
                      value={editOffice.province}
                      onChange={(e) =>
                        setEditOffice({
                          ...editOffice,
                          province: e.target.value,
                        })
                      }
                      required
                    />
                  </Form.Group>
                </Col>
                <Col xs={12} className="d-flex">
                  <Form.Group className="mb-2 me-4 mt-lg-2">
                    <Form.Label className="small-text text-dark">
                      Orario di apertura
                    </Form.Label>
                    <Form.Control
                      className="input"
                      type="time"
                      value={editOffice.openingTime}
                      onChange={(e) =>
                        setEditOffice({
                          ...editOffice,
                          openingTime: e.target.value,
                        })
                      }
                      required
                    />
                  </Form.Group>
                  <Form.Group className="mb-2 me-lg-4 mt-lg-2">
                    <Form.Label className="small-text text-dark">
                      Orario di chiusura
                    </Form.Label>
                    <Form.Control
                      className="input"
                      type="time"
                      value={editOffice.closingTime}
                      onChange={(e) =>
                        setEditOffice({
                          ...editOffice,
                          closingTime: e.target.value,
                        })
                      }
                      required
                    />
                  </Form.Group>
                </Col>
                <Col xs={12} className="d-flex">
                  <Form.Group className="mb-2 me-4 mt-lg-2">
                    <Form.Label className="small-text text-dark">
                      Latitudine
                    </Form.Label>
                    <Form.Control
                      className="input pe-0"
                      type="text"
                      value={editOffice.latitude}
                      onChange={(e) =>
                        setEditOffice({
                          ...editOffice,
                          latitude: Number(e.target.value),
                        })
                      }
                      required
                    />
                  </Form.Group>
                  <Form.Group className="mb-2 me-lg-4 mt-lg-2">
                    <Form.Label className="small-text text-dark">
                      Longitudine
                    </Form.Label>
                    <Form.Control
                      className="input pe-0"
                      type="text"
                      value={editOffice.longitude}
                      onChange={(e) =>
                        setEditOffice({
                          ...editOffice,
                          longitude: Number(e.target.value),
                        })
                      }
                      required
                    />
                  </Form.Group>
                </Col>
                <Col xs={12} lg={4}>
                  <Form.Group
                    className="mb-3 mt-lg-2 w-100"
                    controlId="formStatus"
                  >
                    <Form.Label className="text-dark small-text">
                      Seleziona stato
                    </Form.Label>
                    <Dropdown
                      onSelect={(eventKey) => {
                        if (eventKey) {
                          setEditOffice({
                            ...editOffice,
                            status: eventKey,
                          })
                        }
                      }}
                    >
                      <Dropdown.Toggle
                        className="w-100 bg-white small-text text-dark text-start input d-flex justify-content-between align-items-center"
                        id="dropdown-office-status"
                      >
                        {editOffice.status === "ACTIVE"
                          ? "Attivo"
                          : editOffice.status === "TEMPORARILY_CLOSED"
                            ? "Chiuso temporaneamente"
                            : editOffice.status === "PERMANENTLY_CLOSED"
                              ? "Chiuso definitivamente"
                              : "Nessuno stato selezionato"}
                      </Dropdown.Toggle>

                      <Dropdown.Menu className="w-100 small-text text-dark">
                        <Dropdown.Item eventKey="ACTIVE">Attivo</Dropdown.Item>
                        <Dropdown.Item eventKey="TEMPORARILY_CLOSED">
                          Chiuso temporaneamente
                        </Dropdown.Item>
                        <Dropdown.Item eventKey="PERMANENTLY_CLOSED">
                          Chiuso definitivamente
                        </Dropdown.Item>
                      </Dropdown.Menu>
                    </Dropdown>
                  </Form.Group>
                </Col>
              </Row>
            )}
          </Modal.Body>

          <Modal.Footer>
            {!isEditing ? (
              <div className="d-flex justify-content-between w-100">
                <div>
                  <Button
                    className="btn-custom1 ms-2"
                    onClick={() => handleStartEdit(selectedOffice!)}
                  >
                    Modifica
                  </Button>
                </div>
                <Button variant="secondary" onClick={handleCloseDetails}>
                  Chiudi
                </Button>
              </div>
            ) : (
              <>
                <Button
                  className="btn-custom1"
                  type="submit"
                  disabled={isLoading}
                >
                  Salva Modifiche
                </Button>
                <Button
                  className="btn-custom2"
                  onClick={() => setIsEditing(false)}
                >
                  Annulla
                </Button>
              </>
            )}
          </Modal.Footer>
        </Form>
      </Modal>

      {/* Modale Creazione Nuovo Ufficio */}
      <Modal
        show={showCreateModal}
        onHide={() => setShowCreateModal(false)}
        centered
        size="lg"
      >
        <Modal.Header closeButton>
          <Modal.Title>Crea Nuovo Ufficio</Modal.Title>
        </Modal.Header>
        <Form onSubmit={handleCreateOffice}>
          <Modal.Body>
            <Row>
              <Col md={12}>
                <Form.Group className="mb-3">
                  <Form.Label className="text-small text-dark">
                    Nome Ufficio
                  </Form.Label>
                  <Form.Control
                    className="input"
                    type="text"
                    placeholder="Sede Centrale"
                    value={newOffice.name}
                    onChange={(e) =>
                      setNewOffice({ ...newOffice, name: e.target.value })
                    }
                    required
                  />
                </Form.Group>
              </Col>

              <Col xs={12} className="d-flex flex-column flex-lg-row">
                <Form.Group className="mb-3 me-lg-4 mt-lg-3">
                  <Form.Label className="small-text">Via/Piazza</Form.Label>
                  <Form.Control
                    className="input"
                    placeholder="Via/Piazza"
                    type="text"
                    value={newOffice.street}
                    onChange={(e) =>
                      setNewOffice({ ...newOffice, street: e.target.value })
                    }
                    required
                  />
                </Form.Group>
                <Form.Group className="mb-3 me-lg-4 mt-lg-3">
                  <Form.Label className="small-text">Numero civico</Form.Label>
                  <Form.Control
                    className="input"
                    type="text"
                    placeholder="100"
                    value={newOffice.houseNumber}
                    onChange={(e) =>
                      setNewOffice({
                        ...newOffice,
                        houseNumber: e.target.value,
                      })
                    }
                    required
                  />
                </Form.Group>
                <Form.Group className="mb-3 me-lg-4 mt-lg-3">
                  <Form.Label className="text-small">Cap</Form.Label>
                  <Form.Control
                    className="input"
                    type="text"
                    placeholder="00186"
                    value={newOffice.zipCode}
                    onChange={(e) =>
                      setNewOffice({
                        ...newOffice,
                        zipCode: e.target.value,
                      })
                    }
                    required
                  />
                </Form.Group>
                <Form.Group className="mb-3 me-lg-4 mt-lg-3">
                  <Form.Label className="text-small">Città</Form.Label>
                  <Form.Control
                    className="input"
                    type="text"
                    value={newOffice.city}
                    placeholder="Roma"
                    onChange={(e) =>
                      setNewOffice({
                        ...newOffice,
                        city: e.target.value,
                      })
                    }
                    required
                  />
                </Form.Group>
                <Form.Group className="mb-3 me-lg-4 mt-lg-3">
                  <Form.Label className="text-small">Provincia</Form.Label>
                  <Form.Control
                    className="input"
                    type="text"
                    value={newOffice.province}
                    placeholder="RM"
                    onChange={(e) =>
                      setNewOffice({
                        ...newOffice,
                        province: e.target.value,
                      })
                    }
                    required
                  />
                </Form.Group>
              </Col>
              <Col xs={12} className="d-flex">
                <Form.Group className="mb-3 me-4 mt-lg-3">
                  <Form.Label className="text-small">
                    Orario di apertura
                  </Form.Label>
                  <Form.Control
                    className="input"
                    type="time"
                    value={newOffice.openingTime}
                    onChange={(e) =>
                      setNewOffice({
                        ...newOffice,
                        openingTime: e.target.value,
                      })
                    }
                    required
                  />
                </Form.Group>
                <Form.Group className="mb-3 me-lg-4 mt-lg-3">
                  <Form.Label className="text-small">
                    Orario di chiusura
                  </Form.Label>
                  <Form.Control
                    className="input"
                    type="time"
                    value={newOffice.closingTime}
                    onChange={(e) =>
                      setNewOffice({
                        ...newOffice,
                        closingTime: e.target.value,
                      })
                    }
                    required
                  />
                </Form.Group>
              </Col>
              <Col xs={12} className="d-flex">
                <Form.Group className="mb-3 me-4 mt-lg-3">
                  <Form.Label className="text-small">Latitudine</Form.Label>
                  <Form.Control
                    placeholder="41.902782"
                    className="input pe-0"
                    type="number"
                    value={newOffice.latitude}
                    onChange={(e) =>
                      setNewOffice({
                        ...newOffice,
                        latitude: e.target.value,
                      })
                    }
                    required
                  />
                </Form.Group>
                <Form.Group className="mb-3 me-lg-4 mt-lg-3">
                  <Form.Label className="text-small">Longitudine</Form.Label>
                  <Form.Control
                    placeholder="12.467510"
                    className="input pe-0"
                    type="text"
                    value={newOffice.longitude}
                    onChange={(e) =>
                      setNewOffice({
                        ...newOffice,
                        longitude: e.target.value,
                      })
                    }
                    required
                  />
                </Form.Group>
              </Col>
              <Col xs={12} lg={4}>
                <Form.Group className="mb-3 w-100" controlId="formStatusCreate">
                  <Form.Label className="text-dark small-text">
                    Seleziona stato
                  </Form.Label>
                  <Dropdown
                    onSelect={(eventKey) => {
                      if (eventKey) {
                        setNewOffice({
                          ...newOffice,
                          status: eventKey,
                        })
                      }
                    }}
                  >
                    <Dropdown.Toggle
                      className="w-100 bg-white small-text text-dark text-start input d-flex justify-content-between align-items-center"
                      id="dropdown-office-status-create"
                    >
                      {newOffice.status === "ACTIVE"
                        ? "Attivo"
                        : newOffice.status === "TEMPORARILY_CLOSED"
                          ? "Chiuso temporaneamente"
                          : newOffice.status === "PERMANENTLY_CLOSED"
                            ? "Chiuso definitivamente"
                            : "Nessuno stato selezionato"}
                    </Dropdown.Toggle>

                    <Dropdown.Menu className="w-100 small-text">
                      <Dropdown.Item eventKey="ACTIVE">Attivo</Dropdown.Item>
                      <Dropdown.Item eventKey="TEMPORARILY_CLOSED">
                        Chiuso temporaneamente
                      </Dropdown.Item>
                      <Dropdown.Item eventKey="PERMANENTLY_CLOSED">
                        Chiuso definitivamente
                      </Dropdown.Item>
                    </Dropdown.Menu>
                  </Dropdown>
                </Form.Group>
              </Col>
            </Row>
          </Modal.Body>
          <Modal.Footer>
            <Button
              className="btn-custom2"
              onClick={() => setShowCreateModal(false)}
            >
              Annulla
            </Button>
            <Button className="btn-custom1" type="submit">
              Salva Ufficio
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </>
  )
}

export default AdminActiveOffices
