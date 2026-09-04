import {
  Button,
  Col,
  Container,
  Row,
  Form,
  Card,
  Modal,
  Table,
} from "react-bootstrap"
import { HrNavbar } from "./HrNavbar"
import { IoIosArrowBack, IoIosArrowForward, IoMdAdd } from "react-icons/io"
import "../../styles/mobileText.css"
import { useCallback, useEffect, useState } from "react"
import { getActiveUsers } from "../../services/userService"
import type { ActiveUser } from "../../types/users"
import type {
  ContractDTO,
  ContractResponseDTO,
  ContractStatus,
  ContractType,
  UpdateContractDTO,
} from "../../types/contract"
import {
  createContract,
  getActiveContracts,
  getAllContracts,
  updateContract,
} from "../../services/contractService"
import { FaPencilAlt } from "react-icons/fa"
import { FcSearch } from "react-icons/fc"

export const HrContracts = () => {
  const [showModal, setShowModal] = useState(false)
  const [selectedUserId, setSelectedUserId] = useState("")
  const [users, setUsers] = useState<ActiveUser[]>([])
  const [startDate, setStartDate] = useState("")
  const [endDate, setEndDate] = useState("")
  const [contractType, setContractType] = useState<ContractType | "">("")
  const [hoursOfLeaveDuePerYear, setHoursOfLeaveDuePerYear] = useState<
    number | ""
  >("")
  const [daysOfLeaveDuePerYear, setDaysOfLeaveDuePerYear] = useState<
    number | ""
  >("")
  const [workingHoursPerWeek, setWorkingHoursPerWeek] = useState<number | "">(
    "",
  )

  const [loading, setLoading] = useState(false)
  const [activeContracts, setActiveContracts] = useState<ContractResponseDTO[]>(
    [],
  )
  const [contracts, setContracts] = useState<ContractResponseDTO[]>([])

  const [showDetailsModal, setShowDetailsModal] = useState(false)
  const [selectedContract, setSelectedContract] =
    useState<ContractResponseDTO | null>(null)

  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  const [searchName, setSearchName] = useState("")

  //RECUPERA UTENTI ATTIVI
  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const data = await getActiveUsers()
        setUsers(data)
      } catch (error) {
        console.error("Errore nel recupero degli utenti:", error)
      }
    }

    fetchUsers()
  }, [])

  //FUNZIONE GET TUTTI I CONTRATTI
  const fetchAllContracts = useCallback(
    async (page = 0, name = searchName) => {
      try {
        setLoading(true)

        const response = await getAllContracts(name, page, 15)

        setContracts(response.content)
        setCurrentPage(response.number ? response.number : 0)
        setTotalPages(response.totalPages ? response.totalPages : 0)
      } catch (error) {
        console.error("Errore nel recupero di tutti i contratti:", error)
      } finally {
        setLoading(false)
      }
    },
    [searchName],
  )

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchAllContracts(0)
  }, [fetchAllContracts])

  //FUNZIONA CREA CONTRATTO
  const handleCreateContract = async () => {
    try {
      setLoading(true)

      const payload: ContractDTO = {
        userId: selectedUserId,
        startDate,
        endDate: endDate || null,
        contractType: contractType as ContractType,
        hoursOfLeaveDuePerYear: Number(hoursOfLeaveDuePerYear),
        daysOfLeaveDuePerYear: Number(daysOfLeaveDuePerYear),
        workingHoursPerWeek: Number(workingHoursPerWeek),
      }

      await createContract(payload)

      setShowModal(false)
      alert("Contratto salvato con successo!")
    } catch (error) {
      console.error("Errore nella creazione del contratto:", error)
      if (error instanceof Error) {
        alert(error.message)
      }
    } finally {
      setLoading(false)
    }
  }

  //FUNZIONE GET CONTRATTI ATTIVI
  const fetchActiveContracts = async () => {
    try {
      setLoading(true)

      const response = await getActiveContracts()

      setActiveContracts(response.content)
      console.log("Contratti attivi:", response.content)
    } catch (error) {
      console.error("Errore nel recupero dei contratti attivi:", error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchActiveContracts()
  }, [])

  //MODIFICA CONTRATTO
  const handleUpdateContract = async () => {
    if (!selectedContract) {
      return
    }

    try {
      setLoading(true)

      const payload: UpdateContractDTO = {
        startDate,
        endDate: endDate || null,
        contractType: contractType as ContractType,
        hoursOfLeaveDuePerYear: Number(hoursOfLeaveDuePerYear),
        daysOfLeaveDuePerYear: Number(daysOfLeaveDuePerYear),
        workingHoursPerWeek: Number(workingHoursPerWeek),
      }

      await updateContract(selectedContract.id, payload)

      alert("Contratto modificato con successo!")
    } catch (error) {
      console.error("Errore nella modifica del contratto:", error)

      if (error instanceof Error) {
        alert(error.message)
      }
    } finally {
      setLoading(false)
    }
  }

  //FUNZIONE PER LA DATA
  const formatDateForInput = (date: string | null) => {
    if (!date) return ""

    const [day, month, year] = date.split("/")

    return `${year}-${month}-${day}`
  }

  //FUNZIONE PER APRIRE LA MODIFICA DEL CONTRATTO
  const handleEditContract = (contract: ContractResponseDTO) => {
    setSelectedContract(contract)

    setStartDate(formatDateForInput(contract.startDate))
    setEndDate(formatDateForInput(contract.endDate))
    setContractType(contract.contractType)

    setHoursOfLeaveDuePerYear(contract.hoursOfLeaveDuePerYear)
    setDaysOfLeaveDuePerYear(contract.daysOfLeaveDuePerYear)
    setWorkingHoursPerWeek(contract.workingHoursPerWeek)

    setShowModal(true)
  }

  //FUNZIONE TRADUZIONE TIPO DI CONTRATTO
  const translateContractType = (contractType: ContractType | "") => {
    switch (contractType) {
      case "FULL_TIME_FIXED_TERM_CONTRACT":
        return "Tempo determinato - Full-time"

      case "PART_TIME_FIXED_TERM_CONTRACT":
        return "Tempo determinato - Part-time"

      case "FULL_TIME_PERMANENT_CONTRACT":
        return "Tempo indeterminato - Full-time"

      case "PART_TIME_PERMANENT_CONTRACT":
        return "Tempo indeterminato - Part-time"

      case "APPRENTICESHIP_CONTRACT":
        return "Contratto apprendistato"

      case "EXTRACURRICULAR_INTERNSHIP":
        return "Contratto stagista extracurriculare "
    }
  }

  //FUNZIONE PER TRADURRE TIPO DI STATO
  const translateContractStatus = (contractStatus: ContractStatus) => {
    switch (contractStatus) {
      case "ACTIVE":
        return "ATTIVO"
      case "EXPIRED":
        return "SCADUTO"
      case "SUSPENDED":
        return "SOSPESO"
      case "TERMINATED":
        return "INTERROTTO"
    }
  }

  //FUNZIONE PER COLORARE LO STATO
  const getContractStatusClass = (contractStatus: ContractStatus) => {
    switch (contractStatus) {
      case "ACTIVE":
        return "text-success"

      case "EXPIRED":
        return "text-secondary"

      case "SUSPENDED":
        return "text-warning"

      case "TERMINATED":
        return "text-danger"
    }
  }

  //modale dettagli
  const handleDetails = (contract: ContractResponseDTO) => {
    setSelectedContract(contract)
    setShowDetailsModal(true)
  }

  return (
    <>
      <HrNavbar />
      <Container
        fluid
        className="d-flex flex-column align-items-center flex-grow-1 my-4"
      >
        {/* Barra superiore con titolo e pulsante */}
        <Row className="w-100 justify-content-center mb-4">
          <Col xs={12} md={11} className="px-0">
            <div className=" d-flex align-items-center">
              <h3 className="m-0 small-title text-dark">Contratti</h3>

              <Button
                className="btn-custom1 rounded-circle d-flex align-items-center justify-content-center small-text ms-auto"
                style={{ width: "30px", height: "30px", padding: "0" }}
                onClick={() => setShowModal(true)}
              >
                <IoMdAdd />
              </Button>
            </div>
          </Col>
        </Row>

        {/*CONTRATTI ATTIVI TITOLO*/}
        <Row className="w-100 justify-content-center mb-2">
          <Col xs={12} md={11} className="px-0">
            <h5 className="text-primary small-title">Contratti attivi</h5>

            {/*CONTRATTI ATTIVI CARDS */}
            <Row className="w-100 justify-content-start mb-5">
              {activeContracts.map((contract) => (
                <Col xs={6} lg={3} className="px-0 g-3" key={contract.id}>
                  <Card className="bg-light shadow-sm ms-2">
                    <Card.Body>
                      <div className="w-100 d-flex justify-content-end">
                        <Button
                          className="p-1 rounded-circle border-0 backgroundOrange text-light d-flex align-items-center justify-content-center small-text"
                          onClick={() => handleEditContract(contract)}
                          disabled={loading}
                        >
                          <FaPencilAlt size={10} />
                        </Button>
                      </div>
                      <Card.Title className="text-nowrap small-text text-dark">
                        {contract.userName} {contract.userSurname}
                      </Card.Title>
                      <Card.Text className="small-text text-dark">
                        <strong>Tipo di contratto:</strong>{" "}
                        {translateContractType(contract.contractType)}
                      </Card.Text>

                      <Card.Text className="small-text text-dark">
                        <strong>Data inizio:</strong> {contract.startDate}
                      </Card.Text>

                      <Card.Text className="small-text text-dark">
                        <strong>Scadenza:</strong>{" "}
                        {contract.endDate ? contract.endDate : "-"}
                      </Card.Text>

                      <Button
                        className="btn-custom1 small-text"
                        onClick={() => handleDetails(contract)}
                      >
                        Dettagli
                      </Button>
                    </Card.Body>
                  </Card>
                </Col>
              ))}
            </Row>
          </Col>
        </Row>

        {/*TUTTI I CONTRATTI TABELLA */}
        <Row className="w-100 justify-content-center mb-2">
          <Col xs={12} md={11} className="px-0">
            <h5 className="text-primary small-title">Tutti i contratti</h5>

            {/*SEARCH */}

            <div className="d-flex justify-content-lg-start my-2">
              <Form>
                <Form.Group className="mb-3" controlId="search">
                  <Form.Label className="text-muted small-text mb-0">
                    Cerca dipendente
                  </Form.Label>
                  <div className="position-relative">
                    <FcSearch className="position-absolute top-50 start-0 translate-middle-y ms-2 text-muted" />

                    <Form.Control
                      type="text"
                      placeholder="Nome o cognome"
                      className="input ps-4 small-text"
                      value={searchName}
                      onChange={(e) => setSearchName(e.target.value)}
                    />
                  </div>
                </Form.Group>
              </Form>
            </div>

            <Card className="border border-1 border-secondary">
              <div className="table-responsive m-2">
                <Table striped bordered hover>
                  <thead>
                    <tr>
                      <th className="text-dark small-text text-center">
                        <p className="mb-0">
                          Nome<br></br> dipendente
                        </p>
                      </th>
                      <th className="text-dark small-text text-center">
                        <p>Tipo</p>
                      </th>
                      <th className="text-dark small-text text-center">
                        <p> Data inizio</p>
                      </th>
                      <th className="text-dark small-text text-center">
                        <p>Data fine</p>
                      </th>
                      <th className="text-dark small-text text-center">
                        <p className="mb-0">
                          <span className="text-nowrap">Ore lavorative</span>
                          <br></br> settimanali
                        </p>
                      </th>
                      <th className="text-dark small-text text-center">
                        <p className="mb-0 text-nowrap">Giorni di</p>
                        <p className="mb-0 text-nowrap">ferie annui</p>
                      </th>
                      <th className="text-dark small-text text-center">
                        <p className="mb-0 text-nowrap">Ore di</p>{" "}
                        <p className="mb-0 text-nowrap">permesso annue</p>
                      </th>
                      <th className="text-dark small-text text-center">
                        <p>Stato</p>
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {contracts.map((contract) => (
                      <tr key={contract.id}>
                        <td className="small-text text-center align-middle text-dark">
                          {contract.userName} {contract.userSurname}
                        </td>

                        <td className="small-text text-center">
                          {translateContractType(contract.contractType)}
                        </td>

                        <td className="small-text text-center align-middle text-dark">
                          {contract.startDate}
                        </td>

                        <td className="small-text text-center align-middle text-dark">
                          {contract.endDate ?? "—"}
                        </td>

                        <td className="small-text text-center align-middle text-dark">
                          {contract.workingHoursPerWeek}
                        </td>

                        <td className="small-text text-center align-middle text-dark">
                          {contract.daysOfLeaveDuePerYear}
                        </td>

                        <td className="small-text text-center align-middle text-dark">
                          {contract.hoursOfLeaveDuePerYear}
                        </td>

                        <td
                          className={`small-text text-center align-middle text-dark ${getContractStatusClass(
                            contract.contractStatus,
                          )}`}
                        >
                          {translateContractStatus(contract.contractStatus)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              </div>
              <div className="d-flex justify-content-center gap-2 p-3">
                <Button
                  className="btn-custom1"
                  size="sm"
                  disabled={currentPage === 0}
                  onClick={() => fetchAllContracts(currentPage - 1)}
                >
                  <div className="d-flex align-items-center justify-content-center">
                    <IoIosArrowBack />
                  </div>
                </Button>

                <span className="d-flex align-items-center px-2 small-text">
                  Pagina {currentPage + 1} di {totalPages}
                </span>

                <Button
                  className="btn-custom1"
                  size="sm"
                  disabled={currentPage >= totalPages - 1}
                  onClick={() => fetchAllContracts(currentPage + 1)}
                >
                  <div className="d-flex align-items-center justify-content-center">
                    <IoIosArrowForward />
                  </div>
                </Button>
              </div>
            </Card>
          </Col>
        </Row>
        {/*MODALE*/}
        <Modal show={showModal} onHide={() => setShowModal(false)}>
          <Modal.Header closeButton>
            <Modal.Title className="text-dark small-title">
              {selectedContract
                ? "Modifica contratto"
                : "Aggiungi un nuovo contratto"}
            </Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Form>
              {/*NOME DIPENDENTE */}
              <Form.Group className="mb-3" controlId="inputEmployee">
                <Form.Label className="text-muted mb-0">
                  Nome dipendente
                </Form.Label>

                {selectedContract ? (
                  <Form.Control
                    className="input"
                    type="text"
                    value={`${selectedContract.userName} ${selectedContract.userSurname}`}
                    disabled
                  />
                ) : (
                  <Form.Select
                    className="input"
                    value={selectedUserId}
                    onChange={(e) => setSelectedUserId(e.target.value)}
                    required
                  >
                    <option value="">Seleziona dipendente</option>

                    {users.map((user) => (
                      <option key={user.userId} value={user.userId}>
                        {user.name}
                      </option>
                    ))}
                  </Form.Select>
                )}
              </Form.Group>

              {/*SELECT PER TIPO CONTRATTO*/}
              <Form.Group className="mb-3" controlId="inputContractType">
                <Form.Label className="text-muted mb-0">
                  Tipo di contratto
                </Form.Label>
                <Form.Select
                  className="mb-3 input"
                  value={contractType}
                  onChange={(e) => {
                    setContractType(e.target.value as ContractType)
                  }}
                  required
                >
                  <option className="text-muted">
                    Seleziona il tipo di contratto
                  </option>
                  <option value="FULL_TIME_FIXED_TERM_CONTRACT">
                    Tempo determinato - Full-time
                  </option>
                  <option value="PART_TIME_FIXED_TERM_CONTRACT">
                    Tempo determinato - Part-time
                  </option>
                  <option value="FULL_TIME_PERMANENT_CONTRACT">
                    Tempo indeterminato - Full-time
                  </option>
                  <option value="PART_TIME_PERMANENT_CONTRACT">
                    Tempo indeterminato - Part-time
                  </option>
                  <option value="APPRENTICESHIP_CONTRACT">
                    Contratto apprendistato
                  </option>
                  <option value="EXTRACURRICULAR_INTERNSHIP">
                    Contratto stagista extracurriculare
                  </option>
                </Form.Select>
              </Form.Group>

              {/*DATA INIZIO CONTRATTO */}
              <Form.Group className="mb-3" controlId="inputStartDate">
                <Form.Label className="text-muted mb-0">
                  Data inizio contratto
                </Form.Label>
                <Form.Control
                  className="input"
                  type="date"
                  value={startDate}
                  onChange={(e) => {
                    setStartDate(e.target.value)
                  }}
                  required
                />
              </Form.Group>
              {/* DATA FINE CONTRATTO OPZIONALE*/}
              {contractType !== "FULL_TIME_PERMANENT_CONTRACT" &&
              contractType !== "PART_TIME_PERMANENT_CONTRACT" ? (
                <>
                  <Form.Group className="mb-3" controlId="inputEndDate">
                    <Form.Label className="text-muted mb-0">
                      Data fine contratto
                    </Form.Label>
                    <Form.Control
                      className="input"
                      type="date"
                      value={endDate}
                      onChange={(e) => {
                        setEndDate(e.target.value)
                      }}
                      required
                    />
                  </Form.Group>
                </>
              ) : (
                <p className="text-muted">
                  Tipo di contratto selezionato:{" "}
                  {translateContractType(contractType)}
                </p>
              )}

              {/*TOTALE ORE LAVORATIVE SETTIMANALI */}
              <Form.Group className="mb-3" controlId="inputWorkingHoursPerWeek">
                <Form.Label className="text-muted mb-0">
                  Totale ore lavorative settimanali
                </Form.Label>
                <Form.Control
                  className="input"
                  type="number"
                  value={workingHoursPerWeek}
                  onChange={(e) => {
                    setWorkingHoursPerWeek(Number(e.target.value))
                  }}
                  required
                />
              </Form.Group>

              {/*TOTALE GIORNI DI FERIE ANNUI */}
              <Form.Group className="mb-3" controlId="inputHolidaysPerYear">
                <Form.Label className="text-muted mb-0">
                  Totale giorni di ferie annui
                </Form.Label>
                <Form.Control
                  className="input"
                  type="number"
                  value={daysOfLeaveDuePerYear}
                  onChange={(e) => {
                    setDaysOfLeaveDuePerYear(Number(e.target.value))
                  }}
                  required
                />
              </Form.Group>

              {/*TOTALE ORE DI PERMESSO ANNUE */}
              <Form.Group className="mb-3" controlId="inputLeaveHoursPerYear">
                <Form.Label className="text-muted mb-0">
                  Totale ore di permesso annue
                </Form.Label>
                <Form.Control
                  className="input"
                  type="number"
                  value={hoursOfLeaveDuePerYear}
                  onChange={(e) => {
                    setHoursOfLeaveDuePerYear(Number(e.target.value))
                  }}
                  required
                />
              </Form.Group>
            </Form>
          </Modal.Body>
          <Modal.Footer>
            <Button
              className="btn-custom1"
              onClick={
                selectedContract ? handleUpdateContract : handleCreateContract
              }
              disabled={loading}
            >
              {loading
                ? "Salvataggio..."
                : selectedContract
                  ? "Salva modifiche"
                  : "Salva contratto"}
            </Button>
          </Modal.Footer>
        </Modal>
        {/*MODALE DETTAGLI CONTRATTO */}
        <Modal
          show={showDetailsModal}
          onHide={() => setShowDetailsModal(false)}
        >
          <Modal.Header closeButton>
            <Modal.Title className="small-text text-primary">
              Dettagli contratto {selectedContract?.userName}{" "}
              {selectedContract?.userSurname}
            </Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <ul className="text-dark small-text">
              <li>
                <span className="fw-semibold me-2">Tipo contratto:</span>
                {selectedContract?.contractType}
              </li>
              <li>
                <span className="fw-semibold me-2">Data inizio contratto:</span>
                {selectedContract?.startDate}
              </li>
              {selectedContract?.contractType !==
                "FULL_TIME_PERMANENT_CONTRACT" &&
              selectedContract?.contractType !==
                "PART_TIME_PERMANENT_CONTRACT" ? (
                <>
                  <li>
                    <span className="fw-semibold me-2">
                      Data fine contratto:
                    </span>
                    {selectedContract?.endDate}
                  </li>
                </>
              ) : (
                ""
              )}
              <li>
                <span className="fw-semibold me-2">
                  Totale ore di lavoro settimanali:
                </span>
                {selectedContract?.workingHoursPerWeek}
              </li>
              <li>
                <span className="fw-semibold me-2">
                  Totale giorni di ferie annui:
                </span>
                {selectedContract?.daysOfLeaveDuePerYear}
              </li>
              <li>
                <span className="fw-semibold me-2">
                  Totale ore di permesso annue:
                </span>
                {selectedContract?.hoursOfLeaveDuePerYear}
              </li>
            </ul>
          </Modal.Body>
        </Modal>
      </Container>
    </>
  )
}
