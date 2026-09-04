import {
  Button,
  Col,
  Container,
  Modal,
  Row,
  Form,
  Dropdown,
  Card,
} from "react-bootstrap"
import { ShiftManagerNavbar } from "./ShiftManagerNavbar"
import { IoMdAdd } from "react-icons/io"
import { FaPencilAlt } from "react-icons/fa"
import { useEffect, useState } from "react"

import {
  createShift,
  fetchShifts,
  updateShift,
} from "../../services/shiftService"
import type { OfficeResponseDTO } from "../../types/office"
import { fetchActiveOffices } from "../../services/officeService"
import type { Shift } from "../../types/shift"

export const ShiftManagerTurni = () => {
  const [loading, setLoading] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [activeOfficesList, setActiveOfficesList] = useState<
    OfficeResponseDTO[]
  >([])

  // Stato per la lista dei turni esistenti
  const [shiftsList, setShiftsList] = useState<Shift[]>([])

  // Stato del form (include l'id in modalità modifica)
  const [currentShift, setCurrentShift] = useState<{
    id: string | null
    officeName: string
    startTime: string
    endTime: string
    isActive: boolean
  }>({
    id: null,
    officeName: "",
    startTime: "",
    endTime: "",
    isActive: true,
  })

  // Funzione per caricare i turni attivi filtrati dal backend
  const loadShifts = async () => {
    try {
      const shiftsPageData = await fetchShifts(true) // Richiede solo quelli attivi

      console.log("TURNI ATTIVI DOPO IL REFRESH:", shiftsPageData)
      setShiftsList(shiftsPageData.content || [])
    } catch (err) {
      console.error("Errore nel caricamento dei turni", err)
    }
  }

  // 1. Recupero uffici attivi e turni esistenti all'avvio
  useEffect(() => {
    const loadData = async () => {
      try {
        const activeData = await fetchActiveOffices()
        setActiveOfficesList(activeData)

        await loadShifts() // Chiamiamo la funzione dedicata per i turni
      } catch (err) {
        console.error("Errore nel caricamento dei dati", err)
      }
    }

    loadData()
  }, [])

  // Raggruppiamo i turni per nome ufficio
  const groupedShifts = shiftsList.reduce(
    (acc, shift) => {
      if (!acc[shift.officeName]) {
        acc[shift.officeName] = []
      }
      acc[shift.officeName].push(shift)
      return acc
    },
    {} as Record<string, typeof shiftsList>,
  )

  // Apertura modale in CREAZIONE
  const handleOpenCreate = () => {
    setCurrentShift({
      id: null,
      officeName: "",
      startTime: "",
      endTime: "",
      isActive: true,
    })
    setShowModal(true)
  }

  // Apertura modale in MODIFICA (passando il turno esistente)
  const handleOpenEdit = (shift: Shift) => {
    setCurrentShift({
      id: shift.id,
      officeName: shift.officeName,
      startTime: shift.startTime,
      endTime: shift.endTime,
      isActive: shift.isActive,
    })
    setShowModal(true)
  }

  // Gestione salvataggio (Crea o Modifica)
  const handleSubmitShift = async (e: React.SubmitEvent) => {
    e.preventDefault()
    setLoading(true)

    try {
      if (currentShift.id) {
        // 👇 QUI
        console.log("DATI UPDATE:", {
          id: currentShift.id,
          officeName: currentShift.officeName,
          startTime: currentShift.startTime,
          endTime: currentShift.endTime,
          isActive: currentShift.isActive,
        })

        await updateShift(currentShift.id, {
          officeName: currentShift.officeName,
          startTime: currentShift.startTime,
          endTime: currentShift.endTime,
          isActive: currentShift.isActive,
        })

        alert("Turno modificato con successo")
      } else {
        // crea un nuovo turno
        await createShift({
          officeName: currentShift.officeName,
          startTime: currentShift.startTime,
          endTime: currentShift.endTime,
          isActive: currentShift.isActive,
        })

        alert("Turno salvato con successo")
      }

      await loadShifts()
      setShowModal(false)
    } catch (error) {
      console.error("Errore nel salvataggio del turno:", error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <ShiftManagerNavbar />
      <Container
        fluid
        className="d-flex flex-column justify-content-center flex-grow-1 p-4"
      >
        {/* Barra superiore con titolo e pulsante */}
        <Row className="w-100 justify-content-center mb-4">
          <Col xs={12} md={11} className="bg-light">
            <div className=" d-flex align-items-center px-2">
              <h3 className="m-0 small-title text-dark">Turni attivi</h3>

              <Button
                className="btn-custom1 rounded-circle d-flex align-items-center justify-content-center small-text ms-auto"
                style={{ width: "30px", height: "30px", padding: "0" }}
                onClick={handleOpenCreate}
              >
                <IoMdAdd />
              </Button>
            </div>
          </Col>
        </Row>

        {/* Griglia di visualizzazione dei turni esistenti */}
        <Row className="w-100 justify-content-center mb-4">
          <Col xs={12} md={11}>
            <Row className="g-3 w-100 justify-content-start">
              {Object.entries(groupedShifts).map(
                ([officeName, officeShifts]) => (
                  <Col xs={12} md={3} key={officeName}>
                    <Card className="h-100 shadow-sm p-3">
                      <Card.Body className="d-flex flex-column">
                        {/* Nome dell'ufficio stampato UNA SOLA VOLTA */}
                        <Card.Title className="fw-bold text-primary mb-3">
                          {officeName}
                        </Card.Title>

                        {/* Lista dei turni di questo ufficio uno sotto l'altro */}
                        <div className="d-flex flex-column gap-2 mb-3 flex-grow-1">
                          {officeShifts.map((shift) => (
                            <div
                              key={shift.id}
                              className="p-2 bg-light rounded border d-flex justify-content-between align-items-center"
                            >
                              <div>
                                <span className="small-text fw-semibold">
                                  {shift.startTime} - {shift.endTime}
                                </span>
                                <div
                                  className="text-muted"
                                  style={{ fontSize: "0.8rem" }}
                                >
                                  Stato:{" "}
                                  {shift.isActive ? "Attivo" : "Disabilitato"}
                                </div>
                              </div>

                              {/* Pulsante di modifica per il singolo turno */}
                              <Button
                                size="sm"
                                className="p-1 rounded-circle border-0 backgroundOrange text-light d-flex align-items-center justify-content-center small-text"
                                onClick={() => handleOpenEdit(shift)}
                              >
                                <FaPencilAlt size={10} />
                              </Button>
                            </div>
                          ))}
                        </div>
                      </Card.Body>
                    </Card>
                  </Col>
                ),
              )}
            </Row>
          </Col>
        </Row>
      </Container>

      {/* Modale(Crea / Modifica) */}
      <Modal
        show={showModal}
        onHide={() => setShowModal(false)}
        centered
        size="lg"
      >
        <Modal.Header closeButton className="px-3">
          <Modal.Title>
            {currentShift.id ? "Modifica Turno" : "Crea Nuovo Turno"}
          </Modal.Title>
        </Modal.Header>
        <Form onSubmit={handleSubmitShift}>
          <Modal.Body className="px-3">
            <Row>
              <Col md={12}>
                <Form.Group className="mb-3">
                  <Form.Label className="text-small text-dark">
                    Ufficio/sede
                  </Form.Label>
                  <Dropdown>
                    <Dropdown.Toggle
                      className="w-75 bg-white small-text text-dark text-start input d-flex justify-content-between align-items-center"
                      id="dropdown-office"
                    >
                      {currentShift.officeName || "Seleziona un ufficio"}
                    </Dropdown.Toggle>

                    <Dropdown.Menu className="w-100 small-text">
                      {activeOfficesList.length > 0 ? (
                        activeOfficesList.map((office) => (
                          <Dropdown.Item
                            key={office.id}
                            onClick={() =>
                              setCurrentShift({
                                ...currentShift,
                                officeName: office.name,
                              })
                            }
                          >
                            {office.name}
                          </Dropdown.Item>
                        ))
                      ) : (
                        <Dropdown.Item disabled>
                          Nessun ufficio attivo disponibile
                        </Dropdown.Item>
                      )}
                    </Dropdown.Menu>
                  </Dropdown>
                </Form.Group>
              </Col>

              <Col xs={12} className="d-flex flex-column flex-lg-row">
                <Form.Group className="mb-3 me-lg-4 mt-lg-3">
                  <Form.Label className="small-text">Inizio turno</Form.Label>
                  <Form.Control
                    className="input"
                    type="time"
                    value={currentShift.startTime}
                    onChange={(e) =>
                      setCurrentShift({
                        ...currentShift,
                        startTime: e.target.value,
                      })
                    }
                    required
                  />
                </Form.Group>
                <Form.Group className="mb-3 me-lg-4 mt-lg-3">
                  <Form.Label className="small-text">Fine turno</Form.Label>
                  <Form.Control
                    className="input"
                    type="time"
                    value={currentShift.endTime}
                    onChange={(e) =>
                      setCurrentShift({
                        ...currentShift,
                        endTime: e.target.value,
                      })
                    }
                    required
                  />
                </Form.Group>
              </Col>

              <Col xs={12} md={3} lg={4}>
                <Form.Group className="mb-3 w-100">
                  <Form.Label className="text-dark small-text">
                    Seleziona stato
                  </Form.Label>
                  <Dropdown>
                    <Dropdown.Toggle className="w-100 bg-white small-text text-dark text-start input d-flex justify-content-between align-items-center">
                      {currentShift.isActive ? "Attivo" : "Disabilitato"}
                    </Dropdown.Toggle>

                    <Dropdown.Menu className="w-100 small-text">
                      <Dropdown.Item
                        onClick={() =>
                          setCurrentShift({
                            ...currentShift,
                            isActive: true,
                          })
                        }
                      >
                        Attivo
                      </Dropdown.Item>
                      <Dropdown.Item
                        onClick={() =>
                          setCurrentShift({
                            ...currentShift,
                            isActive: false,
                          })
                        }
                      >
                        Disabilitato
                      </Dropdown.Item>
                    </Dropdown.Menu>
                  </Dropdown>
                </Form.Group>
              </Col>
            </Row>
          </Modal.Body>
          <Modal.Footer>
            <Button className="btn-custom1" type="submit">
              {loading
                ? "Salvataggio..."
                : currentShift.id
                  ? "Salva Modifiche"
                  : "Salva Turno"}
            </Button>
            <Button className="btn-custom2" onClick={() => setShowModal(false)}>
              Annulla
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </>
  )
}
